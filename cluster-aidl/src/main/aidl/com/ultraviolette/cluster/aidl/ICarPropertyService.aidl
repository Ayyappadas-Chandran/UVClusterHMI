package com.ultraviolette.cluster.aidl;

import com.ultraviolette.cluster.aidl.VehicleSnapshot;
import com.ultraviolette.cluster.aidl.ICarPropertyCallback;
interface ICarPropertyService {
    void registerCallback(ICarPropertyCallback cb);
    void unregisterCallback(ICarPropertyCallback cb);
    VehicleSnapshot getLastKnownSnapshot();
    void sendBoolean(int propId, boolean value);
    void sendByteArray(int propId, in byte[] value);
}
