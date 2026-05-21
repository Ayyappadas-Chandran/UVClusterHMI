package com.ultraviolette.uvclusterhmi

import androidx.lifecycle.ViewModel
import com.ultraviolette.uvclusterhmi.domain.manager.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class UiState(
    val isHapticEnabled: Boolean = false
)

class HapticViewModel(private val preferenceManager: PreferenceManager) : ViewModel() {

    private var _uiState: MutableStateFlow<UiState> =
        MutableStateFlow(UiState(preferenceManager.isHapticOn))

    val uiState: StateFlow<UiState>
        get() = _uiState

    init {

        _uiState.update {
            it.copy(preferenceManager.isHapticOn)
        }
    }

    fun saveHaptic(isEnable:Boolean){
        preferenceManager.saveHaptic(isEnable)
        _uiState.update { it.copy(isHapticEnabled = isEnable) }
    }

}
