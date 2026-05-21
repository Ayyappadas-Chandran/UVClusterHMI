package com.ultraviolette.clusterdatabus.config

import android.content.Context
import android.content.SharedPreferences
import com.ultraviolette.cluster.aidl.FeatureConfig

/** Persists the last cloud-received FeatureConfig so it survives offline reboots. */
class ConfigPersistenceHelper(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("cluster_feature_config", Context.MODE_PRIVATE)

    fun save(config: FeatureConfig) {
        prefs.edit()
            .putBoolean(KEY_BT_CALLING, config.btCalling)
            .putBoolean(KEY_BT_MUSIC, config.btMusic)
            .putBoolean(KEY_NAVIGATION, config.navigation)
            .putBoolean(KEY_TPMS_ALERTS, config.tpmsAlerts)
            .putBoolean(KEY_REAR_CAMERA, config.rearCamera)
            .putBoolean(KEY_WIFI_OTA, config.wifiOta)
            .putInt(KEY_ALLOWED_RIDE_MODES, config.allowedRideModes)
            .putLong(KEY_CONFIG_VERSION, config.configVersion)
            .putLong(KEY_CONFIG_TIMESTAMP, config.configTimestamp)
            .apply()
    }

    /** Returns last saved config, or all-features-enabled defaults if no config has ever arrived. */
    fun load(): FeatureConfig = FeatureConfig(
        btCalling        = prefs.getBoolean(KEY_BT_CALLING, true),
        btMusic          = prefs.getBoolean(KEY_BT_MUSIC, true),
        navigation       = prefs.getBoolean(KEY_NAVIGATION, true),
        tpmsAlerts       = prefs.getBoolean(KEY_TPMS_ALERTS, true),
        rearCamera       = prefs.getBoolean(KEY_REAR_CAMERA, true),
        wifiOta          = prefs.getBoolean(KEY_WIFI_OTA, true),
        allowedRideModes = prefs.getInt(KEY_ALLOWED_RIDE_MODES, FeatureConfig.RIDE_MODES_ALL),
        configVersion    = prefs.getLong(KEY_CONFIG_VERSION, 0L),
        configTimestamp  = prefs.getLong(KEY_CONFIG_TIMESTAMP, 0L)
    )

    fun hasSavedConfig(): Boolean = prefs.contains(KEY_CONFIG_VERSION)

    companion object {
        private const val KEY_BT_CALLING         = "bt_calling"
        private const val KEY_BT_MUSIC           = "bt_music"
        private const val KEY_NAVIGATION         = "navigation"
        private const val KEY_TPMS_ALERTS        = "tpms_alerts"
        private const val KEY_REAR_CAMERA        = "rear_camera"
        private const val KEY_WIFI_OTA           = "wifi_ota"
        private const val KEY_ALLOWED_RIDE_MODES = "allowed_ride_modes"
        private const val KEY_CONFIG_VERSION     = "config_version"
        private const val KEY_CONFIG_TIMESTAMP   = "config_timestamp"
    }
}
