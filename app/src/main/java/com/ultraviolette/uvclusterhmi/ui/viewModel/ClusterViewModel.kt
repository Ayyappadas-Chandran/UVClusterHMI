package com.ultraviolette.uvclusterhmi.ui.viewModel

import android.util.Log
import android.view.KeyEvent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ultraviolette.cluster.aidl.BtState
import com.ultraviolette.cluster.aidl.VehicleSnapshot
import com.ultraviolette.cluster.aidl.WifiState
import com.ultraviolette.uvclusterhmi.ClusterApplication
import com.ultraviolette.uvclusterhmi.data.repository.ClusterRepository
import com.ultraviolette.uvclusterhmi.domain.manager.PreferenceManager
import com.ultraviolette.uvclusterhmi.domain.model.BrightnessUiState
import com.ultraviolette.uvclusterhmi.domain.model.ChargingUiState
import com.ultraviolette.uvclusterhmi.domain.model.ClusterPrefsState
import com.ultraviolette.uvclusterhmi.domain.model.ClusterUiState
import com.ultraviolette.uvclusterhmi.domain.model.DashboardUiState
import com.ultraviolette.uvclusterhmi.domain.model.DriveUiState
import com.ultraviolette.uvclusterhmi.domain.model.MenuPosition
import com.ultraviolette.uvclusterhmi.domain.model.MenuUiState
import com.ultraviolette.uvclusterhmi.domain.model.MotorUiState
import com.ultraviolette.uvclusterhmi.domain.model.OdometerUiState
import com.ultraviolette.uvclusterhmi.domain.model.RadarDisplayState
import com.ultraviolette.uvclusterhmi.domain.model.RadarUiState
import com.ultraviolette.uvclusterhmi.domain.model.RegenUiState
import com.ultraviolette.uvclusterhmi.domain.model.RideMode
import com.ultraviolette.uvclusterhmi.domain.model.ScreenMode
import com.ultraviolette.uvclusterhmi.domain.model.ToolbarUiState
import com.ultraviolette.uvclusterhmi.domain.model.TripEntryUiState
import com.ultraviolette.uvclusterhmi.domain.model.TripsVehicleUiState
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlin.math.floor
import kotlin.math.roundToInt

