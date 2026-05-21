package com.ultraviolette.uvclusterhmi.data.repository

import android.bluetooth.BluetoothDevice
import com.ultraviolette.uvclusterhmi.data.dataSource.BluetoothManagerWrapper
import com.ultraviolette.uvclusterhmi.domain.repository.BluetoothRepository

/**
 * A Bluetooth repository implementation that delegates operations to [BluetoothManagerWrapper].
 *
 * @param bluetoothManagerWrapper Handles the actual Bluetooth logic.
 */
class BluetoothRepoImpl(private val bluetoothManagerWrapper: BluetoothManagerWrapper) :
    BluetoothRepository {
    /** Returns `true` if Bluetooth is enabled. */
    override fun isBluetoothEnabled(): Boolean = bluetoothManagerWrapper.isBluetoothEnabled()

    /** Starts Bluetooth device discovery. */
    override fun startDiscovery() = bluetoothManagerWrapper.startDiscovery()

    /** Initiates bonding with the given [bluetoothDevice]. */
    override fun createBond(bluetoothDevice: BluetoothDevice) =
        bluetoothManagerWrapper.createBond(bluetoothDevice)

    /** Registers a receiver for Bluetooth-related broadcasts. */
    override fun registerBluetoothActionReceiver() =
        bluetoothManagerWrapper.registerBluetoothActionReceiver()

    /** Unregisters the Bluetooth broadcast receiver. */
    override fun unregisterBluetoothActionReceiver() =
        bluetoothManagerWrapper.unregisterBluetoothActionReceiver()
    /** Provides scanned Bluetooth devices via [callback].*/
    override fun scanResult(callback: (List<BluetoothDevice>) -> Unit) =
        bluetoothManagerWrapper.scanResult(callback)

    override fun bluetoothStateChange(state: (Boolean) ->Unit) {
        bluetoothManagerWrapper.bluetoothStateChange(state)
    }

    override fun enableBluetooth() {
        bluetoothManagerWrapper.enableBluetooth()
    }

    override fun disableBluetooth() {
       bluetoothManagerWrapper.disableBluetooth()
    }
}