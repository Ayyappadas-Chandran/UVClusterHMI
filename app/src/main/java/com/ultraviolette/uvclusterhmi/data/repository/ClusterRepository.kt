package com.ultraviolette.uvclusterhmi.data.repository

import com.ultraviolette.cluster.aidl.BtState
import com.ultraviolette.cluster.aidl.VehicleData
import com.ultraviolette.cluster.aidl.VehicleSnapshot
import com.ultraviolette.cluster.aidl.WifiState
import com.ultraviolette.uvclusterhmi.data.datasource.BusDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart

class ClusterRepository(private val bus: BusDataSource) {

    val vehicleData: Flow<VehicleData> = bus.vehicleData
        .onStart { emit(VehicleData()) }

    val vehicleSnapshot: Flow<VehicleSnapshot> = bus.vehicleSnapshot
        .onStart { emit(VehicleSnapshot()) }

    val btState: Flow<BtState> = bus.btState
        .onStart { emit(BtState()) }

    val wifiState: Flow<WifiState> = bus.wifiState
        .onStart { emit(WifiState()) }
}
