package com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData

data class ChargerCtxObc(
    val chargerLogBoundary: UInt = 0u,
    val obcStatus: ULong = 0UL,
    val temperature01: Float = 0f,
    val temperature02: Float = 0f,
    val temperature03: Float = 0f,
    val temperature04: Float = 0f,
    val ipAcRmsVoltage: Float = 0f,
    val ipAcRmsCurrent: Float = 0f,
    val fanFrequency: Float = 0f,
    val opFbVoltage: Float = 0f,
    val opFbCurrent: Float = 0f,
    val ipAcSignalFreq: Float = 0f,
    val dcFbVoltage: Float = 0f,
    val llcFreq: Float = 0f,
    val opRippleCurrent: Float = 0f,
    val acVoltageThd: Float = 0f,
    val chargerConnectionState: UInt = 0u,
    val chargerType: UInt = 0u,
    val chargerFwMajorNum: UInt = 0u,
    val chargerFwMinorNum: UInt = 0u,
    val chargerViReq: UInt = 0u,
    val chargerRangeValue: UInt = 0u
)