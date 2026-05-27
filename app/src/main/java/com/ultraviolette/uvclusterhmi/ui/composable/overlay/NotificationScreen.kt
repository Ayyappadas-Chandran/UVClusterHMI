package com.ultraviolette.uvclusterhmi.ui.composable.overlay

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.ultraviolette.uvclusterhmi.R

// ── Illustration aspect ratio locked to original asset (512 × 334 px) ────────
private const val IllustrationAspectRatio = 512f / 334f

/**
 * General-purpose alert notification overlay.
 *
 * All dimensions are derived from container constraints — no hardcoded dp/sp.
 * Pass a [NotificationConfig] built from one of the predefined top-level
 * values in [NotificationConfig.kt], or construct one inline for custom alerts.
 *
 * Screens that previously hard-coded their own layouts
 * (ThermalRunawayScreen, SideStandAlertScreen, IncomingCallScreen) can be
 * replaced with a single call to [NotificationScreen] passing the
 * corresponding predefined config.
 */
@Composable
fun NotificationScreen(config: NotificationConfig) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {

        // ── Responsive size tokens ────────────────────────────────────────────
        val maxW: Dp = maxWidth
        val maxH: Dp = maxHeight

        val iconSize: Dp        = (maxW * 0.12f).coerceAtMost(100.dp)
        val illustrationW: Dp   = (maxW * 0.38f)
        val decorH: Dp          = maxH * 0.04f
        val spacerLg: Dp        = maxH * 0.05f
        val spacerSm: Dp        = maxH * 0.025f
        val inlineGap: Dp       = maxW * 0.025f
        val padH: Dp            = maxW * 0.06f

        val badgeSp: TextUnit   = (maxW.value * 0.040f).coerceIn(16f, 36f).sp
        val primarySp: TextUnit = (maxW.value * 0.035f).coerceIn(13f, 30f).sp
        val secondarySp: TextUnit = (maxW.value * 0.024f).coerceIn(11f, 22f).sp
        val calloutNumSp: TextUnit = (maxW.value * 0.080f).coerceIn(28f, 64f).sp
        val calloutUnitSp: TextUnit = (maxW.value * 0.050f).coerceIn(18f, 42f).sp

        // ── Background layer ──────────────────────────────────────────────────
        when (config.background) {
            NotificationBackground.Flat -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0A0A0A))
                )
            }
            NotificationBackground.DarkRedGradient -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF4B0000),
                                    Color(0xFF120000),
                                    Color.Black,
                                )
                            )
                        )
                )
            }
            NotificationBackground.ThermalGlow -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                )
                Image(
                    painter = painterResource(R.drawable.image_glow_thermal),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.6f,
                )
            }
        }

        // ── Optional top-edge accent gradient ─────────────────────────────────
        if (config.showTopAccent) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f    to config.accentColor.copy(alpha = 0.25f),
                            0.15f to Color.Transparent,
                        )
                    )
            )
        }

        // ── Main content column ───────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(Modifier.padding(horizontal = padH)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(spacerLg))

            // ── Icon + badge (inline or stacked) ──────────────────────────────
            if (config.iconInlineWithBadge) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(Modifier),          // width constrained by parent column
                ) {
                    // Leading flex spacer to centre the row group
                    Spacer(modifier = Modifier.weight(1f))

                    Image(
                        painter = painterResource(config.iconRes),
                        contentDescription = config.badgeText,
                        modifier = Modifier.size(iconSize),
                    )

                    Spacer(modifier = Modifier.width(inlineGap))

                    Text(
                        text = config.badgeText,
                        color = config.accentColor,
                        fontSize = badgeSp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = config.badgeLetterSpacing.em,
                    )

                    Spacer(modifier = Modifier.weight(1f))
                }
            } else {
                Image(
                    painter = painterResource(config.iconRes),
                    contentDescription = config.badgeText,
                    modifier = Modifier.size(iconSize),
                )

                Spacer(modifier = Modifier.height(spacerLg))

                Text(
                    text = config.badgeText,
                    color = config.accentColor,
                    fontSize = badgeSp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = config.badgeLetterSpacing.em,
                )
            }

            // ── Illustration ──────────────────────────────────────────────────
            config.illustrationRes?.let { res ->
                Spacer(modifier = Modifier.height(spacerLg))

                Image(
                    painter = painterResource(res),
                    contentDescription = null,
                    modifier = Modifier
                        .width(illustrationW)
                        .aspectRatio(IllustrationAspectRatio),
                    contentScale = ContentScale.Fit,
                )
            }

            // ── Decoration strip ──────────────────────────────────────────────
            config.decorationRes?.let { res ->
                Image(
                    painter = painterResource(res),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(decorH),
                    contentScale = ContentScale.FillHeight,
                )
            }

            // ── Callout value + unit ──────────────────────────────────────────
            if (config.calloutValue != null) {
                Spacer(modifier = Modifier.height(spacerLg))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = config.calloutValue,
                        color = Color.White,
                        fontSize = calloutNumSp,
                        fontWeight = FontWeight.Bold,
                    )
                    if (config.calloutUnit != null) {
                        Spacer(modifier = Modifier.width(inlineGap * 0.5f))
                        Text(
                            text = config.calloutUnit,
                            color = Color.White,
                            fontSize = calloutUnitSp,
                            fontWeight = FontWeight.Normal,
                        )
                    }
                }
            }

            // ── Primary text ──────────────────────────────────────────────────
            config.primaryText?.let { primary ->
                Spacer(modifier = Modifier.height(spacerSm))

                Text(
                    text = primary,
                    color = Color.White,
                    fontSize = primarySp,
                    fontWeight = FontWeight.Bold,
                )
            }

            // ── Secondary text ────────────────────────────────────────────────
            config.secondaryText?.let { secondary ->
                Spacer(modifier = Modifier.height(spacerSm))

                Text(
                    text = secondary,
                    color = Color(0xFFB2B2B2),
                    fontSize = secondarySp,
                )
            }

            // ── Extra lines ───────────────────────────────────────────────────
            config.extraLines.forEach { line ->
                Spacer(modifier = Modifier.height(spacerSm))

                when (line.style) {
                    NotificationLine.LineStyle.Primary -> Text(
                        text = line.text,
                        color = Color.White,
                        fontSize = primarySp,
                        fontWeight = FontWeight.Bold,
                    )
                    NotificationLine.LineStyle.Secondary -> Text(
                        text = line.text,
                        color = Color(0xFFB2B2B2),
                        fontSize = secondarySp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacerLg))
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(widthDp = 480,  heightDp = 320, name = "ThermalRunaway — 480×320")
@Composable
private fun PreviewThermalRunaway480() {
    NotificationScreen(config = ThermalRunawayNotification)
}

@Preview(widthDp = 800,  heightDp = 480, name = "ThermalRunaway — 800×480")
@Composable
private fun PreviewThermalRunaway800() {
    NotificationScreen(config = ThermalRunawayNotification)
}

@Preview(widthDp = 1024, heightDp = 600, name = "ThermalRunaway — 1024×600")
@Composable
private fun PreviewThermalRunaway1024() {
    NotificationScreen(config = ThermalRunawayNotification)
}

@Preview(widthDp = 800, heightDp = 480, name = "SideStand — 800×480")
@Composable
private fun PreviewSideStand() {
    NotificationScreen(config = SideStandNotification)
}

@Preview(widthDp = 800, heightDp = 480, name = "IncomingCall — 800×480")
@Composable
private fun PreviewIncomingCall() {
    NotificationScreen(config = incomingCallNotification("John's iPhone"))
}
