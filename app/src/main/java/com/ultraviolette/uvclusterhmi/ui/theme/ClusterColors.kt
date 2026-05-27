package com.ultraviolette.uvclusterhmi.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ── Raw palette (from colors.xml + themes.xml) ───────────────────────────────

object ClusterPalette {
    // Backgrounds
    val Black           = Color(0xFF000000)
    val AppBackground   = Color(0xFF151515)
    val Surface         = Color(0xFF1F1F21)
    val ToolBar         = Color(0xFF141415)
    val DayBackground   = Color(0xFFF5F5F5)
    val StrokeColor     = Color(0xFF242424)
    val GreyDark        = Color(0xFF3A3A3A)
    val GreyDarkMedium  = Color(0xFF353535)
    val GreyDarkExtra   = Color(0xFF1A1A1A)

    // Text / Neutrals
    val White           = Color(0xFFFFFFFF)
    val LightGrey       = Color(0xFFECF2F6)
    val LightGreyMedium = Color(0xFF909090)
    val GreyMedium      = Color(0xFF7A8585)
    val GreyLight       = Color(0xFFADADAD)
    val DarkGrey        = Color(0xFFACACAC)
    val UnselectedGrey  = Color(0xFF898989)

    // Ride mode accent colours
    val Ballistic       = Color(0xFFE3142E)
    val Combat          = Color(0xFF1491DE)
    val Glide           = Color(0xFF009A79)

    // Status / Telltale
    val GreenTelltale   = Color(0xFF00E299)
    val GreenText       = Color(0xFF00E59B)
    val GreenParticle   = Color(0xFF4DCB8B)
    val Orange          = Color(0xFFF86C16)
    val RedTelltale     = Color(0xFFFC0D0D)
    val MediumRed       = Color(0xFFFF0000)
    val ArcCyan         = Color(0xFF00E4FF)
    val GlowBlue        = Color(0xFF4FF4FF)

    // Motor-armed grey (unarmed speed colour)
    val MotorArmedGreyNight = Color(0xFF4D4D4D)
    val MotorArmedGreyLight = Color(0xFFC2C2C2)
    val LevelGreyDark   = Color(0xFF9E9E9E)
    val LevelGreyLight  = Color(0xFF949799)

    // Battery / Charging
    val ChargingGreen   = Color(0xFF3FB983)
    val BatteryError    = Color(0xFFE93E4A)   // lockdownRed used for error

    // TPMS
    val TpmsGrey        = Color(0xFF767D98)

    // Lockdown
    val LockdownRed     = Color(0xFFE93E4A)
    val LockdownGrey    = Color(0xFFB2B2B2)

    val Transparent     = Color(0x00000000)
    val BlackTransparent = Color(0xA3000000)
}

// ── Semantic token set — one per ride mode + disarmed ────────────────────────

@Immutable
data class ClusterColors(
    // Core backgrounds
    val background: Color,
    val surface: Color,
    val toolBar: Color,

    // The ride-mode accent: red / blue / green / grey-when-unarmed
    val modeAccent: Color,
    val modeBackground: Color,

    // Speed text: white when armed, mid-grey when disarmed
    val speedActive: Color,
    val speedInactive: Color,

    // Labels on dashboard (ride/range/odo headers) match modeAccent
    val labelText: Color,

    // Gauge arc / progress fill
    val gaugeArc: Color,

    // Telltale colours
    val telltaleGreen: Color,
    val telltaleOrange: Color,
    val telltaleRed: Color,

    // Regen / efficiency levels when armed vs unarmed
    val levelActive: Color,
    val levelInactive: Color,

    // Charging
    val chargingGreen: Color,

    // General text
    val primaryText: Color,
    val secondaryText: Color,

    // Toolbar centre background drawable tint (Day vs Dashboard mode handled in composable)
    val toolbarCenterTint: Color,

    // Side background (day = light, night = dark)
    val bgSide: Color,

    // Power bars background
    val powerBarBg: Color,
) {
    /**
     * Static colour constants — used by screens that don't have access to
     * [LocalClusterColors] (e.g. drawing helpers that run outside a Composition,
     * or preview-only helpers).  For screens inside a [ClusterTheme] wrapper,
     * prefer `LocalClusterColors.current.xxx` instead.
     */
    companion object {
        val background          = ClusterPalette.AppBackground
        val white               = ClusterPalette.White
        val darkGreyMedium      = ClusterPalette.GreyMedium
        val lockdownGrey        = ClusterPalette.LockdownGrey
        val dividerGrey         = ClusterPalette.LightGreyMedium
        val chargingGreen       = ClusterPalette.ChargingGreen
        /** Semitransparent green — sweep-gradient start for the SoC ring. */
        val chargeGradientStart = Color(0x2E3FB983)
        /** Solid green — sweep-gradient end for the SoC ring. */
        val chargeGradientEnd   = Color(0xFF10A362)
        /** Semitransparent white ring background for the SoC arc. */
        val ringBackground      = Color(0x22FFFFFF)
    }
}

