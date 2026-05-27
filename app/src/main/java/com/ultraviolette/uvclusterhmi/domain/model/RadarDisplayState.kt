package com.ultraviolette.uvclusterhmi.domain.model

/** Visual state for one side of the radar indicator on the dashboard. */
enum class RadarDisplayState {
    Off,
    Warn,
    Alert;

    companion object {
        /**
         * Maps [VehicleSnapshot.radarIndicator] to the toolbar / dashboard state.
         *
         * radarIndicator encoding:
         *   1 = radar off / inactive
         *   2 = radar active (warning level)
         *   3 = radar malfunction
         */
        fun fromRaw(raw: Int): RadarDisplayState = when (raw) {
            2    -> Warn
            3    -> Warn   // malfunction shown as warn in dashboard circles; toolbar shows separate icon
            else -> Off
        }
    }
}