package com.ultraviolette.uvclusterhmi.ui.features.settings.data

import android.util.Log.d
import androidx.lifecycle.ViewModel
import com.ultraviolette.uvclusterhmi.DataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UiState(val state: Boolean, val isButton: Boolean)

class DataViewModel(private val dataRepository: DataRepository) : ViewModel() {
    private var _dataStateChange: MutableStateFlow<UiState> = MutableStateFlow(
        UiState(
            state = true,
            isButton = false
        )
    )
    val onDataStateChange: StateFlow<UiState> = _dataStateChange.asStateFlow()
    private val _currentSNetworkSignalLevel = MutableStateFlow(0)
    val currentNetworkSignalLevel: StateFlow<Int> = _currentSNetworkSignalLevel.asStateFlow()
    fun registerReceiver() {
        dataRepository.registerReceiver()
    }

    fun unregisterReceiver() {
        dataRepository.unregisterReceiver()
    }

    fun stateChange(isButton: Boolean = false) {
        d("VM", "stateChange called isButton=$isButton")
     //   _dataStateChange.value = UiState(dataRepository.isDataEnable(), isButton)

    }

    fun setDataState(isEnabled: Boolean) {
        dataRepository.setDataState(isEnabled)
    }

    fun getNetworkSignalLevel(){
	d("LTESignal","getNetworkSignalLevel_VM")
        dataRepository.startListening { level ->
            _currentSNetworkSignalLevel.value = level
        }
    }
    fun stopSignalUpdates() {
        d("LTESignal","stopSignalUpdates_VM")
        dataRepository.stopListening()
    }
}
