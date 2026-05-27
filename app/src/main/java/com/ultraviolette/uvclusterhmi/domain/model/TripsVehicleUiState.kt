package com.ultraviolette.uvclusterhmi.domain.model

import androidx.compose.runtime.Stable

/** Display values for a single trip entry (distance, duration, avg speed). */
@Stable
data class TripEntryUiState(
    /** Formatted distance string e.g. "123 km" or "76 miles". "---" when no data. */
    val distanceDisplay: String = "---",
    /** Formatted duration string e.g. "1 Hrs 23 Mins". "---" when no data. */
    val durationDisplay: String = "---",
    /** Formatted average-speed string e.g. "45 km/h" or "28 mph". "---" when no data. */
    val avgSpeedDisplay: String = "---",
)

/**
 * Live trip-meter data derived from VehicleSnapshot Trip 1/2/3.
 *
 * All display values are already unit-converted and formatted by [ClusterViewModel].
 * Fragments and composables read only this — they never touch VehicleSnapshot or
 * SharedPreferences for trip data.
 */
@Stable
data class TripsVehicleUiState(
    val trip1: TripEntryUiState = TripEntryUiState(),
    val trip2: TripEntryUiState = TripEntryUiState(),
    val trip3: TripEntryUiState = TripEntryUiState(),
    /** "km" or "miles" — same unit as [OdometerUiState.unit]. */
    val unit: String = "km",
)
