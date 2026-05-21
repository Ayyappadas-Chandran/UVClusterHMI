package com.ultraviolette.uvclusterhmi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log.d


class FotaReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent) {
        when (intent.action) {

            ACTION_FOTA_EVENT -> {
                d("FotaReceiver", "FOTA download completed")
                listener?.invoke(ACTION_FOTA_EVENT)
            }

            ACTION_UDP_TIMEOUT -> {
                d("FotaReceiver", "UDP Timeout received")
                listener?.invoke(ACTION_UDP_TIMEOUT)
            }
        }
    }

    companion object {
        const val ACTION_FOTA_EVENT = "com.suprajit.ACTION_FOTA_EVENT"
        const val ACTION_UDP_TIMEOUT = "com.suprajit.ACTION_UDP_TIMEOUT"

        var listener: ((String) -> Unit)? = null
    }
}
