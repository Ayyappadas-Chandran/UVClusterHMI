package com.ultraviolette.clusterdatabus.config

import android.os.RemoteCallbackList
import android.util.Log
import com.ultraviolette.cluster.aidl.FeatureConfig
import com.ultraviolette.cluster.aidl.IConfigCallback

/** Fans out FeatureConfig changes to all bound IConfigCallback subscribers. */
class ConfigSubscriptionManager {

    private val tag = "ClusterBus.ConfigSubMgr"
    private val callbacks = RemoteCallbackList<IConfigCallback>()

    fun register(cb: IConfigCallback?) {
        cb?.let { callbacks.register(it) }
    }

    fun unregister(cb: IConfigCallback?) {
        cb?.let { callbacks.unregister(it) }
    }

    fun broadcastConfig(config: FeatureConfig) {
        val count = callbacks.beginBroadcast()
        try {
            for (i in 0 until count) {
                try {
                    callbacks.getBroadcastItem(i).onConfigChanged(config)
                } catch (e: Exception) {
                    Log.e(tag, "onConfigChanged failed at index $i", e)
                }
            }
        } finally {
            callbacks.finishBroadcast()
        }
    }

    fun kill() {
        callbacks.kill()
    }
}
