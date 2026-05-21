package com.ultraviolette.cluster.aidl;

import com.ultraviolette.cluster.aidl.ISharedSignalCallback;
import com.ultraviolette.cluster.aidl.VehicleData;
import com.ultraviolette.cluster.aidl.VehicleSnapshot;
import com.ultraviolette.cluster.aidl.BtState;
import com.ultraviolette.cluster.aidl.WifiState;

interface ISharedSignalService {
    void registerCallback(ISharedSignalCallback cb);
    void unregisterCallback(ISharedSignalCallback cb);
    VehicleData getLastVehicleData();
    VehicleSnapshot getLastVehicleSnapshot();
    BtState getLastBtState();
    WifiState getLastWifiState();
}
