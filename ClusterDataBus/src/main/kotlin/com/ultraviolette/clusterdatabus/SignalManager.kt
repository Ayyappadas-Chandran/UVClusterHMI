package com.ultraviolette.clusterdatabus

import android.util.Log
import com.ultraviolette.cluster.aidl.BtState
import com.ultraviolette.cluster.aidl.VehicleSnapshot
import com.ultraviolette.cluster.aidl.WifiState

/**
 * Central coordinator — receives raw updates from ConnectionManager,
 * applies change detection, updates StateManager, then fans out via SubscriptionManager.
 * All work is posted to ThreadDispatcher to stay off the Binder thread.
 */
class SignalManager(
    private val stateManager: StateManager,
    private val changeDetector: ChangeDetector,
    private val subscriptionManager: SubscriptionManager,
    private val threadDispatcher: ThreadDispatcher,
    private val persistenceHelper: PersistenceHelper
) {
    private val tag = "ClusterBus.SignalManager"

    fun onVehicleSnapshot(incoming: VehicleSnapshot) {
        threadDispatcher.post {
            if (!changeDetector.hasChanged(incoming, stateManager.vehicleSnapshot)) return@post
            stateManager.update(incoming)
            subscriptionManager.broadcastVehicleSnapshot(incoming)
            Log.d(tag, "VehicleSnapshot broadcast: speed=${incoming.vehicleSpeed}")
        }
    }

    fun onBtState(incoming: BtState) {
        threadDispatcher.post {
            if (!changeDetector.hasChanged(incoming, stateManager.btState)) return@post
            stateManager.update(incoming)
            persistenceHelper.saveBtState(incoming)
            subscriptionManager.broadcastBtState(incoming)
            Log.d(tag, "BtState broadcast: enabled=${incoming.isEnabled}")
        }
    }

    fun onWifiState(incoming: WifiState) {
        threadDispatcher.post {
            if (!changeDetector.hasChanged(incoming, stateManager.wifiState)) return@post
            stateManager.update(incoming)
            persistenceHelper.saveWifiState(incoming)
            subscriptionManager.broadcastWifiState(incoming)
            Log.d(tag, "WifiState broadcast: enabled=${incoming.isEnabled} ssid=${incoming.connectedSsid}")
        }
    }

    /**
     * Handlebar / swift-button press received from CarPropertyService.
     * Always forwarded — no change-detection (repeated presses of the same button are valid).
     */
    fun onHandlebarButton(button: Int) {
        threadDispatcher.post {
            subscriptionManager.broadcastHandlebarButton(button)
            Log.d(tag, "HandlebarButton broadcast: button=$button")
        }
    }
}
