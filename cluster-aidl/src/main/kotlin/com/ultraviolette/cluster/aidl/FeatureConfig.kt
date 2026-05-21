package com.ultraviolette.cluster.aidl

import android.os.Parcel
import android.os.Parcelable

data class FeatureConfig(
    val btCalling: Boolean = true,
    val btMusic: Boolean = true,
    val navigation: Boolean = true,
    val tpmsAlerts: Boolean = true,
    val rearCamera: Boolean = true,
    val wifiOta: Boolean = true,
    /** Bitmask: RIDE_MODE_ECO | RIDE_MODE_SPORT | RIDE_MODE_HYPER */
    val allowedRideModes: Int = RIDE_MODES_ALL,
    /** Monotonically increasing version from cloud; used to drop stale updates. */
    val configVersion: Long = 0L,
    val configTimestamp: Long = 0L
) : Parcelable {

    constructor(parcel: Parcel) : this(
        btCalling        = parcel.readByte() != 0.toByte(),
        btMusic          = parcel.readByte() != 0.toByte(),
        navigation       = parcel.readByte() != 0.toByte(),
        tpmsAlerts       = parcel.readByte() != 0.toByte(),
        rearCamera       = parcel.readByte() != 0.toByte(),
        wifiOta          = parcel.readByte() != 0.toByte(),
        allowedRideModes = parcel.readInt(),
        configVersion    = parcel.readLong(),
        configTimestamp  = parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (btCalling) 1 else 0)
        parcel.writeByte(if (btMusic) 1 else 0)
        parcel.writeByte(if (navigation) 1 else 0)
        parcel.writeByte(if (tpmsAlerts) 1 else 0)
        parcel.writeByte(if (rearCamera) 1 else 0)
        parcel.writeByte(if (wifiOta) 1 else 0)
        parcel.writeInt(allowedRideModes)
        parcel.writeLong(configVersion)
        parcel.writeLong(configTimestamp)
    }

    override fun describeContents(): Int = 0

    fun isRideModeAllowed(modeBit: Int): Boolean = (allowedRideModes and modeBit) != 0

    companion object CREATOR : Parcelable.Creator<FeatureConfig> {
        const val RIDE_MODE_ECO   = 0b001
        const val RIDE_MODE_SPORT = 0b010
        const val RIDE_MODE_HYPER = 0b100
        const val RIDE_MODES_ALL  = 0b111

        override fun createFromParcel(parcel: Parcel): FeatureConfig = FeatureConfig(parcel)
        override fun newArray(size: Int): Array<FeatureConfig?> = arrayOfNulls(size)
    }
}
