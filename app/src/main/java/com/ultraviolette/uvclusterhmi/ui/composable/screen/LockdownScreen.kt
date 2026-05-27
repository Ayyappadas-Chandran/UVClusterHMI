package com.ultraviolette.uvclusterhmi.ui.composable.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.ultraviolette.uvclusterhmi.R

// Design reference: fragment_lockdown.xml
// All sizes are derived from container dimensions — no hardcoded dp/sp constants.

private val LockdownRed  = Color(0xFFE93E4A)   // lockdownRed
private val SelectionRed = Color(0xFFD61A21)   // activeSelectionRed
private val LockdownGrey = Color(0xFFB2B2B2)   // lockdownGrey

/**
 * Full-screen lockdown alert.
 *
 * @param isEnteringLockdown `true` → "park your bike" two-step instructions (VCU 0x5f).
 *                           `false` → "vehicle in lockdown" locked state (VCU 0x01).
 */
@Composable
fun LockdownScreen(isEnteringLockdown: Boolean) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        if (isEnteringLockdown) {
            EnteringLockdownContent(maxWidth, maxHeight)
        } else {
            VehicleLockedContent(maxWidth, maxHeight)
        }
    }
}

// ── "Park the bike" — entering lockdown (0x5f) ───────────────────────────────

@Composable
private fun EnteringLockdownContent(w: Dp, h: Dp) {
    // ── Responsive size tokens ────────────────────────────────────────────────
    val padH         = w * 0.05f
    val padV         = h * 0.05f
    val stepSp       = (w.value * 0.030f).coerceIn(14f, 28f).sp
    val titleSp      = (w.value * 0.030f).coerceIn(14f, 28f).sp
    val subtitleSp   = (w.value * 0.020f).coerceIn(10f, 20f).sp
    val parkIconSize = (w * 0.13f).coerceAtMost(90.dp)
    val distNumSp    = (w.value * 0.055f).coerceIn(22f, 48f).sp
    val distUnitSp   = (w.value * 0.032f).coerceIn(14f, 30f).sp
    val distIconSize = (w * 0.07f).coerceAtMost(56.dp)
    val spacerMd     = h * 0.035f
    val spacerSm     = h * 0.018f

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = padH, vertical = padV),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        // ── Step 1 — Park the Vehicle ─────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text       = "STEP 1",
                color      = SelectionRed,
                fontSize   = stepSp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(spacerMd))
            Image(
                painter            = painterResource(R.drawable.bg_red_stroke),
                contentDescription = "Park",
                modifier           = Modifier.size(parkIconSize),
                contentScale       = ContentScale.Fit,
            )
            Spacer(modifier = Modifier.height(spacerMd))
            Text(
                text       = "Please Park Vehicle",
                color      = SelectionRed,
                fontSize   = titleSp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(spacerSm))
            Text(
                text     = "In an isolated area",
                color    = LockdownGrey,
                fontSize = subtitleSp,
            )
        }

        // ── Vertical divider line ─────────────────────────────────────────────
        Image(
            painter            = painterResource(R.drawable.ic_line),
            contentDescription = null,
            modifier           = Modifier
                .padding(horizontal = w * 0.015f)
                .fillMaxHeight(),
            contentScale       = ContentScale.FillHeight,
        )

        // ── Step 2 — Keep Distance / Road Assistance ──────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text       = "STEP 2",
                color      = Color.White,
                fontSize   = stepSp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(spacerMd))

            // Distance callout: "20 m"
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text       = "20",
                    color      = Color.White,
                    fontSize   = distNumSp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.width(w * 0.012f))
                Text(
                    text     = "m",
                    color    = Color.White,
                    fontSize = distUnitSp,
                    modifier = Modifier.padding(bottom = h * 0.012f),
                )
            }

            Image(
                painter            = painterResource(R.drawable.ic_distance),
                contentDescription = "distance icon",
                modifier           = Modifier.size(distIconSize),
                contentScale       = ContentScale.Fit,
            )
            Spacer(modifier = Modifier.height(spacerSm))
            Text(
                text          = "KEEP 20M DISTANCE",
                color         = SelectionRed,
                fontSize      = titleSp,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 0.06.em,
            )
            Spacer(modifier = Modifier.height(spacerSm))
            Text(
                text     = "Contact Road Assistance",
                color    = LockdownGrey,
                fontSize = subtitleSp,
            )
        }
    }
}

// ── "Vehicle in lockdown" — locked state (0x01) ───────────────────────────────

@Composable
private fun VehicleLockedContent(w: Dp, h: Dp) {
    // ── Responsive size tokens ────────────────────────────────────────────────
    val lockIconSize = (w.value * 0.028f).coerceIn(18f, 30f).dp
    val bikeSz       = (w * 0.22f).coerceAtMost(220.dp)
    val lockLabelSp  = (w.value * 0.022f).coerceIn(11f, 22f).sp
    val subtitleSp   = (w.value * 0.020f).coerceIn(10f, 20f).sp
    val spacerMd     = h * 0.030f

    Box(
        modifier         = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        // Background layers: bike silhouette + splash
        Image(
            painter            = painterResource(R.drawable.bg_lockdown),
            contentDescription = null,
            modifier           = Modifier.fillMaxSize(),
            contentScale       = ContentScale.Fit,
            alpha              = 0.8f,
        )
        Image(
            painter            = painterResource(R.drawable.image_lockdown_splash),
            contentDescription = null,
            modifier           = Modifier.fillMaxSize(),
            contentScale       = ContentScale.Fit,
        )

        // Foreground content
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // "VEHICLE IN LOCKDOWN" row: lock icon + label
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter            = painterResource(R.drawable.ic_lockdown_lock),
                    contentDescription = null,
                    modifier           = Modifier.size(lockIconSize),
                )
                Spacer(modifier = Modifier.width(w * 0.015f))
                Text(
                    text          = "VEHICLE IN LOCKDOWN",
                    color         = LockdownRed,
                    fontSize      = lockLabelSp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 0.06.em,
                )
            }

            Spacer(modifier = Modifier.height(spacerMd))

            // Bike image
            Image(
                painter            = painterResource(R.drawable.image_bike_lockdown),
                contentDescription = null,
                modifier           = Modifier.size(bikeSz),
                contentScale       = ContentScale.Fit,
            )

            Spacer(modifier = Modifier.height(spacerMd))

            Text(
                text     = "Use UV App to unlock your vehicle",
                color    = LockdownGrey,
                fontSize = subtitleSp,
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(widthDp = 480,  heightDp = 320, name = "Entering – Compact")
@Preview(widthDp = 800,  heightDp = 480, name = "Entering – Standard")
@Preview(widthDp = 1024, heightDp = 600, name = "Entering – Wide")
@Composable
private fun LockdownEnteringPreview() {
    LockdownScreen(isEnteringLockdown = true)
}

@Preview(widthDp = 480,  heightDp = 320, name = "Locked – Compact")
@Preview(widthDp = 800,  heightDp = 480, name = "Locked – Standard")
@Preview(widthDp = 1024, heightDp = 600, name = "Locked – Wide")
@Composable
private fun LockdownLockedPreview() {
    LockdownScreen(isEnteringLockdown = false)
}
