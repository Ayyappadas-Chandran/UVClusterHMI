package com.ultraviolette.uvclusterhmi.data.repository

import com.ultraviolette.uvclusterhmi.data.dataSource.WifiManagerWrapper
import com.ultraviolette.uvclusterhmi.domain.repository.WifiRepository
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration

/**
 * Implementation of [WifiRepository] that delegates Wi-Fi operations to [WifiManagerWrapper].
 *
 * @param wifiManagerWrapper Handles the actual Wi-Fi logic.
 */
class WifiRepoImpl(
    private val wifiManagerWrapper: WifiManagerWrapper
) : WifiRepository {

    /** Returns `true` if Wi-Fi is enabled. */
    override fun isWifiEnabled(): Boolean =
        wifiManagerWrapper.isWifiEnabled()

    /** Connects to a hotspot with the given [ssid] and [password]. */
    override fun connectHotspot(ssid: String, password: String, isManual: Boolean) {
        wifiManagerWrapper.connectHotspot(ssid, password, isManual)
    }


    /** Starts a Wi-Fi scan. */
    override fun startScan() =
        wifiManagerWrapper.startScan()


    override fun enableWifi(enable: Boolean) {
        wifiManagerWrapper.enableWifi(enable)
    }

    override fun wifiStateChange(callback: (Boolean) -> Unit) {
        wifiManagerWrapper.wifiState(callback)
    }

    override fun registerWifiStateChangeReceiver() {
        wifiManagerWrapper.registerWifiStateChangeReceiver()
    }

    override fun unregisterWifiStateChangeReceiver() {
        wifiManagerWrapper.unregisterWifiStateChangeReceiver()
    }

    //27/01/2026
    override fun connectionState(callBack: (Boolean) -> Unit) {
        wifiManagerWrapper.connectionState(callBack)
    }

    /** Returns the SSID of the currently connected Wi-Fi network. */
    override fun getConnectedWifiSSID() =
        wifiManagerWrapper.getConnectedWifiSSID()

    /** Forgets the current Wi-Fi network. */
    override fun forgetHotspot() =
        wifiManagerWrapper.forgetHotspot()

    //    /** Provides a list of scanned SSIDs via [callback].*/
//    override fun scanResult(callback: (List<String>) -> Unit) =
//        wifiManagerWrapper.scanResult(callback)
    override fun scanResult(callback: (List<ScanResult>) -> Unit) =
        wifiManagerWrapper.scanResult(callback)

    override fun savedNetworkList(callback: (List<WifiConfiguration>) -> Unit) {
        wifiManagerWrapper.getSavedNetworkList(callback)
    }


    override fun getCurrentSignalLevel(): Int {
        return wifiManagerWrapper.getCurrentSignalLevel()
    }

    override fun connectToSavedNetwork(ssid: String) {
        wifiManagerWrapper.connectToSavedNetwork(ssid)
    }

    override fun connectedSSID(callback: (String?) -> Unit) {
        wifiManagerWrapper.getConnectedSSID(callback)
    }

    override fun reconnectRequestSSID(callback: (String?) -> Unit) {
        wifiManagerWrapper.getReconnectRequestSSID(callback)
    }


}





