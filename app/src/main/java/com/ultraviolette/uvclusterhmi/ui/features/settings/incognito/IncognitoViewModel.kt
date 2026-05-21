package com.ultraviolette.uvclusterhmi.ui.features.settings.incognito

import androidx.lifecycle.ViewModel
import com.ultraviolette.uvclusterhmi.domain.manager.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class IncognitoUiState(
    val isEnabled: Boolean,
    val isButton:Boolean
)
class IncognitoViewModel(private val preferenceManager: PreferenceManager) : ViewModel(){
    private var _isIncognitoEnabled = MutableStateFlow(IncognitoUiState(preferenceManager.isIncognitoEnabled,false))
    val isIncognitoEnabled = _isIncognitoEnabled.asStateFlow()


    fun saveIncognito(isEnabled: Boolean,isButton:Boolean){
        preferenceManager.saveIncognito(isEnabled)
        _isIncognitoEnabled.value = IncognitoUiState(isEnabled,isButton)
    }

}