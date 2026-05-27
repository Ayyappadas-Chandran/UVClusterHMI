package com.ultraviolette.cluster.aidl

import android.os.Parcel
import android.os.Parcelable

/**
 * Full decoded snapshot — built from the PROP_ID_CUSTOM binary blob by CarPropertyService.
 *
 * Field groups (parcel order is immutable — always append new fields at the end):
 *
 * ── Group 1: TellTales (8-byte bitfield, fields 1–28) ─────────────────────
 * Directly mirrors the [TellTales] data class in the UI module.
 *
 * ── Group 2: VcuInfoMsg scalars (fields 29–31) ─────────────────────────────
 * Odometer, range and efficiency extracted from VcuInfoMsg.
 *
 * ── Group 3: VcuInfoMsg flag fields (fields 32–49) ─────────────────────────
 * Single bits from vcuStatusH / vcuStatusL / statusH / statusL.
 *   • sideStandDeployed — fixes the G10 proxy in ClusterViewModel
 *   • thermalRunwayV/H/T — fine-grained runaway source identification
 *   • keyOff, paFwd/Rev/Entry — additional VCU state
 *
 * ── Group 4: VcuMiscInfo fields (fields 50–59) ─────────────────────────────
 * Bits from the 4-word VcuMiscInfo block — radar BSM, RCW, cruise, ballistic.
 *
 * ── Group 5: ImxDbgMsg battery telemetry (fields 60–64) ───────────────────
 * Needed by the future ChargingScreen.
 *
 * ── Group 6: ChargeCtx + ImxAuxMsg (fields 65–69) ─────────────────────────
 * Charger connection, status, type, remaining time, charge limit.
 */
