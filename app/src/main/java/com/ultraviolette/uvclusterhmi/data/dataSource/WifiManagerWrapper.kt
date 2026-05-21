package com.ultraviolette.uvclusterhmi.data.dataSource

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.net.wifi.WifiInfo
import android.util.Log.d
import android.util.Log.e
import android.widget.Toast
import androidx.core.app.ActivityCompat
import kotlin.collections.filter

//import android.net.wifi.WifiEnterpriseConfig

/**
 * A wrapper class to manage Wi-Fi operations such as scanning,  connecting to a hotspot,
 * retrieving the connected SSID, and forgetting saved networks.
 *
 * @param context Application context required for accessing system services.
 * @param onScanResult Callback interface to return scan results.
 *
 * Note: Ensure proper permissions are granted before using (e.g., [Manifest.permission.ACCESS_FINE_LOCATION]).
 */
class WifiManagerWrapper(private val context: Context) {
    private val tag = WifiManagerWrapper::class.java.simpleName

    private val TAG = "WifiManagerWrapper"
    private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private var onScanResultCallback: ((List<ScanResult>) -> Unit)? = null
    private var savedNetworkListCallback: ((List<WifiConfiguration>) -> Unit)? = null
    private var onWifiStateChanged: ((Boolean) -> Unit)? = null
    private var onConnectionState: ((Boolean) -> Unit)? = null
    private var connectedSSID: ((String?) -> Unit)? = null

    private var wifiReconnectedRequestSSID: ((String?) -> Unit)? = null

    private var savedNetWorkConnectedRequestSSID: String? = null

    ///NEW WIFI
    private var suggestionAdded = false

    private var isManualConnection = false

    data class WifiCred(
        val ssid: String,
        val password: String,
        val user: String? = null
    )

    private val clusterConfig: List<WifiCred> = listOf(
        WifiCred("A71 - Engineering", "4cU64c7c", "beacon@ultraviolette.com"),
        WifiCred("UV Factory A - Manufacturing", "4cU64c7c", "beacon@ultraviolette.com"),
        WifiCred("UV-2017", "4cU64c7c", "beacon@ultraviolette.com"),
        WifiCred("UV - dealer", "4cU64c7c2c"),
        WifiCred("Pixel_shetty", "123456789"),
        WifiCred("Xiaomi_das", "qwertyuiopp"),
        WifiCred("CLS071", "Sv0gWR3O8WXv")
    )

    /** BroadcastReceiver for handling Wi-Fi scan results*/
    //27/01/2026
    private val wifiReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            val isWifiEnabled = isWifiEnabled()
            d(TAG, "wifiReceiver :: onReceive wifiReceiver :: isWifiEnabled :: ${isWifiEnabled}")
            onConnectionState?.invoke(isWifiEnabled)
            onWifiStateChanged?.invoke(isWifiEnabled)
            if (isWifiEnabled){
                resetManualConnection()
            }

            startScan()

            val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork
            val capabilities = cm.getNetworkCapabilities(network)
            val isWifiConnected = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

            if (wifiManager.isWifiEnabled && !isWifiConnected && savedNetWorkConnectedRequestSSID==null) {
                autoConnectAvailable()
            }

            val ssid = getConnectedWifiSSID()

            connectedSSID?.invoke(ssid)

            d(TAG, "onReceive: wifiReceiver :: getConnectedWifiSSID: connectionInfo Status : $ssid")

           getSavedNetworkList()


            val error = intent?.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1)
            d(TAG, "onReceive: Error :: $error")

            if (error == WifiManager.ERROR_AUTHENTICATING) {
                d(TAG, "onReceive: WifiManager.ERROR_AUTHENTICATING :: $savedNetWorkConnectedRequestSSID")

                val wifiInfo = intent?.getParcelableExtra<WifiInfo>(WifiManager.EXTRA_WIFI_INFO)
                var ssid = wifiInfo?.ssid?.trim('"')

                d(TAG, "onReceive: WifiManager.ERROR_AUTHENTICATING ::  Failed ID :: $savedNetWorkConnectedRequestSSID")


                wifiReconnectedRequestSSID?.invoke(savedNetWorkConnectedRequestSSID)
                savedNetWorkConnectedRequestSSID=null
            }





