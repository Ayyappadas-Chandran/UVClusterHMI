package com.ultraviolette.clusterdatabus

import com.ultraviolette.cluster.aidl.BtState
import com.ultraviolette.cluster.aidl.VehicleData
import com.ultraviolette.cluster.aidl.VehicleSnapshot
import com.ultraviolette.cluster.aidl.WifiState

/** Thread-safe last-known state cache for all signal types. */
class StateManager {

    @Volatile var vehicleData: VehicleData = VehicleData()
        private set

    @Volatile var vehicleSnapshot: VehicleSnapshot = VehicleSnapshot()
        private set

    @Volatile var btState: BtState = BtState()
        private set

    @Volatile var wifiState: WifiState = WifiState()
        private set

    fun update(data: VehicleData) {
        vehicleData = data
    }

    fun update(snapshot: VehicleSnapshot) {
        vehicleSnapshot = snapshot
    }

    fun update(state: BtState) {
        btState = state
    }

    fun update(state: WifiState) {
        wifiState = state
    }
}
