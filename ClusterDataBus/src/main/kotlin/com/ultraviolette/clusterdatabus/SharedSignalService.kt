package com.ultraviolette.clusterdatabus

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.ultraviolette.cluster.aidl.BtState
import com.ultraviolette.cluster.aidl.IConfigCallback
import com.ultraviolette.cluster.aidl.IConfigService
import com.ultraviolette.cluster.aidl.ISharedSignalCallback
import com.ultraviolette.cluster.aidl.ISharedSignalService
import com.ultraviolette.cluster.aidl.VehicleSnapshot
import com.ultraviolette.cluster.aidl.WifiState
import com.ultraviolette.clusterdatabus.config.ConfigManager

class SharedSignalService : Service() {

    private val tag = "ClusterBus.SharedSignalService"

    private lateinit var stateManager: StateManager
    private lateinit var changeDetector: ChangeDetector
    private lateinit var threadDispatcher: ThreadDispatcher
    private lateinit var subscriptionManager: SubscriptionManager
    private lateinit var persistenceHelper: PersistenceHelper
    private lateinit var permissionManager: PermissionManager
    private lateinit var signalManager: SignalManager
    private lateinit var connectionManager: ConnectionManager
    private lateinit var configManager: ConfigManager

    private val signalBinder = object : ISharedSignalService.Stub() {
        override fun registerCallback(cb: ISharedSignalCallback?) {
            subscriptionManager.register(cb)
        }
        override fun unregisterCallback(cb: ISharedSignalCallback?) {
            subscriptionManager.unregister(cb)
        }
        override fun getLastVehicleSnapshot(): VehicleSnapshot = stateManager.vehicleSnapshot
        override fun getLastBtState(): BtState = stateManager.btState
        override fun getLastWifiState(): WifiState = stateManager.wifiState
        override fun bluetoothEnable()           = connectionManager.enableBluetooth()
        override fun bluetoothDisable()          = connectionManager.disableBluetooth()
        override fun bluetoothStartDiscovery()   = connectionManager.startDiscoveryFromService()
        override fun bluetoothCreateBond(address: String) = connectionManager.createBond(address)

        // ── Wi-Fi control ─────────────────────────────────────────────────────
        override fun wifiEnable()                         = connectionManager.wifiEnable()
        override fun wifiDisable()                        = connectionManager.wifiDisable()
        override fun wifiConnect(ssid: String, password: String) = connectionManager.wifiConnect(ssid, password)
        override fun wifiConnectToSaved(ssid: String)     = connectionManager.wifiConnectToSaved(ssid)
        override fun wifiForget()                         = connectionManager.wifiForget()
        override fun wifiStartScan()                      = connectionManager.wifiStartScan()
    }

    private val configBinder = object : IConfigService.Stub() {
        override fun registerCallback(cb: IConfigCallback?) {
            configManager.registerCallback(cb)
        }
        override fun unregisterCallback(cb: IConfigCallback?) {
            configManager.unregisterCallback(cb)
        }
        override fun getFeatureConfig() = configManager.currentConfig
        override fun forceSync() {
            configManager.forceSync()
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, buildNotification())
        initManagers()
        // Audit runtime permissions on every start — logs which permissions are missing.
        // If Wi-Fi location permission is absent the HMI app process reads scan results
        // via its own BroadcastReceiver (BusDataSource) using its own granted permissions.
        permissionManager.logPermissionAudit()
        restorePersistedState()
        connectionManager.connect()
        configManager.connectCloud()
        Log.d(tag, "SharedSignalService started")
    }

    override fun onBind(intent: Intent): IBinder? = when (intent.action) {
        ACTION_CONFIG -> configBinder
        else          -> signalBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int =
        START_STICKY

    override fun onDestroy() {
        super.onDestroy()
        configManager.disconnectCloud()
        configManager.killSubscriptions()
        connectionManager.disconnect()
        subscriptionManager.kill()
        threadDispatcher.shutdown()
        Log.d(tag, "SharedSignalService destroyed")
    }

    private fun initManagers() {
        stateManager = StateManager()
        changeDetector = ChangeDetector()
        threadDispatcher = ThreadDispatcher()
        subscriptionManager = SubscriptionManager()
        persistenceHelper = PersistenceHelper(this)
        permissionManager = PermissionManager(this)
        signalManager = SignalManager(
            stateManager, changeDetector, subscriptionManager, threadDispatcher, persistenceHelper
        )
        connectionManager = ConnectionManager(this, signalManager, permissionManager)
        configManager = ConfigManager(this) { config ->
            Log.d(tag, "Feature config changed: v${config.configVersion}")
        }
        configManager.init()
    }

    private fun restorePersistedState() {
        stateManager.update(persistenceHelper.loadBtState())
        stateManager.update(persistenceHelper.loadWifiState())
    }

    private fun buildNotification(): Notification {
        val channelId = "cluster_bus"
        val nm = getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(channelId) == null) {
            nm.createNotificationChannel(
                NotificationChannel(channelId, "Cluster Bus", NotificationManager.IMPORTANCE_LOW)
            )
        }
        return Notification.Builder(this, channelId)
            .setContentTitle("UV Cluster Data Bus")
            .setSmallIcon(android.R.drawable.ic_menu_share)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val ACTION_CONFIG = "com.ultraviolette.clusterdatabus.IConfigService"
        private const val NOTIFICATION_ID = 1003
    }
}
