package com.ultraviolette.cluster.aidl;

import com.ultraviolette.cluster.aidl.VehicleSnapshot;
import com.ultraviolette.cluster.aidl.BtState;
import com.ultraviolette.cluster.aidl.WifiState;
import com.ultraviolette.cluster.aidl.BtScanResult;
import com.ultraviolette.cluster.aidl.WifiScanResult;

interface ISharedSignalCallback {
    oneway void onVehicleSnapshot(in VehicleSnapshot snapshot);
    oneway void onBtState(in BtState state);
    oneway void onWifiState(in WifiState state);
    /** Handlebar / swift-button press forwarded from CarPropertyService. */
    oneway void onHandlebarButton(int button);
    /** List of currently-discovered Bluetooth devices, pushed on each new find and on discovery-finished. */
    oneway void onBluetoothScanResult(in List<BtScanResult> devices);
    /** Wi-Fi scan results pushed after each SCAN_RESULTS_AVAILABLE_ACTION broadcast. */
    oneway void onWifiScanResult(in List<WifiScanResult> results);
}
