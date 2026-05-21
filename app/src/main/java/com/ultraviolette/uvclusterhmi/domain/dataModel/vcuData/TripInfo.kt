package com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData

data class TripInfo(
    val distance: Float = 0f,
    val wattHour: Float = 0f,
    val tripDuration: Float = 0f,
    val averageSpeed: Float = 0f
)

data class TripMeterDisp(
    val trip: List<TripInfo> = listOf()
)
