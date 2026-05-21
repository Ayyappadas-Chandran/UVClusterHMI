package com.ultraviolette.clusterdatabus

import android.content.Context
import android.content.SharedPreferences
import com.ultraviolette.cluster.aidl.BtState
import com.ultraviolette.cluster.aidl.WifiState

/** Persists critical signal state across service restarts using SharedPreferences. */
class PersistenceHelper(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("cluster_bus_state", Context.MODE_PRIVATE)

    fun saveBtState(state: BtState) {
        prefs.edit()
            .putBoolean(KEY_BT_ENABLED, state.isEnabled)
            .putString(KEY_BT_DEVICE_NAME, state.pairedDeviceName)
            .putString(KEY_BT_DEVICE_ADDR, state.pairedDeviceAddress)
            .putInt(KEY_BT_BOND_STATE, state.bondState)
            .apply()
    }

    fun loadBtState(): BtState = BtState(
        isEnabled = prefs.getBoolean(KEY_BT_ENABLED, false),
        pairedDeviceName = prefs.getString(KEY_BT_DEVICE_NAME, "") ?: "",
        pairedDeviceAddress = prefs.getString(KEY_BT_DEVICE_ADDR, "") ?: "",
        bondState = prefs.getInt(KEY_BT_BOND_STATE, 10)
    )

    fun saveWifiState(state: WifiState) {
        prefs.edit()
            .putBoolean(KEY_WIFI_ENABLED, state.isEnabled)
            .putString(KEY_WIFI_SSID, state.connectedSsid)
            .putBoolean(KEY_WIFI_CONNECTED, state.isConnected)
            .apply()
    }

    fun loadWifiState(): WifiState = WifiState(
        isEnabled = prefs.getBoolean(KEY_WIFI_ENABLED, false),
        connectedSsid = prefs.getString(KEY_WIFI_SSID, "") ?: "",
        isConnected = prefs.getBoolean(KEY_WIFI_CONNECTED, false)
    )

    companion object {
        private const val KEY_BT_ENABLED = "bt_enabled"
        private const val KEY_BT_DEVICE_NAME = "bt_device_name"
        private const val KEY_BT_DEVICE_ADDR = "bt_device_addr"
        private const val KEY_BT_BOND_STATE = "bt_bond_state"
        private const val KEY_WIFI_ENABLED = "wifi_enabled"
        private const val KEY_WIFI_SSID = "wifi_ssid"
        private const val KEY_WIFI_CONNECTED = "wifi_connected"
    }
}
