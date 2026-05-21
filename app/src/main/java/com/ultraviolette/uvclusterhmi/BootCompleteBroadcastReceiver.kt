package com.ultraviolette.uvclusterhmi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log.d
import com.ultraviolette.uvclusterhmi.data.repository.SharedPreferenceRepoImpl
import com.ultraviolette.uvclusterhmi.domain.manager.PreferenceManager

class BootCompleteBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        val prefs = PreferenceManager(SharedPreferenceRepoImpl(context))
        val pending = prefs.isOtaComplete
        val oldBuild = prefs.isOldBuild
        val newBuild = Build.FINGERPRINT

        if (pending && oldBuild != null && oldBuild != newBuild) {
            d("OTAA", "Reboot reason == OTA Update")

        } else {
            d("OTAA", "Reboot reason == Normal reboot")
        }
        prefs.saveOtaCompleted(false)
        prefs.saveOldBuild("")
    }
}