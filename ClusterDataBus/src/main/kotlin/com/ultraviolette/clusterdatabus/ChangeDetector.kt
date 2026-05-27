package com.ultraviolette.clusterdatabus

import com.ultraviolette.cluster.aidl.BtState
import com.ultraviolette.cluster.aidl.VehicleSnapshot
import com.ultraviolette.cluster.aidl.WifiState

/** Filters out redundant broadcasts by comparing incoming vs cached state. */
class ChangeDetector {

    fun hasChanged(incoming: VehicleSnapshot, cached: VehicleSnapshot): Boolean =
        incoming.vehicleSpeed != cached.vehicleSpeed ||
        incoming.batterySoc != cached.batterySoc ||
        incoming.rideMode != cached.rideMode ||
        incoming.motorArmed != cached.motorArmed ||
        incoming.criticalMalfunction != cached.criticalMalfunction ||
        incoming.thermalRunway != cached.thermalRunway ||
        incoming.odometer != cached.odometer ||
        incoming.speedKph != cached.speedKph ||
        incoming.motorPower != cached.motorPower ||
        incoming.regenLevel != cached.regenLevel ||
        incoming.indicator != cached.indicator ||
        incoming.absMode != cached.absMode ||
        incoming.hillHold != cached.hillHold ||
        incoming.lockdown != cached.lockdown ||
        incoming.rollAngle != cached.rollAngle ||
        incoming.tripDistance != cached.tripDistance ||
        incoming.brightnessLevel != cached.brightnessLevel ||
        incoming.brightnessAuto != cached.brightnessAuto ||
        incoming.trip1Distance != cached.trip1Distance ||
        incoming.trip2Distance != cached.trip2Distance ||
        incoming.trip3Distance != cached.trip3Distance ||
        // Telltale signals that have fast individual-property events (~50 ms).
        // These were missing, so any snapshot where ONLY these fields changed
        // was incorrectly suppressed — causing the observed ~600 ms delay for
        // high-beam and hazard updates (only the slow blob cycle got through).
        incoming.highBeam != cached.highBeam ||
        incoming.hazardLamps != cached.hazardLamps ||
        // Blob-only signals that are safety-relevant and must not be suppressed.
        incoming.sideStandDeployed != cached.sideStandDeployed ||
        incoming.thermalRunwayV != cached.thermalRunwayV ||
        incoming.thermalRunwayT != cached.thermalRunwayT ||
        incoming.thermalRunwayH != cached.thermalRunwayH
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
