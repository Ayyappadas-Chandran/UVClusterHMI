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
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
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
    private val sharedViewModel by activityViewModels<SharedViewModel> { ViewModelFactory(context = requireContext()) }
    private val wifiViewModel by activityViewModels<WifiViewModel> { ViewModelFactory(context = requireContext()) }
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

        wifiViewModel.wifiConnected()
        wifiViewModel.wifiReconnectRequest()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    wifiViewModel.connectedSSID.collect { state ->
                        Log.d(TAG, "initObserver: wifiViewModel.onWifiStateChange ::  Entry  :: $state")
                        updateConnectedNetworkName(state)
                    }
                }
                launch {
                    wifiViewModel.connectionState.collect { state ->
                        Log.d(TAG, "initObserver: Connection State Changed :: $state")
                        handleWifiStateClicked()
                        if (state){
                            startRepeatingScan()
                        }else{
                            stopRepeatingScan()
                        }
                    }
                }

                launch {
                    wifiViewModel.reconnectSSID.collect { ssid ->
                        Log.d(TAG, "initObserver: reconnectSSID :: $ssid")
                        if (ssid!=null){
                            showWifiDialog(ssid)
                        }
                    }
                }

                lifecycleScope.launchWhenStarted {
                    wifiViewModel.selectedItem.collect { selected ->
                        wifiSavedNetworkAdapter.updateSelected(selected)
                        wifiDeviceAdapter.updateSelected(selected)
                    }
                }


            }
        }


        wifiViewModel.scanResult.observe(viewLifecycleOwner) { result ->

            Log.d(TAG, "initObserver:scanResult filtered wifi list :: $result")
            val connectedSsid = wifiViewModel.getConnectedWifiSSID()

            Log.d(TAG, "initObserver:scanResult connectedSsid :: $connectedSsid")
            val filteredList = result.filter { wifi ->
                wifi.ssid != connectedSsid
            }

            Log.d(TAG, "initObserver:scanResult Filtered list :: $filteredList")


            wifiDeviceAdapter.submitList(filteredList)
        }


        wifiViewModel.saveNetworkList.observe(viewLifecycleOwner) { result ->
            Log.d(TAG, "initObserver: Connection savedNetworkList :: ${result.size}")

            val connectedSsid = wifiViewModel.getConnectedWifiSSID()

            val filteredList = result.filter { wifi ->
                wifi.ssid != connectedSsid
            }

            Log.d(TAG, "initObserver: filtered saveNetworkList list :: $filteredList")

            myNetworkVisibility()
            wifiSavedNetworkAdapter.submitList(filteredList)

        }

    }

    /**
     * Handles the UI state for Wi-Fi state.
     */
    private fun handleWifiStateClicked() {
        Log.d(TAG, "handleWifiStateClicked: Entry")
        val isWiFiEnabled = wifiViewModel.isWifiEnabled()
        wifiViewModel.scanResult()

        ivWifiOnSelected.isVisible = isWiFiEnabled
        ivWifiOffSelected.isVisible = !isWiFiEnabled
        tvAvailableDevices.isVisible = isWiFiEnabled
        wifiAvailableDevicesProgressBar.isVisible = isWiFiEnabled
        tvSearching.isVisible = isWiFiEnabled

        Log.d(TAG, "handleWifiStateClicked: Wifi Enabled Status Fetched")
        Log.d(TAG, "handleWifiStateClicked: Wifi Enabled Status :: $isWiFiEnabled")
        val selectedTextColor = ContextCompat.getColor(requireContext(), R.color.white)
        val selectedBackgroundColor =
            ContextCompat.getColor(requireContext(), R.color.activeSelectionRed)
        val unselectedTextColor = ContextCompat.getColor(requireContext(), R.color.unSelected)
        val unselectedBackgroundColor =
            ContextCompat.getColor(requireContext(), R.color.transparent)
        tvWifiOn.setTextColor(if (isWiFiEnabled) selectedTextColor else unselectedTextColor)
        tvWifiOn.setBackgroundColor(if (isWiFiEnabled) selectedBackgroundColor else unselectedBackgroundColor)
        tvWifiOff.setTextColor(if (!isWiFiEnabled) selectedTextColor else unselectedTextColor)
        tvWifiOff.setBackgroundColor(if (!isWiFiEnabled) selectedBackgroundColor else unselectedBackgroundColor)
        Log.d(TAG, "handleWifiStateClicked: Wifi Enabled Status Updated")
    }

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



    /**
     * Observes changes in the Wi-Fi scan results.
     */
    //27/01/2026
    fun handleButtonNavigation(button: Int) {
//        when (button) {
//            ButtonNavigation.Left.ordinal -> {
//                if (wifiViewModel.isWifiEnabled()) {
//                    tvWifiOff.performClick()
//                } else {
//                    tvWifiOn.performClick()
//                }
//            }
//
//            ButtonNavigation.Right.ordinal -> {
//                if (wifiViewModel.isWifiEnabled()) {
//                    tvWifiOff.performClick()
//                } else {
//                    tvWifiOn.performClick()
//                }
//            }
//
//            ButtonNavigation.Back.ordinal -> {
//                //for bug no 46 - pop up exit on button press
//                if (wifiDialog?.isShowing == true) {
//                    wifiDialog?.dismiss()
//                }
//                ivWifiOnSelected.isVisible = false
//                ivWifiOffSelected.isVisible = false
//            }
//        }
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



