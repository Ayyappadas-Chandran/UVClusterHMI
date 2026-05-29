package com.ultraviolette.uvclusterhmi.ui.features.settings.wifi

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.ui.adapter.WifiDeviceAdapter
import com.ultraviolette.uvclusterhmi.ui.viewModel.SharedViewModel
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener
import kotlinx.coroutines.launch


class WifiFragment : Fragment() {
    private lateinit var tvMyNetwork: TextView
    private lateinit var tvWifiName: TextView
    private lateinit var tvWifiOn: TextView
    private lateinit var tvWifiOff: TextView
    private lateinit var tvAvailableDevices: TextView
    private lateinit var tvSearching: TextView
    private lateinit var ivWifiOnSelected: ImageView
    private lateinit var ivDelete: ImageView
    private lateinit var ivWifiOffSelected: ImageView
    private lateinit var rvAvailableDevices: RecyclerView
    private lateinit var savedNetworkList: RecyclerView
    private lateinit var clMyNetwork: ConstraintLayout
    private lateinit var wifiAvailableDevicesProgressBar: ProgressBar
    private var wifiDialog: AlertDialog? = null
    private var wifiConnectedDialog: AlertDialog? = null
    private val sharedViewModel by activityViewModels<SharedViewModel> { com.ultraviolette.uvclusterhmi.utils.ViewModelFactory(context = requireContext()) }
    private val wifiViewModel by activityViewModels<WifiViewModel> {
        WifiViewModel.Factory(requireActivity().application)
    }
    private var clickedUiState: ClickedUiState = ClickedUiState.WifiStateClicked
    private var isWifiStateClicked = true

    private val handler = Handler(Looper.getMainLooper())
    private var isScanning = false
    private val wifiDeviceAdapter = WifiDeviceAdapter { ssid ->
        wifiViewModel.selectItem(ssid)
        showWifiDialog(ssid)
    }

    private val wifiSavedNetworkAdapter = WifiDeviceAdapter { ssid ->
        wifiViewModel.selectItem(ssid)
        wifiViewModel.connectToSavedNetwork(ssid)
    }

