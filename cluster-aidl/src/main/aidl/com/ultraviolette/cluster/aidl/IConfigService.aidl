package com.ultraviolette.cluster.aidl;

import com.ultraviolette.cluster.aidl.IConfigCallback;
import com.ultraviolette.cluster.aidl.FeatureConfig;

interface IConfigService {
    void registerCallback(IConfigCallback cb);
    void unregisterCallback(IConfigCallback cb);
    FeatureConfig getFeatureConfig();
    void forceSync();
}
