package com.ultraviolette.uvclusterhmi.ui.features.menus.myF77

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MyF77ViewModel: ViewModel() {
    private val _adapterPosition = MutableStateFlow(0)
    val adapterPosition: StateFlow<Int> = _adapterPosition as StateFlow<Int>

    fun updateAdapterPosition(position: Int) {
        _adapterPosition.value = position
    }
}