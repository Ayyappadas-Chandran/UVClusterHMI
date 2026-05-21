package com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData

data class VcuInfoMsg(
    val apiVersion: ByteArray = byteArrayOf(),
    val msgSequence: UInt = 0u,
    val millis: UInt = 0u,
    val statusH: UInt =0u,
    val statusL: UInt = 0u,
    val vcuStatusH: UInt = 0u,
    val vcuStatusL: UInt = 0u,
    val roll: Float = 0f,
    val pitch: Float = 0f,
    val odometer: Float = 0f,
    val bmsId: DevUid = DevUid(),
    val throttlePercent: UByte = 0u,
    val mcAutotuneVcuStatus: UByte = 0u,
    val mcAutotuneMcuStatus: UByte = 0u,
    val swifCode: UByte = 0u,
    val speed: ByteArray = byteArrayOf(),
    val actualSpeed: ByteArray = byteArrayOf(),
    val distance: ByteArray = byteArrayOf(),
    val vehicleMetaData: VehicleMetaData = VehicleMetaData(),
    val miscInfo: ByteArray = byteArrayOf(),
    val whPerKm: Float = 0f,
    val whPerKmRegen: Float = 0f,
    val availableModes: UInt = 0u,
    val currentRideMode: UInt = 0u,
    val vehicleRangeType: UInt = 0u,
    val range: UShort = 0u,
    val rtc: ByteArray = byteArrayOf(),
    val bus: UByte = 0u
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VcuInfoMsg

        if (roll != other.roll) return false
        if (pitch != other.pitch) return false
        if (odometer != other.odometer) return false
        if (whPerKm != other.whPerKm) return false
        if (whPerKmRegen != other.whPerKmRegen) return false
        if (!apiVersion.contentEquals(other.apiVersion)) return false
        if (msgSequence != other.msgSequence) return false
        if (millis != other.millis) return false
        if (statusH != other.statusH) return false
        if (statusL != other.statusL) return false
        if (vcuStatusH != other.vcuStatusH) return false
        if (vcuStatusL != other.vcuStatusL) return false
        if (bmsId != other.bmsId) return false
        if (throttlePercent != other.throttlePercent) return false
        if (mcAutotuneVcuStatus != other.mcAutotuneVcuStatus) return false
        if (mcAutotuneMcuStatus != other.mcAutotuneMcuStatus) return false
        if (swifCode != other.swifCode) return false
        if (!speed.contentEquals(other.speed)) return false
        if (!actualSpeed.contentEquals(other.actualSpeed)) return false
        if (!distance.contentEquals(other.distance)) return false
        if (vehicleMetaData != other.vehicleMetaData) return false
        if (!miscInfo.contentEquals(other.miscInfo)) return false
        if (availableModes != other.availableModes) return false
        if (currentRideMode != other.currentRideMode) return false
        if (vehicleRangeType != other.vehicleRangeType) return false
        if (range != other.range) return false
        if (!rtc.contentEquals(other.rtc)) return false
        if (bus != other.bus) return false

        return true
    }

    override fun hashCode(): Int {
        var result = roll.hashCode()
        result = 31 * result + pitch.hashCode()
        result = 31 * result + odometer.hashCode()
        result = 31 * result + whPerKm.hashCode()
        result = 31 * result + whPerKmRegen.hashCode()
        result = 31 * result + apiVersion.contentHashCode()
        result = 31 * result + msgSequence.hashCode()
        result = 31 * result + millis.hashCode()
        result = 31 * result + statusH.hashCode()
        result = 31 * result + statusL.hashCode()
        result = 31 * result + vcuStatusH.hashCode()
        result = 31 * result + vcuStatusL.hashCode()
        result = 31 * result + bmsId.hashCode()
        result = 31 * result + throttlePercent.hashCode()
        result = 31 * result + mcAutotuneVcuStatus.hashCode()
        result = 31 * result + mcAutotuneMcuStatus.hashCode()
        result = 31 * result + swifCode.hashCode()
        result = 31 * result + speed.contentHashCode()
        result = 31 * result + actualSpeed.contentHashCode()
        result = 31 * result + distance.contentHashCode()
        result = 31 * result + vehicleMetaData.hashCode()
        result = 31 * result + miscInfo.contentHashCode()
        result = 31 * result + availableModes.hashCode()
        result = 31 * result + currentRideMode.hashCode()
        result = 31 * result + vehicleRangeType.hashCode()
        result = 31 * result + range.hashCode()
        result = 31 * result + rtc.contentHashCode()
        result = 31 * result + bus.hashCode()
        return result
    }
}

data class DevUid(
    val uidl: UInt = 0u,
    val uidml: UInt = 0u,
    val uidmh: UInt = 0u,
    val uidh: UInt = 0u
)

data class VehicleMetaData(
    val model: UByte = 0u,
    val vcuHw: UByte =0u,
    val batteryPackVariant: UByte = 0u,
    val mcuVariant: UByte = 0u,
    val motorType: UByte = 0u,
    val region: UByte = 0u,
    val reserved1: UByte = 0u,
    val reserved2: UByte = 0u,
    val reserved3: UByte = 0u,
    val reserved4: UByte = 0u,
    val reserved5: UByte = 0u,
    val reserved6: UByte = 0u,
)