data class VehicleSnapshot(
    // ── Group 1: TellTales ───────────────────────────────────────────────────
    val speedKph:        Float   = 0f,
    val brightnessLevel: Int     = 0,
    val brightnessAuto:  Boolean = false,
    val absModeStatus:   Int     = 0,
    val hillHoldIcon:    Int     = 0,
    val indicator:       Int     = 0,
    val lockdown:        Int     = 0,
    val hillHold: Int           = 0,
    val motorTempIcon: Int      = 0,
    val absWarningLamp: Int     = 0,
    val mtcMode: Int            = 0,
    val mtcState: Int           = 0,
    val charger: Int            = 0,
    val rideMode: Int           = 1,
    val modeHover: Int          = 0,
    val milState: Int           = 0,
    val absMode: Int            = 0,
    val batteryError: Int       = 0,
    val batteryOverTemp: Int    = 0,
    val highBeam: Int           = 0,
    val indicatorLeft: Int      = 0,
    val indicatorRight: Int     = 0,
    val milIcon: Int            = 0,
    val motorArmed: Int         = 0,
    val otaPending: Int         = 0,
    val regenUnavailable: Int   = 0,
    val vehicleSpeed: Int       = 0,
    val batterySoc: Int         = 0,
    val hazardLamps: Int        = 0,
    val regenLevel: Int         = 0,
    val criticalMalfunction: Int = 0,
    val radarIndicator: Int     = 0,
    val motorNoArmCause: Int    = 0,
    val availableRideModes: Int = 0,
    val thermalRunway: Int      = 0,
    val cruiseValue: Int = 0,
    val rtcTime: IntArray = IntArray(0),
    /** PROP_ID_VEHICLE_VALUE[1] — raw motor power in watts (W); negative = regen. */
    val motorPower:   Float = 0f,
    /** VcuInfoMsg.roll — vehicle lean/roll angle in degrees. */
    val rollAngle:    Float = 0f,
    /** VcuInfoMsg.distance — trip odometer distance in km. */
    val tripDistance: Float = 0f,

    // ── Group 9: TripMeterDisp — 3 trips × 4 floats (48 bytes from blob) ────
    // Each trip: distance (km), wattHour (Wh), duration (seconds), averageSpeed (km/h)
    val trip1Distance:  Float = 0f,
    val trip1WattHour:  Float = 0f,
    val trip1Duration:  Float = 0f,   // seconds
    val trip1AvgSpeed:  Float = 0f,
    val trip2Distance:  Float = 0f,
    val trip2WattHour:  Float = 0f,
    val trip2Duration:  Float = 0f,
    val trip2AvgSpeed:  Float = 0f,
    val trip3Distance:  Float = 0f,
    val trip3WattHour:  Float = 0f,
    val trip3Duration:  Float = 0f,
    val trip3AvgSpeed:  Float = 0f,

    // ── Group 2: VcuInfoMsg scalars ──────────────────────────────────────────
    val odometer: Float         = 0f,
    val range: Int              = 0,
    val whPerKm: Float          = 0f,

    // ── Group 3: VcuInfoMsg flag fields ──────────────────────────────────────
    /** vcuStatusL bit 29 — side stand deployed; replaces the G10 hillHoldState proxy. */
    val sideStandDeployed: Int  = 0,
    /** BMS statusH bit 21 (STAT_THRM_RUNAWAY_ALRT_V) — thermal runaway voltage. */
    val thermalRunwayV: Int     = 0,
    /** BMS statusH bit 23 (STAT_THRM_RUNAWAY_ALRT_H) — thermal runaway humidity. */
    val thermalRunwayH: Int     = 0,
    /** BMS statusH bit 22 (STAT_THRM_RUNAWAY_ALRT_T) — thermal runaway temperature. */
    val thermalRunwayT: Int     = 0,
    /** vcuStatusL bit 7 (STAT_VCU_VEHICLE_KEY_OFF). */
    val keyOff: Int             = 0,
    /** vcuStatusH bit 14 (STAT_VCU_PA_MODE_FWD). */
    val paFwd: Int              = 0,
    /** vcuStatusH bit 15 (STAT_VCU_PA_MODE_REV). */
    val paRev: Int              = 0,
    /** vcuStatusH bit 16 (STAT_VCU_PA_MODE_ENTRY). */
    val paEntry: Int            = 0,

    // ── Group 4: VcuMiscInfo fields ──────────────────────────────────────────
    /** VcuMiscFlags.STAT_VCU_RDR_BSM_LHS_WARN (bit 53 in misc word 1). */
    val radarLeftWarn: Int      = 0,
    /** VcuMiscFlags.STAT_VCU_RDR_BSM_LHS_ALRT (bit 54). */
    val radarLeftAlert: Int     = 0,
    /** VcuMiscFlags.STAT_VCU_RDR_BSM_RHS_WARN (bit 55). */
    val radarRightWarn: Int     = 0,
    /** VcuMiscFlags.STAT_VCU_RDR_BSM_RHS_ALRT (bit 56). */
    val radarRightAlert: Int    = 0,
    /** VcuMiscFlags.STAT_VCU_RDR_RCW_ALRT (bit 57). */
    val rcwAlert: Int           = 0,
    /** VcuMiscFlags.STAT_VCU_MC_SURGE_MODE (bit 66). */
    val isBallisticPlus: Int    = 0,
    /** VcuMiscFlags.STAT_VCU_MC_CC_OFF (bit 88). */
    val cruiseOff: Int          = 0,
    /** VcuMiscFlags.STAT_VCU_MC_CC_STBY (bit 89). */
    val cruiseStandby: Int      = 0,
    /** VcuMiscFlags.STAT_VCU_MC_CC_ACTIVE (bit 90). */
    val cruiseActive: Int       = 0,
    /** VcuMiscFlags.STAT_VCU_MC_CC_ERROR (bit 91). */
    val cruiseError: Int        = 0,

    // ── Group 5: ImxDbgMsg battery telemetry ─────────────────────────────────
    /** Minutes to full charge (UInt clamped to Int range). */
    val chargeTtf: Int          = 0,
    val packVoltage: Float      = 0f,
    val packCurrent: Float      = 0f,
    val maxCellTemperature: Float = 0f,
    val motorTemperature: Float = 0f,

    // ── Group 6: Charger context ──────────────────────────────────────────────
    /** ChargeCtx.chargerRemainingTime — seconds / minutes remaining. */
    val chargerRemainingTime: Int = 0,
    /** ChargeCtx.connectionState. */
    val connectionState: Int    = 0,
    /** ChargeCtx.chargerStatus. */
    val chargerStatus: Int      = 0,
    /** ChargeCtx.chargerType. */
    val chargerType: Int        = 0,
    /** ImxAuxMsg.chargeLimit — SOC charge limit percentage. */
    val chargeLimit: Int        = 0,
) : Parcelable {

    constructor(parcel: Parcel) : this(
        // Group 1 — TellTales
        hillHold            = parcel.readInt(),
        motorTempIcon       = parcel.readInt(),
        absWarningLamp      = parcel.readInt(),
        mtcMode             = parcel.readInt(),
        mtcState            = parcel.readInt(),
        charger             = parcel.readInt(),
        rideMode            = parcel.readInt(),
        modeHover           = parcel.readInt(),
        milState            = parcel.readInt(),
        absMode             = parcel.readInt(),
        batteryError        = parcel.readInt(),
        batteryOverTemp     = parcel.readInt(),
        highBeam            = parcel.readInt(),
        indicatorLeft       = parcel.readInt(),
        indicatorRight      = parcel.readInt(),
        milIcon             = parcel.readInt(),
        motorArmed          = parcel.readInt(),
        otaPending          = parcel.readInt(),
        regenUnavailable    = parcel.readInt(),
        vehicleSpeed        = parcel.readInt(),
        batterySoc          = parcel.readInt(),
        hazardLamps         = parcel.readInt(),
        regenLevel          = parcel.readInt(),
        criticalMalfunction = parcel.readInt(),
        radarIndicator      = parcel.readInt(),
        motorNoArmCause     = parcel.readInt(),
        availableRideModes  = parcel.readInt(),
        thermalRunway       = parcel.readInt(),
        // Group 2 — VcuInfoMsg scalars
        odometer            = parcel.readFloat(),
        range               = parcel.readInt(),
        whPerKm             = parcel.readFloat(),
        // Group 3 — VcuInfoMsg flags
        sideStandDeployed   = parcel.readInt(),
        thermalRunwayV      = parcel.readInt(),
        thermalRunwayH      = parcel.readInt(),
        thermalRunwayT      = parcel.readInt(),
        keyOff              = parcel.readInt(),
        paFwd               = parcel.readInt(),
        paRev               = parcel.readInt(),
        paEntry             = parcel.readInt(),
        // Group 4 — VcuMiscInfo
        radarLeftWarn       = parcel.readInt(),
        radarLeftAlert      = parcel.readInt(),
        radarRightWarn      = parcel.readInt(),
        radarRightAlert     = parcel.readInt(),
        rcwAlert            = parcel.readInt(),
        isBallisticPlus     = parcel.readInt(),
        cruiseOff           = parcel.readInt(),
        cruiseStandby       = parcel.readInt(),
        cruiseActive        = parcel.readInt(),
        cruiseError         = parcel.readInt(),
        // Group 5 — ImxDbgMsg
        chargeTtf           = parcel.readInt(),
        packVoltage         = parcel.readFloat(),
        packCurrent         = parcel.readFloat(),
        maxCellTemperature  = parcel.readFloat(),
        motorTemperature    = parcel.readFloat(),
        // Group 6 — Charger context
        chargerRemainingTime = parcel.readInt(),
        connectionState     = parcel.readInt(),
        chargerStatus       = parcel.readInt(),
        chargerType         = parcel.readInt(),
        chargeLimit         = parcel.readInt(),
        // Group 7 — Individual-property snapshot fields (appended; parcel order is immutable)
        speedKph            = parcel.readFloat(),
        brightnessLevel     = parcel.readInt(),
        brightnessAuto      = parcel.readInt() != 0,
        absModeStatus       = parcel.readInt(),
        hillHoldIcon        = parcel.readInt(),
        indicator           = parcel.readInt(),
        lockdown            = parcel.readInt(),
        cruiseValue         = parcel.readInt(),
        rtcTime             = parcel.createIntArray() ?: IntArray(0),
        // Group 8 — Derived scalars (appended)
        motorPower          = parcel.readFloat(),
        rollAngle           = parcel.readFloat(),
        tripDistance        = parcel.readFloat(),
        // Group 9 — TripMeterDisp (appended; parcel order is immutable)
        trip1Distance       = parcel.readFloat(),
        trip1WattHour       = parcel.readFloat(),
        trip1Duration       = parcel.readFloat(),
        trip1AvgSpeed       = parcel.readFloat(),
        trip2Distance       = parcel.readFloat(),
        trip2WattHour       = parcel.readFloat(),
        trip2Duration       = parcel.readFloat(),
        trip2AvgSpeed       = parcel.readFloat(),
        trip3Distance       = parcel.readFloat(),
        trip3WattHour       = parcel.readFloat(),
        trip3Duration       = parcel.readFloat(),
        trip3AvgSpeed       = parcel.readFloat(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        // Group 1 — TellTales
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
        // Group 2 — VcuInfoMsg scalars
        parcel.writeFloat(odometer)
        parcel.writeInt(range)
        parcel.writeFloat(whPerKm)
        // Group 3 — VcuInfoMsg flags
        parcel.writeInt(sideStandDeployed)
        parcel.writeInt(thermalRunwayV)
        parcel.writeInt(thermalRunwayH)
        parcel.writeInt(thermalRunwayT)
        parcel.writeInt(keyOff)
        parcel.writeInt(paFwd)
        parcel.writeInt(paRev)
        parcel.writeInt(paEntry)
        // Group 4 — VcuMiscInfo
        parcel.writeInt(radarLeftWarn)
        parcel.writeInt(radarLeftAlert)
        parcel.writeInt(radarRightWarn)
        parcel.writeInt(radarRightAlert)
        parcel.writeInt(rcwAlert)
        parcel.writeInt(isBallisticPlus)
        parcel.writeInt(cruiseOff)
        parcel.writeInt(cruiseStandby)
        parcel.writeInt(cruiseActive)
        parcel.writeInt(cruiseError)
        // Group 5 — ImxDbgMsg
        parcel.writeInt(chargeTtf)
        parcel.writeFloat(packVoltage)
        parcel.writeFloat(packCurrent)
        parcel.writeFloat(maxCellTemperature)
        parcel.writeFloat(motorTemperature)
        // Group 6 — Charger context
        parcel.writeInt(chargerRemainingTime)
        parcel.writeInt(connectionState)
        parcel.writeInt(chargerStatus)
        parcel.writeInt(chargerType)
        parcel.writeInt(chargeLimit)
        // Group 7 — Individual-property fields
        parcel.writeFloat(speedKph)
        parcel.writeInt(brightnessLevel)
        parcel.writeInt(if (brightnessAuto) 1 else 0)
        parcel.writeInt(absModeStatus)
        parcel.writeInt(hillHoldIcon)
        parcel.writeInt(indicator)
        parcel.writeInt(lockdown)
        parcel.writeInt(cruiseValue)
        parcel.writeIntArray(rtcTime)
        // Group 8 — Derived scalars
        parcel.writeFloat(motorPower)
        parcel.writeFloat(rollAngle)
        parcel.writeFloat(tripDistance)
        // Group 9 — TripMeterDisp
        parcel.writeFloat(trip1Distance)
        parcel.writeFloat(trip1WattHour)
        parcel.writeFloat(trip1Duration)
        parcel.writeFloat(trip1AvgSpeed)
        parcel.writeFloat(trip2Distance)
        parcel.writeFloat(trip2WattHour)
        parcel.writeFloat(trip2Duration)
        parcel.writeFloat(trip2AvgSpeed)
        parcel.writeFloat(trip3Distance)
        parcel.writeFloat(trip3WattHour)
        parcel.writeFloat(trip3Duration)
        parcel.writeFloat(trip3AvgSpeed)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<VehicleSnapshot> {
        override fun createFromParcel(parcel: Parcel): VehicleSnapshot = VehicleSnapshot(parcel)
        override fun newArray(size: Int): Array<VehicleSnapshot?> = arrayOfNulls(size)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VehicleSnapshot

        if (speedKph != other.speedKph) return false
        if (brightnessLevel != other.brightnessLevel) return false
        if (brightnessAuto != other.brightnessAuto) return false
        if (absModeStatus != other.absModeStatus) return false
        if (hillHoldIcon != other.hillHoldIcon) return false
        if (indicator != other.indicator) return false
        if (lockdown != other.lockdown) return false
        if (hillHold != other.hillHold) return false
        if (motorTempIcon != other.motorTempIcon) return false
        if (absWarningLamp != other.absWarningLamp) return false
        if (mtcMode != other.mtcMode) return false
        if (mtcState != other.mtcState) return false
        if (charger != other.charger) return false
        if (rideMode != other.rideMode) return false
        if (modeHover != other.modeHover) return false
        if (milState != other.milState) return false
        if (absMode != other.absMode) return false
        if (batteryError != other.batteryError) return false
        if (batteryOverTemp != other.batteryOverTemp) return false
        if (highBeam != other.highBeam) return false
        if (indicatorLeft != other.indicatorLeft) return false
        if (indicatorRight != other.indicatorRight) return false
        if (milIcon != other.milIcon) return false
        if (motorArmed != other.motorArmed) return false
        if (otaPending != other.otaPending) return false
        if (regenUnavailable != other.regenUnavailable) return false
        if (vehicleSpeed != other.vehicleSpeed) return false
        if (batterySoc != other.batterySoc) return false
        if (hazardLamps != other.hazardLamps) return false
        if (regenLevel != other.regenLevel) return false
        if (criticalMalfunction != other.criticalMalfunction) return false
        if (radarIndicator != other.radarIndicator) return false
        if (motorNoArmCause != other.motorNoArmCause) return false
        if (availableRideModes != other.availableRideModes) return false
        if (thermalRunway != other.thermalRunway) return false
        if (cruiseValue != other.cruiseValue) return false
        if (odometer != other.odometer) return false
        if (range != other.range) return false
        if (whPerKm != other.whPerKm) return false
        if (sideStandDeployed != other.sideStandDeployed) return false
        if (thermalRunwayV != other.thermalRunwayV) return false
        if (thermalRunwayH != other.thermalRunwayH) return false
        if (thermalRunwayT != other.thermalRunwayT) return false
        if (keyOff != other.keyOff) return false
        if (paFwd != other.paFwd) return false
        if (paRev != other.paRev) return false
        if (paEntry != other.paEntry) return false
        if (radarLeftWarn != other.radarLeftWarn) return false
        if (radarLeftAlert != other.radarLeftAlert) return false
        if (radarRightWarn != other.radarRightWarn) return false
        if (radarRightAlert != other.radarRightAlert) return false
        if (rcwAlert != other.rcwAlert) return false
        if (isBallisticPlus != other.isBallisticPlus) return false
        if (cruiseOff != other.cruiseOff) return false
        if (cruiseStandby != other.cruiseStandby) return false
        if (cruiseActive != other.cruiseActive) return false
        if (cruiseError != other.cruiseError) return false
        if (chargeTtf != other.chargeTtf) return false
        if (packVoltage != other.packVoltage) return false
        if (packCurrent != other.packCurrent) return false
        if (maxCellTemperature != other.maxCellTemperature) return false
        if (motorTemperature != other.motorTemperature) return false
        if (chargerRemainingTime != other.chargerRemainingTime) return false
        if (connectionState != other.connectionState) return false
        if (chargerStatus != other.chargerStatus) return false
        if (chargerType != other.chargerType) return false
        if (chargeLimit != other.chargeLimit) return false
        if (!rtcTime.contentEquals(other.rtcTime)) return false
        if (motorPower != other.motorPower) return false
        if (rollAngle != other.rollAngle) return false
        if (tripDistance != other.tripDistance) return false
        if (trip1Distance != other.trip1Distance) return false
        if (trip1WattHour != other.trip1WattHour) return false
        if (trip1Duration != other.trip1Duration) return false
        if (trip1AvgSpeed != other.trip1AvgSpeed) return false
        if (trip2Distance != other.trip2Distance) return false
        if (trip2WattHour != other.trip2WattHour) return false
        if (trip2Duration != other.trip2Duration) return false
        if (trip2AvgSpeed != other.trip2AvgSpeed) return false
        if (trip3Distance != other.trip3Distance) return false
        if (trip3WattHour != other.trip3WattHour) return false
        if (trip3Duration != other.trip3Duration) return false
        if (trip3AvgSpeed != other.trip3AvgSpeed) return false

        return true
    }

    override fun hashCode(): Int {
        var result = speedKph.hashCode()
        result = 31 * result + brightnessLevel
        result = 31 * result + brightnessAuto.hashCode()
        result = 31 * result + absModeStatus
        result = 31 * result + hillHoldIcon
        result = 31 * result + indicator
        result = 31 * result + lockdown
        result = 31 * result + hillHold
        result = 31 * result + motorTempIcon
        result = 31 * result + absWarningLamp
        result = 31 * result + mtcMode
        result = 31 * result + mtcState
        result = 31 * result + charger
        result = 31 * result + rideMode
        result = 31 * result + modeHover
        result = 31 * result + milState
        result = 31 * result + absMode
        result = 31 * result + batteryError
        result = 31 * result + batteryOverTemp
        result = 31 * result + highBeam
        result = 31 * result + indicatorLeft
        result = 31 * result + indicatorRight
        result = 31 * result + milIcon
        result = 31 * result + motorArmed
        result = 31 * result + otaPending
        result = 31 * result + regenUnavailable
        result = 31 * result + vehicleSpeed
        result = 31 * result + batterySoc
        result = 31 * result + hazardLamps
        result = 31 * result + regenLevel
        result = 31 * result + criticalMalfunction
        result = 31 * result + radarIndicator
        result = 31 * result + motorNoArmCause
        result = 31 * result + availableRideModes
        result = 31 * result + thermalRunway
        result = 31 * result + cruiseValue
        result = 31 * result + odometer.hashCode()
        result = 31 * result + range
        result = 31 * result + whPerKm.hashCode()
        result = 31 * result + sideStandDeployed
        result = 31 * result + thermalRunwayV
        result = 31 * result + thermalRunwayH
        result = 31 * result + thermalRunwayT
        result = 31 * result + keyOff
        result = 31 * result + paFwd
        result = 31 * result + paRev
        result = 31 * result + paEntry
        result = 31 * result + radarLeftWarn
        result = 31 * result + radarLeftAlert
        result = 31 * result + radarRightWarn
        result = 31 * result + radarRightAlert
        result = 31 * result + rcwAlert
        result = 31 * result + isBallisticPlus
        result = 31 * result + cruiseOff
        result = 31 * result + cruiseStandby
        result = 31 * result + cruiseActive
        result = 31 * result + cruiseError
        result = 31 * result + chargeTtf
        result = 31 * result + packVoltage.hashCode()
        result = 31 * result + packCurrent.hashCode()
        result = 31 * result + maxCellTemperature.hashCode()
        result = 31 * result + motorTemperature.hashCode()
        result = 31 * result + chargerRemainingTime
        result = 31 * result + connectionState
        result = 31 * result + chargerStatus
        result = 31 * result + chargerType
        result = 31 * result + chargeLimit
        result = 31 * result + rtcTime.contentHashCode()
        result = 31 * result + motorPower.hashCode()
        result = 31 * result + rollAngle.hashCode()
        result = 31 * result + tripDistance.hashCode()
        result = 31 * result + trip1Distance.hashCode()
        result = 31 * result + trip1WattHour.hashCode()
        result = 31 * result + trip1Duration.hashCode()
        result = 31 * result + trip1AvgSpeed.hashCode()
        result = 31 * result + trip2Distance.hashCode()
        result = 31 * result + trip2WattHour.hashCode()
        result = 31 * result + trip2Duration.hashCode()
        result = 31 * result + trip2AvgSpeed.hashCode()
        result = 31 * result + trip3Distance.hashCode()
        result = 31 * result + trip3WattHour.hashCode()
        result = 31 * result + trip3Duration.hashCode()
        result = 31 * result + trip3AvgSpeed.hashCode()
        return result
    }
}
