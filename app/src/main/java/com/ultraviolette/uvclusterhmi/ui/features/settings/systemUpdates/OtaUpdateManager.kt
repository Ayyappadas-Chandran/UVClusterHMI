package com.ultraviolette.uvclusterhmi.ui.features.settings.systemUpdates

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import android.content.Context
import android.os.RemoteException
import android.os.UpdateEngine
import android.os.UpdateEngineCallback
import android.util.Log
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OtaUpdateManager(
    private val listener: OtaUpdateListener,
    private val scope: CoroutineScope
) {

    interface OtaUpdateListener {
        fun onStatus(status: Int, percent: Float)
        fun onCompleted(errorCode: Int)
    }
   private val updateEngine = UpdateEngine()

    fun startUpdate(headers:Array<String>,size: Long) {
        val newPath = "/data/vendor/uv_fota/fota/AndroidFiles/payload.bin"
        val newUrl = "file://$newPath"

        val isNewPathValid = validatePayloadPath(newPath)
        Log.d("UV_OTA", "isNewPathValid: $isNewPathValid")
        updateEngine.bind(object : UpdateEngineCallback() {
            override fun onStatusUpdate(status: Int, percent: Float) {
                scope.launch {
                    withContext(Dispatchers.Main) {
                        listener.onStatus(status, percent)
                    }
                }
            }

            override fun onPayloadApplicationComplete(errorCode: Int) {
                scope.launch {
                    withContext(Dispatchers.Main) {
                        listener.onCompleted(errorCode)
                    }
                }
                updateEngine.unbind()
            }
        })

        scope.launch {
            try {
                updateEngine.applyPayload(
                    PAYLOAD_URL,
                    0,
                    size,
                    headers
                )
            } catch (e: Exception) {
                Log.e("UV_OTA", "applyPayload failed", e)
            }
        }
    }
    fun stop() {
        scope.cancel()
        updateEngine.unbind()
    }
    //TODO : Just to check whether the path can be accessible or not
    private fun validatePayloadPath(path: String): Boolean {
        return try {
            val file = java.io.File(path)

            Log.d("UV_OTA", "Checking path: $path")
            Log.d("UV_OTA", "Exists: ${file.exists()}")
            Log.d("UV_OTA", "IsFile: ${file.isFile}")
            Log.d("UV_OTA", "CanRead: ${file.canRead()}")
            Log.d("UV_OTA", "AbsolutePath: ${file.absolutePath}")

            file.exists() && file.isFile && file.canRead()
        } catch (e: Exception) {
            Log.e("UV_OTA", "Path validation failed", e)
            false
        }
    }

    companion object {
        const val PAYLOAD_URL = "file:///data/ota_file/payload.bin"
        const val PAYLOAD_OFFSET = 2481L
        const val PAYLOAD_SIZE_BYTES = 1056779957L
        val HEADERS = arrayOf(
            "FILE_HASH=I8ADCipu7+BEH6kiHFHEfWFOTHV8O6atyPcYT/CKQZg=",
            "FILE_SIZE=1081894806",
            "METADATA_HASH=OkjFc9/O/pBTFVINQ0krVX4M1mRyWekG+nz/Z42BNbs=",
            "METADATA_SIZE=88523"
        )
    }
}

