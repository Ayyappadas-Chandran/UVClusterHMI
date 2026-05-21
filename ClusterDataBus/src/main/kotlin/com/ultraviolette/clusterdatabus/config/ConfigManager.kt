package com.ultraviolette.clusterdatabus.config

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.ultraviolette.cluster.aidl.FeatureConfig
import com.ultraviolette.cluster.aidl.IConfigCallback
import org.json.JSONObject

/**
 * Orchestrates feature-flag config lifecycle:
 *  1. Loads last persisted config on init (fallback when offline)
 *  2. Binds to external MqttService APK to receive live config updates
 *  3. Validates version monotonicity — stale cloud messages are dropped
 *  4. Persists every accepted config to disk
 *  5. Notifies [onConfigUpdated] so SharedSignalService can fan-out via AIDL
 *
 * Expected MQTT payload (QoS 1, retained):
 * {
 *   "version": 3,
 *   "timestamp": 1716144000,
 *   "features": {
 *     "btCalling": true,
 *     "btMusic": true,
 *     "navigation": true,
 *     "tpmsAlerts": true,
 *     "rearCamera": false,
 *     "wifiOta": true,
 *     "allowedRideModes": 7
 *   }
 * }
 *
 * Config topic: cluster/<android_id>/features/config
 * The android_id should be set to the bike VIN during provisioning:
 *   adb shell settings put secure android_id <VIN>
 */
class ConfigManager(
    private val context: Context,
    private val onConfigUpdated: (FeatureConfig) -> Unit
) {
    private val tag = "ClusterBus.ConfigManager"

    private val persistence = ConfigPersistenceHelper(context)
    private val subscriptions = ConfigSubscriptionManager()
    private lateinit var mqttConnector: MqttServiceConnector

    @Volatile var currentConfig: FeatureConfig = persistence.load()
        private set

    val configTopic: String
        get() {
            val deviceId = Settings.Secure.getString(
                context.contentResolver, Settings.Secure.ANDROID_ID
            )
            return "cluster/$deviceId/features/config"
        }

    fun init() {
        mqttConnector = MqttServiceConnector(context, ::onMqttMessage)
        Log.d(tag, "Initialized, persisted version=${currentConfig.configVersion}" +
                " (hasSaved=${persistence.hasSavedConfig()})")
    }

    fun connectCloud() {
        mqttConnector.connect(configTopic)
    }

    fun disconnectCloud() {
        mqttConnector.disconnect()
    }

    fun registerCallback(cb: IConfigCallback?) {
        subscriptions.register(cb)
    }

    fun unregisterCallback(cb: IConfigCallback?) {
        subscriptions.unregister(cb)
    }

    fun killSubscriptions() {
        subscriptions.kill()
    }

    /** Triggers a fresh reconnect on the MqttService broker connection. */
    fun forceSync() {
        Log.i(tag, "Force sync requested")
        mqttConnector.reconnect()
    }

    private fun onMqttMessage(topic: String, payload: String) {
        try {
            val parsed = parsePayload(payload) ?: run {
                Log.w(tag, "Unparseable config payload on $topic")
                return
            }
            if (parsed.configVersion <= currentConfig.configVersion) {
                Log.d(tag, "Dropping stale config v${parsed.configVersion} " +
                        "(current=${currentConfig.configVersion})")
                return
            }
            currentConfig = parsed
            persistence.save(parsed)
            onConfigUpdated(parsed)
            subscriptions.broadcastConfig(parsed)
            Log.i(tag, "Feature config updated to v${parsed.configVersion}: $parsed")
        } catch (e: Exception) {
            Log.e(tag, "Error processing config payload", e)
        }
    }

    private fun parsePayload(json: String): FeatureConfig? {
        val root = JSONObject(json)
        val version   = root.optLong("version", 0L)
        val timestamp = root.optLong("timestamp", 0L)
        val features  = root.optJSONObject("features") ?: return null

        return FeatureConfig(
            btCalling        = features.optBoolean(FeatureFlags.BT_CALLING, true),
            btMusic          = features.optBoolean(FeatureFlags.BT_MUSIC, true),
            navigation       = features.optBoolean(FeatureFlags.NAVIGATION, true),
            tpmsAlerts       = features.optBoolean(FeatureFlags.TPMS_ALERTS, true),
            rearCamera       = features.optBoolean(FeatureFlags.REAR_CAMERA, true),
            wifiOta          = features.optBoolean(FeatureFlags.WIFI_OTA, true),
            allowedRideModes = features.optInt(FeatureFlags.ALLOWED_RIDE_MODES, FeatureConfig.RIDE_MODES_ALL),
            configVersion    = version,
            configTimestamp  = timestamp
        )
    }
}
