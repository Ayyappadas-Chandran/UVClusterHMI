package com.ultraviolette.clusterdatabus

import com.ultraviolette.cluster.aidl.BtState
import com.ultraviolette.cluster.aidl.VehicleData
import com.ultraviolette.cluster.aidl.VehicleSnapshot
import com.ultraviolette.cluster.aidl.WifiState

/** Filters out redundant broadcasts by comparing incoming vs cached state. */
class ChangeDetector {

    fun hasChanged(incoming: VehicleData, cached: VehicleData): Boolean =
        incoming.speed != cached.speed ||
        incoming.soc != cached.soc ||
        incoming.regenLevel != cached.regenLevel ||
        incoming.rideModeRaw != cached.rideModeRaw ||
        incoming.indicator != cached.indicator ||
        incoming.absMode != cached.absMode ||
        incoming.hillHoldState != cached.hillHoldState ||
        incoming.lockdown != cached.lockdown ||
        incoming.sleepWake != cached.sleepWake ||
        incoming.chargerEvt != cached.chargerEvt ||
        incoming.cruise != cached.cruise

    fun hasChanged(incoming: VehicleSnapshot, cached: VehicleSnapshot): Boolean =
        incoming.vehicleSpeed != cached.vehicleSpeed ||
        incoming.batterySoc != cached.batterySoc ||
        incoming.rideMode != cached.rideMode ||
        incoming.motorArmed != cached.motorArmed ||
        incoming.criticalMalfunction != cached.criticalMalfunction ||
        incoming.thermalRunway != cached.thermalRunway ||
        incoming.odometer != cached.odometer

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
