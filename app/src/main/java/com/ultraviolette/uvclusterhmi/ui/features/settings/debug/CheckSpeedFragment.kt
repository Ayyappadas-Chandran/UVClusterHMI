
package com.ultraviolette.uvclusterhmi.ui.features.settings.debug

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ultraviolette.uvclusterhmi.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL
import android.util.Log.d
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import java.io.BufferedInputStream
import android.net.wifi.WifiManager
import android.widget.ImageView
import android.telephony.TelephonyManager
import androidx.annotation.RequiresPermission
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
class CheckSpeedFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.fragment_check_speed, container, false)

        return rootView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val downloadStatusText = view.findViewById<TextView>(R.id.downloadText)
        val uploadStatusText = view.findViewById<TextView>(R.id.uploadText)
        val startUploadBtn = view.findViewById<Button>(R.id.checkUpload)
        val startDownloadBtn = view.findViewById<Button>(R.id.checkDownload)
        val returnBtn = view.findViewById<ImageView>(R.id.backBtn)
        val ssidText = view.findViewById<TextView>(R.id.ssidText)
	val imeiText = view.findViewById<TextView>(R.id.imeiText)
        lifecycleScope.launch {
            
            val result = withContext(Dispatchers.IO) {
                  getNetworkStatus()
            }
            ssidText.text = "$result"

        }
	lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                getImei()
            }
            imeiText.text = "Imei no.: $result"

        }

        startDownloadBtn.setOnClickListener {
            d("CheckSpeedFragment", "download btn clicked")
            lifecycleScope.launch {
                downloadStatusText.text = "Testing download..."
                val result = withContext(Dispatchers.IO) {
                    testDownloadSpeed()
                }
                downloadStatusText.text = result
            }

        }

        startUploadBtn.setOnClickListener {
            d("CheckSpeedFragment", "Upload btn clicked")
            lifecycleScope.launch {
                uploadStatusText.text = "Testing upload..."
                val result = withContext(Dispatchers.IO) {
                    testUploadSpeed()
                }
                uploadStatusText.text = result
            }
        }

        returnBtn.setOnClickListener {
            findNavController().navigate(R.id.action_checkSpeedFragment_to_debugFragment)
        }


    }

    private fun testDownloadSpeed(): String {
        return try {
            val url = URL("https://speed.cloudflare.com/__down?bytes=50000000")
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.setRequestProperty("Connection", "close")
            connection.connect()

            val startTime = System.currentTimeMillis()

            val inputStream = BufferedInputStream(connection.inputStream)
            val buffer = ByteArray(8 * 1024)
            var totalBytes = 0L

            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                totalBytes += read
            }

            inputStream.close()
            connection.disconnect()

            val endTime = System.currentTimeMillis()

            val timeSec = (endTime - startTime) / 1000.0
            val speedMbps = (totalBytes * 8) / (timeSec * 1_000_000)

            "Download:\nSpeed: %.2f Mbps\nTime: %.2f sec"
                .format(speedMbps, timeSec)

        } catch (e: Exception) {
	      // "Download Error: ${e.message}"
	    d("CheckSpeedFragment", "Download failed: ${e.message}")
            "Download failed"
        }
    }

    private fun testUploadSpeed(): String {
        return try {
            d("CheckSpeedFragment", "testUploadSpeed")
            //val url = URL("http://postman-echo.com/post")
            val url = URL("https://postman-echo.com/post")
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/octet-stream")

            val dataSize = 10 * 1024 * 1024 // 10MB (safe)
            val buffer = ByteArray(8 * 1024)

            val startTime = System.currentTimeMillis()

            val outputStream = connection.outputStream

            var bytesWritten = 0

            while (bytesWritten < dataSize) {
                val bytesToWrite = minOf(buffer.size, dataSize - bytesWritten)
                outputStream.write(buffer, 0, bytesToWrite)
                bytesWritten += bytesToWrite
            }

            outputStream.flush()
            outputStream.close()

            // Trigger request
            val responseCode = connection.responseCode

            val endTime = System.currentTimeMillis()

            val timeSec = (endTime - startTime) / 1000.0
            val speedMbps = (dataSize * 8) / (timeSec * 1_000_000)

            d("CheckSpeedFragment", "Upload speed $speedMbps time: $timeSec")

            "Upload:\nSpeed: %.2f Mbps\nTime: %.2f sec\nResponse: $responseCode"
                .format(speedMbps, timeSec)

        } catch (e: Exception) {
            //"Upload Error: ${e.message}"
	    d("CheckSpeedFragment", "Upload failed: ${e.message}")
            "Upload failed"
        }
    }
    fun getWifiSSID(context: Context?): String {
        return try {
            val wifiManager = context?.applicationContext
                ?.getSystemService(Context.WIFI_SERVICE) as WifiManager

            val info = wifiManager.connectionInfo
            var ssid = info.ssid

            // Remove quotes if present
            if (ssid != null && ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length - 1)
            }

            ssid ?: "Unknown SSID"

        } catch (e: Exception) {
            "SSID Error"
        }
    }
    fun getNetworkStatus(): String {
        return try {
            val context = requireContext()

            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val wifiManager =
                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

            val wifiInfo = wifiManager.connectionInfo

            // 🔹 1. Check WIFI FIRST (PRIORITY)
            if (wifiInfo != null &&
                wifiInfo.networkId != -1 &&
                wifiInfo.ssid != null &&
                wifiInfo.ssid != "<unknown ssid>"
            ) {
                var ssid = wifiInfo.ssid

                if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                    ssid = ssid.substring(1, ssid.length - 1)
                }

                return "SSID : $ssid"
            }

            // 🔹 2. Check MOBILE DATA
            val activeNetwork = connectivityManager.activeNetwork ?: return "No connection"
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                ?: return "No connection"

            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return "LTE connected"
            }

            // 🔹 3. No connection
            "No connection"

        } catch (e: Exception) {
            "Network Error"
        }
    }    


    private fun getImei(): String {
        return try {
            val tm =
                requireContext().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            tm.getImei(0) ?: ""
        } catch (e: Exception) {
            ""
        }
    }

}

