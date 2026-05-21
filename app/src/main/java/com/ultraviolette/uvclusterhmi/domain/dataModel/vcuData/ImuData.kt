package com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData

data class ImuData(
    val millis: UInt = 0u,
    val accelX: Float = 0f,
    val accelY: Float = 0f,
    val accelZ: Float = 0f,
    val gyroX: Float = 0f,
    val gyroY: Float = 0f,
    val gyroZ: Float = 0f,
    val magX: Float = 0f,
    val magY: Float = 0f,
    val magZ: Float = 0f,
    val quatW: Float = 0f,
    val quatX: Float = 0f,
    val quatY: Float = 0f,
    val quatZ: Float = 0f,
    val orientationX: Float = 0f,
    val orientationY: Float = 0f,
    val orientationZ: Float = 0f
)