package com.ultraviolette.uvclusterhmi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.ultraviolette.uvclusterhmi.data.repository.SharedPreferenceRepoImpl
import com.ultraviolette.uvclusterhmi.domain.manager.PreferenceManager

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "DefaultNightMode onReceive: Boot Completed :: Before")
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            Log.d("BootReceiver", "DefaultNightMode onReceive: Boot Completed :: After Theme update")
            MyViewModelProvider.instance.onBootCompleted()
        }
    }
}
