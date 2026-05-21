package com.ultraviolette.uvclusterhmi.data.datasource

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.ultraviolette.cluster.aidl.BtState
import com.ultraviolette.cluster.aidl.ISharedSignalCallback
import com.ultraviolette.cluster.aidl.ISharedSignalService
import com.ultraviolette.cluster.aidl.VehicleData
import com.ultraviolette.cluster.aidl.VehicleSnapshot
import com.ultraviolette.cluster.aidl.WifiState
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class BusDataSource(private val context: Context) {

    private val tag = "HMI/BusDataSource"

    private val _vehicleData = MutableSharedFlow<VehicleData>(replay = 1)
    val vehicleData: SharedFlow<VehicleData> = _vehicleData.asSharedFlow()

    private val _vehicleSnapshot = MutableSharedFlow<VehicleSnapshot>(replay = 1)
    val vehicleSnapshot: SharedFlow<VehicleSnapshot> = _vehicleSnapshot.asSharedFlow()

    private val _btState = MutableSharedFlow<BtState>(replay = 1)
    val btState: SharedFlow<BtState> = _btState.asSharedFlow()

    private val _wifiState = MutableSharedFlow<WifiState>(replay = 1)
    val wifiState: SharedFlow<WifiState> = _wifiState.asSharedFlow()

    private var service: ISharedSignalService? = null

    private val callback = object : ISharedSignalCallback.Stub() {
        override fun onVehicleData(data: VehicleData) {
            _vehicleData.tryEmit(data)
            Log.d(tag, "onVehicleData: speed=${data.speed} soc=${data.soc}")
        }
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
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            Log.i(tag, "Connected to ClusterDataBus")
            service = ISharedSignalService.Stub.asInterface(binder)
            try {
                service?.registerCallback(callback)
                service?.getLastVehicleData()?.let { _vehicleData.tryEmit(it) }
                service?.getLastVehicleSnapshot()?.let { _vehicleSnapshot.tryEmit(it) }
                service?.getLastBtState()?.let { state ->
                    _btState.tryEmit(state)
                    Log.d(tag, "Seeded BtState: enabled=${state.isEnabled}")
                }
                service?.getLastWifiState()?.let { state ->
                    _wifiState.tryEmit(state)
                    Log.d(tag, "Seeded WifiState: enabled=${state.isEnabled} ssid=${state.connectedSsid}")
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to register with ClusterDataBus", e)
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
    }

    fun disconnect() {
        try { service?.unregisterCallback(callback) } catch (_: Exception) {}
        try { context.unbindService(connection) } catch (_: Exception) {}
        service = null
    }
}
