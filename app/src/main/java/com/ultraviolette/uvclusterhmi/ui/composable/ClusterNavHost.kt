package com.ultraviolette.uvclusterhmi.ui.composable

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import com.ultraviolette.uvclusterhmi.domain.model.ClusterUiState
import com.ultraviolette.uvclusterhmi.domain.model.MenuPosition
import com.ultraviolette.uvclusterhmi.domain.model.ScreenMode
import com.ultraviolette.uvclusterhmi.ui.composable.overlay.NotificationScreen
import com.ultraviolette.uvclusterhmi.ui.composable.overlay.SideStandNotification
import com.ultraviolette.uvclusterhmi.ui.composable.overlay.ThermalRunawayNotification
import com.ultraviolette.uvclusterhmi.ui.composable.overlay.incomingCallNotification
import com.ultraviolette.uvclusterhmi.ui.composable.screen.ChargingScreen
import com.ultraviolette.uvclusterhmi.ui.composable.screen.HoverModeScreen
import com.ultraviolette.uvclusterhmi.ui.composable.screen.LockdownScreen
import com.ultraviolette.uvclusterhmi.ui.composable.screen.MenuScreen
import com.ultraviolette.uvclusterhmi.ui.composable.screen.TpmsAlertScreen

/**
 * Top-level Compose screen router (Rule 7).
 *
 * Receives the fully-derived [uiState] from ClusterViewModel and dispatches to the
 * correct composable based on [ClusterUiState.Active.screenMode].
 *
 * **Notification screens** (ThermalRunaway, SideStandAlert, IncomingCall) are all
 * rendered by the single [NotificationScreen] template — no individual screen files.
 * Each alert's visual identity is defined as a [NotificationConfig] in
 * `overlay/NotificationConfig.kt`.
 *
 * Screens not yet migrated to Compose return `Unit` so the Fragment container beneath
 * remains fully visible and interactive (touch events pass through the empty ComposeView).
 *
 * @param onMenuTileTap  Called when the user taps a tile in the Compose Menu; caller
 *                       should call [ClusterViewModel.onMenuTileTapped] and then navigate
 *                       the Fragment nav controller to the appropriate sub-screen.
 * @param onMenuDismiss  Called when the user taps outside the tiles or presses Back;
 *                       caller should call [ClusterViewModel.dismissMenu].
 *
 * Migration status:
 *  ✅ SplashScreen       — handled by caller before this composable is shown
 *  ✅ ThermalRunaway     — [NotificationScreen] via [ThermalRunawayNotification]
 *  ✅ Lockdown           — [LockdownScreen]
 *  ✅ HoverMode          — [HoverModeScreen]
 *  ✅ SideStandAlert     — [NotificationScreen] via [SideStandNotification]
 *  ✅ TpmsAlert          — [TpmsAlertScreen]
 *  ✅ IncomingCall       — [NotificationScreen] via [incomingCallNotification]
 *  ✅ Charging           — [ChargingScreen]
 *  ✅ Menu               — [MenuScreen]
 *  🔲 RearCamera         — G9: CameraPreviewFragment still active; VCU trigger not yet available
 *  🔲 NavigationActive   — G9: not yet available from VCU
 *  🔲 Riding (Dashboard) — pending (last, most complex)
 */
@Composable
fun ClusterNavHost(
    uiState: ClusterUiState.Active,
    onMenuTileTap: (MenuPosition) -> Unit = {},
    onMenuDismiss: () -> Unit = {},
) {
    // Only screens that have been migrated to Compose need content here.
    // The `else ->` branch intentionally produces nothing — the XML Fragment
    // nav beneath is still responsible for those screens.
    AnimatedContent(
        targetState = uiState.screenMode,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "ClusterNavHost",
    ) { mode ->
        when (mode) {
            // ── Notification template screens ─────────────────────────────────
            // All share one composable; visual identity defined in NotificationConfig.kt.
            is ScreenMode.ThermalRunaway ->
                NotificationScreen(config = ThermalRunawayNotification)

            is ScreenMode.SideStandAlert ->
                NotificationScreen(config = SideStandNotification)

            is ScreenMode.IncomingCall ->
                NotificationScreen(config = incomingCallNotification(mode.callerName))

            // ── Custom-layout screens ─────────────────────────────────────────
            is ScreenMode.Lockdown ->
                LockdownScreen(mode.isEnteringLockdown)

            is ScreenMode.HoverMode ->
                HoverModeScreen(uiState)

            is ScreenMode.TpmsAlert ->
                TpmsAlertScreen()

            is ScreenMode.Charging ->
                ChargingScreen(state = uiState.charging)

            is ScreenMode.Menu ->
                MenuScreen(
                    uiState   = uiState.menu,
                    onTileTap = onMenuTileTap,
                    onDismiss = onMenuDismiss,
                )

            // ── Pending migration ─────────────────────────────────────────────
            // G9: RearCamera / NavigationActive — VCU trigger not yet available;
            // Fragment nav (CameraPreviewFragment / MapFragment) handles these.
            is ScreenMode.RearCamera       -> Unit
            is ScreenMode.NavigationActive -> Unit

            // All other modes: Fragment nav still handles these — return nothing.
            else -> Unit
        }
    }
}
