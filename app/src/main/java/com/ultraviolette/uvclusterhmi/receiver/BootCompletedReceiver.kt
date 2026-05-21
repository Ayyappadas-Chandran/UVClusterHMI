package com.ultraviolette.uvclusterhmi.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootCompletedReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {

        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {

            Log.d("BootReceiver", "🚀 Boot completed")

            val prefs = context.getSharedPreferences("boot_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("is_boot", true).apply()
        }
    }
}
