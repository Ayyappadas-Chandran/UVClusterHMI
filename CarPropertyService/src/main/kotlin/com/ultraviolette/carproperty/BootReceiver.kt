package com.ultraviolette.carproperty

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        context ?: return
        Log.d("CarProp.BootReceiver", "Boot completed — starting CarPropertyService")
        val svc = Intent(context, CarPropertyService::class.java)
        context.startForegroundService(svc)
    }
}
