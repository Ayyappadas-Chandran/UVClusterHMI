package com.ultraviolette.uvclusterhmi.ui.features.myF77.tutorial

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TutorialViewModel: ViewModel() {
    private var _selectedVideoPosition: MutableLiveData<Int> = MutableLiveData()
    val selectedVideoPosition: LiveData<Int>
        get() = _selectedVideoPosition

    fun handleSelectedVideoPosition(position: Int) {
        _selectedVideoPosition.value = position
    }
}