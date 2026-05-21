package com.ultraviolette.uvclusterhmi.ui.features.controls.trips

import androidx.lifecycle.ViewModel
import com.ultraviolette.uvclusterhmi.domain.dataModel.TripDetails
import com.ultraviolette.uvclusterhmi.domain.manager.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TripsUiState(
    val selectedTrip: Int = 1,
    val isResetMode: Boolean = false,
    val tripDetails1: TripDetails? = null,
    val tripDetails2: TripDetails? = null,
    val tripDetails3: TripDetails? = null
)

class TripsViewModel(private val preferenceManager: PreferenceManager) :
    ViewModel() {

    private val _uiState = MutableStateFlow(TripsUiState())
    val uiState: StateFlow<TripsUiState> = _uiState.asStateFlow()

    fun getTrips(): Int {
        return preferenceManager.trips
    }
    init {
        _uiState.value = _uiState.value.copy(
            tripDetails1 = preferenceManager.tripDetails1,
            tripDetails2 = preferenceManager.tripDetails2,
            tripDetails3 = preferenceManager.tripDetails3
        )
    }

    fun onTripSelected(trip: Int) {
        preferenceManager.saveTrip(trip)
        _uiState.value = _uiState.value.copy(selectedTrip = trip, isResetMode = false)
    }

    fun onResetState(isReset: Boolean) {
        _uiState.value = _uiState.value.copy(isResetMode = isReset)
    }

    fun resetTripDetails(trip: Int) {
        val tripDetails = TripDetails("---", "---", "---")
        when (trip) {
            1 -> {
                preferenceManager.saveTripDetails1(tripDetails)
                _uiState.value = _uiState.value.copy(tripDetails1 = tripDetails)
            }

            2 -> {
                preferenceManager.saveTripDetails2(tripDetails)
                _uiState.value = _uiState.value.copy(tripDetails2 = tripDetails)
            }

            3 -> {
                preferenceManager.saveTripDetails3(tripDetails)
                _uiState.value = _uiState.value.copy(tripDetails3 = tripDetails)
            }
        }
    }

   /* init {
        val tripDetails = TripDetails("971 Km", "10 Hrs 20 Mins", "60 Km/h")
        preferenceManager.saveTripDetails1(tripDetails)
        preferenceManager.saveTripDetails2(tripDetails)
        preferenceManager.saveTripDetails3(tripDetails)
        _uiState.value = _uiState.value.copy(
            tripDetails1 = tripDetails,
            tripDetails2 = tripDetails,
            tripDetails3 = tripDetails
        )
    }*/
}
