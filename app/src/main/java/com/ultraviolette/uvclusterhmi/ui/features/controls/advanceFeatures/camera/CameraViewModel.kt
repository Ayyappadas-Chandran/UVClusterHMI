package com.ultraviolette.uvclusterhmi.ui.features.controls.advanceFeatures.camera

import androidx.lifecycle.ViewModel
import com.ultraviolette.uvclusterhmi.domain.manager.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CameraUiState(
    val isOn :Boolean = false,
    val isButton:Boolean = false
)
class CameraViewModel (private val preferenceManager: PreferenceManager): ViewModel() {
    private var _uiState = MutableStateFlow(CameraUiState())
    val uiState = _uiState.asStateFlow()

    fun saveCamera(isOn: Boolean,isButton:Boolean){
        preferenceManager.saveCamera(isOn)
        _uiState.update{
            it.copy(
                isOn = isOn,
                isButton = isButton
            )
        }
    }

    fun initCamera(isButton: Boolean){
        _uiState.update{
            it.copy(
                isOn = preferenceManager.isCameraOn,
                isButton = isButton
            )
        }
    }
}