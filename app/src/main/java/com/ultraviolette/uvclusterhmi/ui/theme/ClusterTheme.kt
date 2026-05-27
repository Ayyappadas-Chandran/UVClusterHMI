package com.ultraviolette.uvclusterhmi.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import com.ultraviolette.uvclusterhmi.domain.model.RideMode

// ── Ambient accessors (use inside any composable) ─────────────────────────────

object ClusterTheme {
    val colors: ClusterColors
        @Composable @ReadOnlyComposable
        get() = LocalClusterColors.current

    val typography: ClusterTypography
        @Composable @ReadOnlyComposable
        get() = LocalClusterTypography.current
}

// ── Theme entry-point ─────────────────────────────────────────────────────────

/**
 * Root theme wrapper for all cluster screens.
 *
 * @param rideMode   Current ride mode — drives modeAccent colour.
 * @param isNightMode True for the dark/night palette; false for day.
 * @param isMotorArmed When false the mode accent is replaced with the
 *                     unarmed grey (motor-off state).
 * @param content   Composable content to theme.
 */
@Composable
fun ClusterTheme(
    rideMode: RideMode = RideMode.Glide,
    isNightMode: Boolean = true,
    isMotorArmed: Boolean = true,
    typography: ClusterTypography = DefaultClusterTypography,
    content: @Composable () -> Unit,
) {
    val colors = resolveColors(rideMode, isNightMode, isMotorArmed)

    CompositionLocalProvider(
        LocalClusterColors    provides colors,
        LocalClusterTypography provides typography,
    ) {
        content()
    }
}

// ── Color resolution ──────────────────────────────────────────────────────────

private fun resolveColors(
    rideMode: RideMode,
    isNightMode: Boolean,
    isMotorArmed: Boolean,
): ClusterColors = when {
    !isMotorArmed && isNightMode  -> NightUnarmedColors
    !isMotorArmed && !isNightMode -> DayUnarmedColors
    isNightMode -> when (rideMode) {
        RideMode.Glide     -> NightGlideColors
        RideMode.Combat    -> NightCombatColors
        RideMode.Ballistic -> NightBallisticColors
    }
    else -> when (rideMode) {
        RideMode.Glide     -> DayGlideColors
        RideMode.Combat    -> DayCombatColors
        RideMode.Ballistic -> DayBallisticColors
    }
}