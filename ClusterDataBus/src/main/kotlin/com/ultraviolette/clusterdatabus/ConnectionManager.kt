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
import com.ultraviolette.cluster.aidl.BtState
import com.ultraviolette.cluster.aidl.ICarPropertyCallback
import com.ultraviolette.cluster.aidl.ICarPropertyService
import com.ultraviolette.cluster.aidl.VehicleData
import com.ultraviolette.cluster.aidl.VehicleSnapshot
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
        override fun onVehicleData(data: VehicleData) { signalManager.onVehicleData(data) }
        override fun onVehicleSnapshot(snapshot: VehicleSnapshot) { signalManager.onVehicleSnapshot(snapshot) }
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

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d(tag, "BT discovery finished")
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

    private fun startDiscovery() {
        val adapter = bluetoothAdapter ?: return
        if (!hasScanPermission()) return
        if (adapter.isDiscovering) adapter.cancelDiscovery()
        adapter.startDiscovery()
    }

    // ── WiFi ──────────────────────────────────────────────────────────────────

    private val wifiReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            signalManager.onWifiState(currentWifiState())
        }
    }

    @SuppressLint("MissingPermission")
    private fun currentWifiState(): WifiState {
        val wm = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.activeNetwork?.let { cm.getNetworkCapabilities(it) }
        val isWifiConnected = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        val ssid   = if (isWifiConnected) wm.connectionInfo?.ssid?.trim('"') ?: "" else ""
        val signal = if (isWifiConnected)
            WifiManager.calculateSignalLevel(wm.connectionInfo?.rssi ?: -100, 6) else 0
        return WifiState(
            isEnabled     = wm.isWifiEnabled,
            connectedSsid = ssid,
            signalLevel   = signal,
            isConnected   = isWifiConnected
        )
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
        }
        context.registerReceiver(wifiReceiver, filter)
        signalManager.onWifiState(currentWifiState())
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
