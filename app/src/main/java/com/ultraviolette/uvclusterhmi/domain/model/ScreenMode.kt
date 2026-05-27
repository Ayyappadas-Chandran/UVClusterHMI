package com.ultraviolette.uvclusterhmi.domain.model

sealed interface ScreenMode {
    /** Default riding dashboard. */
    data object Riding : ScreenMode

    // ── Safety-critical overlays (highest priority) ───────────────────────────
    /** Battery thermal runaway — highest priority: covers everything. */
    data object ThermalRunaway : ScreenMode
    /**
     * Lockdown mode.
     * @param isEnteringLockdown true = "park the bike" (0x5f), false = "vehicle locked" (0x01).
     */
    data class Lockdown(val isEnteringLockdown: Boolean) : ScreenMode

    // ── Vehicle-state overlays ─────────────────────────────────────────────────
    /** Battery critically low — full screen, shows speed + odo + "HOVER MODE". */
    data object HoverMode : ScreenMode
    data object SideStandAlert : ScreenMode
    data object TpmsAlert : ScreenMode

    // ── Connectivity overlays ─────────────────────────────────────────────────
    /** Bluetooth HFP ringing — shows caller name, informational only. */
    data class IncomingCall(val callerName: String) : ScreenMode

    // ── External systems ──────────────────────────────────────────────────────
    data object RearCamera : ScreenMode
    data object NavigationActive : ScreenMode

    // ── Charging ──────────────────────────────────────────────────────────────
    /**
     * Charger plugged in — full-screen ChargingScreen.
     * @param isKeyOff true = key has been turned off (active charging dashboard with SoC ring).
     *                 false = key is still on (instructional "turn off key / unplug" screen).
     */
    data class Charging(val isKeyOff: Boolean) : ScreenMode

    // ── User navigation ───────────────────────────────────────────────────────
    data object Menu : ScreenMode
}