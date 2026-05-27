package com.ultraviolette.uvclusterhmi.ui.composable.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.model.MenuPosition
import com.ultraviolette.uvclusterhmi.domain.model.MenuUiState

// Design reference: fragment_menu.xml
// Background: #151515  |  Tile normal: #1C1C1C, stroke: #303030
// Tile active (selected): @color/activeSelectionRed = #D61A21
// Title unselected: #A5B6C3  |  Sub-label: #898989  |  Value: white

private val MenuBg       = Color(0xFF151515)
private val TileNormal   = Color(0xFF1C1C1C)
private val TileActive   = Color(0xFFD61A21)   // activeSelectionRed
private val TitleDimmed  = Color(0xFFA5B6C3)   // unSelectedTitle
private val SubDimmed    = Color(0xFF898989)    // unSelected
private val ValueWhite   = Color.White
private val GreenText    = Color(0xFF4CAF50)

/**
 * Top-level HMI menu screen ([ScreenMode.Menu]).
 *
 * Seven tiles: MyF77 | Battery | Settings | Media | Controls | TPMS | Navigate.
 * Layout adapts to Compact / Standard / Wide via [BoxWithConstraints].
 *
 * Inputs:
 * - Touch: [onTileTap] — tap navigates directly to sub-screen (mirrors Fragment behaviour)
 * - Handlebar: handled in [ClusterViewModel.handleMenuButton]; selection change
 *   reflected by [uiState.selectedPosition].
 *
 * @param uiState       Live menu display state from ClusterViewModel.
 * @param onTileTap     Called with the tapped tile's [MenuPosition] — MainActivity
 *                      navigates to the corresponding Fragment sub-screen.
 * @param onDismiss     Called when user swipes or taps outside tiles — returns to dashboard.
 */
@Composable
fun MenuScreen(
    uiState: MenuUiState,
    onTileTap: (MenuPosition) -> Unit,
    onDismiss: () -> Unit,
) {
    MenuContent(uiState = uiState, onTileTap = onTileTap, onDismiss = onDismiss)
}

@Composable
private fun MenuContent(
    uiState: MenuUiState,
    onTileTap: (MenuPosition) -> Unit,
    onDismiss: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MenuBg)
            .clickable(onClick = onDismiss),   // tap outside tiles → dismiss
    ) {
        val isWide  = maxWidth >= 800.dp
        val gap     = if (isWide) 12.dp  else 8.dp
        val pad     = if (isWide) 16.dp  else 10.dp
        val sel     = uiState.selectedPosition

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad),
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            // ── Row 1: MyF77 | Battery | Settings ─────────────────────────────
            Row(
                modifier = Modifier
                    .weight(0.38f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gap),
            ) {
                // MyF77 tile
                MenuTile(
                    selected = sel == MenuPosition.MyF77.ordinal,
                    modifier = Modifier.weight(0.49f).fillMaxHeight(),
                    onClick  = { onTileTap(MenuPosition.MyF77) },
                ) {
                    MyF77TileContent(isWide = isWide)
                }

                // Battery tile
                MenuTile(
                    selected = sel == MenuPosition.Battery.ordinal,
                    modifier = Modifier.weight(0.35f).fillMaxHeight(),
                    onClick  = { onTileTap(MenuPosition.Battery) },
                ) {
                    BatteryTileContent(
                        batterySoc   = uiState.batterySoc,
                        batteryLimit = uiState.batteryLimit,
                        isWide       = isWide,
                    )
                }

                // Settings tile (narrow icon-only strip)
                MenuTile(
                    selected = sel == MenuPosition.Setting.ordinal,
                    modifier = Modifier.weight(0.12f).fillMaxHeight(),
                    onClick  = { onTileTap(MenuPosition.Setting) },
                ) {
                    SettingsTileContent(isWide = isWide)
                }
            }

            // ── Row 2: Media | Controls | (TPMS + Navigate) ──────────────────
            Row(
                modifier = Modifier
                    .weight(0.62f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gap),
            ) {
                // Media tile
                MenuTile(
                    selected = sel == MenuPosition.Music.ordinal,
                    modifier = Modifier.weight(0.32f).fillMaxHeight(),
                    onClick  = { onTileTap(MenuPosition.Music) },
                ) {
                    MediaTileContent(isWide = isWide)
                }

                // Controls tile
                MenuTile(
                    selected = sel == MenuPosition.Controls.ordinal,
                    modifier = Modifier.weight(0.35f).fillMaxHeight(),
                    onClick  = { onTileTap(MenuPosition.Controls) },
                ) {
                    ControlsTileContent(
                        absLabel       = uiState.absLabel,
                        tractionLabel  = uiState.tractionLabel,
                        regenLabel     = uiState.regenLabel,
                        hillHoldEnabled = uiState.hillHoldEnabled,
                        isWide         = isWide,
                    )
                }

                // TPMS + Navigate stacked vertically
                Column(
                    modifier = Modifier
                        .weight(0.28f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(gap),
                ) {
                    MenuTile(
                        selected = sel == MenuPosition.Tpms.ordinal,
                        modifier = Modifier.weight(0.6f).fillMaxWidth(),
                        onClick  = { onTileTap(MenuPosition.Tpms) },
                    ) {
                        TpmsTileContent(isWide = isWide)
                    }
                    MenuTile(
                        selected = sel == MenuPosition.Navigate.ordinal,
                        modifier = Modifier.weight(0.4f).fillMaxWidth(),
                        onClick  = { onTileTap(MenuPosition.Navigate) },
                    ) {
                        NavigateTileContent(isWide = isWide)
                    }
                }
            }
        }
    }
}

