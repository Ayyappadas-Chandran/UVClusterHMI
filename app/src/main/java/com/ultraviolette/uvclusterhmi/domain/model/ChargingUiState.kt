package com.ultraviolette.uvclusterhmi.domain.model

import androidx.compose.runtime.Immutable

/**
 * State for the charging screen.
 *
 * Two visible sub-states, gated by [keyOff]:
 *  - keyOff == true  → active charging dashboard (SoC ring, range, time-to-full)
 *  - keyOff == false → instructional "turn off key / unplug charger" screen
 *
 * The composable does not decide when this screen shows — [ScreenMode.Charging]
 * is set by [ClusterViewModel.resolveScreenMode] when the charger is plugged in,
 * and cleared when [chargerPlugged] becomes false.
 */
@Immutable
data class ChargingUiState(
    val chargerPlugged: Boolean    = false,
    val keyOff: Boolean            = false,
    val batterySoc: Int            = 0,        // 0..100
    val rangeDisplay: String       = "",       // already formatted, e.g. "150 km" / "93 miles"
    val timeToFullSeconds: Int     = 0,        // remaining seconds; <=0 means unknown / done
    val rideMode: RideMode         = RideMode.Glide,
    val isFastCharging: Boolean    = false,
)