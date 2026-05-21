package com.ultraviolette.cluster.aidl

import android.os.Parcel
import android.os.Parcelable

/** Full decoded snapshot — mirrors TellTales + key VcuInfoMsg fields */
data class VehicleSnapshot(
    val hillHold: Int = 0,
    val motorTempIcon: Int = 0,
    val absWarningLamp: Int = 0,
    val mtcMode: Int = 0,
    val mtcState: Int = 0,
    val charger: Int = 0,
    val rideMode: Int = 1,
    val modeHover: Int = 0,
    val milState: Int = 0,
    val absMode: Int = 0,
    val batteryError: Int = 0,
    val batteryOverTemp: Int = 0,
    val highBeam: Int = 0,
    val indicatorLeft: Int = 0,
    val indicatorRight: Int = 0,
    val milIcon: Int = 0,
    val motorArmed: Int = 0,
    val otaPending: Int = 0,
    val regenUnavailable: Int = 0,
    val vehicleSpeed: Int = 0,
    val batterySoc: Int = 0,
    val hazardLamps: Int = 0,
    val regenLevel: Int = 0,
    val criticalMalfunction: Int = 0,
    val radarIndicator: Int = 0,
    val motorNoArmCause: Int = 0,
    val availableRideModes: Int = 0,
    val thermalRunway: Int = 0,
    val odometer: Float = 0f,
    val range: Int = 0,
    val whPerKm: Float = 0f
) : Parcelable {

    constructor(parcel: Parcel) : this(
        hillHold = parcel.readInt(),
        motorTempIcon = parcel.readInt(),
        absWarningLamp = parcel.readInt(),
        mtcMode = parcel.readInt(),
        mtcState = parcel.readInt(),
        charger = parcel.readInt(),
        rideMode = parcel.readInt(),
        modeHover = parcel.readInt(),
        milState = parcel.readInt(),
        absMode = parcel.readInt(),
        batteryError = parcel.readInt(),
        batteryOverTemp = parcel.readInt(),
        highBeam = parcel.readInt(),
        indicatorLeft = parcel.readInt(),
        indicatorRight = parcel.readInt(),
        milIcon = parcel.readInt(),
        motorArmed = parcel.readInt(),
        otaPending = parcel.readInt(),
        regenUnavailable = parcel.readInt(),
        vehicleSpeed = parcel.readInt(),
        batterySoc = parcel.readInt(),
        hazardLamps = parcel.readInt(),
        regenLevel = parcel.readInt(),
        criticalMalfunction = parcel.readInt(),
        radarIndicator = parcel.readInt(),
        motorNoArmCause = parcel.readInt(),
        availableRideModes = parcel.readInt(),
        thermalRunway = parcel.readInt(),
        odometer = parcel.readFloat(),
        range = parcel.readInt(),
        whPerKm = parcel.readFloat()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(hillHold)
        parcel.writeInt(motorTempIcon)
        parcel.writeInt(absWarningLamp)
        parcel.writeInt(mtcMode)
        parcel.writeInt(mtcState)
        parcel.writeInt(charger)
        parcel.writeInt(rideMode)
        parcel.writeInt(modeHover)
        parcel.writeInt(milState)
        parcel.writeInt(absMode)
        parcel.writeInt(batteryError)
        parcel.writeInt(batteryOverTemp)
        parcel.writeInt(highBeam)
        parcel.writeInt(indicatorLeft)
        parcel.writeInt(indicatorRight)
        parcel.writeInt(milIcon)
        parcel.writeInt(motorArmed)
        parcel.writeInt(otaPending)
        parcel.writeInt(regenUnavailable)
        parcel.writeInt(vehicleSpeed)
        parcel.writeInt(batterySoc)
        parcel.writeInt(hazardLamps)
        parcel.writeInt(regenLevel)
        parcel.writeInt(criticalMalfunction)
        parcel.writeInt(radarIndicator)
        parcel.writeInt(motorNoArmCause)
        parcel.writeInt(availableRideModes)
        parcel.writeInt(thermalRunway)
        parcel.writeFloat(odometer)
        parcel.writeInt(range)
        parcel.writeFloat(whPerKm)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<VehicleSnapshot> {
        override fun createFromParcel(parcel: Parcel): VehicleSnapshot = VehicleSnapshot(parcel)
        override fun newArray(size: Int): Array<VehicleSnapshot?> = arrayOfNulls(size)
    }
}
