package com.ultraviolette.clusterdatabus

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.IBinder
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.ultraviolette.cluster.aidl.BtScanResult
import com.ultraviolette.cluster.aidl.BtState
import com.ultraviolette.cluster.aidl.ICarPropertyCallback
import com.ultraviolette.cluster.aidl.ICarPropertyService
import com.ultraviolette.cluster.aidl.VehicleSnapshot
import com.ultraviolette.cluster.aidl.WifiScanResult
import com.ultraviolette.cluster.aidl.WifiState

/**
 * Manages all upstream signal sources for ClusterDataBus:
 *  - CarPropertyService (AIDL bind)
 *  - Bluetooth (BroadcastReceiver: adapter, bond, profiles, call state)
 *  - WiFi (BroadcastReceiver + WifiManager/ConnectivityManager)
 */
class ConnectionManager(
    private val context: Context,
    private val signalManager: SignalManager,
    private val permissionManager: PermissionManager
) {
    private val tag = "ClusterBus.ConnectionManager"

    // ── Car Property ──────────────────────────────────────────────────────────

    private var carService: ICarPropertyService? = null

    private val carCallback = object : ICarPropertyCallback.Stub() {
        override fun onVehicleSnapshot(snapshot: VehicleSnapshot) {
            signalManager.onVehicleSnapshot(snapshot)
        }

        override fun onHandlebarButton(button: Int) {
            // Forward to all ISharedSignalCallback subscribers (HMI app etc.)
            signalManager.onHandlebarButton(button)
        }

        override fun onChargerEvent(code: Int) {
            // Charger connect/disconnect events — reflected in VehicleSnapshot.charger;
            // no additional fan-out needed at this time.
            Log.d(tag, "onChargerEvent: code=$code")
        }

        override fun onMotorNoArmEvent(reason: IntArray?) {
            // Motor arm-failure reasons — carried via VehicleSnapshot fields;
            // no additional fan-out needed at this time.
            Log.d(tag, "onMotorNoArmEvent: reason=${reason?.toList()}")
        }

        override fun onVehicleInfoRequest() {
            // VCU requests vehicle identification info (VIN, firmware version etc.).
            // Not yet handled; CarPropertyService responds directly.
            Log.d(tag, "onVehicleInfoRequest")
        }

        override fun onHeartbeatControl(value: Int) {
            // Periodic liveness ping from VCU — acknowledged by CarPropertyService.
            Log.d(tag, "onHeartbeatControl: value=$value")
        }

        override fun onFotaProgress(progress: IntArray?) {
            // FOTA update progress percentage array — no fan-out at this time.
            Log.d(tag, "onFotaProgress: progress=${progress?.toList()}")
        }

        override fun onSleepWake(value: Int) {
            // VCU sleep/wake state change — no fan-out at this time.
            Log.d(tag, "onSleepWake: value=$value")
        }
    }

    private val carConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.d(tag, "CarPropertyService connected")
            carService = ICarPropertyService.Stub.asInterface(service)
            try { carService?.registerCallback(carCallback) } catch (e: Exception) {
                Log.e(tag, "Failed to register car callback", e)
            }
        }
        override fun onServiceDisconnected(name: ComponentName) {
            Log.w(tag, "CarPropertyService disconnected")
            carService = null
        }
    }

    // ── Bluetooth ─────────────────────────────────────────────────────────────

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var btSessionManager: BtSessionManager? = null

    @Volatile private var currentBtState = BtState()

    /** Accumulates devices found during the current discovery cycle. Cleared on each new startDiscovery. */
    private val discoveredDevices = mutableListOf<BtScanResult>()

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {

                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    val enabled = state == BluetoothAdapter.STATE_ON
                    currentBtState = currentBtState.copy(isEnabled = enabled)
                    pushBtState()
                    if (enabled) startDiscovery()
                }

                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val device = intent.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java
                    )
                    val bondState = intent.getIntExtra(
                        BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE
                    )
                    onBondStateChanged(device, bondState)
                }

                BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED -> {
                    val profileState = intent.getIntExtra(
                        BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_DISCONNECTED
                    )
                    currentBtState = currentBtState.copy(
                        a2dpConnected = profileState == BluetoothProfile.STATE_CONNECTED
                    )
                    pushBtState()
                }

                BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED -> {
                    val profileState = intent.getIntExtra(
                        BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_DISCONNECTED
                    )
                    currentBtState = currentBtState.copy(
                        hfpConnected = profileState == BluetoothProfile.STATE_CONNECTED
                    )
                    pushBtState()
                }

                TelephonyManager.ACTION_PHONE_STATE_CHANGED -> {
                    val callState = when (intent.getStringExtra(TelephonyManager.EXTRA_STATE)) {
                        TelephonyManager.EXTRA_STATE_RINGING  -> 1
                        TelephonyManager.EXTRA_STATE_OFFHOOK  -> 2
                        else                                   -> 0  // EXTRA_STATE_IDLE
                    }
                    currentBtState = currentBtState.copy(callState = callState)
                    pushBtState()
                }

                BluetoothDevice.ACTION_FOUND -> {
                    val device = intent.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java
                    ) ?: return
                    val name = deviceName(device)
                    val result = BtScanResult(
                        name      = name,
                        address   = device.address,
                        bondState = device.bondState
                    )
                    synchronized(discoveredDevices) {
                        if (discoveredDevices.none { it.address == result.address }) {
                            discoveredDevices.add(result)
                        }
                    }
                    signalManager.onBluetoothScanResult(discoveredDevices.toList())
                    Log.d(tag, "BT device found: $name (${device.address})")
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d(tag, "BT discovery finished — ${discoveredDevices.size} device(s) found")
                    signalManager.onBluetoothScanResult(discoveredDevices.toList())
                }
            }
        }
    }

    private fun onBondStateChanged(device: BluetoothDevice?, bondState: Int) {
        device ?: return
        val name = deviceName(device)
        currentBtState = currentBtState.copy(
            pairedDeviceName    = if (bondState == BluetoothDevice.BOND_BONDED) name else "",
            pairedDeviceAddress = if (bondState == BluetoothDevice.BOND_BONDED) device.address else "",
            bondState           = bondState
        )
        if (bondState == BluetoothDevice.BOND_BONDED) {
            Log.d(tag, "Bonded with $name — connecting profiles")
            btSessionManager?.connectProfiles(device)
        }
        pushBtState()
    }

    private fun pushBtState() = signalManager.onBtState(currentBtState)

    @SuppressLint("MissingPermission")
    fun enableBluetooth() {
        try {
            bluetoothAdapter?.enable()
            Log.d(tag, "bluetoothEnable requested")
        } catch (e: Exception) {
            Log.e(tag, "bluetoothEnable failed", e)
        }
    }

    @SuppressLint("MissingPermission")
    fun disableBluetooth() {
        try {
            bluetoothAdapter?.disable()
            Log.d(tag, "bluetoothDisable requested")
        } catch (e: Exception) {
            Log.e(tag, "bluetoothDisable failed", e)
        }
    }

    fun startDiscoveryFromService() {
        Log.d(tag, "startDiscoveryFromService requested")
        startDiscovery()
    }

    @SuppressLint("MissingPermission")
    fun createBond(address: String) {
        try {
            val device = bluetoothAdapter?.getRemoteDevice(address)
            device?.createBond()
            Log.d(tag, "createBond requested for $address")
        } catch (e: Exception) {
            Log.e(tag, "createBond failed for $address", e)
        }
    }

    private fun startDiscovery() {
        val adapter = bluetoothAdapter ?: return
        if (!hasScanPermission()) return
        synchronized(discoveredDevices) { discoveredDevices.clear() }
        signalManager.onBluetoothScanResult(emptyList())  // clear on HMI side immediately
        if (adapter.isDiscovering) adapter.cancelDiscovery()
        adapter.startDiscovery()
    }

    // ── WiFi ──────────────────────────────────────────────────────────────────

    private val wifiReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION -> {
                    val succeeded = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false
                    )
                    Log.d(tag, "SCAN_RESULTS_AVAILABLE: succeeded=$succeeded")
                    // Always push — even throttled/stale results are better than nothing.
                    pushWifiScanResults()
                }
                WifiManager.WIFI_STATE_CHANGED_ACTION -> {
                    val state = intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN
                    )
                    Log.d(tag, "WIFI_STATE_CHANGED: state=$state")
                    signalManager.onWifiState(currentWifiState())
                    // Auto-scan when adapter turns on so available-devices list populates.
                    if (state == WifiManager.WIFI_STATE_ENABLED) {
                        pushWifiScanResults()
                        wifiStartScan()
                        Log.d(tag, "WiFi turned ON — pushed cache and started scan")
                    }
                }
                else -> {
                    signalManager.onWifiState(currentWifiState())
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun currentWifiState(): WifiState {
        val wm = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork  = cm.activeNetwork
        val caps           = activeNetwork?.let { cm.getNetworkCapabilities(it) }
        val isWifiConnected = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

        // On API 31+, WifiInfo from NetworkCapabilities respects NEARBY_WIFI_DEVICES and
        // doesn't redact SSID the way deprecated getConnectionInfo() does.
        // On API 29–30 fall back to getConnectionInfo(); SSID may be "<unknown ssid>"
        // if ACCESS_FINE_LOCATION hasn't been granted.
        val ssid: String
        val rssi: Int
        if (isWifiConnected) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val wifiInfo = caps?.transportInfo as? android.net.wifi.WifiInfo
                val rawSsid = wifiInfo?.ssid?.trim('"') ?: ""
                ssid = if (rawSsid == "<unknown ssid>") "" else rawSsid
                rssi = wifiInfo?.rssi ?: -100
            } else {
                @Suppress("DEPRECATION")
                val connInfo = wm.connectionInfo
                val rawSsid = connInfo?.ssid?.trim('"') ?: ""
                ssid = if (rawSsid == "<unknown ssid>") "" else rawSsid
                rssi = connInfo?.rssi ?: -100
            }
        } else {
            ssid = ""
            rssi = -100
        }

        val signal = if (isWifiConnected) WifiManager.calculateSignalLevel(rssi, 6) else 0

        return WifiState(
            isEnabled     = wm.isWifiEnabled,
            connectedSsid = ssid,
            signalLevel   = signal,
            isConnected   = isWifiConnected
        )
    }

    @SuppressLint("MissingPermission")
    private fun pushWifiScanResults() {
        // getScanResults() silently returns an empty list when ACCESS_FINE_LOCATION /
        // NEARBY_WIFI_DEVICES is not granted.  ClusterDataBus is a background service
        // without a UI, so it cannot request runtime permissions itself.  If permission
        // is missing here the HMI app process reads scan results via its own receiver
        // (BusDataSource.wifiScanReceiver) and pushes them through the existing flow.
        if (!permissionManager.canReadWifiScanResults()) {
            Log.w(tag, "pushWifiScanResults: location permission not granted to ClusterDataBus " +
                "— scan results will be provided by the HMI app process instead.")
            return
        }
        try {
            val wm = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val scanList = wm.scanResults ?: run {
                Log.w(tag, "pushWifiScanResults: wm.scanResults returned null")
                return
            }
            Log.d(tag, "pushWifiScanResults: raw scan list size = ${scanList.size}")

            @Suppress("DEPRECATION")
            val savedSsids: Set<String> = try {
                wm.configuredNetworks
                    ?.mapNotNull { it.SSID?.trim('"')?.takeIf { s -> s.isNotBlank() } }
                    ?.toSet()
                    ?: emptySet()
            } catch (e: Exception) {
                Log.w(tag, "getConfiguredNetworks failed (non-fatal): ${e.message}")
                emptySet()
            }

            val connectedSsid = currentWifiState().connectedSsid

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
                        isSaved   = sr.SSID in savedSsids || sr.SSID == connectedSsid
                    )
                }
            signalManager.onWifiScanResult(results)
            Log.d(tag, "WiFi scan results pushed: ${results.size} network(s) " +
                "(raw=${scanList.size}, saved=${savedSsids.size})")
        } catch (e: Exception) {
            Log.e(tag, "pushWifiScanResults failed", e)
        }
    }

    @SuppressLint("MissingPermission")
    fun wifiEnable() {
        try {
            val wm = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            @Suppress("DEPRECATION")
            wm.isWifiEnabled = true
            Log.d(tag, "wifiEnable requested")
        } catch (e: Exception) {
            Log.e(tag, "wifiEnable failed", e)
        }
    }

    @SuppressLint("MissingPermission")
    fun wifiDisable() {
        try {
            val wm = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            @Suppress("DEPRECATION")
            wm.isWifiEnabled = false
            Log.d(tag, "wifiDisable requested")
        } catch (e: Exception) {
            Log.e(tag, "wifiDisable failed", e)
        }
    }

    @SuppressLint("MissingPermission")
    fun wifiConnect(ssid: String, password: String) {
        try {
            val wm = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            @Suppress("DEPRECATION")
            val config = android.net.wifi.WifiConfiguration().apply {
                SSID = "\"$ssid\""
                preSharedKey = "\"$password\""
            }
            @Suppress("DEPRECATION")
            val networkId = wm.addNetwork(config)
            if (networkId != -1) {
                wm.disconnect()
                wm.enableNetwork(networkId, true)
                wm.reconnect()
                Log.d(tag, "wifiConnect: connecting to $ssid (networkId=$networkId)")
            } else {
                Log.w(tag, "wifiConnect: addNetwork returned -1 for $ssid")
            }
        } catch (e: Exception) {
            Log.e(tag, "wifiConnect failed for $ssid", e)
        }
    }

    @SuppressLint("MissingPermission")
    fun wifiConnectToSaved(ssid: String) {
        try {
            val wm = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            @Suppress("DEPRECATION")
            val network = wm.configuredNetworks?.find { it.SSID?.trim('"') == ssid }
            if (network != null) {
                wm.disconnect()
                wm.enableNetwork(network.networkId, true)
                wm.reconnect()
                Log.d(tag, "wifiConnectToSaved: connecting to saved network $ssid")
            } else {
                Log.w(tag, "wifiConnectToSaved: no configured network found for $ssid")
            }
        } catch (e: Exception) {
            Log.e(tag, "wifiConnectToSaved failed for $ssid", e)
        }
    }

    @SuppressLint("MissingPermission")
    fun wifiForget() {
        try {
            val wm = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            @Suppress("DEPRECATION")
            val networkId = wm.connectionInfo?.networkId ?: -1
            if (networkId != -1) {
                wm.removeNetwork(networkId)
                wm.disconnect()
                Log.d(tag, "wifiForget: removed networkId=$networkId")
            } else {
                Log.w(tag, "wifiForget: no active connection to forget")
            }
        } catch (e: Exception) {
            Log.e(tag, "wifiForget failed", e)
        }
    }

    @SuppressLint("MissingPermission")
    fun wifiStartScan() {
        try {
            val wm = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            @Suppress("DEPRECATION")
            val started = wm.startScan()
            Log.d(tag, "wifiStartScan requested (started=$started)")
        } catch (e: Exception) {
            Log.e(tag, "wifiStartScan failed", e)
        }
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    fun connect() {
        bindCarPropertyService()
        setupBluetooth()
        setupWifi()
    }

    fun disconnect() {
        try { carService?.unregisterCallback(carCallback) } catch (e: Exception) {
            Log.e(tag, "unregister car callback failed", e)
        }
        try { context.unbindService(carConnection) } catch (_: Exception) {}
        carService = null

        try { context.unregisterReceiver(bluetoothReceiver) } catch (_: Exception) {}
        try { context.unregisterReceiver(wifiReceiver) } catch (_: Exception) {}
    }

    private fun bindCarPropertyService() {
        if (!permissionManager.canBindCarPropertyService()) return
        val intent = Intent(PermissionManager.ACTION_CAR_PROPERTY).apply {
            setPackage(PermissionManager.PKG_CAR_PROPERTY)
        }
        val bound = context.bindService(intent, carConnection, Context.BIND_AUTO_CREATE)
        Log.d(tag, "bindCarPropertyService: $bound")
    }

    private fun setupBluetooth() {
        val bm = context.getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bm.adapter
        val adapter = bluetoothAdapter ?: run {
            Log.w(tag, "No Bluetooth adapter found")
            return
        }
        btSessionManager = BtSessionManager(context, adapter)

        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)
            addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
        }
        context.registerReceiver(bluetoothReceiver, filter)

        currentBtState = currentBtState.copy(isEnabled = adapter.isEnabled)
        pushBtState()

        if (adapter.isEnabled) startDiscovery()
    }

    private fun setupWifi() {
        val filter = IntentFilter().apply {
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
            addAction(ConnectivityManager.CONNECTIVITY_ACTION)
            addAction(WifiManager.RSSI_CHANGED_ACTION)
            addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        }
        context.registerReceiver(wifiReceiver, filter)

        val currentState = currentWifiState()
        signalManager.onWifiState(currentState)

        // If WiFi is already enabled on service start, seed any cached scan results
        // immediately (so HMI gets them without waiting for the next broadcast) and
        // also kick off a fresh scan.
        if (currentState.isEnabled) {
            pushWifiScanResults()   // publish stale cache, if any
            wifiStartScan()         // start a fresh scan in the background
            Log.d(tag, "setupWifi: WiFi already enabled — seeded scan cache and started fresh scan")
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun deviceName(device: BluetoothDevice): String {
        if (!hasConnectPermission()) return device.address
        return try { device.name ?: device.address } catch (e: Exception) { device.address }
    }

    private fun hasConnectPermission(): Boolean =
        ActivityCompat.checkSelfPermission(
            context, Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED

    private fun hasScanPermission(): Boolean =
        ActivityCompat.checkSelfPermission(
            context, Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED
}
