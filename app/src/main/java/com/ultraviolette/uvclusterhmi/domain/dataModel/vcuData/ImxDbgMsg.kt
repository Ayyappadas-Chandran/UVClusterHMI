package com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData

data class ImxDbgMsg(
    val soc: UInt = 0u,
    val packVoltage: Float = 0f,
    val packCurrent: Float = 0f,
    val maxCellTemperature: Float = 0f,
    val maxCellVoltage: Float = 0f,
    val minCellVoltage: Float = 0f,
    val motorTemperature: Float = 0f,
    val motorHeatSinkTemperature: Float = 0f,
    val fetTemp: Float = 0f,
    val shaftRpm: Int = 0,
    val availableModes: UInt = 0u,
    val dischargeAh: Float = 0f,
    val chargeAh: Float = 0f,
    val dischargeEnergy: Float = 0f,
    val chargeEnergy: Float = 0f,
    val chargeTtf: UInt = 0u
)