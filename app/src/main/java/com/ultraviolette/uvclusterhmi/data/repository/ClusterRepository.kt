package com.ultraviolette.uvclusterhmi.data.repository

import com.ultraviolette.cluster.aidl.BtScanResult
import com.ultraviolette.cluster.aidl.BtState
import com.ultraviolette.cluster.aidl.VehicleSnapshot
import com.ultraviolette.cluster.aidl.WifiScanResult
import com.ultraviolette.cluster.aidl.WifiState
import com.ultraviolette.uvclusterhmi.data.datasource.BusDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.onStart

class ClusterRepository(private val bus: BusDataSource) {

    val vehicleSnapshot: Flow<VehicleSnapshot> = bus.vehicleSnapshot
        .onStart { emit(VehicleSnapshot()) }

    val btState: Flow<BtState> = bus.btState
        .onStart { emit(BtState()) }

    val wifiState: Flow<WifiState> = bus.wifiState
        .onStart { emit(WifiState()) }

    /** Handlebar / swift-button press events forwarded from CarPropertyService via ClusterDataBus.
     *  replay=0 — momentary events; do not re-deliver to late subscribers. */
    val handlebarButton: SharedFlow<Int> = bus.handlebarButton

    /** Bluetooth devices discovered during the current scan cycle. Emits empty list when discovery starts. */
    val btScanResults: SharedFlow<List<BtScanResult>> = bus.btScanResults

    /** Wi-Fi networks discovered during the last scan cycle. replay=1 so late subscribers get the last result. */
    val wifiScanResults: SharedFlow<List<WifiScanResult>> = bus.wifiScanResults

    // ── Bluetooth control ─────────────────────────────────────────────────────
    fun bluetoothEnable()                    = bus.bluetoothEnable()
    fun bluetoothDisable()                   = bus.bluetoothDisable()
    fun bluetoothStartDiscovery()            = bus.bluetoothStartDiscovery()
    fun bluetoothCreateBond(address: String) = bus.bluetoothCreateBond(address)

    // ── Wi-Fi control ─────────────────────────────────────────────────────────
    fun wifiEnable()                              = bus.wifiEnable()
    fun wifiDisable()                             = bus.wifiDisable()
    fun wifiConnect(ssid: String, password: String) = bus.wifiConnect(ssid, password)
    fun wifiConnectToSaved(ssid: String)          = bus.wifiConnectToSaved(ssid)
    fun wifiForget()                              = bus.wifiForget()
    fun wifiStartScan()                           = bus.wifiStartScan()
}
