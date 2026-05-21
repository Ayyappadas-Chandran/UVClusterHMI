package com.ultraviolette.uvclusterhmi.domain.dataModel

/**
 * Represents the details of a trip including distance, duration, and average speed.
 *
 * @property distance The total distance covered during the trip, typically in kilometers or miles.
 * @property duration The total duration of the trip, typically in the format HH:MM:SS.
 * @property averageSpeed The average speed maintained during the trip, usually in km/h or mph.
 */
data class TripDetails (
    val distance : String,
    val duration: String,
    val averageSpeed: String
)