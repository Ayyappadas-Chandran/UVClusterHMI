
package com.ultraviolette.uvclusterhmi.ui.features.settings.wifi

import android.Manifest

import android.annotation.SuppressLint

import android.content.Context

import android.content.pm.PackageManager

import android.net.wifi.*

import android.os.Handler

import android.os.Looper

import android.util.Log

import androidx.core.app.ActivityCompat

class WifiAutoConnector(

    private val context: Context

) {

    private val TAG = "WIFI_DEBUG"

    private val wifiManager =

        context.applicationContext.getSystemService(

            Context.WIFI_SERVICE

        ) as WifiManager

    private val handler = Handler(Looper.getMainLooper())

    private val failedNetworks = mutableMapOf<String, Int>()

    private val MAX_RETRIES = 3

    data class WifiCred(

        val ssid: String,

        val password: String,

        val user: String? = null

    )

    private val wifiMap = mapOf(

        "A71 - Engineering" to WifiCred(

            "A71 - Engineering",

            "4cU64c7c",

            "beacon@ultraviolette.com"

        ),

        "UV Factory A - Manufacturing" to WifiCred(

            "UV Factory A - Manufacturing",

            "4cU64c7c",

            "beacon@ultraviolette.com"

        ), 
         "CLS071" to WifiCred(

           "CLS071",

           "Sv0gWR3O8WXv"

       ),

        "Pixel_shetty" to WifiCred(

            "Pixel_shetty",

            "123456789"

        ),

        "Xiaomi_das" to WifiCred(

            "Xiaomi_das",

            "qwertyuiopp"

        )

    )

    fun startAutoConnectLoop() {

        handler.postDelayed(object : Runnable {

            override fun run() {

                maintainWifiConnection()

                handler.postDelayed(this, 20000)

            }

        }, 5000)

    }

    fun stopAutoConnectLoop() {

        handler.removeCallbacksAndMessages(null)

    }

    private fun maintainWifiConnection() {

        val connectedSSID = getConnectedSSID()

        if (connectedSSID != null && wifiMap.containsKey(connectedSSID)) {

            Log.d(TAG, "Already connected to known WiFi: $connectedSSID")

            failedNetworks.clear()

            return

        }

        Log.d(TAG, "Not connected → attempting connect")

        if (ActivityCompat.checkSelfPermission(

                context,

                Manifest.permission.ACCESS_FINE_LOCATION

            ) != PackageManager.PERMISSION_GRANTED

        ) {

            Log.e(TAG, "Missing ACCESS_FINE_LOCATION permission")

            return

        }

        val cachedResults = wifiManager.scanResults

        if (cachedResults.isNullOrEmpty()) {

            Log.w(TAG, "No scan results available yet")

            return

        }

        autoConnect(cachedResults)

    }

    private fun getConnectedSSID(): String? {

        return try {

            @SuppressLint("MissingPermission")

            val info = wifiManager.connectionInfo

            val ssid = info?.ssid

                ?.removePrefix("\"")

                ?.removeSuffix("\"")

            if (ssid.isNullOrEmpty() || ssid == "<unknown ssid>") null

            else ssid

        } catch (e: Exception) {

            null

        }

    }

    private fun autoConnect(scanResults: List<ScanResult>) {

        val sorted = scanResults.sortedByDescending {

            it.capabilities.contains("EAP")

        }

        for (result in sorted) {

            val ssid = result.SSID

            val cred = wifiMap[ssid] ?: continue

            val retries = failedNetworks.getOrDefault(ssid, 0)

            if (retries >= MAX_RETRIES) {

                Log.w(TAG, "Skipping $ssid — max retries reached")

                continue

            }

            Log.d(TAG, "Matched SSID: $ssid (attempt ${retries + 1})")

            val success = if (result.capabilities.contains("EAP")) {

                connectEnterpriseWifi(

                    ssid,

                    cred.user ?: "",

                    cred.password

                )

            } else {

                connectPersonalWifi(

                    ssid,

                    cred.password

                )

            }

            if (!success) {

                failedNetworks[ssid] = retries + 1

                Log.e(TAG, "Failed to queue connection for $ssid")

            } else {

                Log.d(TAG, "Connection queued for $ssid")

            }

            return

        }

    }

    private fun connectPersonalWifi(

        ssid: String,

        password: String

    ): Boolean {

        Log.d(TAG, "Connecting PERSONAL: $ssid")

        return try {

            @SuppressLint("MissingPermission")

            val config = WifiConfiguration().apply {

                SSID = "\"$ssid\""

                preSharedKey = "\"$password\""

                allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)

                macRandomizationSetting = WifiConfiguration.RANDOMIZATION_NONE

                priority = 1000

            }

            connect(config)

        } catch (e: Exception) {

            Log.e(TAG, "connectPersonalWifi exception: ${e.message}")

            false

        }

    }

    // ✅ UPDATED ONLY THIS METHOD

    private fun connectEnterpriseWifi(

        ssid: String,

        username: String,

        password: String

    ): Boolean {

        Log.d(TAG, "Connecting ENTERPRISE (Suggestion API): $ssid")

        return try {

            val enterpriseConfig = WifiEnterpriseConfig().apply {

                identity = username

                anonymousIdentity = "anonymous"

                this.password = password

                eapMethod = WifiEnterpriseConfig.Eap.PEAP

                phase2Method = WifiEnterpriseConfig.Phase2.MSCHAPV2

                try {

                    val field = WifiEnterpriseConfig::class.java

                        .getDeclaredField("mFields")

                    field.isAccessible = true

                    @Suppress("UNCHECKED_CAST")

                    val fields = field.get(this) as HashMap<String, String>

                    fields["peaplabel"] = "0"

                    fields["peapversion"] = "0"

                    Log.d(TAG, "peaplabel + peapversion set")

                } catch (e: Exception) {

                    Log.w(TAG, "reflection failed: ${e.message}")

                }

                @Suppress("DEPRECATION")

                setCaCertificate(null)

                domainSuffixMatch = ""

            }

            val suggestion = WifiNetworkSuggestion.Builder()

                .setSsid(ssid)

                .setWpa2EnterpriseConfig(enterpriseConfig)

                .setIsAppInteractionRequired(false)

                .setPriority(1000)

                .build()

            val status = wifiManager.addNetworkSuggestions(listOf(suggestion))

            if (status == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {

                Log.d(TAG, "Suggestion added for $ssid")

                true

            } else {

                Log.e(TAG, "Suggestion failed: $status")

                false

            }

        } catch (e: Exception) {

            Log.e(TAG, "connectEnterpriseWifi exception: ${e.message}")

            false

        }

    }

    @SuppressLint("MissingPermission")

    private fun connect(config: WifiConfiguration): Boolean {

        val existingNetId = wifiManager.configuredNetworks

            ?.firstOrNull { it.SSID == config.SSID }

            ?.networkId ?: -1

        val netId = if (existingNetId != -1) {

            Log.d(TAG, "Updating existing network: $existingNetId")

            config.networkId = existingNetId

            wifiManager.updateNetwork(config)

        } else {

            Log.d(TAG, "Adding new network")

            wifiManager.addNetwork(config)

        }

        Log.d(TAG, "netId = $netId")

        return if (netId != -1) {

            wifiManager.disconnect()

            wifiManager.enableNetwork(netId, true)

            wifiManager.reconnect()

            Log.d(TAG, "Connection requested for netId=$netId")

            true

        } else {

            Log.e(TAG, "Failed to add/update network")

            false

        }

    }

}

