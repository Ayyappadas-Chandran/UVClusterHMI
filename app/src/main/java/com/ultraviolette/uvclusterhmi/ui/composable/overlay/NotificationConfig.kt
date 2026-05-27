package com.ultraviolette.uvclusterhmi.ui.composable.overlay

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.ultraviolette.uvclusterhmi.R

/**
 * All data needed to render an alert notification overlay via [NotificationScreen].
 *
 * This is a pure-data structure — no Composable code.
 * Predefined configs for each alert type are defined as top-level values
 * at the bottom of this file.
 *
 * Sizing inside [NotificationScreen] is fully container-relative (no hardcoded dp/sp).
 */
@Immutable
data class NotificationConfig(

    /** Background style. */
    val background: NotificationBackground,

    /** Icon drawable resource. Rendered at 12% of container width. */
    @DrawableRes val iconRes: Int,

    /** Short badge label, e.g. "CRITICAL ALERT". Rendered in [accentColor]. */
    val badgeText: String,

    /** Accent colour: badge text, icon tint (when applicable), top-border accent. */
    val accentColor: Color,

    /**
     * When `true`, icon and badge are placed side-by-side in a [Row].
     * When `false` (default), icon appears above the badge (column layout).
     */
    val iconInlineWithBadge: Boolean = false,

    /** Primary white body text below the badge. */
    val primaryText: String? = null,

    /** Dimmed grey body text below primary. */
    val secondaryText: String? = null,

    /** Additional text lines, rendered below [secondaryText]. */
    val extraLines: List<NotificationLine> = emptyList(),

    /** Optional illustration image rendered between badge and primary text. */
    @DrawableRes val illustrationRes: Int? = null,

    /** Optional small decoration image rendered just below the illustration. */
    @DrawableRes val decorationRes: Int? = null,

    /**
     * Large callout value (e.g. "20") rendered as an oversized numeral.
     * Displayed together with [calloutUnit] in a baseline-aligned Row.
     */
    val calloutValue: String? = null,

    /** Unit string next to [calloutValue], e.g. "m". */
    val calloutUnit: String? = null,

    /**
     * When `true`, draws a thin top-edge gradient accent in [accentColor].
     * Used by Side Stand alert to reinforce the danger border.
     */
    val showTopAccent: Boolean = false,

    /** Letter spacing for badge text in em units. */
    val badgeLetterSpacing: Float = 0.15f,
)

// ── Line types for extraLines ─────────────────────────────────────────────────

@Immutable
data class NotificationLine(
    val text: String,
    val style: LineStyle,
) {
    enum class LineStyle {
        /** White, FontWeight.Bold — same weight as [NotificationConfig.primaryText]. */
        Primary,
        /** Dimmed grey (#B2B2B2) — same as [NotificationConfig.secondaryText]. */
        Secondary,
    }
}

// ── Background variants ───────────────────────────────────────────────────────

enum class NotificationBackground {
    /** Near-black #0A0A0A flat fill — incoming-call style. */
    Flat,
    /** Vertical gradient #4B0000 → #120000 → #000000 — side-stand style. */
    DarkRedGradient,
    /** Black fill + glow image overlay (alpha 0.6) — thermal runaway style. */
    ThermalGlow,
}

// ── Predefined notification configs ──────────────────────────────────────────
// These are the single source of truth for each alert's visual identity.
// ClusterNavHost passes them to NotificationScreen; no individual screen files needed.

private val AlertRed   = Color(0xFFE93E4A)   // lockdownRed from colors.xml
private val AlertGreen = Color(0xFF4CAF50)   // greenTextColor

/** Thermal-runaway critical alert — full glow background, parking illustration. */
val ThermalRunawayNotification = NotificationConfig(
    background          = NotificationBackground.ThermalGlow,
    iconRes             = R.drawable.warning,
    badgeText           = "CRITICAL ALERT",
    accentColor         = AlertRed,
    iconInlineWithBadge = true,
    illustrationRes     = R.drawable.image_parking,
    decorationRes       = R.drawable.image_thermal_line_effect,
    calloutValue        = "20",
    calloutUnit         = "m",
    primaryText         = "KEEP 20M DISTANCE",
    secondaryText       = "Contact Road Assistance",
    extraLines          = listOf(
        NotificationLine("Please Park Vehicle",    NotificationLine.LineStyle.Primary),
        NotificationLine("In an isolated area",    NotificationLine.LineStyle.Secondary),
    ),
    badgeLetterSpacing  = 0.08f,
)

/** Side-stand deployed — dark-red gradient background, top accent border. */
val SideStandNotification = NotificationConfig(
    background         = NotificationBackground.DarkRedGradient,
    iconRes            = R.drawable.ic_motor_off,
    badgeText          = "SIDE STAND DEPLOYED",
    accentColor        = AlertRed,
    primaryText        = "Motor Disarmed",
    secondaryText      = "Retract side stand to re-arm",
    showTopAccent      = true,
    badgeLetterSpacing = 0.15f,
)

/** Incoming Bluetooth call — flat dark background, green accent. */
fun incomingCallNotification(callerName: String) = NotificationConfig(
    background         = NotificationBackground.Flat,
    iconRes            = R.drawable.ic_headphone,
    badgeText          = "INCOMING CALL",
    accentColor        = AlertGreen,
    primaryText        = callerName,
    secondaryText      = "Answer on your Bluetooth device",
    badgeLetterSpacing = 0.22f,
)
