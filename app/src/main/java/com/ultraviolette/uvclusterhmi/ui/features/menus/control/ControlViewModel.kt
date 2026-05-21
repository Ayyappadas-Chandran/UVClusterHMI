package com.ultraviolette.uvclusterhmi.ui.features.menus.control

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ControlViewModel : ViewModel() {
    private val _adapterPosition = MutableStateFlow(0)
    val adapterPosition : StateFlow<Int> = _adapterPosition.asStateFlow()

    fun updatePosition(position: Int) {
        _adapterPosition.value = position
    }
}