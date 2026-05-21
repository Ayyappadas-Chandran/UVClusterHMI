package com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData

data class ImxAuxMsg(
    val chargeLimit: UInt = 0u,
    val lightFx: UInt = 0u,
    val sentryCtrl: UInt = 0u
)