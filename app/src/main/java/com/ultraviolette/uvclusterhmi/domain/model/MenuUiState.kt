package com.ultraviolette.uvclusterhmi.domain.model

import androidx.compose.runtime.Stable

/**
 * Display state for the HMI top-level menu ([ScreenMode.Menu]).
 *
 * All values are derived from [ClusterViewModel] — composables never
 * read SharedPreferences or CarViewModel directly.
 */
@Stable
data class MenuUiState(
    /** Currently highlighted tile (ordinal of [MenuPosition]). */
    val selectedPosition: Int      = 0,

    // ── Battery tile ──────────────────────────────────────────────────────────
    /** Battery SOC 0–100 %. */
    val batterySoc: Int            = 0,
    /** User-configured charge limit, e.g. 80 (%). */
    val batteryLimit: Int          = 80,

    // ── Controls tile ─────────────────────────────────────────────────────────
    /** ABS mode label: "Dual", "Mono", or "Off". */
    val absLabel: String           = "Dual",
    /** Traction control level: "T1", "T2", "T3", or "Off". */
    val tractionLabel: String      = "Off",
    /** Regen preset: "R0" … "R9". */
    val regenLabel: String         = "R0",
    /** Whether Hill Hold is enabled. */
    val hillHoldEnabled: Boolean   = false,
)
