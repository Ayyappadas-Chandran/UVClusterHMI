package com.ultraviolette.uvclusterhmi.ui.features.controls.violette

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultraviolette.uvclusterhmi.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VioletteUiState(
    val stateText: Int,
    val stateTextColor: Int,
    val violetteIconRes: Int,
    val isVioletteDetailVisible: Boolean,
    val isVioletteEnableLayoutVisible: Boolean,
    val isBgVioletteEnableVisible: Boolean,
    val isVioletteOnVisible: Boolean,
    val isOnVisible: Boolean,
    val isBgVioletteDisableVisible: Boolean,
    val isVioletteOffVisible: Boolean,
    val isOffVisible: Boolean
)

class VioletteViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        VioletteUiState(
            stateText = R.string.violette_is_off,
            stateTextColor = R.color.unSelected,
            violetteIconRes = R.drawable.ic_violette,
            isVioletteDetailVisible = true,
            isVioletteEnableLayoutVisible = false,
            isBgVioletteEnableVisible = false,
            isVioletteOnVisible = false,
            isOnVisible = false,
            isBgVioletteDisableVisible = false,
            isVioletteOffVisible = false,
            isOffVisible = false
        )
    )
    val uiState: StateFlow<VioletteUiState> = _uiState.asStateFlow()

    private var violetteJob: Job? = null

    fun simulateViolette() {
        violetteJob?.cancel()
        violetteJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                stateText = R.string.synced_5_mins_ago,
                stateTextColor = R.color.unSelectedTitle,
                violetteIconRes = R.drawable.ic_violette_on,
                isVioletteDetailVisible = false,
                isVioletteEnableLayoutVisible = true,
            )
            delay(5000)
            _uiState.value = _uiState.value.copy(
                stateText = R.string.violette_is_on,
                stateTextColor = R.color.unSelectedTitle,
                violetteIconRes = R.drawable.ic_violette_on,
                isVioletteEnableLayoutVisible = false,
                isBgVioletteEnableVisible = true,
                isVioletteOnVisible = true,
                isOnVisible = true,
            )
            delay(5000)
            _uiState.value = _uiState.value.copy(
                stateText = R.string.violette_is_off,
                stateTextColor = R.color.unSelected,
                violetteIconRes = R.drawable.ic_violette,
                isBgVioletteEnableVisible = false,
                isVioletteOnVisible = false,
                isOnVisible = false,
                isBgVioletteDisableVisible = true,
                isVioletteOffVisible = true,
                isOffVisible = true,
            )
        }
    }

    fun stopSimulation() {
        violetteJob?.cancel()
        _uiState.value = VioletteUiState(
            stateText = R.string.violette_is_off,
            stateTextColor = R.color.unSelected,
            violetteIconRes = R.drawable.ic_violette,
            isVioletteDetailVisible = true,
            isVioletteEnableLayoutVisible = false,
            isBgVioletteEnableVisible = false,
            isVioletteOnVisible = false,
            isOnVisible = false,
            isBgVioletteDisableVisible = false,
            isVioletteOffVisible = false,
            isOffVisible = false
        )
    }

    override fun onCleared() {
        super.onCleared()
        violetteJob?.cancel()
    }
}