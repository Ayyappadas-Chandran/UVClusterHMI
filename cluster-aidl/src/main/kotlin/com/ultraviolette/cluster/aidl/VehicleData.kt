package com.ultraviolette.cluster.aidl

import android.os.Parcel
import android.os.Parcelable

data class VehicleData(
    val speed: Float = 0f,
    val soc: Int = 0,
    val regenLevel: Int = 0,
    val rideModeRaw: Int = 0,
    val indicator: Int = 0,
    val absMode: Int = 0,
    val hillHoldState: Int = 0,
    val hillHoldIcon: Int = 0,
    val highBeamTelltale: Int = 0,
    val hazardLightTelltale: Int = 0,
    val motorArmDisarmTelltale: Int = 0,
    val lockdown: Int = 0,
    val sleepWake: Int = 0,
    val mtcMode: Int = 0,
    val chargerEvt: Int = 0,
    val cruise: Int = 0,
    val absModeStatus: Int = 0
) : Parcelable {

    constructor(parcel: Parcel) : this(
        speed = parcel.readFloat(),
        soc = parcel.readInt(),
        regenLevel = parcel.readInt(),
        rideModeRaw = parcel.readInt(),
        indicator = parcel.readInt(),
        absMode = parcel.readInt(),
        hillHoldState = parcel.readInt(),
        hillHoldIcon = parcel.readInt(),
        highBeamTelltale = parcel.readInt(),
        hazardLightTelltale = parcel.readInt(),
        motorArmDisarmTelltale = parcel.readInt(),
        lockdown = parcel.readInt(),
        sleepWake = parcel.readInt(),
        mtcMode = parcel.readInt(),
        chargerEvt = parcel.readInt(),
        cruise = parcel.readInt(),
        absModeStatus = parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloat(speed)
        parcel.writeInt(soc)
        parcel.writeInt(regenLevel)
        parcel.writeInt(rideModeRaw)
        parcel.writeInt(indicator)
        parcel.writeInt(absMode)
        parcel.writeInt(hillHoldState)
        parcel.writeInt(hillHoldIcon)
        parcel.writeInt(highBeamTelltale)
        parcel.writeInt(hazardLightTelltale)
        parcel.writeInt(motorArmDisarmTelltale)
        parcel.writeInt(lockdown)
        parcel.writeInt(sleepWake)
        parcel.writeInt(mtcMode)
        parcel.writeInt(chargerEvt)
        parcel.writeInt(cruise)
        parcel.writeInt(absModeStatus)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<VehicleData> {
        override fun createFromParcel(parcel: Parcel): VehicleData = VehicleData(parcel)
        override fun newArray(size: Int): Array<VehicleData?> = arrayOfNulls(size)
    }
}
