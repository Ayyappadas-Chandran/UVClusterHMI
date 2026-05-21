package com.ultraviolette.uvclusterhmi.ui.features.menus.battery

import androidx.lifecycle.ViewModel
import com.ultraviolette.uvclusterhmi.domain.manager.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class BatteryUiState(
    val isWinterMode :Boolean= false,
    val batteryLimit:Int
)

class BatteryViewModel(private val preferenceManager: PreferenceManager): ViewModel() {

    private val _batteryUiState = MutableStateFlow(
        BatteryUiState(
            batteryLimit = preferenceManager.batteryLimit
        )
    )
    val batteryUiState: StateFlow<BatteryUiState> = _batteryUiState.asStateFlow()

    fun updateBatteryLimitChange(limit: Int) {
        preferenceManager.saveBatteryLimit(limit)
        _batteryUiState.update { it.copy(batteryLimit = limit) }
    }

    fun updateBatteryUi(isWinterMode : Boolean){
        _batteryUiState.update { it.copy(isWinterMode = isWinterMode) }
    }
}