class ClusterViewModel(
    repository: ClusterRepository,
    private val prefs: PreferenceManager,
) : ViewModel() {

    // ── Local mutable signals ─────────────────────────────────────────────────

    private val _menuVisible    = MutableStateFlow(false)
    private val _splashDone     = MutableStateFlow(false)
    /** Updated by MainActivity from TelephonyManager — not a vehicle signal. */
    private val _cellularSignal = MutableStateFlow(0)

    // ── Menu navigation state ─────────────────────────────────────────────────
    /** Currently highlighted menu tile ordinal (0 = MyF77). */
    private val _menuPosition   = MutableStateFlow(0)

    /**
     * One-shot event: user pressed Enter on a menu tile.
     * Collected by MainActivity to navigate to the corresponding Fragment.
     */
    private val _menuNavEvent   = MutableSharedFlow<MenuPosition>(
        extraBufferCapacity = 1,
        onBufferOverflow    = BufferOverflow.DROP_OLDEST,
    )
    val menuNavEvent: SharedFlow<MenuPosition> = _menuNavEvent

    enum class IndicatorMode { Off, Left, Right, Hazard }

    // ── Menu button-navigation maps (mirrors MenuFragment button maps) ─────────
    // Each map: from current MenuPosition, which button moves to which tile.
    private val menuNavMaps: Map<MenuPosition, Map<ButtonNavigation, MenuPosition>> = mapOf(
        MenuPosition.MyF77    to mapOf(
            ButtonNavigation.Right  to MenuPosition.Battery,
            ButtonNavigation.Bottom to MenuPosition.Music,
        ),
        MenuPosition.Battery  to mapOf(
            ButtonNavigation.Right  to MenuPosition.Setting,
            ButtonNavigation.Bottom to MenuPosition.Controls,
            ButtonNavigation.Left   to MenuPosition.MyF77,
        ),
        MenuPosition.Setting  to mapOf(
            ButtonNavigation.Left   to MenuPosition.Battery,
            ButtonNavigation.Bottom to MenuPosition.Tpms,
        ),
        MenuPosition.Music    to mapOf(
            ButtonNavigation.Right  to MenuPosition.Controls,
            ButtonNavigation.Top    to MenuPosition.MyF77,
        ),
        MenuPosition.Controls to mapOf(
            ButtonNavigation.Right  to MenuPosition.Tpms,
            ButtonNavigation.Left   to MenuPosition.Music,
            ButtonNavigation.Top    to MenuPosition.Battery,
        ),
        MenuPosition.Tpms     to mapOf(
            ButtonNavigation.Top    to MenuPosition.Setting,
            ButtonNavigation.Bottom to MenuPosition.Navigate,
            ButtonNavigation.Left   to MenuPosition.Controls,
        ),
        MenuPosition.Navigate to mapOf(
            ButtonNavigation.Left   to MenuPosition.Controls,
            ButtonNavigation.Top    to MenuPosition.Tpms,
        ),
    )

    // ── Intermediate combine: bundle raw AIDL flows into one object ───────────

    private data class RawSignals(
        val snapshot: VehicleSnapshot = VehicleSnapshot(),
        val btState: BtState          = BtState(),
        val wifiState: WifiState      = WifiState(),
    )

    private val rawSignals = combine(
        repository.vehicleSnapshot,
        repository.btState,
        repository.wifiState,
    ) { vs, bt, wifi -> RawSignals(vs, bt, wifi) }

    // ── Main UI state ─────────────────────────────────────────────────────────

    val uiState: StateFlow<ClusterUiState> = combine(
        rawSignals,
        _menuVisible,
        _splashDone,
        _cellularSignal,
        _menuPosition,
    ) { raw, menuVisible, splashDone, cellularSignal, menuPosition ->
        buildActiveState(raw, menuVisible, splashDone, cellularSignal, menuPosition)
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = ClusterUiState.Loading,
    )

    // ── Public actions ────────────────────────────────────────────────────────

    /** Called by SplashScreen once both intro + update videos finish. */
    fun onSplashComplete() {
        prefs.saveUpdate(true)
        _splashDone.value = true
    }

    /** Called by MainActivity when TelephonyManager reports a signal-level change. */
    fun updateCellularSignal(level: Int) {
        _cellularSignal.value = level
    }

    /** Handlebar button dispatcher — called from MainActivity.dispatchKeyEvent. */
    fun handleHandlebarButton(keyCode: Int): Boolean {
        // While menu is visible, directional/Enter/Back buttons go to menu navigation.
        if (_menuVisible.value) {
            val btn = keyCodeToButton(keyCode)
            if (btn != null) return handleMenuButton(btn)
        }
        return when (keyCode) {
            KeyEvent.KEYCODE_MENU -> { _menuVisible.value = !_menuVisible.value; true }
            KeyEvent.KEYCODE_BACK -> {
                if (_menuVisible.value) { _menuVisible.value = false; true } else false
            }
            else -> false
        }
    }

    private fun keyCodeToButton(keyCode: Int): ButtonNavigation? = when (keyCode) {
        KeyEvent.KEYCODE_DPAD_LEFT  -> ButtonNavigation.Left
        KeyEvent.KEYCODE_DPAD_RIGHT -> ButtonNavigation.Right
        KeyEvent.KEYCODE_DPAD_UP    -> ButtonNavigation.Top
        KeyEvent.KEYCODE_DPAD_DOWN  -> ButtonNavigation.Bottom
        KeyEvent.KEYCODE_ENTER,
        KeyEvent.KEYCODE_DPAD_CENTER -> ButtonNavigation.Enter
        KeyEvent.KEYCODE_BACK       -> ButtonNavigation.Back
        KeyEvent.KEYCODE_MENU       -> null  // toggle menu, not a direction
        else                        -> null
    }

    fun dismissMenu() {
        _menuVisible.value = false
        _menuPosition.value = 0  // reset selection to MyF77 on close
    }

    /**
     * Handle a directional or Enter button press while the menu is on screen.
     * Called from [ClusterNavHost] via the `onMenuButton` lambda passed down to [MenuScreen].
     *
     * Returns true if the button was consumed (navigating within menu or entering a sub-screen).
     */
    fun handleMenuButton(button: ButtonNavigation): Boolean {
        val pos   = _menuPosition.value
        val current = MenuPosition.values().getOrElse(pos) { MenuPosition.MyF77 }

        if (button == ButtonNavigation.Back) {
            dismissMenu()
            return true
        }

        if (button == ButtonNavigation.Enter) {
            // Navigate tile has no sub-screen yet (simulated)
            if (current != MenuPosition.Navigate) {
                _menuVisible.value = false
                _menuNavEvent.tryEmit(current)
            }
            return true
        }

        val nextPos = menuNavMaps[current]?.get(button)
        if (nextPos != null) {
            _menuPosition.value = nextPos.ordinal
        }
        return nextPos != null
    }

    /** Called by MenuScreen touch handler when a tile is tapped directly. */
    fun onMenuTileTapped(position: MenuPosition) {
        _menuPosition.value = position.ordinal
        if (position != MenuPosition.Navigate) {
            _menuVisible.value = false
            _menuNavEvent.tryEmit(position)
        }
    }

    // ── State builder ─────────────────────────────────────────────────────────

    private fun buildActiveState(
        raw: RawSignals,
        menuVisible: Boolean,
        splashDone: Boolean,
        cellularSignal: Int,
        menuPosition: Int,
    ): ClusterUiState.Active {

        val snap = raw.snapshot
        val bt   = raw.btState
        val wifi = raw.wifiState

        val rideMode     = RideMode.fromRaw(snap.rideMode)
        val isArmed      = snap.motorArmed == 1
        // Use speedKph (Float, from PROP_ID_VEHICLE_VALUE event, ~50ms latency) as the
        // authoritative speed. vehicleSpeed (9-bit Int from the blob) is no longer written
        // by CarPropertyService — it will always be 0 after the new architecture is live.
        val speedKmh     = snap.speedKph.roundToInt()
        val regenRaw     = snap.regenLevel.coerceIn(0, 9)
        val regenUnavail = snap.regenUnavailable == 1
        val distUnit     = prefs.distanceUnit
        val isMiles      = distUnit == "miles"

        // ── Motor ─────────────────────────────────────────────────────────────
        // motorPower: raw watts from PROP_ID_VEHICLE_VALUE[1]; negative = regen.
        // Divide by 1000 to get kW, then normalise to 0–1 for the 10 kW power bar.
        val scaledPowerKw      = snap.motorPower / 1000f
        val powerBarProgress   = (kotlin.math.abs(scaledPowerKw) / 10f).coerceIn(0f, 1f)
        val motor = MotorUiState(
            isArmed          = isArmed,
            showArmedIcon    = isArmed && speedKmh == 0,
            speedDisplay     = when {
                !isArmed && speedKmh == 0 -> "---"
                isArmed  && speedKmh == 0 -> "000"
                else -> String.format("%03d",
                    if (isMiles) (speedKmh * 0.621371).roundToInt() else speedKmh)
            },
            speedKmh         = speedKmh,
            motorPower       = snap.motorPower,
            powerBarProgress = powerBarProgress,
        )

        // ── Radar ─────────────────────────────────────────────────────────────
        // Use VcuMiscInfo-derived per-side BSM state (more accurate than the
        // single 2-bit radarIndicator from TellTales).
        val radar = RadarUiState(
            leftState = when {
                snap.radarLeftAlert > 0 -> RadarDisplayState.Alert
                snap.radarLeftWarn  > 0 -> RadarDisplayState.Warn
                else                    -> RadarDisplayState.Off
            },
            rightState = when {
                snap.radarRightAlert > 0 -> RadarDisplayState.Alert
                snap.radarRightWarn  > 0 -> RadarDisplayState.Warn
                else                     -> RadarDisplayState.Off
            },
            rcwActive = snap.rcwAlert > 0,
        )

        // ── Drive ─────────────────────────────────────────────────────────────
        val drive = DriveUiState(
            rideMode        = rideMode,
            isHoverMode     = snap.modeHover == 1,
            isSurgeMode     = snap.isBallisticPlus > 0,
            isBallisticPlus = snap.isBallisticPlus > 0,
        )

        // ── Regen ─────────────────────────────────────────────────────────────
        val is10Levels   = prefs.is10Levels
        val displayLevel = if (regenUnavail) 0 else regenRaw
        val regen = RegenUiState(
            level         = regenRaw,
            isUnavailable = regenUnavail,
            displayLevel  = displayLevel,
            is10Levels    = is10Levels,
        )

        // ── Odometer ──────────────────────────────────────────────────────────
        val odoKm    = snap.odometer.toInt()
        val rangeKm  = snap.range
        val whPerKm  = snap.whPerKm.toInt()
        val tripKm   = snap.tripDistance.toInt()
        val odo = OdometerUiState(
            odoDisplay       = if (isMiles) (odoKm   * 0.621371).roundToInt() else odoKm,
            rangeDisplay     = if (isMiles) (rangeKm * 0.621371).roundToInt() else rangeKm,
            whPerUnitDisplay = if (isMiles) (whPerKm / 0.621371).roundToInt() else whPerKm,
            efficiencyLevel  = computeEfficiency(snap.whPerKm),
            unit             = distUnit,
            tripDisplay      = if (isMiles) (tripKm  * 0.621371).roundToInt() else tripKm,
        )

        // ── Dashboard ─────────────────────────────────────────────────────────
        val dashboard = DashboardUiState(
            motor         = motor,
            radar         = radar,
            drive         = drive,
            regen         = regen,
            odo           = odo,
            alertsEnabled = prefs.isConsoleAlertsOn,
            rollAngle     = snap.rollAngle,
        )

        // ── Toolbar ───────────────────────────────────────────────────────────
        val toolbar = ToolbarUiState(
            batterySoc = snap.batterySoc,
            btEnabled = bt.isEnabled,
            wifiEnabled = wifi.isEnabled,
            wifiSignalLevel = wifi.signalLevel,
            cellularSignalLevel = cellularSignal,
            leftIndicatorOn = snap.indicator == 2,
            rightIndicatorOn = snap.indicator == 1,
            hazardActive = snap.hazardLamps == 1,
            regenLevel = if (regenUnavail) 0 else regenRaw,
            regenUnavailable = regenUnavail,
            rideMode = rideMode,
            highBeam = snap.highBeam == 1,
            hillHoldState = snap.hillHold,
            hillHoldEnabled = prefs.isHillHold,
            absMode = snap.absMode,
            absWarning = snap.absWarningLamp,
            mtcState = snap.mtcState,
            mtcMode = snap.mtcMode,
            motorArmed = isArmed,
            motorTempIcon = snap.motorTempIcon,
            batteryError = snap.batteryError == 1,
            batteryOverTemp = snap.batteryOverTemp == 1,
            criticalMalfunction = snap.criticalMalfunction == 1,
            milState = snap.milState,
            milIcon = snap.milIcon,
            otaPending = snap.otaPending == 1,
            radarState = snap.radarIndicator,
            cruiseStandby = snap.cruiseStandby > 0,
            cruiseActive = snap.cruiseActive  > 0,
            cruiseError = snap.cruiseError   > 0,
            cruiseOff = snap.cruiseOff     > 0,
            isHoverMode = snap.modeHover == 1,
            indicatorMode = when {
                snap.hazardLamps == 1 -> IndicatorMode.Hazard
                snap.indicatorLeft  == 1 -> IndicatorMode.Left
                snap.indicatorRight == 1 -> IndicatorMode.Right
                else -> IndicatorMode.Off
            },
            chargerPlugged = snap.charger == 1 || snap.charger == 2,
            chargerState   = snap.charger,
        )


        // ── Prefs ─────────────────────────────────────────────────────────────
        val clusterPrefs = ClusterPrefsState(
            mode            = prefs.mode,
            distanceUnit    = distUnit,
            is10LevelsRegen = is10Levels,
            consoleAlertsOn = prefs.isConsoleAlertsOn,
            radarOn         = prefs.isRadarOn,
            is12HourFormat  = prefs.isNormalTimeFormat,
            hillHoldEnabled = prefs.isHillHold,
        )

        // ── Menu ──────────────────────────────────────────────────────────────
        val absLabel = when {
            snap.absWarningLamp != 0 -> "Off"
            snap.absMode == 1        -> "Mono"
            else                     -> "Dual"
        }
        val tractionLabel = when (snap.mtcMode) {
            0x02 -> "T1"
            0x03 -> "T2"
            0x04 -> "T3"
            else -> "Off"
        }
        val menu = MenuUiState(
            selectedPosition = menuPosition,
            batterySoc       = snap.batterySoc,
            batteryLimit     = prefs.batteryLimit,
            absLabel         = absLabel,
            tractionLabel    = tractionLabel,
            regenLabel       = "R${prefs.regenValue}",
            hillHoldEnabled  = prefs.isHillHold,
        )

        // ── Trips ─────────────────────────────────────────────────────────────
        val trips = TripsVehicleUiState(
            trip1 = buildTripEntry(snap.trip1Distance, snap.trip1Duration, snap.trip1AvgSpeed, isMiles),
            trip2 = buildTripEntry(snap.trip2Distance, snap.trip2Duration, snap.trip2AvgSpeed, isMiles),
            trip3 = buildTripEntry(snap.trip3Distance, snap.trip3Duration, snap.trip3AvgSpeed, isMiles),
            unit  = distUnit,
        )

        // ── Brightness (VCU command to set display brightness) ────────────────
        val brightness = BrightnessUiState(
            level  = snap.brightnessLevel,
            isAuto = snap.brightnessAuto,
        )

        // ── Charging ──────────────────────────────────────────────────────────
        // charger field (TellTales bits 14-15): 0 = off, 1 = AC slow, 2 = DC fast.
        // isFastCharging = charger == 2 (DC fast charge).
        // keyOff = snap.keyOff (VcuInfoMsg vcuStatusL bit 7): key has been turned off
        //   → shows the active SoC ring + range + time-to-full dashboard.
        //   → false = key is still on → shows "TURN OFF KEY / UNPLUG CHARGER" instruction.
        val chargerPlugged = snap.charger == 1 || snap.charger == 2
        val isFastCharging = snap.charger == 2
        val chargingRangeDisplay = if (isMiles)
            "${(rangeKm * 0.621371).roundToInt()} miles"
        else
            "$rangeKm km"
        val charging = ChargingUiState(
            chargerPlugged    = chargerPlugged,
            keyOff            = snap.keyOff == 1,
            batterySoc        = snap.batterySoc,
            rangeDisplay      = chargingRangeDisplay,
            timeToFullSeconds = snap.chargerRemainingTime,
            rideMode          = rideMode,
            isFastCharging    = isFastCharging,
        )

        return ClusterUiState.Active(
            dashboard  = dashboard,
            toolbar    = toolbar,
            prefs      = clusterPrefs,
            screenMode = resolveScreenMode(snap, bt, menuVisible),
            menu       = menu,
            splashDone = splashDone,
            brightness = brightness,
            trips      = trips,
            charging   = charging,
        )
    }

    // ── Screen mode priority ──────────────────────────────────────────────────
    // ThermalRunaway > Lockdown > SideStandAlert > TpmsAlert > IncomingCall >
    // RearCamera > NavigationActive > Menu > Riding

    private fun resolveScreenMode(
        snap: VehicleSnapshot,
        bt: BtState,
        menuVisible: Boolean,
    ): ScreenMode {
        // Thermal runaway: highest priority — immediate safety
        if (snap.thermalRunway > 0)
            return ScreenMode.ThermalRunaway

        // Lockdown: 0x5f = entering (park the bike), 0x01 = vehicle locked
        if (snap.lockdown != 0)
            return ScreenMode.Lockdown(isEnteringLockdown = snap.lockdown == 0x5f)

        // HoverMode: battery critically low — takes over the riding screen
        if (snap.modeHover > 0)
            return ScreenMode.HoverMode

        if (snap.sideStandDeployed > 0)
            return ScreenMode.SideStandAlert

        if (snap.criticalMalfunction > 0)
            return ScreenMode.TpmsAlert

        if (bt.callState == 1)
            return ScreenMode.IncomingCall(callerName = bt.pairedDeviceName.ifBlank { "Unknown" })

        // Charging: charger plugged in takes over the riding/menu screen.
        // Priority is below safety and connectivity overlays but above navigation and menu.
        if (snap.charger == 1 || snap.charger == 2)
            return ScreenMode.Charging(isKeyOff = snap.keyOff == 1)

        // G9: RearCamera / NavigationActive not yet available
        if (menuVisible) return ScreenMode.Menu

        return ScreenMode.Riding
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Converts raw trip scalars from VehicleSnapshot into display-ready strings.
     * Returns all "---" defaults when the trip contains no data (distance and duration
     * are both zero — e.g. after a reset or before any trip is recorded).
     */
    private fun buildTripEntry(
        distanceKm: Float,
        durationSecs: Float,
        avgSpeedKmh: Float,
        isMiles: Boolean,
    ): TripEntryUiState {
        if (distanceKm <= 0f && durationSecs <= 0f) return TripEntryUiState()

        val displayDist = if (isMiles)
            "${(distanceKm * 0.621371).roundToInt()} miles"
        else
            "${distanceKm.toInt()} km"

        val totalSecs = durationSecs.toLong()
        val hours     = (totalSecs / 3600).toInt()
        val minutes   = ((totalSecs % 3600) / 60).toInt()

        val displayAvgSpeed = if (isMiles)
            "${(avgSpeedKmh * 0.621371).roundToInt()} mph"
        else
            "${avgSpeedKmh.toInt()} km/h"

        return TripEntryUiState(
            distanceDisplay = displayDist,
            durationDisplay = "$hours Hrs $minutes Mins",
            avgSpeedDisplay = displayAvgSpeed,
        )
    }

    /**
     * Maps Wh/km to an efficiency icon level 0–9.
     * 9 = best (low Wh/km), 1 = worst, 0 = no data.
     */
    private fun computeEfficiency(whPerKm: Float): Int = when {
        whPerKm <= 0f  -> 0
        whPerKm >= 90f -> 1
        whPerKm <= 35f -> 9
        else -> 8 - floor(((whPerKm - 36f) / 54f) * 7f).toInt()
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    class Factory(app: ClusterApplication) : ViewModelProvider.Factory {
        private val repository = app.clusterRepository
        private val prefs      = app.preferenceManager

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ClusterViewModel(repository, prefs) as T
    }
}