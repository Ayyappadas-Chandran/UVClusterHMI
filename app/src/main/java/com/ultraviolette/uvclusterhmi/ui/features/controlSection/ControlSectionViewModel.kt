package com.ultraviolette.uvclusterhmi.ui.features.controlSection

import androidx.lifecycle.ViewModel
import com.ultraviolette.uvclusterhmi.domain.manager.PreferenceManager

class ControlSectionViewModel(private val preferenceManager: PreferenceManager) :
    ViewModel() {
    val brightnessLevel: Int
        get() = preferenceManager.brightnessValue
    val isAutoBrightnessEnabled: Boolean
        get() = preferenceManager.isAutoBrightnessEnabled
    val isParallaxEnabled: Boolean
        get() = preferenceManager.isParallaxEnabled
    val mode: String
        get() = preferenceManager.mode
    val isIncognitoEnabled: Boolean
        get() = preferenceManager.isIncognitoEnabled
    val isHillHold: Boolean
        get() = preferenceManager.isHillHold
    val isDataEnabled: Boolean
        get() = preferenceManager.isDataEnabled
    val isMonoAbs: Boolean
        get() = preferenceManager.isMonoAbs

    fun saveBrightness(value: Int) {
        preferenceManager.saveBrightness(value)
    }

    fun saveBrightnessState(state: Boolean) {
        preferenceManager.saveBrightnessState(state)
    }

    fun saveMode(theme: String) {
        preferenceManager.saveMode(theme)
    }

    fun saveTheme(isParallax: Boolean) {
        preferenceManager.saveTheme(isParallax)
    }

    fun saveIncognito(isIncognitoEnabled: Boolean) {
        preferenceManager.saveIncognito(isIncognitoEnabled)
    }

    fun saveHillHold(isHillHold: Boolean) {
        preferenceManager.saveHillHold(isHillHold)
    }

    fun saveData(isDataEnabled: Boolean) {
        preferenceManager.saveData(isDataEnabled)
    }

    fun saveCustomModeAbs(isMono: Boolean) {
        preferenceManager.saveCustomModeAbs(isMono)
    }
}