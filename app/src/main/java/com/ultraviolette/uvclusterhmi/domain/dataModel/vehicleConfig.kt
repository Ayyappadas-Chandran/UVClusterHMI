package com.ultraviolette.uvclusterhmi.domain.dataModel

import com.google.gson.annotations.SerializedName

data class Vehicles(
    val vehicles: Map<String, VehicleConfig>
)

data class VehicleConfig(
    @SerializedName("power_save_threshold")
    val powerSaveThreshold: Int,

    @SerializedName("Meter unit")
    val meterUnit: String,

    @SerializedName("Power unit")
    val powerUnit: String,

    val limits: Limits
)

data class Limits(
    val speed: RangeLimit,
    val odo: RangeLimit,
    val range: RangeLimit,

    @SerializedName("wh_per_km")
    val whPerKm: RangeLimit,

    val ride: RangeLimit,
    val soc: RangeLimit,
    val eff: RangeLimit
)

data class RangeLimit(
    val min: Int,
    val max: Int
)
