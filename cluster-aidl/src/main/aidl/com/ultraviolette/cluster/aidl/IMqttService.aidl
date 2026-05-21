package com.ultraviolette.cluster.aidl;

import com.ultraviolette.cluster.aidl.IMqttCallback;

interface IMqttService {
    /** Subscribe to a topic. Messages arrive via IMqttCallback.onMessage(). */
    void subscribe(String topic, int qos);

    /** Unsubscribe from a previously subscribed topic. */
    void unsubscribe(String topic);

    /**
     * Publish a message.
     * @param retained true = broker stores the last message for late subscribers.
     */
    void publish(String topic, String payload, int qos, boolean retained);

    /** Register to receive messages and connection-state events. */
    void registerCallback(IMqttCallback cb);

    /** Unregister a previously registered callback. */
    void unregisterCallback(IMqttCallback cb);

    /** Returns true if currently connected to the broker. */
    boolean isConnected();

    /** Force an immediate reconnect attempt (e.g. after a network change). */
    void reconnect();
}
