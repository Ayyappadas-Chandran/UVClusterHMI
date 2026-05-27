package com.ultraviolette.cluster.aidl;

import com.ultraviolette.cluster.aidl.VehicleSnapshot;
import com.ultraviolette.cluster.aidl.BtState;
import com.ultraviolette.cluster.aidl.WifiState;

interface ISharedSignalCallback {
    oneway void onVehicleSnapshot(in VehicleSnapshot snapshot);
    oneway void onBtState(in BtState state);
    oneway void onWifiState(in WifiState state);
    /** Handlebar / swift-button press forwarded from CarPropertyService. */
    oneway void onHandlebarButton(int button);
}
