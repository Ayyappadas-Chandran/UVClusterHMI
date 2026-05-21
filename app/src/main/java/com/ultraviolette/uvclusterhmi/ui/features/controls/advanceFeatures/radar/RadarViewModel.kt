package com.ultraviolette.uvclusterhmi.ui.features.controls.advanceFeatures.radar

import androidx.lifecycle.ViewModel
import com.ultraviolette.uvclusterhmi.domain.manager.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RadarUiState(
    val isRadarOn: Boolean = false,
    val isConsoleAlertsOn: Boolean = false,
    val isMirrorAlertsOn: Boolean = false,
    val isAudioAlertsOn: Boolean = false,
    val selectedOption: RadarSelectedOption = RadarSelectedOption.RADAR
)

enum class RadarSelectedOption {
    RADAR,
    CONSOLE_ALERTS,
    MIRROR_ALERTS,
    AUDIO_ALERTS,
}

class RadarViewModel(private val preferenceManager: PreferenceManager) :
    ViewModel() {

    private val _uiState = MutableStateFlow(RadarUiState())
    val uiState: StateFlow<RadarUiState> = _uiState.asStateFlow()

    private val navigableOptions = RadarSelectedOption.values().toList()

    fun setRadar(isOn: Boolean) {
        _uiState.update { it.copy(isRadarOn = isOn) }
    }

    fun setConsoleAlerts(isOn: Boolean) {
        preferenceManager.saveConsoleAlerts(isOn)
        _uiState.update { it.copy(isConsoleAlertsOn = isOn) }
    }

    fun setMirrorAlerts(isOn: Boolean) {
        preferenceManager.saveMirrorAlerts(isOn)
        _uiState.update { it.copy(isMirrorAlertsOn = isOn) }
    }

    fun setAudioAlerts(isOn: Boolean) {
        preferenceManager.saveAudioAlerts(isOn)
        _uiState.update { it.copy(isAudioAlertsOn = isOn) }
    }

    fun setSelectedOption(option: RadarSelectedOption) {
        _uiState.update { it.copy(selectedOption = option) }
    }

    fun toggleCurrentSelection() {
        _uiState.update { currentState ->
            when (currentState.selectedOption) {
                RadarSelectedOption.RADAR -> currentState.copy(isRadarOn = !currentState.isRadarOn)
                RadarSelectedOption.CONSOLE_ALERTS -> currentState.copy(isConsoleAlertsOn = !currentState.isConsoleAlertsOn)
                RadarSelectedOption.MIRROR_ALERTS -> currentState.copy(isMirrorAlertsOn = !currentState.isMirrorAlertsOn)
                RadarSelectedOption.AUDIO_ALERTS -> currentState.copy(isAudioAlertsOn = !currentState.isAudioAlertsOn)
            }
        }
    }

    fun selectNextOption() {
       if(!_uiState.value.isRadarOn) return
        _uiState.update { currentState ->
            val currentIndex = navigableOptions.indexOf(currentState.selectedOption)
            val nextIndex =
                if (currentIndex == -1) 0 else (currentIndex + 1) % navigableOptions.size
            currentState.copy(selectedOption = navigableOptions[nextIndex])
        }
    }

    fun selectPreviousOption() {
        if(!_uiState.value.isRadarOn) return
        _uiState.update { currentState ->
            val currentIndex = navigableOptions.indexOf(currentState.selectedOption)
            val prevIndex =
                if (currentIndex == -1) navigableOptions.size - 1 else (currentIndex - 1 + navigableOptions.size) % navigableOptions.size
            currentState.copy(selectedOption = navigableOptions[prevIndex])
        }
    }

    init {
        _uiState.update {
            it.copy(
                isAudioAlertsOn = preferenceManager.isAudioAlertsOn,
                isConsoleAlertsOn = preferenceManager.isConsoleAlertsOn,
                isMirrorAlertsOn = preferenceManager.isMirrorAlertsOn,
                isRadarOn = preferenceManager.isRadarOn
            )
        }
    }
}

