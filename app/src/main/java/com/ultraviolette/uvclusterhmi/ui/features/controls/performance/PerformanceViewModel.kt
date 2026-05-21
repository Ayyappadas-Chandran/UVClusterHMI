package com.ultraviolette.uvclusterhmi.ui.features.controls.performance

import androidx.lifecycle.ViewModel
import com.ultraviolette.uvclusterhmi.domain.manager.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PerformanceUiState(
    val is10Levels: Boolean = true,
    val isHillHold: Boolean = false,
    val isBallisticPlus: Boolean = false,
    val regenValue: Int = 0,
    val isMonoAbs: Boolean = false,
    val isCruise: Boolean = false,
    val tractionLevel: String = "Off",
    val focusedState: FocusedState = FocusedState.BallisticPlus,
)

enum class FocusedState {
    RegenModes,
    Regen,
    HillHold,
    BallisticPlus,
    Abs,
    CruiseControl,
    TractionControl
}

class PerformanceViewModel(private val preferenceManager: PreferenceManager) :
    ViewModel() {
    private val _uiState = MutableStateFlow(PerformanceUiState())
    val uiState: StateFlow<PerformanceUiState> = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(
                is10Levels = preferenceManager.is10Levels,
                isHillHold = preferenceManager.isHillHold,
                regenValue = preferenceManager.regenValue,
                isBallisticPlus = preferenceManager.isBallisticPlus,
                isMonoAbs = preferenceManager.isMonoAbs,
                isCruise = preferenceManager.isCruise,
                tractionLevel = preferenceManager.tractionControlLevel
            )
        }
    }

    fun saveRegenValue(value: Int) {
        preferenceManager.saveRegenValue(value)
        _uiState.update { it.copy(regenValue = value) }
    }

    fun saveRegenModes(is10Levels: Boolean) {
        preferenceManager.saveRegenType(is10Levels)
        _uiState.update { it.copy(is10Levels = is10Levels) }
    }

    fun saveHillHold(isHillHold: Boolean) {
        preferenceManager.saveHillHold(isHillHold)
        _uiState.update { it.copy(isHillHold = isHillHold) }
    }

    fun setFocusedState(focusedState: FocusedState) {
        _uiState.update { it.copy(focusedState = focusedState) }
    }

    fun saveBallisticPlus(isBallisticPlus: Boolean) {
        preferenceManager.saveBallisticPlus(isBallisticPlus)
        _uiState.update { it.copy(isBallisticPlus = isBallisticPlus) }

    }

    fun saveAbs(isMono: Boolean) {
        preferenceManager.saveCustomModeAbs(isMono)
        _uiState.update { it.copy(isMonoAbs = isMono) }
    }

    fun saveCruise(isCruise: Boolean) {
        preferenceManager.saveCruise(isCruise)
        _uiState.update { it.copy(isCruise = isCruise) }
    }

    fun saveTractionLevel(tractionLevel: String) {
        preferenceManager.saveTractionControl(tractionLevel)
        _uiState.update { it.copy(tractionLevel = tractionLevel) }
    }
}



