package com.ultraviolette.uvclusterhmi.ui.features.settings.bluetooth

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ultraviolette.uvclusterhmi.domain.repository.BluetoothRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel responsible for managing Bluetooth operations.
 * It acts as an interface between the UI layer and the BluetoothRepository.
 *
 * @param bluetoothRepository The repository that handles Bluetooth-related logic.
 */
class BluetoothViewModel(private val bluetoothRepository: BluetoothRepository) : ViewModel() {
    private val _scanResult = MutableLiveData<List<BluetoothDevice>>()
    /**
     * LiveData holding the list of discovered Bluetooth devices.
     */
    val scanResult: LiveData<List<BluetoothDevice>>
        get() = _scanResult

    /**
     * Initiates the Bluetooth scanning process and updates scanResult LiveData
     * with the list of discovered Bluetooth devices.
     */
    fun scanResult() {
        bluetoothRepository.scanResult { result ->
            _scanResult.value = result
        }
    }

    private val _onBluetoothStateChange = MutableStateFlow(isBluetoothEnabled())
    val onBluetoothStateChange: StateFlow<Boolean>
        get() = _onBluetoothStateChange

    fun bluetoothStateChange() {
        bluetoothRepository.bluetoothStateChange {
            _onBluetoothStateChange.value = it
        }
    }

    /**
     * Checks if Bluetooth is currently enabled on the device.
     *
     * @return true if Bluetooth is enabled, false otherwise.
     */
    fun isBluetoothEnabled(): Boolean {
        return bluetoothRepository.isBluetoothEnabled()
    }

    /**
     * Starts the discovery process to search for nearby Bluetooth devices.
     */
    fun startDiscovery() {
        bluetoothRepository.startDiscovery()
    }

    fun enableBluetooth() {
        bluetoothRepository.enableBluetooth()
    }

    fun disableBluetooth() {
        bluetoothRepository.disableBluetooth()
    }

    /**
     * Initiates bonding (pairing) with the specified Bluetooth device.
     *
     * @param bluetoothDevice The device to bond with.
     */
    fun createBond(bluetoothDevice: BluetoothDevice) {
        bluetoothRepository.createBond(bluetoothDevice)
    }

    /**
     * Registers the broadcast receiver to listen for Bluetooth-related system events.
     */
    fun registerBluetoothActionReceiver() {
        bluetoothRepository.registerBluetoothActionReceiver()
    }

    /**
     * Unregisters the previously registered Bluetooth broadcast receiver.
     */
    fun unregisterBluetoothActionReceiver() {
        bluetoothRepository.unregisterBluetoothActionReceiver()
    }
}