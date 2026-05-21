package com.ultraviolette.uvclusterhmi.domain.repository
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration

/**
 * Defines Wi-Fi-related operations to be implemented by a repository.
 */
interface WifiRepository {

    /** Returns `true` if Wi-Fi is currently enabled. */
    fun isWifiEnabled(): Boolean

    /** Connects to a Wi-Fi hotspot using the given [ssid] and [password]. */
    fun connectHotspot(ssid: String, password: String, isManual: Boolean)

    /** Starts scanning for available Wi-Fi networks. */
    fun startScan()

    /** Returns the SSID of the currently connected Wi-Fi network. */
    fun getConnectedWifiSSID(): String

    /** Forgets the currently connected Wi-Fi network. */
    fun forgetHotspot()

    /** Provides a list of scanned SSIDs through the given [callback].*/
//    fun scanResult(callback: (List<String>) -> Unit)


    fun scanResult(callback: (List<ScanResult>) -> Unit)
    fun savedNetworkList(callback: (List<WifiConfiguration>) -> Unit)
    fun enableWifi(enable: Boolean)

    fun wifiStateChange(callback: (Boolean) -> Unit)

    fun registerWifiStateChangeReceiver()

    fun unregisterWifiStateChangeReceiver()

    //27/01/2026
    fun connectionState(callBack:(Boolean) -> Unit)


    fun getCurrentSignalLevel(): Int

    fun connectToSavedNetwork(ssid: String)

    fun connectedSSID(callback: (String?) -> Unit)

    fun reconnectRequestSSID(callback: (String?) -> Unit)

}



