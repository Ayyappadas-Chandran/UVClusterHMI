package com.ultraviolette.uvclusterhmi.ui.viewModel

import android.os.Build
import android.util.Log.d
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultraviolette.uvclusterhmi.domain.dataModel.ChildItem
import com.ultraviolette.uvclusterhmi.domain.dataModel.RangeLimit
import com.ultraviolette.uvclusterhmi.domain.manager.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel to manage and expose child item click events in the Settings menu.
 */
class SharedViewModel(private val preferenceManager: PreferenceManager) : ViewModel() {
    var hasThemeConfigChanged = false
    var hasGeneralFragmentDestroyed = false
    val mode: String
        get() = preferenceManager.mode
    val batteryLimit: Int
        get() = preferenceManager.batteryLimit
    var speedLimit: RangeLimit = RangeLimit(0, 999)
    var odoLimit: RangeLimit = RangeLimit(0, 999999)
    var rangeLimit: RangeLimit = RangeLimit(0, 999)
    var whPerKmLimit: RangeLimit = RangeLimit(0, 999)
    var rideLimit: RangeLimit = RangeLimit(0, 999)
    var socLimit: RangeLimit = RangeLimit(0, 100)
    var peakMotTemp = 0f
    var peakHeatSinkTemp = 0f
    val isAutoBrightnessEnabled: Boolean
        get() = preferenceManager.isAutoBrightnessEnabled
    val regenValue: Int
        get() = preferenceManager.regenValue
    val isOtaComplete: Boolean
        get() = preferenceManager.isOtaComplete

    val isRadarOn: Boolean
        get() = preferenceManager.isRadarOn
    val isConsoleAlertsOn: Boolean
        get() = preferenceManager.isConsoleAlertsOn
    // SharedViewModel.kt
    private val _isHillHold = MutableStateFlow<Boolean?>(null) // null = not yet loaded
    val isHillHold: StateFlow<Boolean?> = _isHillHold

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _isHillHold.value = preferenceManager.isHillHold
        }
    }

    fun setHillHold(value: Boolean) {
        _isHillHold.value = value
    }
    private var _afChildClick: MutableLiveData<Boolean> = MutableLiveData()
    val afChildClick: LiveData<Boolean>
        get() = _afChildClick

    private var _settingsChildClick: MutableLiveData<Boolean> = MutableLiveData()
    val settingsChildClick: LiveData<Boolean>
        get() = _settingsChildClick

    private var _rvChildClick: MutableLiveData<Pair<ChildItem?, String>> =
        MutableLiveData()
    val rvChildClick: LiveData<Pair<ChildItem?, String>>
        get() = _rvChildClick

    private var _settingsBlurStatus: MutableLiveData<Boolean> = MutableLiveData()
    val settingsBlurStatus: LiveData<Boolean>
        get() = _settingsBlurStatus
    val distanceUnit: String
        get() = preferenceManager.distanceUnit
     val rideMode :String
        get() = preferenceManager.rideMode
    var isParkAssistEntry = false
     val normalTimeFormat: Boolean
        get() = preferenceManager.isNormalTimeFormat


    private var _radarOn: MutableStateFlow<Boolean> =
        MutableStateFlow(preferenceManager.isRadarOn)
    val radarOn: StateFlow<Boolean> = _radarOn
    
    val vinTextValue: String
        get() = preferenceManager.vinNumber

    fun saveVimNumber(vimNumber: String){
        preferenceManager.saveVinNumber(vimNumber)
    }

    val imeiNumber: String
        get() = preferenceManager.imeiNumber
    fun saveIemiNumber(iemiNumber: String){
        preferenceManager.saveIemiNumber(iemiNumber)
    }

    fun saveUpdate(shouldUpdate: Boolean) {
        preferenceManager.saveUpdate(shouldUpdate)
    }

    fun saveRegenValue(value: Int) {
        preferenceManager.saveRegenValue(value)
    }

    fun saveLanguage(language: String) {
        preferenceManager.saveLanguage(language)
    }

    fun saveMode(mode: String) {
        preferenceManager.saveMode(mode)
    }

    fun saveBrightness(brightnessLevel: Int) {
        preferenceManager.saveBrightness(brightnessLevel)
    }

    fun saveBrightnessState(isAuto: Boolean) {
        preferenceManager.saveBrightnessState(isAuto)
    }

    fun saveHillHold(isHillHold: Boolean) {
        preferenceManager.saveHillHold(isHillHold)
    }

    fun saveCustomModeAbs(isMono: Boolean) {
        preferenceManager.saveCustomModeAbs(isMono)
    }

    fun otaCompleted(isFinish: Boolean) {
        if (isFinish) {
            preferenceManager.saveOtaCompleted(true)
            preferenceManager.saveOldBuild(Build.FINGERPRINT)
        }
    }

    fun handleSettingsBlur(shouldBlur: Boolean) {
        _settingsBlurStatus.value = shouldBlur
    }

    fun handleAfChildClick(isClicked: Boolean) {
        _afChildClick.value = isClicked
    }

    fun handleSettingsChildClick(isClicked: Boolean) {
        _settingsChildClick.value = isClicked
    }

    fun handleRvChildClick(clickedItem: ChildItem? = null, type: String = "") {
        _rvChildClick.value = Pair(clickedItem, type)
    }

    fun saveRadar(isRadarOn: Boolean) {
        _radarOn.value = isRadarOn
        preferenceManager.saveRadar(isRadarOn)
    }
    fun saveBallisticPlus(isBallisticPlus: Boolean) {
        preferenceManager.saveBallisticPlus(isBallisticPlus)
    }

    fun resetThemeChangeFlag() {
        viewModelScope.launch {
            // Give VHAL 1 second to finish re-sending old cached data
            delay(3000)
            hasThemeConfigChanged = false
            d("ThemeChange", "Flag reset to false. Alerts are now active.")
        }
    }
}