// ── Night / dark base (used for all ride modes at night) ─────────────────────

private val nightBase = ClusterColors(
    background       = ClusterPalette.AppBackground,
    surface          = ClusterPalette.Surface,
    toolBar          = ClusterPalette.ToolBar,
    modeAccent       = ClusterPalette.Glide,           // overridden per mode
    modeBackground   = ClusterPalette.AppBackground,
    speedActive      = ClusterPalette.White,
    speedInactive    = ClusterPalette.LightGreyMedium,
    labelText        = ClusterPalette.Glide,
    gaugeArc         = ClusterPalette.ArcCyan,
    telltaleGreen    = ClusterPalette.GreenTelltale,
    telltaleOrange   = ClusterPalette.Orange,
    telltaleRed      = ClusterPalette.RedTelltale,
    levelActive      = ClusterPalette.Glide,
    levelInactive    = ClusterPalette.LevelGreyLight,
    chargingGreen    = ClusterPalette.ChargingGreen,
    primaryText      = ClusterPalette.White,
    secondaryText    = ClusterPalette.LightGreyMedium,
    toolbarCenterTint = ClusterPalette.White,
    bgSide           = ClusterPalette.GreyDarkExtra,
    powerBarBg       = ClusterPalette.GreyDark,
)

// ── Day base ─────────────────────────────────────────────────────────────────

private val dayBase = nightBase.copy(
    background    = ClusterPalette.DayBackground,
    surface       = ClusterPalette.DayBackground,
    primaryText   = ClusterPalette.Black,
    secondaryText = ClusterPalette.GreyMedium,
    speedActive   = ClusterPalette.Black,
    bgSide        = Color(0xFFE5E5E5),
    powerBarBg    = Color(0xFFCACACA),
)

// ── Per-mode colour sets (night) ──────────────────────────────────────────────

val NightGlideColors     = nightBase
val NightCombatColors    = nightBase.copy(modeAccent = ClusterPalette.Combat,    labelText = ClusterPalette.Combat,    levelActive = ClusterPalette.Combat)
val NightBallisticColors = nightBase.copy(modeAccent = ClusterPalette.Ballistic, labelText = ClusterPalette.Ballistic, levelActive = ClusterPalette.Ballistic)
val NightUnarmedColors   = nightBase.copy(modeAccent = ClusterPalette.LevelGreyLight, labelText = ClusterPalette.LevelGreyLight, levelActive = ClusterPalette.LevelGreyLight)

// ── Per-mode colour sets (day) ────────────────────────────────────────────────

val DayGlideColors     = dayBase
val DayCombatColors    = dayBase.copy(modeAccent = ClusterPalette.Combat,    labelText = ClusterPalette.Combat,    levelActive = ClusterPalette.Combat)
val DayBallisticColors = dayBase.copy(modeAccent = ClusterPalette.Ballistic, labelText = ClusterPalette.Ballistic, levelActive = ClusterPalette.Ballistic)
val DayUnarmedColors   = dayBase.copy(modeAccent = ClusterPalette.LevelGreyLight, labelText = ClusterPalette.LevelGreyLight, levelActive = ClusterPalette.LevelGreyLight)

// ── CompositionLocal ─────────────────────────────────────────────────────────

val LocalClusterColors = staticCompositionLocalOf { NightGlideColors }