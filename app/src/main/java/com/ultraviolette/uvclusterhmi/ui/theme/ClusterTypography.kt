package com.ultraviolette.uvclusterhmi.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ultraviolette.uvclusterhmi.R

// ── Font families ─────────────────────────────────────────────────────────────

/** Primary UI font — headings, mode labels, menu items */
val BrutalType = FontFamily(
    Font(R.font.brutal_type_thin,        FontWeight.Thin),
    Font(R.font.brutal_type_extra_light, FontWeight.ExtraLight),
    Font(R.font.brutal_type_light,       FontWeight.Light),
    Font(R.font.brutal_type,             FontWeight.Normal),
    Font(R.font.brutal_type_medium,      FontWeight.Medium),
    Font(R.font.brutal_type_bold,        FontWeight.Bold),
    Font(R.font.brutal_type_extra_bold,  FontWeight.ExtraBold),
    Font(R.font.brutal_type_black,       FontWeight.Black),
)

/** Monospace — speed display and safety-critical numeric readouts */
val AoiMono = FontFamily(
    Font(R.font.aoi_mono_standard,  FontWeight.Normal),
    Font(R.font.aoi_mono_extended,  FontWeight.Bold),
)

/** Data readouts — odo, range, Wh/km */
val DisketMono = FontFamily(
    Font(R.font.disket_mono_regular, FontWeight.Normal),
    Font(R.font.disket_mono_bold,    FontWeight.Bold),
)

// ── Semantic type scale ───────────────────────────────────────────────────────

@Immutable
data class ClusterTypography(
    // Speed: huge mono, always visible even at 200% font scale (clamped)
    val speedLarge: TextStyle,
    val speedCompact: TextStyle,

    // Mode name: BALLISTIC / COMBAT / GLIDE
    val modeLabel: TextStyle,

    // Ride/Range/Odo values
    val dataValue: TextStyle,

    // Ride/Range/Odo labels (small caps)
    val dataLabel: TextStyle,

    // Regen indicator "R3"
    val regenLabel: TextStyle,

    // Toolbar time
    val toolbarTime: TextStyle,

    // Toolbar battery %
    val toolbarBattery: TextStyle,

    // Menu item title
    val menuTitle: TextStyle,

    // Menu item subtitle
    val menuSubtitle: TextStyle,

    // Rec speed (60 km/h recommended)
    val recSpeed: TextStyle,
) {
    /**
     * Static text-style shorthands — for use in screens without [LocalClusterTypography]
     * access (standalone composables, Canvas helpers, or @Preview).
     *
     * [sairaRegular] is a logical alias for UI body text; mapped to [BrutalType]
     * because Saira is not bundled in this project.
     */
    companion object {
        val brutalType   = TextStyle(
            fontFamily  = BrutalType,
            fontWeight  = FontWeight.Normal,
            fontSize    = 14.sp,
            lineHeight  = 18.sp,
        )
        val sairaRegular = TextStyle(
            fontFamily  = BrutalType,
            fontWeight  = FontWeight.Normal,
            fontSize    = 14.sp,
            lineHeight  = 18.sp,
        )
    }
}

val DefaultClusterTypography = ClusterTypography(
    speedLarge = TextStyle(
        fontFamily = AoiMono,
        fontWeight = FontWeight.Normal,
        fontSize    = 96.sp,
        lineHeight  = 96.sp,
        // Safety-critical: clamp so 200% accessibility scale does not overflow
        // letterSpacing kept default (0)
    ),
    speedCompact = TextStyle(
        fontFamily = AoiMono,
        fontWeight = FontWeight.Normal,
        fontSize    = 64.sp,
        lineHeight  = 64.sp,
    ),
    modeLabel = TextStyle(
        fontFamily  = BrutalType,
        fontWeight  = FontWeight.ExtraBold,
        fontSize    = 14.sp,
        lineHeight  = 18.sp,
    ),
    dataValue = TextStyle(
        fontFamily  = DisketMono,
        fontWeight  = FontWeight.Bold,
        fontSize    = 20.sp,
        lineHeight  = 24.sp,
    ),
    dataLabel = TextStyle(
        fontFamily  = BrutalType,
        fontWeight  = FontWeight.Medium,
        fontSize    = 10.sp,
        lineHeight  = 14.sp,
    ),
    regenLabel = TextStyle(
        fontFamily  = BrutalType,
        fontWeight  = FontWeight.Bold,
        fontSize    = 12.sp,
        lineHeight  = 16.sp,
    ),
    toolbarTime = TextStyle(
        fontFamily  = BrutalType,
        fontWeight  = FontWeight.Medium,
        fontSize    = 14.sp,
        lineHeight  = 18.sp,
    ),
    toolbarBattery = TextStyle(
        fontFamily  = BrutalType,
        fontWeight  = FontWeight.Medium,
        fontSize    = 12.sp,
        lineHeight  = 16.sp,
    ),
    menuTitle = TextStyle(
        fontFamily  = BrutalType,
        fontWeight  = FontWeight.Bold,
        fontSize    = 16.sp,
        lineHeight  = 20.sp,
    ),
    menuSubtitle = TextStyle(
        fontFamily  = BrutalType,
        fontWeight  = FontWeight.Normal,
        fontSize    = 12.sp,
        lineHeight  = 16.sp,
    ),
    recSpeed = TextStyle(
        fontFamily  = BrutalType,
        fontWeight  = FontWeight.Medium,
        fontSize    = 10.sp,
        lineHeight  = 14.sp,
    ),
)

val LocalClusterTypography = staticCompositionLocalOf { DefaultClusterTypography }