package com.ultraviolette.clusterdatabus

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat

/** Manages dual-profile (A2DP + HFP) Bluetooth connections. */
class BtSessionManager(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter
) {
    private val tag = "ClusterBus.BtSessionManager"

    fun connectProfiles(device: BluetoothDevice) {
        if (!hasConnectPermission()) {
            Log.w(tag, "Missing BLUETOOTH_CONNECT permission")
            return
        }
        connectA2dp(device)
        connectHfp(device)
    }

    private fun connectA2dp(device: BluetoothDevice) {
        bluetoothAdapter.getProfileProxy(context, object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
                if (profile != BluetoothProfile.A2DP) return
                invokeConnect(proxy, device, "A2DP")
            }
            override fun onServiceDisconnected(profile: Int) {
                Log.d(tag, "A2DP service disconnected")
            }
        }, BluetoothProfile.A2DP)
    }

    private fun connectHfp(device: BluetoothDevice) {
        bluetoothAdapter.getProfileProxy(context, object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
                if (profile != BluetoothProfile.HEADSET) return
                invokeConnect(proxy, device, "HFP")
            }
            override fun onServiceDisconnected(profile: Int) {
                Log.d(tag, "HFP service disconnected")
            }
        }, BluetoothProfile.HEADSET)
    }

    private fun invokeConnect(proxy: BluetoothProfile?, device: BluetoothDevice, label: String) {
        try {
            val method = proxy?.javaClass?.getMethod("connect", BluetoothDevice::class.java)
            method?.isAccessible = true
            method?.invoke(proxy, device)
            Log.d(tag, "$label connect invoked for ${deviceName(device)}")
        } catch (e: Exception) {
            Log.e(tag, "$label connect failed", e)
        }
    }

    private fun deviceName(device: BluetoothDevice): String {
        if (!hasConnectPermission()) return device.address
        return try { device.name ?: device.address } catch (e: Exception) { device.address }
    }

    private fun hasConnectPermission(): Boolean =
        ActivityCompat.checkSelfPermission(
            context, Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
}