//            val connectedSSID = getConnectedWifiSSID()
//
//            d(TAG, "onReceive: wifiReceiver :: Connected SSID :: $connectedSSID")


//
//            d(TAG, "wifiReceiver :: onReceive wifiReceiver :: Entry   ACTION :: ${intent?.action}")
//            if (intent?.action == WifiManager.WIFI_STATE_CHANGED_ACTION) {
//                val wifiState =
//                    intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)
//                d(TAG, "wifiReceiver :: onReceive: WIFI State :: $wifiState")
//                onWifiStateChanged?.invoke(wifiState == WifiManager.WIFI_STATE_ENABLED)
//            }
//            val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//            val network = cm.activeNetwork
//            val capabilities = cm.getNetworkCapabilities(network)
//            val isWifiConnected =
//                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
//            d("wifiReceiver :: WifiConnectionState","Is Wifi connected :$isWifiConnected")
////            onConnectionState?.invoke(isWifiConnected)
////
//
//            ///WIFFI
//            if (isWifiConnected) {
//                d(TAG, "wifiReceiver :: onReceive: isWifiConnected :: Entry")
//                resetManualConnection()
//                suggestionAdded = false
//            }
//            ///END
//            val success = intent?.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false) ?: false
//            if (ActivityCompat.checkSelfPermission(
//                    this@WifiManagerWrapper.context, Manifest.permission.ACCESS_FINE_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                return
//            }
//            autoConnectFromClusterConfig()
//
//            if (!success) {
//                handleScanFailure()
//            }
        }
    }

    /** Returns the state of the Wi-Fi on the device */
    fun isWifiEnabled(): Boolean {
//        d(TAG, "isWifiEnabled: Entry")
        return wifiManager.isWifiEnabled
    }

    /**
     * Sets the state of the Wi-Fi on the device.
     */
    fun enableWifi(enable: Boolean) {
        try {
            if (wifiManager.isWifiEnabled != enable) {
                wifiManager.isWifiEnabled = enable
            }
        } catch (e: SecurityException) {
            Toast.makeText(
                context,
                "Permission denied: can't change Wi-Fi state",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    fun getWifiStatus() {
        d(TAG, "getWifiStatus: Entry")
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork
        val capabilities = cm.getNetworkCapabilities(network)
        val isWifiConnected = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        d(TAG, "getWifiStatus: WiFi Status Manual Check :: $isWifiConnected")
        onConnectionState?.invoke(isWifiConnected)

        connectedSSID?.invoke(getConnectedWifiSSID())

        getSavedNetworkList()
    }



///WIFFI
    /** Starts a Wi-Fi scan and registers the broadcast receiver to handle scan results.*/
//    fun startScan() {
//        if (ActivityCompat.checkSelfPermission(
//                context, Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            return
//        }
//        val results = wifiManager.scanResults
//        d(tag, "results :${results.size}")
//        val ssids = results.map(ScanResult::SSID).filter { it.isNotEmpty() }
//        onScanResult?.invoke(ssids)
//    }


    fun startScan() {
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        getSavedNetworkList()

        val ssid =  getConnectedWifiSSID()
        connectedSSID?.invoke(ssid)

        d(TAG, "onReceive: wifiReceiver :: getConnectedWifiSSID: connectionInfo Status : $ssid")

        val savedNetworks: List<WifiConfiguration> = wifiManager.configuredNetworks ?: emptyList()

        val uniqueNetworks = savedNetworks
            .groupBy { it.SSID.trim('"') }
            .map { it.value.first() }

        val filteredNetworks = uniqueNetworks.filter { it.SSID.trim('"') != ssid }

        val allResults = wifiManager.scanResults

        // SSIDs to exclude (from filteredNetworks)
        val excludeSsids = filteredNetworks.map { it.SSID.trim('"') }

        // Remove all scan results whose SSID is in the exclude list
        val filteredResults = allResults.filter {result->
            !excludeSsids.contains(result.SSID) && !result.SSID.isNullOrEmpty() }

        onScanResultCallback?.invoke(filteredResults)

        d(tag, "Scan triggered: $filteredResults")
    }

    ///END
    /** Handles successful Wi-Fi scan results. */
    private fun handleScanSuccess() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val results = wifiManager.scanResults
        d(tag, "results :${results.size}")
        val ssids = results.map(ScanResult::SSID).filter { it.isNotEmpty() }
//        onScanResult?.invoke(ssids)
    }

    /** Handles Wi-Fi scan failure. */
    private fun handleScanFailure() {
//        onScanResult?.invoke(emptyList())
    }

    /**
     * Connects to a given Wi-Fi hotspot.
     *
     * @param ssid The SSID of the hotspot.
     * @param password The password for the hotspot.
     */
    fun connectHotspot(ssid: String, password: String, isManual: Boolean = false) {

        ///WIFFI
        isManualConnection = isManual
        ///END
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiConfig = WifiConfiguration().apply {
            SSID = "\"$ssid\""
            preSharedKey = "\"$password\""
            allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
        }
        val networkId = wifiManager.addNetwork(wifiConfig)
        d(tag, "networkId :$networkId")
        wifiManager.disconnect()
        wifiManager.enableNetwork(networkId, true)
        wifiManager.reconnect()
    }

    fun registerWifiStateChangeReceiver() {
        val intentFilter = IntentFilter()

        // Wi-Fi adapter state (enabled/disabled)
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)

        // Scan results available
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)

        // Network state changes (connected/disconnected)
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)

        // Supplicant state changes (authentication, association, etc.)
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)

        // Authentication errors (wrong password, etc.)
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)

        // Connectivity changes (system-wide)
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)

        context.registerReceiver(wifiReceiver, intentFilter)
    }


    fun unregisterWifiStateChangeReceiver() {
        context.unregisterReceiver(wifiReceiver)
    }

    /**
     * Returns the SSID of the currently connected Wi-Fi network.
     *
     * @return SSID of the connected Wi-Fi or empty string if not connected.
     */
    fun getConnectedWifiSSID(): String {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val connectionInfo = wifiManager.connectionInfo
        val ssid = connectionInfo?.ssid?.trim('"')

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return "<unknown ssid>"
        val capabilities = cm.getNetworkCapabilities(network) ?: return "<unknown ssid>"

        // Only return SSID if the active network is Wi‑Fi
        return if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) &&
            !ssid.isNullOrEmpty() &&
            ssid != "<unknown ssid>") {
            ssid
        } else {
            "<unknown ssid>"
        }
    }

    private fun getSavedNetworkList() {

        val ssid = getConnectedWifiSSID()

        val savedNetworks: List<WifiConfiguration> = wifiManager.configuredNetworks ?: emptyList()

        val uniqueNetworks = savedNetworks
            .groupBy { it.SSID.trim('"') }
            .map { it.value.first() }

        val filteredNetworks = uniqueNetworks.filter { it.SSID.trim('"') != ssid }

        savedNetworkListCallback?.invoke(filteredNetworks)
    }

    /** Forgets the currently connected Wi-Fi network.*/
    @SuppressLint("MissingPermission")
    fun forgetHotspot() {
        val ssid = getConnectedWifiSSID()?.trim('"')
        d(tag, "Forget SSID: $ssid")

        if (ssid.isNullOrEmpty()) return

        val configuredNetworks = wifiManager.configuredNetworks ?: return
        for (config in configuredNetworks) {
            if (config.SSID.trim('"') == ssid) {
                // Disconnect immediately
                wifiManager.disconnect()

                // Disable the network so it won’t reconnect in this session
                val disabled = wifiManager.disableNetwork(config.networkId)
                d(tag, "Disabled network for SSID: $ssid - Success: $disabled")

                // Remove the network from system configuration
                val removed = wifiManager.removeNetwork(config.networkId)
                d(tag, "Removed network for SSID: $ssid - Success: $removed")

                // Persist changes so they survive reboot / WiFi toggle
                val saved = wifiManager.saveConfiguration()
                d(tag, "Saved configuration after removal - Success: $saved")

                break
            }
        }
    }



    fun getSavedNetworkList(callback: (List<WifiConfiguration>) -> Unit) {
        savedNetworkListCallback = callback
    }


    fun connectToSavedNetwork(ssid: String) {
        val configs = wifiManager.configuredNetworks
        savedNetWorkConnectedRequestSSID = ssid

        d(TAG, "Connecting to connectToSavedNetwork Requested :: $ssid")

        val targetConfig = configs?.find { it.SSID.trim('"') == ssid }
        targetConfig?.let { config ->
            val networkId = config.networkId
            if (networkId != -1) {
                // Disconnect first
                wifiManager.disconnect()

                // Disable other networks to force switch
                configs?.forEach { other ->
                    if (other.networkId != networkId) {
                        wifiManager.disableNetwork(other.networkId)
                    }
                }

                // Enable and reconnect to target
                wifiManager.enableNetwork(networkId, true)
                wifiManager.reconnect()

                d(TAG, "Connecting to connectToSavedNetwork :: $ssid")
            } else {
                d(TAG, "Invalid networkId for connectToSavedNetwork :: $ssid")
            }
        }
    }


    fun autoConnectAvailable() {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

        // Get all saved networks
        val savedNetworks = wifiManager.configuredNetworks ?: emptyList()

        // Get current scan results
        val availableSsids = wifiManager.scanResults.map { it.SSID }

        // Find the first saved network that is actually available
        val targetConfig = savedNetworks.firstOrNull { availableSsids.contains(it.SSID.trim('"')) }

        if (targetConfig != null) {
            wifiManager.enableNetwork(targetConfig.networkId, true)
            wifiManager.reconnect()
            d("WifiManagerWrapper", "autoConnectAvailable Connecting to available SSID: ${targetConfig.SSID.trim('"')}")
        } else {
            d("WifiManagerWrapper", "autoConnectAvailable No saved networks available in current scan")
            autoConnectFromClusterConfig()
        }
    }




    fun connectHotspotWithSuggestion(ssid: String, password: String) {
        val suggestion = WifiNetworkSuggestion.Builder()
            .setSsid(ssid)
            .setWpa2Passphrase(password)
            .setIsAppInteractionRequired(true)
            .build()

        val suggestionsList = listOf(suggestion)
        val status = wifiManager.addNetworkSuggestions(suggestionsList)

        if (status == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
            Toast.makeText(context, "Wi-Fi suggestion added", Toast.LENGTH_SHORT).show()
            d(tag, "Suggestion added successfully")

            // Register receiver to detect post-connection
            val intentFilter =
                IntentFilter(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION)
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context?, intent: Intent?) {
                    Toast.makeText(ctx, "Connected to $ssid", Toast.LENGTH_SHORT).show()
                    d(tag, "Post-connection broadcast received for $ssid")

                    // Check actual Wi-Fi connection status
                    val connectivityManager =
                        ctx?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                    val network = connectivityManager?.activeNetwork
                    val capabilities =
                        network?.let { connectivityManager.getNetworkCapabilities(it) }
                    val isWifi =
                        capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
                    if (isWifi) {
                        val connectedSsid = wifiManager.connectionInfo.ssid
                        d(tag, "Currently connected to $connectedSsid")
                    }

                    // Unregister receiver after use
                    try {
                        ctx?.unregisterReceiver(this)
                    } catch (e: Exception) {
                        e(tag, "exception unregistering wifi broadcast receiver ${e.message}")
                    }
                }
            }
            context.registerReceiver(receiver, intentFilter)

        } else {
            Toast.makeText(context, "Suggestion failed: $status", Toast.LENGTH_LONG).show()
            e(tag, "Suggestion failed with status: $status")
        }
    }