// ── Tile container ────────────────────────────────────────────────────────────

@Composable
private fun MenuTile(
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) TileActive else TileNormal)
            .clickable(onClick = onClick)
            .padding(0.dp),  // padding inside is handled by each content composable
    ) {
        content()
    }
}

// ── Tile content composables ──────────────────────────────────────────────────

@Composable
private fun MyF77TileContent(isWide: Boolean) {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text     = "MY X-47",
            color    = ValueWhite,
            fontSize = if (isWide) 20.sp else 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp),
        )
        // Bike image anchored at bottom-start
        Image(
            painter = painterResource(R.drawable.image_x_47_menu),
            contentDescription = "X-47 bike",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 0.dp, bottom = 0.dp),
        )
    }
}

@Composable
private fun BatteryTileContent(
    batterySoc: Int,
    batteryLimit: Int,
    isWide: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text     = "BATTERY",
            color    = TitleDimmed,
            fontSize = if (isWide) 18.sp else 14.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.weight(1f))
        // SOC % + progress bar
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text       = batterySoc.toString(),
                color      = ValueWhite,
                fontSize   = if (isWide) 28.sp else 22.sp,
                fontWeight = FontWeight.Normal,
            )
            Text(
                text     = "%",
                color    = ValueWhite,
                fontSize = if (isWide) 18.sp else 14.sp,
                modifier = Modifier.padding(bottom = 2.dp, start = 2.dp),
            )
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (batterySoc / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isWide) 10.dp else 8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color    = ValueWhite,
            trackColor = Color(0xFF303030),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text     = "LIMIT ${batteryLimit}%",
            color    = SubDimmed,
            fontSize = if (isWide) 14.sp else 11.sp,
        )
    }
}

@Composable
private fun SettingsTileContent(isWide: Boolean) {
    Box(
        modifier          = Modifier.fillMaxSize(),
        contentAlignment  = Alignment.Center,
    ) {
        Image(
            painter           = painterResource(R.drawable.ic_settings_menu),
            contentDescription = "Settings",
            modifier          = Modifier.size(if (isWide) 40.dp else 28.dp),
        )
    }
}

@Composable
private fun MediaTileContent(isWide: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text       = "MEDIA",
            color      = TitleDimmed,
            fontSize   = if (isWide) 18.sp else 14.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(if (isWide) 12.dp else 8.dp))
        Text(text = "Now Playing", color = SubDimmed, fontSize = 13.sp)
        Text(
            text       = "SONG 01",
            color      = ValueWhite,
            fontSize   = if (isWide) 24.sp else 18.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.weight(1f))
        Text(text = "CONNECTED", color = GreenText, fontSize = 12.sp)
        Text(
            text     = "Bluetooth Device",
            color    = ValueWhite,
            fontSize = if (isWide) 16.sp else 12.sp,
        )
    }
}

@Composable
private fun ControlsTileContent(
    absLabel: String,
    tractionLabel: String,
    regenLabel: String,
    hillHoldEnabled: Boolean,
    isWide: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text       = "CONTROLS",
            color      = TitleDimmed,
            fontSize   = if (isWide) 18.sp else 14.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.weight(1f))
        // ABS + Traction row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            ControlStatColumn("ABS", absLabel, isWide)
            ControlStatColumn("TC", tractionLabel, isWide)
        }
        Spacer(Modifier.height(if (isWide) 8.dp else 4.dp))
        // Regen + HillHold row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            ControlStatColumn("REGEN", regenLabel, isWide)
            ControlStatColumn("HILL HOLD", if (hillHoldEnabled) "On" else "Off", isWide)
        }
    }
}

@Composable
private fun ControlStatColumn(label: String, value: String, isWide: Boolean) {
    Column {
        Text(text = label, color = SubDimmed, fontSize = if (isWide) 13.sp else 11.sp)
        Text(
            text       = value,
            color      = ValueWhite,
            fontSize   = if (isWide) 24.sp else 18.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun TpmsTileContent(isWide: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(
            text       = "TPMS",
            color      = TitleDimmed,
            fontSize   = if (isWide) 18.sp else 13.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TpmsPressureColumn("FRONT", "–– PSI", isWide)
            TpmsPressureColumn("REAR",  "–– PSI", isWide)
        }
    }
}

@Composable
private fun TpmsPressureColumn(label: String, value: String, isWide: Boolean) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(GreenText)
            )
            Spacer(Modifier.width(4.dp))
            Text(text = label, color = SubDimmed, fontSize = 11.sp)
        }
        Text(
            text     = value,
            color    = ValueWhite,
            fontSize = if (isWide) 22.sp else 16.sp,
        )
    }
}

@Composable
private fun NavigateTileContent(isWide: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(
            text       = "NAVIGATE",
            color      = TitleDimmed,
            fontSize   = if (isWide) 18.sp else 13.sp,
            fontWeight = FontWeight.Bold,
            modifier   = Modifier.align(Alignment.TopStart),
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(widthDp = 480,  heightDp = 320, name = "Compact")
@Preview(widthDp = 800,  heightDp = 480, name = "Standard")
@Preview(widthDp = 1024, heightDp = 600, name = "Wide")
@Composable
private fun MenuScreenPreview() {
    MenuContent(
        uiState   = MenuUiState(
            selectedPosition = MenuPosition.Battery.ordinal,
            batterySoc       = 72,
            batteryLimit     = 80,
            absLabel         = "Mono",
            tractionLabel    = "T2",
            regenLabel       = "R6",
            hillHoldEnabled  = true,
        ),
        onTileTap = {},
        onDismiss = {},
    )
}
