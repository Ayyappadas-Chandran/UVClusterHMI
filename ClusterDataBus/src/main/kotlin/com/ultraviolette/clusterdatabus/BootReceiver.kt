package com.ultraviolette.clusterdatabus

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        context ?: return
        Log.d("ClusterBus.BootReceiver", "Boot completed — starting SharedSignalService")
        val svc = Intent(context, SharedSignalService::class.java)
        context.startForegroundService(svc)
    }
}