//    fun connectEnterpriseWifi(cred: WifiCred) {
//
//        val enterpriseConfig = WifiEnterpriseConfig().apply {
//            identity = cred.user
//            password = cred.password
//            eapMethod = WifiEnterpriseConfig.Eap.PEAP
//            phase2Method = WifiEnterpriseConfig.Phase2.MSCHAPV2
//
//            setDomainSuffixMatch("ultraviolette.com")
//        }
//
//        val suggestion = WifiNetworkSuggestion.Builder()
//            .setSsid(cred.ssid)
//            .setWpa2EnterpriseConfig(enterpriseConfig)
//            .build()
//
//        val status = wifiManager.addNetworkSuggestions(listOf(suggestion))
//
//        if (status == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
//            d(tag, "Enterprise suggestion added for ${cred.ssid}")
//        } else {
//            e(tag, "Enterprise suggestion failed: $status")
//        }
//    }
//
//    fun scanResult(callback: (List<String>) -> Unit) {
//        onScanResult = callback
//    }

    fun scanResult(callback: (List<ScanResult>) -> Unit) {
        onScanResultCallback = callback
//        // All scanned networks
//        val allResults = wifiManager.scanResults
//
//        // Saved networks (pre‑Android 10 only)
//        val savedNetworks: List<WifiConfiguration> = wifiManager.configuredNetworks ?: emptyList()
//
//        // Deduplicate by SSID
//        val uniqueSaved = savedNetworks
//            .groupBy { it.SSID.trim('"') }
//            .map { it.value.first() }
//
//        val savedSsids = uniqueSaved.map { it.SSID.trim('"') }
//
//        // Filter out any SSID that is already saved
//        val filteredResults = allResults.filter { result ->
//            result.SSID.isNotEmpty() && !savedSsids.contains(result.SSID)
//        }
//
//        callback(filteredResults)
    }

    fun wifiState(callback: (Boolean) -> Unit) {
        onWifiStateChanged = callback
    }

    //27/01/2026
    fun connectionState(callback: (Boolean) -> Unit) {
        d(TAG, "connectionState: Entry")
        onConnectionState = callback

        getWifiStatus()
    }
    fun getCurrentSignalLevel(): Int {
        val rssi = wifiManager.connectionInfo.rssi
        return if (rssi <= -100) {
            0
        } else {
            WifiManager.calculateSignalLevel(rssi, 6)
        }
    }

    ///WIFFI
    fun autoConnectFromClusterConfig() {

        if (isManualConnection) {
            d(tag, "Manual connection active → skipping auto-connect")
            return
        }

        val current = getConnectedWifiSSID()

        val isKnownNetwork = clusterConfig.any { it.ssid == current }

        if (isKnownNetwork) {
            d(tag, "Already connected to known network: $current")
            return
        }

        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val scanResults = wifiManager.scanResults

        d(tag, "Available networks: ${scanResults.map { it.SSID }}")
        d(tag, "Available networks: Match Checking Total List $scanResults")

        for (cred in clusterConfig) {

            d(TAG, "autoConnectFromClusterConfig: Match Checking Checking SSID :: ${cred.ssid}")

            val match = scanResults.find { it.SSID?.trim('"') == cred.ssid.trim('"') }

            if (match != null) {

                if (suggestionAdded) {
                    d(tag, "Suggestion already added, skipping")
                    return
                }

                d(tag, "Auto connecting to ${cred.ssid}")

//                if (cred.user != null) {
//                    // ENTERPRISE WIFI
//                    connectEnterpriseWifi(cred)
//                } else {
                    // PERSONAL WIFI
                    connectHotspotWithSuggestion(cred.ssid, cred.password)
                //}

                suggestionAdded = true
                return
            }
        }

        d(tag, "No matching WiFi found")
    }

    fun resetManualConnection() {
        isManualConnection = false
    }

    fun getConnectedSSID(callback: (String?) -> Unit) {
        connectedSSID = callback
    }

    fun getReconnectRequestSSID(callback: (String?) -> Unit) {
        wifiReconnectedRequestSSID = callback
    }

    ///ENDD
}



