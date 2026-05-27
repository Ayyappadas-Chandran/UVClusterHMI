package com.ultraviolette.uvclusterhmi.ui.composable.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.model.ClusterUiState
import com.ultraviolette.uvclusterhmi.domain.model.DashboardUiState
import com.ultraviolette.uvclusterhmi.domain.model.MotorUiState
import com.ultraviolette.uvclusterhmi.domain.model.OdometerUiState

// Design reference: fragment_hover_mode.xml
// hoverModeRed: #FF5151
// All sizes are derived from container dimensions — no hardcoded dp/sp constants.

private val HoverRed = Color(0xFFFF5151)

/**
 * Full-screen Hover Mode display.
 *
 * Shows speed, speed unit, "HOVER MODE" label, battery warning, and odometer.
 * Purely read-only — regen adjustments via handlebar buttons are handled by
 * [MainActivity.handleButtonNavigation] which keeps write operations out of composables.
 *
 * Shown when [ScreenMode.HoverMode] is active (snap.modeHover > 0).
 */
@Composable
fun HoverModeScreen(uiState: ClusterUiState.Active) {
    val motor = uiState.dashboard.motor
    val odo   = uiState.dashboard.odo
    HoverModeContent(
        speedDisplay = motor.speedDisplay,
        unit         = odo.unit,
        odoDisplay   = "${odo.odoDisplay} ${odo.unit}",
    )
}

@Composable
private fun HoverModeContent(
    speedDisplay: String,
    unit: String,
    odoDisplay: String,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        val w = maxWidth
        val h = maxHeight

        // ── Responsive size tokens — all fractions of container ───────────────
        val odoSp         = (w.value * 0.026f).coerceIn(11f, 24f).sp
        val speedSp       = (w.value * 0.135f).coerceIn(56f, 130f).sp
        val unitSp        = (w.value * 0.022f).coerceIn(10f, 22f).sp
        val modeLabelSp   = (w.value * 0.038f).coerceIn(16f, 36f).sp
        val warningLabelSp= (w.value * 0.022f).coerceIn(10f, 22f).sp
        val sideImagePad  = w * 0.07f
        val spacerLg      = h * 0.05f
        val spacerSm      = h * 0.028f
        val spacerXs      = h * 0.010f

        // ── Background glow ───────────────────────────────────────────────────
        Image(
            painter            = painterResource(R.drawable.image_glow_thermal),
            contentDescription = null,
            modifier           = Modifier.fillMaxSize(),
            contentScale       = ContentScale.Crop,
            alpha              = 0.4f,
        )

        // ── Decorative speed side images ──────────────────────────────────────
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter            = painterResource(R.drawable.image_speed_left),
                contentDescription = null,
                modifier           = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = sideImagePad),
                contentScale       = ContentScale.Fit,
            )
            Image(
                painter            = painterResource(R.drawable.image_speed_right),
                contentDescription = null,
                modifier           = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = sideImagePad),
                contentScale       = ContentScale.Fit,
            )
        }

        // ── Bottom decoration ─────────────────────────────────────────────────
        Image(
            painter            = painterResource(R.drawable.group_627272),
            contentDescription = null,
            modifier           = Modifier.align(Alignment.BottomCenter),
            contentScale       = ContentScale.FillWidth,
        )

        // ── Center column: ODO / speed / unit / HOVER MODE / warning ─────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.align(Alignment.Center),
        ) {
            // ODO label + value
            Text(text = "odo",      color = HoverRed,     fontSize = odoSp)
            Spacer(modifier = Modifier.height(spacerXs))
            Text(text = odoDisplay, color = Color.White,  fontSize = odoSp)

            Spacer(modifier = Modifier.height(spacerSm))

            // Large speed numeral
            Text(
                text       = speedDisplay,
                color      = Color.White,
                fontSize   = speedSp,
                fontWeight = FontWeight.Normal,
                maxLines   = 1,
            )

            // Speed unit below numeral
            Text(
                text     = if (unit == "miles") "mph" else "km/h",
                color    = Color.White,
                fontSize = unitSp,
            )

            Spacer(modifier = Modifier.height(spacerLg))

            // "HOVER MODE" label
            Text(
                text          = "HOVER MODE",
                color         = HoverRed,
                fontSize      = modeLabelSp,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 0.12.em,
            )

            Spacer(modifier = Modifier.height(spacerSm))

            // Battery warning
            Text(
                text          = "BATTERY CRITICALLY LOW",
                color         = Color.White,
                fontSize      = warningLabelSp,
                letterSpacing = 0.06.em,
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(widthDp = 480,  heightDp = 320, name = "Compact")
@Preview(widthDp = 800,  heightDp = 480, name = "Standard")
@Preview(widthDp = 1024, heightDp = 600, name = "Wide")
@Composable
private fun HoverModeScreenPreview() {
    HoverModeContent(speedDisplay = "042", unit = "km", odoDisplay = "1300 km")
}
