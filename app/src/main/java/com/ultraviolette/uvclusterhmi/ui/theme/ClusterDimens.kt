package com.ultraviolette.uvclusterhmi.ui.theme

import androidx.compose.ui.unit.dp
import com.ultraviolette.uvclusterhmi.R

/**
 * Semantic dimension scale for the cluster HMI.
 *
 * Breakpoints drive [BoxWithConstraints] routing in screen roots:
 * ```
 * BoxWithConstraints {
 *     if (maxWidth < ClusterDimens.compactBreakpoint) CompactLayout(...)
 *     else if (maxWidth < ClusterDimens.wideBreakpoint)  StandardLayout(...)
 *     else WideLayout(...)
 * }
 * ```
 *
 * Current hardware: 1024×600.  Future SKU size TBD.
 * Preview widths:
 *   @Preview(widthDp = 480,  heightDp = 320, name = "Compact")
 *   @Preview(widthDp = 800,  heightDp = 480, name = "Standard")
 *   @Preview(widthDp = 1024, heightDp = 600, name = "Wide")
 */
object ClusterDimens {

    // ── Breakpoints ───────────────────────────────────────────────────────────
    val compactBreakpoint  = 480.dp
    val wideBreakpoint     = 800.dp

    // ── Touch / handlebar targets ─────────────────────────────────────────────
    /** Minimum interactive touch target — never go below this. */
    val minTouchTarget     = 48.dp

    // ── Toolbar ───────────────────────────────────────────────────────────────
    val toolbarHeight      = 48.dp
    val toolbarIconSize    = 24.dp
    val toolbarPaddingH    = 12.dp

    // ── Dashboard ─────────────────────────────────────────────────────────────
    val dashboardPaddingH  = 16.dp
    val dashboardPaddingV  = 8.dp

    /** Speed text font scales — composable clamps at this size. */
    val speedFontSizeLarge   = 96
    val speedFontSizeCompact = 64

    // ── Gauges ────────────────────────────────────────────────────────────────
    /** Stroke width for arc gauges (CurvedProgressBar etc.) */
    val gaugeStroke        = 8.dp
    /** Min dimension fraction used by gauge canvas: gaugeSize = size.minDimension * gaugeFraction */
    const val gaugeFraction = 0.85f

    // ── Regen / efficiency level indicators ──────────────────────────────────
    val regenIconSize      = 16.dp
    val regenSpacing       = 4.dp

    // ── Radar indicator circles ───────────────────────────────────────────────
    val radarIndicatorSize = 80.dp

    // ── Overlays ──────────────────────────────────────────────────────────────
    val overlayCornerRadius = 12.dp
    val overlayElevation    = 8.dp

    // ── Menu ──────────────────────────────────────────────────────────────────
    val menuTileWidthCompact  = 80.dp
    val menuTileWidthStandard = 100.dp
    val menuTileWidthWide     = 120.dp
    val menuTileHeight        = 64.dp
    val menuTileSpacing       = 8.dp

    // ── Charging screen ───────────────────────────────────────────────────────
    /** Size of the key/plug instruction icons on the charging review screen. */
    val chargingInstructionIcon = 72.dp

    /** Drawable resource IDs for ride mode and charging UI icons. */
    object IconRes {
        val KEY           = R.drawable.ic_key
        val PLUG          = R.drawable.ic_plug
        val CHARGING_BOLT = R.drawable.ic_charge
        val GLIDE         = R.drawable.ic_glide
        val COMBAT        = R.drawable.ic_combat
        val BALLISTIC     = R.drawable.ic_ballistic
    }
}