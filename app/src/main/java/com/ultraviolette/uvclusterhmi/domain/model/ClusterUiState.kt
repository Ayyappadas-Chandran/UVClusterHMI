package com.ultraviolette.uvclusterhmi.domain.model

import com.ultraviolette.cluster.aidl.BtState
import com.ultraviolette.cluster.aidl.VehicleData
import com.ultraviolette.cluster.aidl.VehicleSnapshot
import com.ultraviolette.cluster.aidl.WifiState

sealed interface ClusterUiState {
    data object Loading : ClusterUiState

    data class Active(
        val vehicleData: VehicleData,
        val vehicleSnapshot: VehicleSnapshot,
        val btState: BtState,
        val wifiState: WifiState,
        val screenMode: ScreenMode
    ) : ClusterUiState

    data class Error(val message: String) : ClusterUiState
}
