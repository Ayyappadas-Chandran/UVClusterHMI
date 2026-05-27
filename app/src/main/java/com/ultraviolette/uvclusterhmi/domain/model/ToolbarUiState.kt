package com.ultraviolette.uvclusterhmi.domain.model

import androidx.compose.runtime.Stable
import com.ultraviolette.uvclusterhmi.ui.viewModel.ClusterViewModel

/**
 * State for the persistent toolbar (top bar in activity_main).
 *
 * All data is derived in [ClusterViewModel] from [VehicleSnapshot],
 * [BtState], [WifiState], and the cellular signal level observed
 * by [MainActivity] from the system [TelephonyManager].
 */
@Stable
data class ToolbarUiState(
    // ── Time ─────────────────────────────────────────────────────────────────
    /** Formatted current time, e.g. "10:30 AM" or "22:30". */
    val formattedTime: String = "",

    // ── Battery ───────────────────────────────────────────────────────────────
    /** Battery SOC 0–100 %. */
    val batterySoc: Int = 0,

    // ── Connectivity ──────────────────────────────────────────────────────────
    val btEnabled: Boolean = false,
    val wifiEnabled: Boolean = false,
    /** 0–5 signal bars; 0 = no signal or disabled. */
    val wifiSignalLevel: Int = 0,
    /** 0–4 cellular signal bars (from TelephonyManager, not VCU). */
    val cellularSignalLevel: Int = 0,

    // ── Indicators ────────────────────────────────────────────────────────────
    val leftIndicatorOn: Boolean = false,
    val rightIndicatorOn: Boolean = false,
    val hazardActive: Boolean = false,

    // ── Regen (toolbar display) ───────────────────────────────────────────────
    /** Effective regen level (0 when unavailable). */
    val regenLevel: Int = 0,
    val regenUnavailable: Boolean = false,

    // ── Ride mode ─────────────────────────────────────────────────────────────
    val rideMode: RideMode = RideMode.Glide,
    /** True when the active destination is DashboardScreen. */
    val isDashboard: Boolean = true,

    // ── Telltales ─────────────────────────────────────────────────────────────
    val highBeam: Boolean = false,

    /**
     * Hill Hold icon state:
     *   0 = hidden, 1 = malfunction, 2 = on, 3 = active, 4 = active-variant
     */
    val hillHoldState: Int = 0,
    /** User has enabled Hill Hold in settings. */
    val hillHoldEnabled: Boolean = false,

    /**
     * ABS mode: 1 = mono ABS active.
     * Warning lamp: 0 = ok, 1 = malfunction, 2 = malfunction + blink.
     */
    val absMode: Int = 0,
    val absWarning: Int = 0,

    /**
     * MTC (traction control) state:
     *   0 = show mode icon, 1 = blink (intervening), 4 = malfunction,
     *   2/3/5 = active variants.
     * MTC mode: 1 = malfunction, 2/3/4 = levels.
     */
    val mtcState: Int = 0,
    val mtcMode: Int = 0,

    val motorArmed: Boolean = false,
    /**
     * Motor temp icon: 0 = ok, 2 = orange warning, 3 = red critical.
     */
    val motorTempIcon: Int = 0,

    val batteryError: Boolean = false,
    val batteryOverTemp: Boolean = false,
    val criticalMalfunction: Boolean = false,

    /**
     * MIL (malfunction indicator lamp):
     *   milIcon 1 = show.  milState 1 = international, 2 = domestic.
     */
    val milState: Int = 0,
    val milIcon: Int = 0,

    val otaPending: Boolean = false,

    /**
     * Radar toolbar icon:
     *   1 = off/invisible, 2 = active, 3 = malfunction.
     */
    val radarState: Int = 0,

    // ── Cruise control (not yet available from VCU, all false for now) ────────
    val cruiseStandby: Boolean = false,
    val cruiseActive: Boolean = false,
    val cruiseError: Boolean = false,
    val cruiseOff: Boolean = true,

    // ── Hover mode flag (drives DashboardScreen layout) ───────────────────────
    val isHoverMode: Boolean = false,
    val indicatorMode: ClusterViewModel.IndicatorMode = ClusterViewModel.IndicatorMode.Off,

    /**
     * True when a charger is physically plugged in.
     * Derived from TellTales.charger (value 1 or 2 = plugged-in states).
     * Used by screens that need to know charger presence without a full ScreenMode switch.
     */
    val chargerPlugged: Boolean = false,

    /**
     * Raw VCU charger value: 0 = off, 1 = slow charge, 2 = fast charge.
     * Used by MainActivity.navigateChargingScreen() to distinguish slow vs. fast.
     */
    val chargerState: Int = 0,
)