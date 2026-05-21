package com.ultraviolette.uvclusterhmi.ui.features.controls.rideModes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultraviolette.uvclusterhmi.domain.manager.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RideMode {
    Rain, Street, Sport, Custom
}

enum class CustomModeSubScreen {
    ABS, TractionControl, RideModes,
}

data class RideModesUiState(
    val selectedRideMode: RideMode = RideMode.Rain,
    val customModeAbsIsMono: Boolean = false,
    val tractionControlLevel: String = "Off",
    val customModeSubScreen: CustomModeSubScreen = CustomModeSubScreen.RideModes
)

class RideModesViewModel(private val preferencesManager: PreferenceManager) :
    ViewModel() {

    private val _uiState = MutableStateFlow(RideModesUiState())
    val uiState: StateFlow<RideModesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            /*val rideMode = when (preferencesManager.rideMode) {
                "Rain" -> RideMode.Rain
                "Street" -> RideMode.Street
                "Sport" -> RideMode.Sport
                "Custom" -> RideMode.Custom
                else -> RideMode.Rain
            }*/

            val rideMode = runCatching{
                enumValueOf<RideMode>(preferencesManager.rideMode)
            }.getOrDefault( RideMode.Rain)

            _uiState.update {
                it.copy(
                    selectedRideMode = rideMode,
                    customModeAbsIsMono = preferencesManager.isMonoAbs,
                    tractionControlLevel = preferencesManager.tractionControlLevel
                )
            }
        }
    }

    fun onRideModeSelected(rideMode: RideMode) {
        viewModelScope.launch {
            /*val rideModeStr = when (rideMode) {
                RideMode.Rain -> "Rain"
                RideMode.Street -> "Street"
                RideMode.Sport -> "Sport"
                RideMode.Custom -> "Custom"
            }*/
            preferencesManager.saveRideMode(rideMode.name)
            _uiState.update { it.copy(selectedRideMode = rideMode, customModeSubScreen = CustomModeSubScreen.RideModes) }
        }
    }

    fun onCustomModeAbsSelected(isMono: Boolean) {
        viewModelScope.launch {
           // preferencesManager.saveCustomModeAbs(isMono)
            _uiState.update { it.copy(customModeAbsIsMono = isMono) }
        }
    }

    fun onTractionControlSelected(level: String) {
        viewModelScope.launch {
            preferencesManager.saveTractionControl(level)
            _uiState.update { it.copy(tractionControlLevel = level) }
        }
    }

    fun onNextMode() {
        if (_uiState.value.selectedRideMode == RideMode.Custom) {
            when (_uiState.value.customModeSubScreen) {
                CustomModeSubScreen.RideModes -> {
                    val nextMode = when (_uiState.value.selectedRideMode) {
                        RideMode.Rain -> RideMode.Street
                        RideMode.Street -> RideMode.Sport
                        RideMode.Sport -> RideMode.Custom
                        RideMode.Custom -> RideMode.Rain
                    }
                    onRideModeSelected(nextMode)
                }
                CustomModeSubScreen.ABS -> {
                    onCustomModeAbsToggle()
                }
                CustomModeSubScreen.TractionControl -> {
                    onNextTractionControl()
                }
            }
        } else {
            val currentMode = _uiState.value.selectedRideMode
            val nextMode = when (currentMode) {
                RideMode.Rain -> RideMode.Street
                RideMode.Street -> RideMode.Sport
                RideMode.Sport -> RideMode.Custom
                RideMode.Custom -> RideMode.Rain
            }
            onRideModeSelected(nextMode)
        }
    }

    fun onPreviousMode() {
        if (_uiState.value.selectedRideMode == RideMode.Custom) {
            when (_uiState.value.customModeSubScreen) {
                CustomModeSubScreen.RideModes -> {
                     onRideModeSelected(RideMode.Sport)
                }
                CustomModeSubScreen.ABS -> {
                    onCustomModeAbsToggle()
                }
                CustomModeSubScreen.TractionControl -> {
                    onPreviousTractionControl()
                }
            }
        } else {
            val currentMode = _uiState.value.selectedRideMode
            val prevMode = when (currentMode) {
                RideMode.Rain -> return
                RideMode.Street -> RideMode.Rain
                RideMode.Sport -> RideMode.Street
                RideMode.Custom -> RideMode.Sport
            }
            onRideModeSelected(prevMode)
        }
    }

    fun onCustomSubScreenChanged(subScreen: CustomModeSubScreen) {
        _uiState.update { it.copy(customModeSubScreen = subScreen) }
    }

    private fun onNextTractionControl() {
        val currentLevel = _uiState.value.tractionControlLevel
        val nextLevel = when (currentLevel.lowercase()) {
            "off" -> "TC1"
            "tc1" -> "TC2"
            "tc2" -> "TC3"
            "tc3" -> "Off"
            else -> "Off"
        }
        onTractionControlSelected(nextLevel)
    }

    private fun onPreviousTractionControl() {
        val currentLevel = _uiState.value.tractionControlLevel
        val prevLevel = when (currentLevel.lowercase()) {
            "off" -> "TC3"
            "tc1" -> "Off"
            "tc2" -> "TC1"
            "tc3" -> "TC2"
            else -> "Off"
        }
        onTractionControlSelected(prevLevel)
    }

    private fun onCustomModeAbsToggle() {
        onCustomModeAbsSelected(!_uiState.value.customModeAbsIsMono)
    }

    fun onCustomSubScreenCycleUp() {
        if (_uiState.value.selectedRideMode != RideMode.Custom) return
        val current = _uiState.value.customModeSubScreen
        val next = when (current) {
            CustomModeSubScreen.RideModes -> CustomModeSubScreen.ABS
            CustomModeSubScreen.ABS -> CustomModeSubScreen.TractionControl
            CustomModeSubScreen.TractionControl -> CustomModeSubScreen.RideModes
        }
        onCustomSubScreenChanged(next)
    }

    fun onCustomSubScreenCycleDown() {
        if (_uiState.value.selectedRideMode != RideMode.Custom) return
        val current = _uiState.value.customModeSubScreen
        val next = when (current) {
            CustomModeSubScreen.RideModes -> CustomModeSubScreen.TractionControl
            CustomModeSubScreen.ABS -> CustomModeSubScreen.RideModes
            CustomModeSubScreen.TractionControl -> CustomModeSubScreen.ABS
        }
        onCustomSubScreenChanged(next)
    }
     fun saveCustomAbs(isMono:Boolean){
        preferencesManager.saveCustomModeAbs(isMono)
    }
}

