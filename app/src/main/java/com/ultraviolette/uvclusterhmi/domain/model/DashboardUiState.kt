package com.ultraviolette.uvclusterhmi.domain.model

import androidx.compose.runtime.Stable

// ─────────────────────────────────────────────────────────────────────────────
// All sub-states are @Stable data classes so Compose can skip recomposition
// when individual fields are unchanged.  Composables receive only these
// primitives / data classes / lambdas — never raw AIDL objects.
// ─────────────────────────────────────────────────────────────────────────────

/** Motor arm state and derived display values. */
@Stable
data class MotorUiState(
    /** True when the VCU reports the motor is armed. */
    val isArmed: Boolean = false,
    /** Show the motor-armed icon: only when armed AND speed == 0. */
    val showArmedIcon: Boolean = false,
    /** Display string for the speedometer: "---" unarmed+0, "000" armed+0, else "%03d". */
    val speedDisplay: String = "---",
    /** Raw km/h value used for gesture gating (swipe-to-menu blocked above 0). */
    val speedKmh: Int = 0,
    /** Raw motor power from PROP_ID_VEHICLE_VALUE[1], in watts (negative = regen). */
    val motorPower: Float = 0f,
    /** Normalized power-bar progress 0f–1f: abs(motorPower / 1000) / 10 clamped to 1. */
    val powerBarProgress: Float = 0f,
)

/** Per-side radar indicator state derived from VehicleSnapshot.radarIndicator. */
@Stable
data class RadarUiState(
    val leftState: RadarDisplayState  = RadarDisplayState.Off,
    val rightState: RadarDisplayState = RadarDisplayState.Off,
    /** Rear Collision Warning — both sides alert simultaneously. */
    val rcwActive: Boolean = false,
)

/** Ride-mode and mode-modifier flags. */
@Stable
data class DriveUiState(
    val rideMode: RideMode      = RideMode.Glide,
    /** Hover mode: power-bar widgets hidden. */
    val isHoverMode: Boolean    = false,
    /** SurgeMode (not yet available from VCU — always false). */
    val isSurgeMode: Boolean    = false,
    /** BallisticPlus / SurgeMode badge (not yet available — always false). */
    val isBallisticPlus: Boolean = false,
)

/** Regen level display, including the 4-level vs 10-level variant. */
@Stable
data class RegenUiState(
    /** Raw 0–9 level from VehicleSnapshot. */
    val level: Int          = 0,
    /** True when VCU reports regen unavailable. */
    val isUnavailable: Boolean = false,
    /**
     * Effective level for rendering: 0 when unavailable, else [level].
     * In 4-level mode, maps as: 0-2→0, 3-5→3, 6-8→6, 9→9.
     */
    val displayLevel: Int   = 0,
    /** Whether 10-level (true) or 4-level (false) regen display is active. */
    val is10Levels: Boolean = true,
)

/** Odometer, range, trip and efficiency readouts — display-ready, unit-converted. */
@Stable
data class OdometerUiState(
    /** Odometer in the user's preferred unit (km or miles). */
    val odoDisplay: Int    = 0,
    /** Range in the user's preferred unit. */
    val rangeDisplay: Int  = 0,
    /** Wh per unit (Wh/km or Wh/mile) for display. */
    val whPerUnitDisplay: Int = 0,
    /** Efficiency icon level 0–9 (0 = worst, 9 = best). */
    val efficiencyLevel: Int  = 0,
    /** "km" or "miles" — shown as the unit suffix. */
    val unit: String       = "km",
    /** Trip odometer in the user's preferred unit. */
    val tripDisplay: Int   = 0,
)

/**
 * Complete dashboard UI state consumed by [DashboardScreen].
 *
 * Derived once per VehicleSnapshot update inside [ClusterViewModel] —
 * composables never parse raw AIDL objects.
 */
@Stable
data class DashboardUiState(
    val motor: MotorUiState       = MotorUiState(),
    val radar: RadarUiState       = RadarUiState(),
    val drive: DriveUiState       = DriveUiState(),
    val regen: RegenUiState       = RegenUiState(),
    val odo: OdometerUiState      = OdometerUiState(),
    /** Radar / console overlay alerts enabled (user pref: isConsoleAlertsOn). */
    val alertsEnabled: Boolean    = false,
    /** Vehicle lean/roll angle in degrees — feeds AngleGaugeView. */
    val rollAngle: Float          = 0f,
)
