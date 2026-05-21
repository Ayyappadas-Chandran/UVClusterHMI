package com.ultraviolette.uvclusterhmi.domain.dataModel

/**
 * Represents a time zone entry with its corresponding time.
 *
 * @property timeZone The name or identifier of the time zone (e.g., "Asia").
 * @property time The current time in that time zone, formatted as a string.
 */
data class TimeZoneItem(
    val timeZone: String,
    val time: String
)
