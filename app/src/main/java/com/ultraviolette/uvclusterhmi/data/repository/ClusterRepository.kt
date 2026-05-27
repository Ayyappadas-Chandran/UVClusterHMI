package com.ultraviolette.uvclusterhmi.data.repository

import com.ultraviolette.cluster.aidl.BtState
import com.ultraviolette.cluster.aidl.VehicleSnapshot
import com.ultraviolette.cluster.aidl.WifiState
import com.ultraviolette.uvclusterhmi.data.datasource.BusDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.onStart

class ClusterRepository(private val bus: BusDataSource) {

    val vehicleSnapshot: Flow<VehicleSnapshot> = bus.vehicleSnapshot
        .onStart { emit(VehicleSnapshot()) }

    val btState: Flow<BtState> = bus.btState
        .onStart { emit(BtState()) }

    val wifiState: Flow<WifiState> = bus.wifiState
        .onStart { emit(WifiState()) }

    /** Handlebar / swift-button press events forwarded from CarPropertyService via ClusterDataBus.
     *  replay=0 — momentary events; do not re-deliver to late subscribers. */
    val handlebarButton: SharedFlow<Int> = bus.handlebarButton
}
