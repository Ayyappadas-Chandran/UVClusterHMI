package com.ultraviolette.uvclusterhmi.ui.features.settings.bluetooth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.ultraviolette.cluster.aidl.BtScanResult
import com.ultraviolette.uvclusterhmi.ClusterApplication
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import com.ultraviolette.uvclusterhmi.ui.adapter.BluetoothDeviceAdapter
import com.ultraviolette.uvclusterhmi.ui.viewModel.SharedViewModel
import com.ultraviolette.uvclusterhmi.utils.Utilities.invisible
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener
import com.ultraviolette.uvclusterhmi.utils.Utilities.visible
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
import kotlinx.coroutines.launch

class BluetoothFragment : Fragment() {
    private lateinit var tvBluetoothOn: TextView
    private lateinit var tvBluetoothOff: TextView
    private lateinit var tvDiscoverable: TextView
    private lateinit var tvAvailableDevices: TextView
    private lateinit var ivBluetoothOnSelected: ImageView
    private lateinit var ivBluetoothOffSelected: ImageView
    private lateinit var rvAvailableDevices: RecyclerView
    private lateinit var pbAvailableDevices: ProgressBar
    private val  sharedViewModel by activityViewModels<SharedViewModel> { ViewModelFactory(context = requireContext()) }
    private val bluetoothViewModel: BluetoothViewModel by viewModels {
        BluetoothViewModel.Factory(requireActivity().application)
    }
    private var clickedUiState: ClickedUiState = ClickedUiState.BluetoothStateClicked
    private var isBluetoothStateClicked = true

