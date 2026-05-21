package com.ultraviolette.uvclusterhmi.ui.features.myF77.document

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DocumentMenuViewModel: ViewModel() {
    private val _adapterPosition = MutableStateFlow(0)
    val adapterPosition: StateFlow<Int>
        get() = _adapterPosition as StateFlow<Int>

    fun updateAdapterPosition(position : Int){
        _adapterPosition.value = position
    }
}