package com.ultraviolette.uvclusterhmi.domain.model

import androidx.compose.runtime.Stable

/**
 * Display-brightness command from the VCU via PROP_ID_DISPLAY_BRIGHTNESS.
 * Distinct from user preferences — this is a vehicle signal.
 */
@Stable
data class BrightnessUiState(
    /** Brightness level 0–100 as reported by VCU. */
    val level: Int     = 0,
    /** True when VCU has auto-brightness active. */
    val isAuto: Boolean = false,
)

sealed interface ClusterUiState {

    /** Initial state while ClusterDataBus service is connecting. */
    data object Loading : ClusterUiState

    /**
     * Live operating state.
     *
     * All sub-states are derived from raw AIDL payloads inside [ClusterViewModel]
     * so composables receive only semantically-meaningful, @Stable types.
     */
    data class Active(
        val dashboard: DashboardUiState   = DashboardUiState(),
        val toolbar: ToolbarUiState       = ToolbarUiState(),
        val prefs: ClusterPrefsState      = ClusterPrefsState(),
        val screenMode: ScreenMode        = ScreenMode.Riding,
        /** State for the HMI top-level menu (valid when screenMode == ScreenMode.Menu). */
        val menu: MenuUiState             = MenuUiState(),
        /** True once the splash video sequence has completed. */
        val splashDone: Boolean           = false,
        /** VCU brightness command — observed by MainActivity to set display brightness. */
        val brightness: BrightnessUiState = BrightnessUiState(),
        /** Live trip meter data from VCU TripMeterDisp blob. */
        val trips: TripsVehicleUiState    = TripsVehicleUiState(),
        /** Charging screen state (valid when screenMode == ScreenMode.Charging). */
        val charging: ChargingUiState     = ChargingUiState(),
    ) : ClusterUiState

    data class Error(val message: String) : ClusterUiState
}
