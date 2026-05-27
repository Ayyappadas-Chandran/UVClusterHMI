package com.ultraviolette.uvclusterhmi.ui.composable.screen

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import com.ultraviolette.uvclusterhmi.domain.model.ChargingUiState
import com.ultraviolette.uvclusterhmi.domain.model.RideMode
import com.ultraviolette.uvclusterhmi.ui.theme.ClusterColors
import com.ultraviolette.uvclusterhmi.ui.theme.ClusterDimens
import com.ultraviolette.uvclusterhmi.ui.theme.ClusterTypography

/**
 * Charging screen — Compose replacement for ChargingFragment.
 *
 * Reads only [ChargingUiState] from the dashboard. Two visible states:
 *   • keyOff == false → instructional "turn off key / unplug charger"
 *   • keyOff == true  → active charging dashboard with SoC ring
 *
 * Showing/hiding this screen is owned by [ClusterViewModel.resolveScreenMode]
 * via [ScreenMode.Charging]; this composable does not navigate.
 *
 * Konami debug-unlock (Back, Right, Left, Bottom, Left within 2s) is
 * handled in the ViewModel against handlebar key events — this composable
 * has no awareness of it.
 */
@Composable
fun ChargingScreen(
    state: ChargingUiState,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ClusterColors.background),
    ) {
        Crossfade(
            targetState = state.keyOff,
            animationSpec = tween(durationMillis = 300),
            label = "ChargingState",
        ) { keyOff ->
            if (keyOff) ChargingActive(state) else ChargingReview()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Key-on review screen: "TURN OFF KEY / UNPLUG CHARGER" instructions
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ChargingReview(modifier: Modifier = Modifier) {
    // Two instructional columns side by side. Original XML pixel-pins them
    // at marginStart=268dp / 709dp on a wide cluster; here we let a Row
    // with weighted spacers reflow them.
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val compact = maxWidth < ClusterDimens.compactBreakpoint

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (compact) 24.dp else 48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            InstructionColumn(
                iconRes  = ClusterDimens.IconRes.KEY,
                caption  = "To start charging",
                headline = "TURN OFF KEY",
            )
            // Divider line — original was a vertical ic_line drawable
            Spacer(
                Modifier
                    .width(1.dp)
                    .fillMaxHeight(0.5f)
                    .background(ClusterColors.dividerGrey),
            )
            InstructionColumn(
                iconRes  = ClusterDimens.IconRes.PLUG,
                caption  = "To resume riding",
                headline = "UNPLUG CHARGER",
            )
        }
    }
}

@Composable
private fun InstructionColumn(
    iconRes: Int,
    caption: String,
    headline: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter            = androidx.compose.ui.res.painterResource(iconRes),
            contentDescription = null,
            tint               = Color.Unspecified,
            modifier           = Modifier.size(ClusterDimens.chargingInstructionIcon),
        )
        Spacer(Modifier.height(48.dp))
        Text(
            text      = caption.uppercase(),
            color     = ClusterColors.lockdownGrey,
            fontSize  = 20.sp,
            fontWeight = FontWeight.W500,
            style     = ClusterTypography.sairaRegular,
            textAlign = TextAlign.Center,
        )
        Text(
            text      = headline,
            color     = ClusterColors.white,
            fontSize  = 20.sp,
            fontWeight = FontWeight.W400,
            style     = ClusterTypography.brutalType,
            textAlign = TextAlign.Center,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Key-off active charging screen: ring + SoC% + range + time-to-full
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ChargingActive(
    state: ChargingUiState,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val compact = maxWidth < ClusterDimens.compactBreakpoint
        // Ring sizes from container so it scales across cluster sizes.
        val ringSize: Dp = (maxHeight * 0.45f).coerceIn(160.dp, 320.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = if (compact) 16.dp else 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top status badge — "FAST CHARGING" with bolt icon
            if (state.isFastCharging) {
                FastChargingBadge()
            } else {
                Spacer(Modifier.height(28.dp))
            }

            Spacer(Modifier.weight(1f))

            // Center row: range (left) | ring (center) | time (right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                MetricColumn(
                    value = state.rangeDisplay,
                    label = state.rideMode.displayName(),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = if (compact) 16.dp else 32.dp),
                )
                BatteryRing(
                    progress = state.batterySoc.toFloat(),
                    modifier = Modifier.size(ringSize),
                )
                MetricColumn(
                    value = formatTimeLeft(state.timeToFullSeconds),
                    label = "Time left",
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = if (compact) 16.dp else 32.dp),
                )
            }

            Spacer(Modifier.weight(1f))

            // Bottom: ride mode icon
            RideModeBadge(rideMode = state.rideMode)
        }
    }
}

@Composable
private fun FastChargingBadge(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter            = androidx.compose.ui.res.painterResource(
                ClusterDimens.IconRes.CHARGING_BOLT,
            ),
            contentDescription = null,
            tint               = ClusterColors.chargingGreen,
            modifier           = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text       = "FAST CHARGING",
            color      = ClusterColors.chargingGreen,
            fontSize   = 22.sp,
            style      = ClusterTypography.brutalType,
        )
    }
}

