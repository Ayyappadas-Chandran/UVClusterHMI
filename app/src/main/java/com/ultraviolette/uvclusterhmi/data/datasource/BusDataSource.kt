package com.ultraviolette.uvclusterhmi.data.datasource

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.DeadObjectException
import android.os.IBinder
import com.ultraviolette.cluster.aidl.BtState
import com.ultraviolette.cluster.aidl.ISharedSignalCallback
import com.ultraviolette.cluster.aidl.ISharedSignalService
import com.ultraviolette.cluster.aidl.VehicleSnapshot
import com.ultraviolette.cluster.aidl.WifiState
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class BusDataSource(private val context: Context) {

    private val tag = "HMI/BusDataSource"


    private val _vehicleSnapshot = MutableSharedFlow<VehicleSnapshot>(replay = 1)
    val vehicleSnapshot: SharedFlow<VehicleSnapshot> = _vehicleSnapshot.asSharedFlow()

    private val _btState = MutableSharedFlow<BtState>(replay = 1)
    val btState: SharedFlow<BtState> = _btState.asSharedFlow()

    private val _wifiState = MutableSharedFlow<WifiState>(replay = 1)
    val wifiState: SharedFlow<WifiState> = _wifiState.asSharedFlow()

    // replay=0: button presses are momentary events; do not replay to late subscribers.
    private val _handlebarButton = MutableSharedFlow<Int>(replay = 0)
    val handlebarButton: SharedFlow<Int> = _handlebarButton.asSharedFlow()

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
    }

    fun disconnect() {
        try { service?.unregisterCallback(callback) } catch (_: Exception) {}
        try { context.unbindService(connection) } catch (_: Exception) {}
        service = null
    }
}
