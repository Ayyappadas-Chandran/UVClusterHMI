package com.ultraviolette.uvclusterhmi.ui.viewModel

import android.view.KeyEvent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ultraviolette.cluster.aidl.BtState
import com.ultraviolette.cluster.aidl.VehicleData
import com.ultraviolette.cluster.aidl.VehicleSnapshot
import com.ultraviolette.uvclusterhmi.ClusterApplication
import com.ultraviolette.uvclusterhmi.data.repository.ClusterRepository
import com.ultraviolette.uvclusterhmi.domain.model.ClusterUiState
import com.ultraviolette.uvclusterhmi.domain.model.ScreenMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ClusterViewModel(private val repository: ClusterRepository) : ViewModel() {

    private val _menuVisible = MutableStateFlow(false)

    val uiState: StateFlow<ClusterUiState> = combine(
        repository.vehicleData,
        repository.vehicleSnapshot,
        repository.btState,
        repository.wifiState,
        _menuVisible
    ) { vehicleData, vehicleSnapshot, btState, wifiState, menuVisible ->
        ClusterUiState.Active(
            vehicleData = vehicleData,
            vehicleSnapshot = vehicleSnapshot,
            btState = btState,
            wifiState = wifiState,
            screenMode = resolveScreenMode(vehicleData, vehicleSnapshot, btState, menuVisible)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ClusterUiState.Loading
    )

    // TODO: VehicleData needs a sideStandDeployed field (VCU bit 29, STAT_VCU_SIDE_STAND_DEPLOYED).
    //       hillHoldState > 0 is a proxy until VehicleData is updated.
    // TODO: TPMS signal not yet in VehicleData; criticalMalfunction used as proxy.
    // TODO: RearCamera / NavigationActive signals not yet wired — stubs return false.
    private fun resolveScreenMode(
        vehicleData: VehicleData,
        vehicleSnapshot: VehicleSnapshot,
        btState: BtState,
        menuVisible: Boolean
    ): ScreenMode {
        if (vehicleData.hillHoldState > 0 && vehicleData.rideModeRaw != 0)
            return ScreenMode.SideStandAlert

        if (vehicleSnapshot.criticalMalfunction > 0)
            return ScreenMode.TpmsAlert

        if (btState.callState == 1)
            return ScreenMode.IncomingCall

        val rearCameraActive = false
        if (vehicleData.speed < 15f && rearCameraActive)
            return ScreenMode.RearCamera

        val navActive = false
        if (navActive)
            return ScreenMode.NavigationActive

        if (menuVisible) return ScreenMode.Menu

        return ScreenMode.Riding
    }

    fun handleHandlebarButton(keyCode: Int): Boolean = when (keyCode) {
        KeyEvent.KEYCODE_MENU -> { _menuVisible.value = !_menuVisible.value; true }
        KeyEvent.KEYCODE_BACK -> {
            if (_menuVisible.value) { _menuVisible.value = false; true } else false
        }
        else -> false
    }

    class Factory(app: ClusterApplication) : ViewModelProvider.Factory {
        private val repository = app.clusterRepository

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ClusterViewModel(repository) as T
    }
}