    /**
     * Activity result launcher for enabling Bluetooth.
     */
    private val bluetoothRequest =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            handleUi()
            bluetoothViewModel.startDiscovery()
        }


    /**
     * Create adapter for the available list
     */
    private val bluetoothDeviceAdapter = BluetoothDeviceAdapter { result ->
        clickedUiState = ClickedUiState.BluetoothDeviceClicked
        handleUi()
        bluetoothViewModel.createBond(result.address)
        sharedViewModel.handleSettingsChildClick(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_bluetooth, container, false)
    }

    fun handleButtonNavigation(button: Int) {
        when (button) {
            ButtonNavigation.Left.ordinal -> {
                if (bluetoothViewModel.isBluetoothEnabled()) {
                    tvBluetoothOff.performClick()
                } else {
                    tvBluetoothOn.performClick()
                }
            }

            ButtonNavigation.Right.ordinal -> {
                if (bluetoothViewModel.isBluetoothEnabled()) {
                    tvBluetoothOff.performClick()
                } else {
                    tvBluetoothOn.performClick()
                }
            }

            ButtonNavigation.Back.ordinal -> {
                ivBluetoothOnSelected.visibility = View.INVISIBLE
                ivBluetoothOffSelected.visibility = View.INVISIBLE
                Log.d("ButtonNavigation", "bluetooth reset called")
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("BluetoothFragment", "OnViewCreated")
        initViews(view)
        initObserver()
        if (bluetoothViewModel.isBluetoothEnabled()) {
            pbAvailableDevices.isVisible = true
            bluetoothViewModel.startDiscovery()
        }
        initClickListener()
        rvAvailableDevices.adapter = bluetoothDeviceAdapter
    }

    override fun onResume() {
        super.onResume()
        Log.d("BluetoothFragment", "onResume")
    }

    /**
     * Binds UI components from the provided root view using their IDs.
     *
     * @param view The root view containing the layout elements.
     *
     * Initializes:
     * - tvBluetoothOn,tvBluetoothOff,tvDiscoverable,tvAvailableDevices (TextViews)
     * - ivBluetoothOnSelected,ivBluetoothOffSelected (ImageViews)
     * - pbAvailableDevices (progressbar)
     * - rvAvailableDevices (RecyclerView)
     */
    private fun initViews(view: View) {
        tvBluetoothOn = view.findViewById(R.id.tvBluetoothOn)
        tvBluetoothOff = view.findViewById(R.id.tvBluetoothOff)
        tvDiscoverable = view.findViewById(R.id.tvDiscoverable)
        tvAvailableDevices = view.findViewById(R.id.tvAvailableDevices)

        ivBluetoothOnSelected = view.findViewById(R.id.ivBluetoothOnSelected)
        ivBluetoothOffSelected = view.findViewById(R.id.ivBluetoothOffSelected)

        rvAvailableDevices = view.findViewById(R.id.rvAvailableDevices)
        pbAvailableDevices = view.findViewById(R.id.pbAvailableDevices)
    }


    /**
     * Observes the [BluetoothViewModel]'s scan result and updates the UI accordingly.
     */
    private fun initObserver() {
        sharedViewModel.settingsChildClick.observe(viewLifecycleOwner) { isClicked ->
            if (!isClicked) resetBluetoothStateClicked() else handleUi()
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    bluetoothViewModel.scanResults.collect { results ->
                        bluetoothDeviceAdapter.submitList(results)
                        pbAvailableDevices.isVisible = false
                    }
                }
                launch {
                    bluetoothViewModel.isEnabled.collect {
                        if (bluetoothViewModel.isBluetoothEnabled()) {
                            pbAvailableDevices.visible()
                        } else {
                            pbAvailableDevices.invisible()
                        }
                        handleBluetoothStateClicked()
                    }
                }
            }
        }
    }

    /**
     * Initializes click listeners for UI components.
     */
    private fun initClickListener() {
        tvBluetoothOn.setOnSoundClickListener(requireContext()) {
            bluetoothDeviceAdapter.handleParentClick()
            if (bluetoothViewModel.isBluetoothEnabled()) {
                return@setOnSoundClickListener
            }
            sharedViewModel.handleSettingsChildClick(true)
            clickedUiState = ClickedUiState.BluetoothStateClicked
            handleUi()
            bluetoothViewModel.enableBluetooth()
        }
        tvBluetoothOff.setOnSoundClickListener(requireContext()) {
            bluetoothDeviceAdapter.handleParentClick()
            if (!bluetoothViewModel.isBluetoothEnabled()) {
                return@setOnSoundClickListener
            }
            sharedViewModel.handleSettingsChildClick(true)
            clickedUiState = ClickedUiState.BluetoothStateClicked
            handleUi()
            bluetoothViewModel.disableBluetooth()
        }
    }

    /**
     * Handles the UI state based on the [ClickedUiState].
     */
    private fun handleUi() {
        when (clickedUiState) {
            ClickedUiState.BluetoothStateClicked -> {
                handleBluetoothStateClicked()
            }

            ClickedUiState.BluetoothDeviceClicked -> {
                resetBluetoothStateClicked()
            }
        }
    }

    /**
     * Handles the UI state when the Bluetooth state is clicked.
     */
    private fun handleBluetoothStateClicked() {
        ivBluetoothOnSelected.isVisible = bluetoothViewModel.isBluetoothEnabled()
        ivBluetoothOffSelected.isVisible = !bluetoothViewModel.isBluetoothEnabled()
        val selectedTextColor = ContextCompat.getColor(requireContext(), R.color.white)
        val selectedBackgroundColor =
            ContextCompat.getColor(requireContext(), R.color.activeSelectionRed)
        val unselectedTextColor = ContextCompat.getColor(requireContext(), R.color.unSelected)
        val unselectedBackgroundClickedUiState =
            ContextCompat.getColor(requireContext(), R.color.transparent)
        tvBluetoothOn.setTextColor(if (bluetoothViewModel.isBluetoothEnabled()) selectedTextColor else unselectedTextColor)
        tvBluetoothOn.setBackgroundColor(if (bluetoothViewModel.isBluetoothEnabled()) selectedBackgroundColor else unselectedBackgroundClickedUiState)
        tvBluetoothOff.setTextColor(if (!bluetoothViewModel.isBluetoothEnabled()) selectedTextColor else unselectedTextColor)
        tvBluetoothOff.setBackgroundColor(if (!bluetoothViewModel.isBluetoothEnabled()) selectedBackgroundColor else unselectedBackgroundClickedUiState)
    }

    /**
     * Resets the UI state when the Bluetooth device is clicked.
     */
    private fun resetBluetoothStateClicked() {
        ivBluetoothOnSelected.isVisible = false
        ivBluetoothOffSelected.isVisible = false
        val selectedTextColor = ContextCompat.getColor(
            requireContext(),
            if (isBluetoothStateClicked) R.color.black else R.color.white
        )
        val selectedBackgroundColor = ContextCompat.getColor(
            requireContext(),
            if (isBluetoothStateClicked) R.color.white else R.color.grey
        )
        val unselectedTextColor = ContextCompat.getColor(requireContext(), R.color.unSelected)
        val unselectedBackgroundColor =
            ContextCompat.getColor(requireContext(), R.color.transparent)
        tvBluetoothOn.setTextColor(if (bluetoothViewModel.isBluetoothEnabled()) selectedTextColor else unselectedTextColor)
        tvBluetoothOn.setBackgroundColor(if (bluetoothViewModel.isBluetoothEnabled()) selectedBackgroundColor else unselectedBackgroundColor)
        tvBluetoothOff.setTextColor(if (!bluetoothViewModel.isBluetoothEnabled()) selectedTextColor else unselectedTextColor)
        tvBluetoothOff.setBackgroundColor(if (!bluetoothViewModel.isBluetoothEnabled()) selectedBackgroundColor else unselectedBackgroundColor)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("BluetoothFragment", "OnDestroy")
    }

    /**
     * Sealed class representing different UI states for handling click events.
     */
    sealed class ClickedUiState() {
        object BluetoothStateClicked : ClickedUiState()
        object BluetoothDeviceClicked : ClickedUiState()
    }
}