package com.ultraviolette.cluster.aidl;

import com.ultraviolette.cluster.aidl.VehicleSnapshot;

interface ICarPropertyCallback {
    oneway void onVehicleSnapshot(in VehicleSnapshot snapshot);
    void onHandlebarButton(int button);
    void onChargerEvent(int code);
    void onMotorNoArmEvent(in int[] reason);
    void onVehicleInfoRequest();
    void onHeartbeatControl(int value);
    void onFotaProgress(in int[] progress);
    void onSleepWake(int value);
}
