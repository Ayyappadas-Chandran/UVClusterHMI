package com.ultraviolette.uvclusterhmi.ui.features.settings.HabitatResetFragment

import android.content.Intent
import android.os.Bundle
import android.os.UserHandle
import android.service.controls.ControlsProviderService.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ultraviolette.uvclusterhmi.R

class HabitatResetFragment: Fragment(){

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_habitat_reset,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val button = view.findViewById<View>(R.id.haibitatreset)

        button.setOnClickListener {
            val intent = Intent("com.example.database.ACTION_RETAIN_LOGS").apply {
                setPackage("com.example.database")
//                putExtra("used_pct", usedPct)
            }

            try {
                //requireContext().sendBroadcastAsUser(intent, UserHandle.ALL)
                Log.d(TAG, "✅ Broadcast SENT successfully")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Broadcast FAILED", e)
            }

        }
    }


}