@Composable
private fun MetricColumn(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text     = value,
            color    = ClusterColors.white,
            fontSize = 24.sp,
            style    = ClusterTypography.sairaRegular,
        )
        Text(
            text     = label,
            color    = ClusterColors.darkGreyMedium,
            fontSize = 20.sp,
        )
    }
}

@Composable
private fun RideModeBadge(
    rideMode: RideMode,
    modifier: Modifier = Modifier,
) {
    Crossfade(
        targetState   = rideMode,
        animationSpec = tween(200),
        label         = "RideModeIcon",
        modifier      = modifier,
    ) { mode ->
        Icon(
            painter            = androidx.compose.ui.res.painterResource(
                rideModeIcon(mode),
            ),
            contentDescription = mode.displayName(),
            tint               = Color.Unspecified,
            modifier           = Modifier.size(82.dp),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Battery ring — port of CircularGradientProgress to Compose Canvas
// ─────────────────────────────────────────────────────────────────────────────

/**
 * 300° sweep starting at top (-90°). Background ring is semitransparent dark;
 * progress is a green sweep gradient.
 *
 * Original View used stroke 34px at 260dp diameter (~13% of diameter).
 * Here we compute stroke from container size so the ring stays proportional.
 *
 * Animates SoC with spring; renders the percentage label centered.
 */
@Composable
fun BatteryRing(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(
        targetValue   = progress.coerceIn(0f, 100f),
        animationSpec = spring(
            dampingRatio    = Spring.DampingRatioMediumBouncy,
            stiffness       = Spring.StiffnessLow,
        ),
        label = "BatterySoc",
    )

    val sweepBrush = Brush.sweepGradient(
        colors = listOf(
            ClusterColors.chargeGradientStart,   // #2E3FB983
            ClusterColors.chargeGradientEnd,     // #10A362
        ),
    )

    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = size.minDimension * 0.13f
            val inset  = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(inset, inset)
            val totalSweep = 300f
            val startAngle = -90f - (totalSweep / 2f) + 90f
            // start at top (-90°) sweeping clockwise; arc is centered on the top.

            // Background arc
            drawArc(
                color      = ClusterColors.ringBackground,
                startAngle = startAngle,
                sweepAngle = totalSweep,
                useCenter  = false,
                topLeft    = topLeft,
                size       = arcSize,
                style      = Stroke(width = stroke, cap = StrokeCap.Butt),
            )

            // Progress arc
            drawArc(
                brush      = sweepBrush,
                startAngle = startAngle,
                sweepAngle = totalSweep * (animatedProgress / 100f),
                useCenter  = false,
                topLeft    = topLeft,
                size       = arcSize,
                style      = Stroke(width = stroke, cap = StrokeCap.Butt),
            )
        }

        // SoC % label centered
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text     = animatedProgress.toInt().toString(),
                color    = ClusterColors.white,
                fontSize = 52.sp,           // semantic equivalent of 52.54sp from XML
                fontWeight = FontWeight.W500,
                style    = ClusterTypography.brutalType,
            )
            Text(
                text     = "%",
                color    = ClusterColors.white,
                fontSize = 20.sp,
                style    = ClusterTypography.brutalType,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun formatTimeLeft(seconds: Int): String {
    if (seconds <= 0) return "--"
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    return if (h > 0) "${h}Hr ${m}Min" else "${m}Min"
}

private fun RideMode.displayName(): String = when (this) {
    RideMode.Glide     -> "Glide"
    RideMode.Combat    -> "Combat"
    RideMode.Ballistic -> "Ballistic"
}

private fun rideModeIcon(mode: RideMode): Int = when (mode) {
    RideMode.Glide     -> ClusterDimens.IconRes.GLIDE
    RideMode.Combat    -> ClusterDimens.IconRes.COMBAT
    RideMode.Ballistic -> ClusterDimens.IconRes.BALLISTIC
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

@Preview(widthDp = 320, heightDp = 240, name = "Compact – Charging Active")
@Composable
private fun PreviewChargingActiveCompact() {
    ChargingScreen(
        state = ChargingUiState(
            chargerPlugged = true, keyOff = true, batterySoc = 80,
            rangeDisplay = "150 km", timeToFullSeconds = 7800,
            rideMode = RideMode.Ballistic, isFastCharging = true,
        ),
    )
}

@Preview(widthDp = 480, heightDp = 320, name = "Standard – Charging Active")
@Composable
private fun PreviewChargingActiveStandard() {
    ChargingScreen(
        state = ChargingUiState(
            chargerPlugged = true, keyOff = true, batterySoc = 80,
            rangeDisplay = "150 km", timeToFullSeconds = 7800,
            rideMode = RideMode.Ballistic, isFastCharging = true,
        ),
    )
}

@Preview(widthDp = 800, heightDp = 480, name = "Wide – Charging Active")
@Composable
private fun PreviewChargingActiveWide() {
    ChargingScreen(
        state = ChargingUiState(
            chargerPlugged = true, keyOff = true, batterySoc = 80,
            rangeDisplay = "150 km", timeToFullSeconds = 7800,
            rideMode = RideMode.Ballistic, isFastCharging = true,
        ),
    )
}

@Preview(widthDp = 800, heightDp = 480, name = "Wide – Review (key on)")
@Composable
private fun PreviewChargingReviewWide() {
    ChargingScreen(
        state = ChargingUiState(
            chargerPlugged = true, keyOff = false,
        ),
    )
}