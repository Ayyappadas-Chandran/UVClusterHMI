package com.ultraviolette.clusterdatabus

import android.os.RemoteCallbackList
import android.util.Log
import com.ultraviolette.cluster.aidl.BtScanResult
import com.ultraviolette.cluster.aidl.BtState
import com.ultraviolette.cluster.aidl.ISharedSignalCallback
import com.ultraviolette.cluster.aidl.VehicleSnapshot
import com.ultraviolette.cluster.aidl.WifiScanResult
import com.ultraviolette.cluster.aidl.WifiState

/** Manages the set of ISharedSignalCallback subscribers and fans out updates. */
class SubscriptionManager {

    private val tag = "ClusterBus.SubscriptionManager"
    private val callbacks = RemoteCallbackList<ISharedSignalCallback>()

    fun register(cb: ISharedSignalCallback?) {
        cb?.let { callbacks.register(it) }
    }

    fun unregister(cb: ISharedSignalCallback?) {
        cb?.let { callbacks.unregister(it) }
    }

    fun broadcastVehicleSnapshot(snapshot: VehicleSnapshot) {
        broadcast("onVehicleSnapshot") { it.onVehicleSnapshot(snapshot) }
    }

    fun broadcastBtState(state: BtState) {
        broadcast("onBtState") { it.onBtState(state) }
    }

    fun broadcastWifiState(state: WifiState) {
        broadcast("onWifiState") { it.onWifiState(state) }
    }

    fun broadcastHandlebarButton(button: Int) {
        broadcast("onHandlebarButton") { it.onHandlebarButton(button) }
    }

    fun broadcastBluetoothScanResult(devices: List<BtScanResult>) {
        broadcast("onBluetoothScanResult") { it.onBluetoothScanResult(devices) }
    }

    fun broadcastWifiScanResult(results: List<WifiScanResult>) {
        broadcast("onWifiScanResult") { it.onWifiScanResult(results) }
    }

    fun kill() {
        callbacks.kill()
    }

    private inline fun broadcast(label: String, action: (ISharedSignalCallback) -> Unit) {
        val count = callbacks.beginBroadcast()
        try {
            for (i in 0 until count) {
                try {
                    action(callbacks.getBroadcastItem(i))
                } catch (e: Exception) {
                    Log.e(tag, "$label failed at index $i", e)
                }
            }
        } finally {
            callbacks.finishBroadcast()
        }
    }
}
