package com.ultraviolette.uvclusterhmi.ui.composable.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.ultraviolette.uvclusterhmi.R

// Design reference: dialog_tpms.xml + fragment_tpms.xml
// All sizes are derived from container dimensions — no hardcoded dp/sp constants.

private val TpmsYellow = Color(0xFFFFCC00)
private val TpmsGrey   = Color(0xFF7A7A7A)

/**
 * Full-screen TPMS alert overlay.
 *
 * Shows recommended tyre pressures and a temperature advisory when
 * [ScreenMode.TpmsAlert] is active.
 * Purely read-only; clears automatically when the VCU signal clears.
 */
@Composable
fun TpmsAlertScreen() {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        val w = maxWidth
        val h = maxHeight

        // ── Responsive size tokens — all fractions of container ───────────────
        val padH         = w * 0.05f
        val iconSize     = (w * 0.07f).coerceAtMost(56.dp)
        val titleSp      = (w.value * 0.030f).coerceIn(14f, 26f).sp
        val labelSp      = (w.value * 0.022f).coerceIn(10f, 18f).sp
        val valueSp      = (w.value * 0.055f).coerceIn(22f, 48f).sp
        val warningSp    = (w.value * 0.018f).coerceIn(9f, 16f).sp
        val iconGap      = w * 0.020f
        val imageWidth   = w * 0.55f
        val dividerH     = h * 0.14f
        val spacerLg     = h * 0.04f
        val spacerSm     = h * 0.025f

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = padH),
        ) {
            Spacer(modifier = Modifier.height(spacerLg))

            // ── Header: icon + title ──────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_tpms_error),
                    contentDescription = "TPMS error",
                    modifier = Modifier.size(iconSize),
                )
                Spacer(modifier = Modifier.width(iconGap))
                Text(
                    text = "RECOMMENDED TYRE PRESSURE",
                    color = Color.White,
                    fontSize = titleSp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.06.em,
                )
            }

            Spacer(modifier = Modifier.height(spacerLg))

            // ── Bike tyre diagram (aspect ratio locked to asset 512 × 334) ────
            Image(
                painter = painterResource(R.drawable.image_x47_tpms),
                contentDescription = "Bike tyre diagram",
                modifier = Modifier
                    .width(imageWidth)
                    .aspectRatio(512f / 334f),
                contentScale = ContentScale.Fit,
            )

            Spacer(modifier = Modifier.height(spacerSm))

            // ── Pressure row: FRONT | vertical divider | REAR ─────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "FRONT",
                        color = TpmsGrey,
                        fontSize = labelSp,
                        letterSpacing = 0.08.em,
                    )
                    Spacer(modifier = Modifier.height(spacerSm * 0.5f))
                    Text(
                        text = "29 PSI",
                        color = Color.White,
                        fontSize = valueSp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                // 1 dp vertical divider — intentionally minimal
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(dividerH)
                        .background(TpmsGrey)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "REAR",
                        color = TpmsGrey,
                        fontSize = labelSp,
                        letterSpacing = 0.08.em,
                    )
                    Spacer(modifier = Modifier.height(spacerSm * 0.5f))
                    Text(
                        text = "33 PSI",
                        color = Color.White,
                        fontSize = valueSp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacerSm))

            HorizontalDivider(color = TpmsGrey.copy(alpha = 0.4f))

            Spacer(modifier = Modifier.height(spacerSm))

            // ── Temperature advisory ──────────────────────────────────────────
            Text(
                text = "Increase 2–4 PSI when temp is high\n(Do not deflate)",
                color = TpmsYellow,
                fontSize = warningSp,
                textAlign = TextAlign.Center,
                lineHeight = (warningSp.value * 1.4f).sp,
            )

            Spacer(modifier = Modifier.height(spacerLg))
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(widthDp = 480,  heightDp = 320, name = "Compact")
@Preview(widthDp = 800,  heightDp = 480, name = "Standard")
@Preview(widthDp = 1024, heightDp = 600, name = "Wide")
@Composable
private fun TpmsAlertScreenPreview() {
    TpmsAlertScreen()
}
