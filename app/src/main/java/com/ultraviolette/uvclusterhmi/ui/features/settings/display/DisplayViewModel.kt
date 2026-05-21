package com.ultraviolette.uvclusterhmi.ui.features.settings.display

import androidx.lifecycle.ViewModel
import com.ultraviolette.uvclusterhmi.domain.manager.PreferenceManager

class DisplayViewModel(private val preferenceManager: PreferenceManager) :
    ViewModel() {
    val brightnessLevel: Int
        get() = preferenceManager.brightnessValue
    val isAutoBrightnessEnabled: Boolean
        get() = preferenceManager.isAutoBrightnessEnabled
    val isParallaxEnabled: Boolean
        get() = preferenceManager.isParallaxEnabled
    val mode: String
        get() = preferenceManager.mode

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
}