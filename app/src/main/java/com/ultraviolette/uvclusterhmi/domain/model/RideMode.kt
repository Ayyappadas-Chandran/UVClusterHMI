package com.ultraviolette.uvclusterhmi.domain.model

/** Current ride mode sent by the VCU (VehicleSnapshot.rideMode). */
enum class RideMode {
    Glide,      // rideMode == 1
    Combat,     // rideMode == 2
    Ballistic;  // rideMode == 3 (or any other value)

    companion object {
        fun fromRaw(raw: Int): RideMode = when (raw) {
            1    -> Glide
            2    -> Combat
            else -> Ballistic
        }
    }
}