package com.ultraviolette.clusterdatabus.config

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.RemoteException
import android.util.Log
import com.ultraviolette.cluster.aidl.IMqttCallback
import com.ultraviolette.cluster.aidl.IMqttService

/**
 * Binds to the external MqttService APK via [IMqttService] AIDL.
 *
 * The MqttService APK owns all broker credentials and connection logic.
 * This connector only subscribes to the relevant topic and forwards
 * incoming messages to [onMessage].
 *
 * Binding intent action — replace with the actual action exported by the MqttService APK:
 *   ACTION_MQTT_SERVICE = "com.ultraviolette.mqttservice.IMqttService"
 *
 * If the service APK is not installed or dies, the connector retries binding
 * with a fixed [REBIND_DELAY_MS] interval until [disconnect] is called.
 */
class MqttServiceConnector(
    private val context: Context,
    private val onMessage: (topic: String, payload: String) -> Unit
) {
    private val tag = "ClusterBus.MqttServiceConnector"

    @Volatile private var mqttService: IMqttService? = null
    @Volatile private var stopped = false
    private var subscribedTopic: String? = null
    private val rebindHandler = Handler(Looper.getMainLooper())

    private val mqttCallback = object : IMqttCallback.Stub() {
        override fun onMessage(topic: String, payload: String) {
            onMessage(topic, payload)
        }
        override fun onConnected() {
            Log.i(tag, "Broker connected — re-subscribing to ${subscribedTopic ?: "none"}")
            resubscribe()
        }
        override fun onDisconnected(reason: String) {
            Log.w(tag, "Broker disconnected: $reason")
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            val service = IMqttService.Stub.asInterface(binder)
            mqttService = service
            try {
                service.registerCallback(mqttCallback)
                resubscribe()
            } catch (e: RemoteException) {
                Log.e(tag, "Failed to register after bind", e)
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mqttService = null
            Log.w(tag, "MqttService binding lost — will rebind")
            if (!stopped) scheduleRebind()
        }
    }

    /**
     * Bind to MqttService and subscribe to [topic] on connect.
     * Safe to call multiple times; no-ops if already connecting.
     */
    fun connect(topic: String) {
        stopped = false
        subscribedTopic = topic
        bindToService()
    }

    /**
     * Unsubscribe, unregister callback, and unbind from MqttService.
     * No further callbacks will fire after this returns.
     */
    fun disconnect() {
        stopped = true
        rebindHandler.removeCallbacksAndMessages(null)
        val service = mqttService
        if (service != null) {
            try {
                subscribedTopic?.let { service.unsubscribe(it) }
                service.unregisterCallback(mqttCallback)
            } catch (e: RemoteException) {
                Log.w(tag, "Error during disconnect cleanup", e)
            }
        }
        try { context.unbindService(serviceConnection) } catch (_: Exception) {}
        mqttService = null
    }

    /**
     * Ask MqttService to force-reconnect to the broker.
     * Useful after a network state change event.
     */
    fun reconnect() {
        try {
            mqttService?.reconnect() ?: Log.w(tag, "reconnect() called but not bound")
        } catch (e: RemoteException) {
            Log.e(tag, "reconnect() failed", e)
        }
    }

    private fun bindToService() {
        val intent = Intent(ACTION_MQTT_SERVICE).apply {
            // Explicit package required for bindService on API 21+
            `package` = MQTT_SERVICE_PACKAGE
        }
        val bound = try {
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        } catch (e: Exception) {
            Log.e(tag, "bindService failed", e)
            false
        }
        if (!bound) {
            Log.w(tag, "MqttService not available yet — will retry")
            scheduleRebind()
        }
    }

    private fun scheduleRebind() {
        rebindHandler.postDelayed({
            if (!stopped) bindToService()
        }, REBIND_DELAY_MS)
    }

    private fun resubscribe() {
        val topic = subscribedTopic ?: return
        try {
            mqttService?.subscribe(topic, QOS_AT_LEAST_ONCE)
        } catch (e: RemoteException) {
            Log.e(tag, "subscribe($topic) failed", e)
        }
    }

    companion object {
        // TODO: replace with the actual intent action exported by your MqttService APK
        const val ACTION_MQTT_SERVICE  = "com.ultraviolette.mqttservice.IMqttService"
        const val MQTT_SERVICE_PACKAGE = "com.ultraviolette.mqttservice"

        private const val QOS_AT_LEAST_ONCE = 1
        private const val REBIND_DELAY_MS   = 5_000L
    }
}
