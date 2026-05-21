package com.ultraviolette.cluster.aidl;

import com.ultraviolette.cluster.aidl.ICarPropertyCallback;
import com.ultraviolette.cluster.aidl.VehicleData;

interface ICarPropertyService {
    void registerCallback(ICarPropertyCallback cb);
    void unregisterCallback(ICarPropertyCallback cb);
    VehicleData getLastKnownData();
    void sendBoolean(int propId, boolean value);
    void sendByteArray(int propId, in byte[] value);
}