    private val TAG = "WifiFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_wifi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        handleWifiStateClicked()
        initClickListener()
        initObserver()
    }


    /**
     * Binds UI components from the provided root view using their IDs.
     *
     * @param view The root view containing the layout elements.
     *
     * Initializes:
     * - tvMyNetwork,tvWifiName,tvWifiOn,tvWifiOff,tvSearching,tvAvailableDevices (TextViews)
     * - ivWifiOnSelected,ivWifiOffSelected,ivDelete (ImageViews)
     * - rvAvailableDevices (RecyclerView)
     * - clMyNetwork (ConstraintLayout)
     */
    private fun initViews(view: View) {
        tvMyNetwork = view.findViewById(R.id.tvMyNetwork)
        tvWifiName = view.findViewById(R.id.tvWifiName)
        tvWifiOn = view.findViewById(R.id.tvWifiOn)
        tvWifiOff = view.findViewById(R.id.tvWifiOff)
        tvSearching = view.findViewById(R.id.tvSearching)
        tvAvailableDevices = view.findViewById(R.id.tvAvailableDevices)

        ivWifiOnSelected = view.findViewById(R.id.ivWifiOnSelected)
        ivWifiOffSelected = view.findViewById(R.id.ivWifiOffSelected)
        ivDelete = view.findViewById(R.id.ivDeleteWifiNetwork)

        rvAvailableDevices = view.findViewById(R.id.rvAvailableDevices)
        savedNetworkList = view.findViewById(R.id.savedNetworkList)

        rvAvailableDevices.isNestedScrollingEnabled = false
        savedNetworkList.isNestedScrollingEnabled = false

        clMyNetwork = view.findViewById(R.id.clMyNetwork)
        wifiAvailableDevicesProgressBar = view.findViewById(R.id.wifiAvailableDevicesProgressBar)


        savedNetworkList.adapter = wifiSavedNetworkAdapter
        rvAvailableDevices.adapter = wifiDeviceAdapter


    }


    private fun initClickListener() {
        tvWifiOn.setOnSoundClickListener(requireContext()) {
            if (wifiViewModel.isWifiEnabled()) {
                return@setOnSoundClickListener
            }
            wifiViewModel.enableWifi(true)
        }

        tvWifiOff.setOnSoundClickListener(requireContext()) {
            if (!wifiViewModel.isWifiEnabled()) {
                return@setOnSoundClickListener
            }
            wifiViewModel.enableWifi(false)
        }

        ivDelete.setOnSoundClickListener(requireContext()) {
            wifiViewModel.forgetHotspot()
            wifiViewModel.startScan()
            wifiViewModel.getSavedNetworkList()
        }

    }

    private fun initObserver() {
        // wifiConnected() fires an initial wifiStartScan so results populate as soon as
        // the service is bound. wifiReconnectRequest() is a no-op in the new architecture.
        wifiViewModel.wifiConnected()
        wifiViewModel.wifiReconnectRequest()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                // ── Primary state: WiFi enabled / disabled ────────────────────
                // This is the CRITICAL collector.  The old code only watched
                // connectionState (isConnected), so toggling WiFi on while not yet
                // connected to a network never refreshed the button highlights or
                // triggered the first scan.  isEnabled fires on every adapter-state
                // change (enabled/disabled) regardless of connection state.
                launch {
                    wifiViewModel.isEnabled.collect { isOn ->
                        Log.d(TAG, "initObserver: isEnabled changed :: $isOn")
                        handleWifiStateUi(isOn)
                        if (isOn) {
                            // Adapter just turned on — kick off scan immediately so the
                            // available-devices list populates without waiting for the
                            // connection-state callback.
                            Log.d(TAG, "initObserver: WiFi enabled — starting scan")
                            wifiViewModel.startScan()
                        } else {
                            // Clear stale results so the list doesn't show old networks
                            // after the adapter is turned off.
                            wifiDeviceAdapter.submitList(emptyList())
                            stopRepeatingScan()
                        }
                    }
                }

                // ── Connected / disconnected ──────────────────────────────────
                launch {
                    wifiViewModel.connectionState.collect { isConnected ->
                        Log.d(TAG, "initObserver: connectionState changed :: $isConnected")
                        // Re-read isEnabled so button highlights stay consistent.
                        handleWifiStateUi(wifiViewModel.isWifiEnabled())
                        if (isConnected) startRepeatingScan() else stopRepeatingScan()
                    }
                }

                launch {
                    wifiViewModel.connectedSSID.collect { ssid ->
                        Log.d(TAG, "initObserver: connectedSSID changed :: $ssid")
                        updateConnectedNetworkName(ssid)
                    }
                }

                launch {
                    wifiViewModel.reconnectSSID.collect { ssid ->
                        Log.d(TAG, "initObserver: reconnectSSID :: $ssid")
                        // A saved network is in range but we are not connected.
                        // Auto-connect silently — the credentials are already stored.
                        // Never show the password dialog here; that dialog is only for
                        // new networks tapped from the available-devices list.
                        if (ssid != null) {
                            Log.d(TAG, "initObserver: auto-connecting to saved network $ssid")
                            wifiViewModel.connectToSavedNetwork(ssid)
                        }
                    }
                }

                launch {
                    wifiViewModel.selectedItem.collect { selected ->
                        wifiSavedNetworkAdapter.updateSelected(selected)
                        wifiDeviceAdapter.updateSelected(selected)
                    }
                }

                launch {
                    wifiViewModel.scanResult.collect { result ->
                        Log.d(TAG, "initObserver: scanResult updated — ${result.size} network(s)")
                        val connectedSsid = wifiViewModel.getConnectedWifiSSID()
                        Log.d(TAG, "initObserver: connectedSsid for filter :: $connectedSsid")
                        val filteredList = result.filter { it.ssid != connectedSsid }
                        Log.d(TAG, "initObserver: filteredScanList size :: ${filteredList.size}")
                        wifiDeviceAdapter.submitList(filteredList)
                    }
                }

                launch {
                    wifiViewModel.saveNetworkList.collect { result ->
                        Log.d(TAG, "initObserver: saveNetworkList updated — ${result.size} network(s)")
                        val connectedSsid = wifiViewModel.getConnectedWifiSSID()
                        val filteredList = result.filter { it.ssid != connectedSsid }
                        Log.d(TAG, "initObserver: filteredSavedList :: $filteredList")
                        myNetworkVisibility()
                        wifiSavedNetworkAdapter.submitList(filteredList)
                    }
                }
            }
        }
    }

    /**
     * Updates all WiFi-enable/disable-related UI elements.
     *
     * @param isOn The current adapter-enabled state, passed in from the [isEnabled] flow
     *             collector so we never risk a timing mismatch from a sync ViewModel read.
     */
    private fun handleWifiStateUi(isOn: Boolean) {
        Log.d(TAG, "handleWifiStateUi: isOn=$isOn")

        // Selection indicators
        ivWifiOnSelected.isVisible = isOn
        ivWifiOffSelected.isVisible = !isOn

        // Show/hide the "Available devices" section
        tvAvailableDevices.isVisible = isOn
        wifiAvailableDevicesProgressBar.isVisible = isOn
        tvSearching.isVisible = isOn

        // ON / OFF button highlight colours
        val selectedText  = ContextCompat.getColor(requireContext(), R.color.white)
        val selectedBg    = ContextCompat.getColor(requireContext(), R.color.activeSelectionRed)
        val unselectedText = ContextCompat.getColor(requireContext(), R.color.unSelected)
        val unselectedBg   = ContextCompat.getColor(requireContext(), R.color.transparent)

        tvWifiOn.setTextColor(if (isOn)  selectedText  else unselectedText)
        tvWifiOn.setBackgroundColor(if (isOn)  selectedBg    else unselectedBg)
        tvWifiOff.setTextColor(if (!isOn) selectedText  else unselectedText)
        tvWifiOff.setBackgroundColor(if (!isOn) selectedBg    else unselectedBg)

        Log.d(TAG, "handleWifiStateUi: button states updated")
    }

    /**
     * Kept for call sites that were already compiled against the old name.
     * Reads the current enabled state from the ViewModel and delegates to [handleWifiStateUi].
     */
    private fun handleWifiStateClicked() = handleWifiStateUi(wifiViewModel.isWifiEnabled())

    private fun updateConnectedNetworkName(ssid: String?) {
        val status = ssid != "<unknown ssid>" && ssid != null
        tvWifiName.isVisible =  status && wifiViewModel.isConnectionStateActive()
        clMyNetwork.isVisible =  status && wifiViewModel.isConnectionStateActive()
        Log.d(TAG, "handleConnectionState: Visibility Status updated")
        val connectedNetwork = ssid?.trim('"')
        Log.d(TAG, "handleConnectionState: Connected Network :: $connectedNetwork")
        tvWifiName.text = connectedNetwork
        myNetworkVisibility()
    }

    private fun myNetworkVisibility(){
        val isListEmpty = !wifiViewModel.isSavedNetworkListEmpty()
        val connectedNetwork = wifiViewModel.isNetworkConnected()

        Log.d(TAG, "myNetworkVisibility: States isListEmpty:: $isListEmpty  :: connectedNetwork :: $connectedNetwork")
        savedNetworkList.isVisible = isListEmpty && wifiViewModel.isConnectionStateActive()
        tvMyNetwork.isVisible = (isListEmpty || connectedNetwork) && wifiViewModel.isConnectionStateActive()
    }


    private val scanRunnable = object : Runnable {
        override fun run() {
            if (!isScanning) return

            // Show for 2 seconds
            wifiAvailableDevicesProgressBar.isVisible = true
            handler.postDelayed({
                wifiAvailableDevicesProgressBar.isVisible = false

                // After hiding for 3 seconds, restart the cycle
                handler.postDelayed(this, 3000)
            }, 2000)
        }
    }

    fun startRepeatingScan() {
        if (isScanning) return
        isScanning = true
        handler.post(scanRunnable)
    }

    fun stopRepeatingScan() {
        isScanning = false
        handler.removeCallbacks(scanRunnable)
        wifiAvailableDevicesProgressBar.isVisible = false
    }



    fun handleButtonNavigation(button: Int) {
        // Wifi button navigation: dismiss any open dialog on Back.
        // Left/Right toggle not used since the toolbar handles enable/disable.
        when (button) {
            com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation.Back.ordinal -> {
                if (wifiDialog?.isShowing == true) {
                    wifiDialog?.dismiss()
                } else {
                    android.util.Log.d(TAG, "handleButtonNavigation: Back — no dialog to dismiss")
                }
            }
            else -> android.util.Log.d(TAG, "handleButtonNavigation: unhandled button=$button")
        }
    }

    /**
     * Displays a Wi-Fi password input dialog with a blurred background.
     *
     * Prompts the user to enter a password to connect to the specified SSID.
     * Applies a blur effect (Android 12+) to the background while the dialog is shown.
     *
     * @param ssid The Wi-Fi SSID to connect to.
     */
    private fun showWifiDialog(ssid: String) {
        Log.d(TAG, "showWifiDialog: Entry")
        val rootView =
            requireActivity().window.decorView.findViewById<ViewGroup>(android.R.id.content)
        // Blur the background
        val blur = RenderEffect.createBlurEffect(20f, 20f, Shader.TileMode.CLAMP)
        rootView.setRenderEffect(blur)
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_wifi_password, null)
        val tvConnect = dialogView.findViewById<TextView>(R.id.tvConnect)
        val etPassword = dialogView.findViewById<TextView>(R.id.etPassword)
        val ivEnter = dialogView.findViewById<ImageView>(R.id.ivEnter)
        //for bug no 46 - pop up exit on button press
        wifiDialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setOnDismissListener {
                rootView.setRenderEffect(null)
                wifiDialog = null
            }
            .create()
        tvConnect.text = buildString {
            append(getString(R.string.connect_to))
            append(ssid)
        }
        ivEnter.setOnSoundClickListener(requireContext()) {
            val password = etPassword.text.toString()
            wifiViewModel.connectHotspot(ssid, password)
            wifiDialog?.dismiss()

        }
        wifiDialog?.show()
    }

    /**
     * Represents different UI states for handling click events.
     */
    sealed class ClickedUiState() {
        object WifiStateClicked : ClickedUiState()
        object WifiDeviceClicked : ClickedUiState()
        object WifiDeleteClicked : ClickedUiState()
        object WifiSavedNetworkClicked : ClickedUiState()
    }
}



