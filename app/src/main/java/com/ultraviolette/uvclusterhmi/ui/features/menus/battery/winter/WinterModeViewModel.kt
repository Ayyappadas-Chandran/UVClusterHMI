package com.ultraviolette.uvclusterhmi.ui.features.menus.battery.winter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultraviolette.uvclusterhmi.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WinterModeUiState(
    var stepColor : Int = -1,
    var plugColor: Int = -1,
    var plugChargerColor: Int = -1,
    var chargerStatus :Boolean = false,
    var winterModelStatus: Boolean = false
)

class WinterModeViewModel : ViewModel() {
    private var _uiState = MutableStateFlow(WinterModeUiState(
        plugColor = R.color.lightGrey,
        stepColor = R.color.white,
        plugChargerColor = R.color.white,
        chargerStatus = true,
        winterModelStatus = false
    ))
    val uiState = _uiState
    private var winterModeJob: Job? = null

    fun simulateWinterMode(){
        winterModeJob?.cancel()
        winterModeJob = viewModelScope.launch {
            delay(5000)
            _uiState.update{
                it.copy(
                    plugColor = R.color.green,
                    stepColor = R.color.green,
                    plugChargerColor = R.color.green
                )
            }
            delay(5000)
            _uiState.update{
                it.copy(
                    chargerStatus = false,
                    winterModelStatus = true
                )
            }
        }
    }

    fun cancelSimulation() {
        winterModeJob?.cancel()
        _uiState.update {
            it.copy(
                plugColor = R.color.lightGrey,
                stepColor = R.color.white,
                plugChargerColor = R.color.white,
                chargerStatus = true,
                winterModelStatus = false
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        winterModeJob?.cancel()
    }
}