package com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData

data class ChargeCtx(
    val chargerBoundary: UInt = 0u,
    val connectionState: UInt = 0u,
    val chargerStatus: UInt = 0u,
    val chargerType: UInt = 0u,
    val chargerFwMajorNum: UInt = 0u,
    val chargerFwMinorNum: UInt = 0u,
    val chargerRemainingTime: UInt = 0u,
    val chargerRangeValue: UInt = 0u,
    val acInputVoltage: Float = 0f,
    val chargeTemp: Float = 0f,
    val acInputCurrent: Float = 0f,
    val pfcFetTEmp: Float = 0f,
    val vofbVolts: Float = 0f,
    val iofbVolts: Float = 0f,
    val chargeViRequest: UInt = 0u
)