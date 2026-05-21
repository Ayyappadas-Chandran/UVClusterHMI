package com.ultraviolette.cluster.aidl;

import com.ultraviolette.cluster.aidl.VehicleData;
import com.ultraviolette.cluster.aidl.VehicleSnapshot;

interface ICarPropertyCallback {
    oneway void onVehicleData(in VehicleData data);
    oneway void onVehicleSnapshot(in VehicleSnapshot snapshot);
}
