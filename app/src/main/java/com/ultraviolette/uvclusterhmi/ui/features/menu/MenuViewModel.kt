package com.ultraviolette.uvclusterhmi.ui.features.menu

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ultraviolette.uvclusterhmi.domain.manager.PreferenceManager

class MenuViewModel(private val preferenceManager: PreferenceManager) : ViewModel() {
    var lastMenuPosition = 0
    private var _currentMenuPosition: MutableLiveData<Int> = MutableLiveData(0)
    val currentMenuPosition: LiveData<Int>
        get() = _currentMenuPosition

    val rideModePreset: String
        get() = preferenceManager.rideMode

    val hillHoldState: Boolean
        get() = preferenceManager.isHillHold

    val absState: Boolean
        get() = preferenceManager.isMonoAbs

    val regenLevel: Int
        get() = preferenceManager.regenValue

    val tractionLevel: String
        get() = preferenceManager.tractionControlLevel

    fun handleCurrentMenuPosition(position: Int) {
        _currentMenuPosition.value = position
    }

}

