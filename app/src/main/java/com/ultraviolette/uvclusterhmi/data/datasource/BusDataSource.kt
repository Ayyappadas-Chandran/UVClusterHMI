package com.ultraviolette.uvclusterhmi.data.datasource

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.net.wifi.WifiManager
import android.os.DeadObjectException
import android.os.IBinder
import android.util.Log
import com.ultraviolette.cluster.aidl.BtScanResult
import com.ultraviolette.cluster.aidl.BtState
import com.ultraviolette.cluster.aidl.ISharedSignalCallback
import com.ultraviolette.cluster.aidl.ISharedSignalService
import com.ultraviolette.cluster.aidl.VehicleSnapshot
import com.ultraviolette.cluster.aidl.WifiScanResult
import com.ultraviolette.cluster.aidl.WifiState
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class BusDataSource(private val context: Context) {

    private val tag = "HMI/BusDataSource"

    // ── Local WiFi scan receiver ───────────────────────────────────────────────
    // ClusterDataBus is a background service and cannot hold runtime location
    // permissions.  The HMI app (this process) has ACCESS_FINE_LOCATION granted,
    // so we read WifiManager.scanResults here and push them into the same flow
    // that the AIDL callback uses.  ClusterDataBus still calls wifiStartScan() to
    // trigger the scan; both processes receive SCAN_RESULTS_AVAILABLE_ACTION.

    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            when (intent?.action) {
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION -> {
                    val succeeded = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                    Log.d(tag, "wifiScanReceiver: SCAN_RESULTS_AVAILABLE succeeded=$succeeded")
                    readLocalWifiScanResults()
                }
                WifiManager.WIFI_STATE_CHANGED_ACTION -> {
                    val state = intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN
                    )
                    Log.d(tag, "wifiScanReceiver: WIFI_STATE_CHANGED state=$state")
                    // Auto-trigger a scan when the adapter turns on so the available-devices
                    // list populates immediately without waiting for ClusterDataBus.
                    if (state == WifiManager.WIFI_STATE_ENABLED) {
                        triggerLocalScan()
                    }
                }
            }
        }
    }

    private var wifiScanReceiverRegistered = false

    @SuppressLint("MissingPermission")
    private fun readLocalWifiScanResults() {
        try {
            val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val scanList = wm.scanResults ?: run {
                Log.w(tag, "readLocalWifiScanResults: wm.scanResults returned null")
                return
            }
            @Suppress("DEPRECATION")
            val savedSsids: Set<String> = try {
                wm.configuredNetworks
                    ?.mapNotNull { it.SSID?.trim('"')?.takeIf { s -> s.isNotBlank() } }
                    ?.toSet()
                    ?: emptySet()
            } catch (e: Exception) {
                Log.w(tag, "readLocalWifiScanResults: getConfiguredNetworks failed (non-fatal): ${e.message}")
                emptySet()
            }

            val results = scanList
                .filter { it.SSID?.isNotBlank() == true }
                .distinctBy { it.SSID }
                .map { sr ->
                    WifiScanResult(
                        ssid      = sr.SSID,
                        level     = WifiManager.calculateSignalLevel(sr.level, 6),
                        isSecured = sr.capabilities.contains("WEP") ||
                            sr.capabilities.contains("WPA") ||
                            sr.capabilities.contains("WPA2") ||
                            sr.capabilities.contains("WPA3"),
                        isSaved   = sr.SSID in savedSsids
                    )
                }
            _wifiScanResults.tryEmit(results)
            Log.d(tag, "readLocalWifiScanResults: pushed ${results.size} network(s) " +
                "(raw=${scanList.size}, saved=${savedSsids.size})")
        } catch (e: Exception) {
            Log.e(tag, "readLocalWifiScanResults failed", e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun triggerLocalScan() {
        try {
            val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            @Suppress("DEPRECATION")
            wm.startScan()
            Log.d(tag, "triggerLocalScan: scan requested from HMI process")
        } catch (e: Exception) {
            Log.w(tag, "triggerLocalScan failed (non-fatal): ${e.message}")
        }
    }


    private val _vehicleSnapshot = MutableSharedFlow<VehicleSnapshot>(replay = 1)
    val vehicleSnapshot: SharedFlow<VehicleSnapshot> = _vehicleSnapshot.asSharedFlow()

    private val _btState = MutableSharedFlow<BtState>(replay = 1)
    val btState: SharedFlow<BtState> = _btState.asSharedFlow()

    private val _wifiState = MutableSharedFlow<WifiState>(replay = 1)
    val wifiState: SharedFlow<WifiState> = _wifiState.asSharedFlow()

    // replay=0: button presses are momentary events; do not replay to late subscribers.
    // extraBufferCapacity=64: prevents the Binder thread from dropping events when
    // no coroutine collector is immediately suspended (e.g. during UI recomposition).
    private val _handlebarButton = MutableSharedFlow<Int>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val handlebarButton: SharedFlow<Int> = _handlebarButton.asSharedFlow()

    // replay=1: late subscribers get the most recent scan result immediately.
    private val _btScanResults = MutableSharedFlow<List<BtScanResult>>(replay = 1)
    val btScanResults: SharedFlow<List<BtScanResult>> = _btScanResults.asSharedFlow()

    // replay=1: late subscribers get the most recent WiFi scan result immediately.
    private val _wifiScanResults = MutableSharedFlow<List<WifiScanResult>>(replay = 1)
    val wifiScanResults: SharedFlow<List<WifiScanResult>> = _wifiScanResults.asSharedFlow()

    private var service: ISharedSignalService? = null

    private val callback = object : ISharedSignalCallback.Stub() {
        override fun onVehicleSnapshot(snapshot: VehicleSnapshot) {
            _vehicleSnapshot.tryEmit(snapshot)
            Log.d(tag, "onVehicleSnapshot: speed=${snapshot.vehicleSpeed}")
        }
        override fun onBtState(state: BtState) {
            _btState.tryEmit(state)
            Log.d(tag, "onBtState: enabled=${state.isEnabled} device=${state.pairedDeviceName} " +
                "call=${state.callState} a2dp=${state.a2dpConnected} hfp=${state.hfpConnected}")
        }
        override fun onWifiState(state: WifiState) {
            _wifiState.tryEmit(state)
            Log.d(tag, "onWifiState: enabled=${state.isEnabled} connected=${state.isConnected} " +
                "ssid=${state.connectedSsid} signal=${state.signalLevel}")
        }
        override fun onHandlebarButton(button: Int) {
            _handlebarButton.tryEmit(button)
            Log.d(tag, "onHandlebarButton: button=$button")
        }
        override fun onBluetoothScanResult(devices: List<BtScanResult>) {
            _btScanResults.tryEmit(devices)
            Log.d(tag, "onBluetoothScanResult: ${devices.size} device(s)")
        }
        override fun onWifiScanResult(results: List<WifiScanResult>) {
            // ClusterDataBus runs in a separate process that cannot hold runtime
            // ACCESS_FINE_LOCATION, so its scan results are always empty.  An empty
            // push from the bus must never overwrite valid results already produced
            // by the HMI process's own wifiScanReceiver.  If ClusterDataBus ever
            // gains permission and returns real results, those will still be accepted.
            if (results.isEmpty()) {
                Log.d(tag, "onWifiScanResult: ignoring empty result from ClusterDataBus " +
                    "— HMI local scan is authoritative")
                return
            }
            _wifiScanResults.tryEmit(results)
            Log.d(tag, "onWifiScanResult: ${results.size} network(s)")
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            Log.i(tag, "Connected to ClusterDataBus")
            val svc = ISharedSignalService.Stub.asInterface(binder)

            // Register the callback first. A DeadObjectException here means the
            // previous service instance is still dying and the system handed us
            // its stale binder. Return without setting `service` — Android will
            // call onServiceDisconnected shortly, then onServiceConnected again
            // once the restarted service is ready (START_STICKY ensures this).
            try {
                svc.registerCallback(callback)
            } catch (e: DeadObjectException) {
                Log.w(tag, "Binder dead on connect — awaiting service restart")
                return
            } catch (e: Exception) {
                Log.e(tag, "Failed to register with ClusterDataBus", e)
                return
            }

            // Binder is live; publish `service` so disconnect() can unregister.
            service = svc

            // Seed flows with last-known values so the UI has data immediately
            // without waiting for the first broadcast event.
            try {
                svc.getLastVehicleSnapshot()?.let { _vehicleSnapshot.tryEmit(it) }
                svc.getLastBtState()?.let { state ->
                    _btState.tryEmit(state)
                    Log.d(tag, "Seeded BtState: enabled=${state.isEnabled}")
                }
                svc.getLastWifiState()?.let { state ->
                    _wifiState.tryEmit(state)
                    Log.d(tag, "Seeded WifiState: enabled=${state.isEnabled} ssid=${state.connectedSsid}")
                    // If WiFi is already enabled, read any cached scan results immediately
                    // and trigger a fresh scan so the available-devices list isn't empty.
                    if (state.isEnabled) {
                        readLocalWifiScanResults()
                        triggerLocalScan()
                    }
                }
            } catch (e: Exception) {
                Log.w(tag, "Seeding last-known state failed (non-fatal)", e)
            }
        }
        override fun onServiceDisconnected(name: ComponentName) {
            Log.w(tag, "Disconnected from ClusterDataBus")
            service = null
        }
    }

    fun connect() {
        Log.d(tag, "Binding to ClusterDataBus...")
        val intent = Intent("com.ultraviolette.clusterdatabus.ISharedSignalService").apply {
            setPackage("com.ultraviolette.clusterdatabus")
        }
        val bound = context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        Log.d(tag, "bindService returned: $bound")

        // Register local WiFi scan receiver so this process reads results using its
        // own ACCESS_FINE_LOCATION permission (ClusterDataBus cannot hold it at runtime).
        if (!wifiScanReceiverRegistered) {
            val filter = IntentFilter().apply {
                addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
                addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            }
            context.registerReceiver(wifiScanReceiver, filter)
            wifiScanReceiverRegistered = true
            Log.d(tag, "WiFi scan receiver registered in HMI process")
        }
    }

    fun disconnect() {
        try { service?.unregisterCallback(callback) } catch (_: Exception) {}
        try { context.unbindService(connection) } catch (_: Exception) {}
        service = null

        if (wifiScanReceiverRegistered) {
            try { context.unregisterReceiver(wifiScanReceiver) } catch (_: Exception) {}
            wifiScanReceiverRegistered = false
            Log.d(tag, "WiFi scan receiver unregistered")
        }
    }

    // ── Bluetooth control — delegates to ClusterDataBus via AIDL ──────────────

    fun bluetoothEnable() {
        try { service?.bluetoothEnable() } catch (e: Exception) {
            Log.e(tag, "bluetoothEnable failed", e)
        }
    }

    fun bluetoothDisable() {
        try { service?.bluetoothDisable() } catch (e: Exception) {
            Log.e(tag, "bluetoothDisable failed", e)
        }
    }

    fun bluetoothStartDiscovery() {
        try { service?.bluetoothStartDiscovery() } catch (e: Exception) {
            Log.e(tag, "bluetoothStartDiscovery failed", e)
        }
    }

    fun bluetoothCreateBond(address: String) {
        try { service?.bluetoothCreateBond(address) } catch (e: Exception) {
            Log.e(tag, "bluetoothCreateBond failed for $address", e)
        }
    }

    // ── Wi-Fi control — delegates to ClusterDataBus via AIDL ──────────────────

    fun wifiEnable() {
        try { service?.wifiEnable() } catch (e: Exception) {
            Log.e(tag, "wifiEnable failed", e)
        }
    }

    fun wifiDisable() {
        try { service?.wifiDisable() } catch (e: Exception) {
            Log.e(tag, "wifiDisable failed", e)
        }
    }

    fun wifiConnect(ssid: String, password: String) {
        try { service?.wifiConnect(ssid, password) } catch (e: Exception) {
            Log.e(tag, "wifiConnect failed for $ssid", e)
        }
    }

    fun wifiConnectToSaved(ssid: String) {
        try { service?.wifiConnectToSaved(ssid) } catch (e: Exception) {
            Log.e(tag, "wifiConnectToSaved failed for $ssid", e)
        }
    }

    fun wifiForget() {
        try { service?.wifiForget() } catch (e: Exception) {
            Log.e(tag, "wifiForget failed", e)
        }
    }

    fun wifiStartScan() {
        // Trigger through ClusterDataBus (AIDL) so the service process also has fresh data,
        // and also trigger locally so this process's wifiScanReceiver fires promptly.
        try { service?.wifiStartScan() } catch (e: Exception) {
            Log.e(tag, "wifiStartScan (aidl) failed", e)
        }
        triggerLocalScan()
    }
}
