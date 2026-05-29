package com.ultraviolette.clusterdatabus

import com.ultraviolette.cluster.aidl.BtState
import com.ultraviolette.cluster.aidl.VehicleSnapshot
import com.ultraviolette.cluster.aidl.WifiState

/** Filters out redundant broadcasts by comparing incoming vs cached state. */
class ChangeDetector {

    fun hasChanged(incoming: VehicleSnapshot, cached: VehicleSnapshot): Boolean =
        // ── Speed / power ─────────────────────────────────────────────────────
        incoming.speedKph             != cached.speedKph             ||
        incoming.vehicleSpeed         != cached.vehicleSpeed         ||
        incoming.motorPower           != cached.motorPower           ||
        incoming.motorArmed           != cached.motorArmed           ||
        // ── Battery / SOC ─────────────────────────────────────────────────────
        incoming.batterySoc           != cached.batterySoc           ||
        incoming.batteryError         != cached.batteryError         ||
        incoming.batteryOverTemp      != cached.batteryOverTemp      ||
        // ── Regen ─────────────────────────────────────────────────────────────
        incoming.regenLevel           != cached.regenLevel           ||
        incoming.regenUnavailable     != cached.regenUnavailable     ||
        // ── Drive / ride mode ─────────────────────────────────────────────────
        incoming.rideMode             != cached.rideMode             ||
        incoming.modeHover            != cached.modeHover            ||
        incoming.isBallisticPlus      != cached.isBallisticPlus      ||
        // ── MTC / ABS / hill-hold ─────────────────────────────────────────────
        incoming.mtcMode              != cached.mtcMode              ||
        incoming.mtcState             != cached.mtcState             ||
        incoming.absMode              != cached.absMode              ||
        incoming.absWarningLamp       != cached.absWarningLamp       ||
        incoming.hillHold             != cached.hillHold             ||
        incoming.hillHoldIcon         != cached.hillHoldIcon         ||
        // ── Telltale warnings ────────────────────────────────────────────────
        incoming.motorTempIcon        != cached.motorTempIcon        ||
        incoming.highBeam             != cached.highBeam             ||
        incoming.hazardLamps          != cached.hazardLamps          ||
        incoming.indicator            != cached.indicator            ||
        incoming.milState             != cached.milState             ||
        incoming.milIcon              != cached.milIcon              ||
        incoming.otaPending           != cached.otaPending           ||
        incoming.criticalMalfunction  != cached.criticalMalfunction  ||
        // ── Safety alerts ─────────────────────────────────────────────────────
        incoming.thermalRunway        != cached.thermalRunway        ||
        incoming.thermalRunwayV       != cached.thermalRunwayV       ||
        incoming.thermalRunwayH       != cached.thermalRunwayH       ||
        incoming.thermalRunwayT       != cached.thermalRunwayT       ||
        incoming.sideStandDeployed    != cached.sideStandDeployed    ||
        incoming.lockdown             != cached.lockdown             ||
        // ── Radar ─────────────────────────────────────────────────────────────
        incoming.radarIndicator       != cached.radarIndicator       ||
        incoming.radarLeftWarn        != cached.radarLeftWarn        ||
        incoming.radarLeftAlert       != cached.radarLeftAlert       ||
        incoming.radarRightWarn       != cached.radarRightWarn       ||
        incoming.radarRightAlert      != cached.radarRightAlert      ||
        incoming.rcwAlert             != cached.rcwAlert             ||
        // ── Cruise control ────────────────────────────────────────────────────
        incoming.cruiseOff            != cached.cruiseOff            ||
        incoming.cruiseStandby        != cached.cruiseStandby        ||
        incoming.cruiseActive         != cached.cruiseActive         ||
        incoming.cruiseError          != cached.cruiseError          ||
        incoming.cruiseValue          != cached.cruiseValue          ||
        // ── Charging / power state ────────────────────────────────────────────
        incoming.charger              != cached.charger              ||
        incoming.keyOff               != cached.keyOff              ||
        // ── Odometer / range / trip ───────────────────────────────────────────
        incoming.odometer             != cached.odometer             ||
        incoming.range                != cached.range                ||
        incoming.whPerKm              != cached.whPerKm              ||
        incoming.rollAngle            != cached.rollAngle            ||
        incoming.tripDistance         != cached.tripDistance         ||
        incoming.trip1Distance        != cached.trip1Distance        ||
        incoming.trip2Distance        != cached.trip2Distance        ||
        incoming.trip3Distance        != cached.trip3Distance        ||
        // ── Brightness ────────────────────────────────────────────────────────
        incoming.brightnessLevel      != cached.brightnessLevel      ||
        incoming.brightnessAuto       != cached.brightnessAuto
    fun hasChanged(incoming: BtState, cached: BtState): Boolean =
        incoming.isEnabled != cached.isEnabled ||
        incoming.bondState != cached.bondState ||
        incoming.callState != cached.callState ||
        incoming.a2dpConnected != cached.a2dpConnected ||
        incoming.hfpConnected != cached.hfpConnected ||
        incoming.pairedDeviceAddress != cached.pairedDeviceAddress

    fun hasChanged(incoming: WifiState, cached: WifiState): Boolean =
        incoming.isEnabled != cached.isEnabled ||
        incoming.isConnected != cached.isConnected ||
        incoming.connectedSsid != cached.connectedSsid ||
        incoming.signalLevel != cached.signalLevel
}
