package com.ultraviolette.cluster.aidl;

interface IMqttCallback {
    /** Called when a message arrives on a subscribed topic. */
    oneway void onMessage(String topic, String payload);

    /** Called when the broker connection is established (or re-established). */
    oneway void onConnected();

    /** Called when the connection to the broker is lost. */
    oneway void onDisconnected(String reason);
}
