package com.ultraviolette.uvclusterhmi.data.dataSource

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION_CODES
import android.util.Log.d
import android.util.Log.e
import android.util.Log.i
import android.util.Log.w
import androidx.core.app.ActivityCompat
import com.ultraviolette.uvclusterhmi.utils.Utilities.getBluetoothDevice


/**
 * A wrapper class to manage Bluetooth operations such as discovery, bonding,
 * and broadcasting Bluetooth events.
 *
 * @param context The context required to access system services and register receivers.
 * @param onScanResult Callback interface to communicate Bluetooth events to the caller.
 *
 * Note: Make sure required permissions are granted before calling Bluetooth methods:
 * - For Android 12+: BLUETOOTH_SCAN, BLUETOOTH_CONNECT
 * - For earlier versions: ACCESS_FINE_LOCATION
 */

class BluetoothManagerWrapper(
    private val context: Context
) {
    private val tag: String = BluetoothManagerWrapper::class.java.simpleName
    private val bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private var availableBluetoothDevices = ArrayList<BluetoothDevice>()
    private var onScanResult: ((List<BluetoothDevice>) -> Unit)? = null
    private var onBluetoothStateChange: ((Boolean) -> Unit)? = null

    /** BroadcastReceiver to handle Bluetooth device discovery and bonding events */
    private val bluetoothActionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            when (intent.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    d("BluetoothState", "Bluetooth State change")
                    val bluetoothState =
                        intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    onBluetoothStateChange?.invoke(bluetoothState == BluetoothAdapter.STATE_ON)
                    if (bluetoothState == BluetoothAdapter.STATE_ON) {
                        startDiscovery()
                    }
                }

                BluetoothDevice.ACTION_FOUND -> {
                    d(tag, "ACTION_FOUND")
                    if (Build.VERSION.SDK_INT >= VERSION_CODES.S &&
                        context?.let {
                            ActivityCompat.checkSelfPermission(
                                it,
                                Manifest.permission.BLUETOOTH_CONNECT
                            )
                        } != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    val device: BluetoothDevice? =
                        if (Build.VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(
                                BluetoothDevice.EXTRA_DEVICE,
                                BluetoothDevice::class.java
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }
                    device?.let { it ->
                        d(tag, "devices:$it")
                        if (availableBluetoothDevices.none { it.address == device.address }) {
                            availableBluetoothDevices.add(it)
                        }
                    }
                }

                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val device: BluetoothDevice? =
                        if (Build.VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(
                                BluetoothDevice.EXTRA_DEVICE,
                                BluetoothDevice::class.java
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }
                    d(tag, "ACTION_BOND_STATE_CHANGED")
                    val bondState = intent.getIntExtra(
                        BluetoothDevice.EXTRA_BOND_STATE,
                        BluetoothDevice.BOND_NONE
                    )
                    val prevBondState = intent.getIntExtra(
                        BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE,
                        BluetoothDevice.BOND_NONE
                    )
                    onStateChange(bondState, prevBondState, device)
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    onScanResult?.invoke(availableBluetoothDevices)
                    d(
                        tag,
                        "Bluetooth discovery finished with ${availableBluetoothDevices.size} devices"
                    )
                }
            }
        }
    }

    /** Returns state of Bluetooth adapter on the device */
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter.isEnabled
    }

    /** Starts Bluetooth device discovery.*/
    fun startDiscovery() {
        if ((Build.VERSION.SDK_INT >= VERSION_CODES.S) &&
            (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED)
        ) {
            return
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }
        bluetoothAdapter.startDiscovery()
    }

    /** Retrieves paired devices from the Bluetooth adapter.*/
    private fun getPairedDevices() {
        if (bluetoothAdapter.isEnabled) {
            if (Build.VERSION.SDK_INT >= VERSION_CODES.S && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            val bondedDevicesSet =
                bluetoothAdapter.bondedDevices
            val pairedBluetoothDevices = if (bondedDevicesSet.isNullOrEmpty()) {
                ArrayList()
            } else {
                ArrayList(bondedDevicesSet)
            }
            d(tag, "$pairedBluetoothDevices")
        }
    }

    /**
     * Handles Bluetooth bonding state changes.
     * Connects to profiles (A2DP, HFP) when bonding is successful.
     */
    fun onStateChange(bondState: Int, prevBondState: Int, device: BluetoothDevice?) {
        if (bondState == BluetoothDevice.BOND_BONDED && device != null) {
            i(tag, "Bonded successfully with: ${getBluetoothDevice(device, context)}")
            // A2DP Connection
            bluetoothAdapter.getProfileProxy(
                context,
                object : BluetoothProfile.ServiceListener {
                    override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
                        if (profile == BluetoothProfile.A2DP) {
                            connectToProfile(device, proxy, "A2DP")
                        }
                    }

                    override fun onServiceDisconnected(profile: Int) {
                        if (profile == BluetoothProfile.A2DP) {
                            i(tag, "A2DP service disconnected")
                        }
                    }
                },
                BluetoothProfile.A2DP
            )
            // HFP (HEADSET) Connection
            bluetoothAdapter.getProfileProxy(
                context,
                object : BluetoothProfile.ServiceListener {
                    override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
                        if (profile == BluetoothProfile.HEADSET) {
                            connectToProfile(device, proxy, "HFP")
                        }
                    }

                    override fun onServiceDisconnected(profile: Int) {
                        if (profile == BluetoothProfile.HEADSET) {
                            i(tag, "HFP service disconnected")
                        }
                    }
                },
                BluetoothProfile.HEADSET
            )
        } else if (bondState == BluetoothDevice.BOND_NONE &&
            prevBondState == BluetoothDevice.BOND_BONDING
        ) {
            device?.let{
                w(tag, "Bonding failed or was cancelled for: ${getBluetoothDevice(device,context)}")
            }
        }
    }

    /**
     * Attempts to connect to the specified Bluetooth profile on the given device using reflection.
     *
     * @param device The [BluetoothDevice] to connect.
     * @param proxy The [BluetoothProfile] proxy instance (e.g., A2DP, HFP).
     * @param profileName Name of the profile (used for logging).
     */
    private fun connectToProfile(
        device: BluetoothDevice,
        proxy: BluetoothProfile?,
        profileName: String
    ) {
        try {
            val method = proxy?.javaClass?.getMethod("connect", BluetoothDevice::class.java)
            method?.isAccessible = true
            method?.invoke(proxy, device)
            d(tag, "$profileName connect invoked for ${getBluetoothDevice(device,context)}")
        } catch (e: Exception) {
            e(tag, "$profileName connection failed", e)
        }
    }

    /** Creates a bond with the given [BluetoothDevice].*/
    fun createBond(bluetoothDevice: BluetoothDevice) {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.S && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        bluetoothDevice.createBond()
        bluetoothDevice.setPairingConfirmation(true) // working in system application only
    }

    /** Registers the BroadcastReceiver for Bluetooth actions. */
    fun registerBluetoothActionReceiver() {
        d(tag,"Bluetooth listener registered")
        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        context.registerReceiver(bluetoothActionReceiver, filter)
    }

    /** Unregisters the BroadcastReceiver. */
    fun unregisterBluetoothActionReceiver() {
        d(tag,"Bluetooth listener registered")
        context.unregisterReceiver(bluetoothActionReceiver)
    }

    fun enableBluetooth() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            d(tag,"Permission not granted")
            return
        }
        bluetoothAdapter.enable()
    }

    fun disableBluetooth() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            d(tag,"Permission not granted")
            return
        }
        bluetoothAdapter.disable()
    }

    fun scanResult(callback: (List<BluetoothDevice>) -> Unit) {
        onScanResult = callback
    }

    fun bluetoothStateChange(state: (Boolean) -> Unit) {
        onBluetoothStateChange = state
    }
}