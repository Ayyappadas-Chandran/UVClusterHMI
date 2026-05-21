package com.ultraviolette.cluster.aidl;

import com.ultraviolette.cluster.aidl.FeatureConfig;

interface IConfigCallback {
    oneway void onConfigChanged(in FeatureConfig config);
}
