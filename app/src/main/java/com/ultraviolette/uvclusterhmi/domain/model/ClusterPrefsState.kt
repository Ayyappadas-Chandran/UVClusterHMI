package com.ultraviolette.uvclusterhmi.domain.model

import androidx.compose.runtime.Stable

/**
 * User-preference values needed by composable screens.
 *
 * Sourced from [PreferenceManager] inside [ClusterViewModel] —
 * composables never access SharedPreferences directly.
 */
@Stable
data class ClusterPrefsState(
    /** "day" / "night" / "Auto" — from PreferenceManager.mode. */
    val mode: String          = "Auto",
    /** "km" or "miles" — controls odometer / range / speed unit conversion. */
    val distanceUnit: String  = "km",
    /** True = 10-level regen display; false = 4-level (0/3/6/9). */
    val is10LevelsRegen: Boolean = true,
    /** Whether radar / console overlay alerts are shown. */
    val consoleAlertsOn: Boolean = false,
    /** Whether the radar feature is enabled in settings. */
    val radarOn: Boolean      = false,
    /** True = 12-hour clock; false = 24-hour clock. */
    val is12HourFormat: Boolean = true,
    /** Whether Hill Hold is enabled (user toggle). */
    val hillHoldEnabled: Boolean = false,
)