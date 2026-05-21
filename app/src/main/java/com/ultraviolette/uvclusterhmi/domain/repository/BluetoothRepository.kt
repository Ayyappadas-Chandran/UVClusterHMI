package com.ultraviolette.uvclusterhmi.domain.repository

import android.bluetooth.BluetoothDevice

/**
 * Defines Bluetooth-related operations to be implemented by a repository.
 */
interface BluetoothRepository {

    /** Returns `true` if Bluetooth is currently enabled. */
    fun isBluetoothEnabled(): Boolean

    /** Starts Bluetooth device discovery. */
    fun startDiscovery()

    /** Initiates bonding (pairing) with the given [bluetoothDevice]. */
    fun createBond(bluetoothDevice: BluetoothDevice)

    /** Registers a broadcast receiver for Bluetooth-related actions. */
    fun registerBluetoothActionReceiver()

    /** Unregisters the Bluetooth broadcast receiver. */
    fun unregisterBluetoothActionReceiver()

    /** Delivers scanned [BluetoothDevice]s through the provided [callback].*/
    fun scanResult(callback: (List<BluetoothDevice>) -> Unit)

    fun enableBluetooth()

    fun disableBluetooth()

    fun bluetoothStateChange(state:(Boolean) ->Unit)
}
