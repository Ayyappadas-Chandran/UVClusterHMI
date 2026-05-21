package com.ultraviolette.uvclusterhmi.ui.features.dashboard

import androidx.lifecycle.ViewModel
import com.ultraviolette.uvclusterhmi.domain.manager.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class UiState(
    val efficiency: Int = 0,
    val regenValue: Int,
    val regenType: Boolean,
    val power: Float = 0.0f,
    val themeMode: Int,
    val isMotorArmed: Boolean,
    val isBallisticPlus: Boolean,
    val isRegenUnAvailable:Boolean
)

class DashboardViewModel(private val preferenceManager: PreferenceManager) : ViewModel() {
    private var _uiState = MutableStateFlow(
        UiState(
            regenValue = preferenceManager.regenValue,
            themeMode = preferenceManager.themeMode,
            regenType = preferenceManager.is10Levels,
            isMotorArmed = false,
            isBallisticPlus = preferenceManager.isBallisticPlus,
            isRegenUnAvailable=false
        )
    )
    val uiState = _uiState.asStateFlow()
    fun setThemeMode(themeMode: Int) {
        preferenceManager.saveThemeMode(themeMode)
        _uiState.update { it.copy(themeMode = themeMode) }
    }

    fun setRegenValue(regenValue: Int) {
        preferenceManager.saveRegenValue(regenValue)
        _uiState.update { it.copy(regenValue = regenValue) }
    }

    fun setEfficiencyValue(efficiency: Int) {
        _uiState.update { it.copy(efficiency) }
    }

    fun getRegenValue() {
        _uiState.update { it.copy(regenValue = preferenceManager.regenValue) }
    }

    fun setMotorArmed(armed: Boolean) {
        _uiState.update { it.copy(isMotorArmed = armed) }
    }
    fun setRegenUnAvailable(isRegenUnAvailable: Boolean) {
        _uiState.update { it.copy(isRegenUnAvailable = isRegenUnAvailable) }
    }

    fun setRegenType(is10Level: Boolean) {
        preferenceManager.saveRegenType(is10Levels)
        _uiState.update { it.copy(regenType = is10Level) }
    }

    val is10Levels: Boolean
        get() = preferenceManager.is10Levels
    val isSurgeMode: Boolean
        get() = preferenceManager.isBallisticPlus

    fun ballisticPlus(isBallisticPlus: Boolean) {
         preferenceManager.saveBallisticPlus(isBallisticPlus)
        _uiState.update { it.copy(isBallisticPlus = isBallisticPlus) }
    }
    fun setPowerValue(power: Float) {
        _uiState.update { it.copy(power = power) }
    }
}




