package com.ultraviolette.uvclusterhmi.ui.features.settings.bluetooth

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ultraviolette.cluster.aidl.BtScanResult
import com.ultraviolette.uvclusterhmi.ClusterApplication
import com.ultraviolette.uvclusterhmi.data.repository.ClusterRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import android.util.Log

/**
 * ViewModel for the Bluetooth settings screen.
 *
 * State and control commands flow through ClusterDataBus (AIDL) rather than
 * calling Android Bluetooth APIs directly from the HMI process.
 */
class BluetoothViewModel(private val repository: ClusterRepository) : ViewModel() {

    private val tag = "HMI/BluetoothViewModel"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /** True when the Bluetooth adapter is enabled. Derived from ClusterDataBus BtState. */
    val isEnabled: StateFlow<Boolean> = repository.btState
        .map { it.isEnabled }
        .stateIn(scope, kotlinx.coroutines.flow.SharingStarted.Eagerly, false)

    /** Devices discovered during the current scan cycle. Empty list when scan starts or BT is off. */
    val scanResults: StateFlow<List<BtScanResult>> = repository.btScanResults
        .stateIn(scope, kotlinx.coroutines.flow.SharingStarted.Eagerly, emptyList())

    /** Convenience accessor for the current enabled state (synchronous). */
    fun isBluetoothEnabled(): Boolean = isEnabled.value

    fun enableBluetooth() {
        Log.d(tag, "enableBluetooth()")
        repository.bluetoothEnable()
    }

    fun disableBluetooth() {
        Log.d(tag, "disableBluetooth()")
        repository.bluetoothDisable()
    }

    fun startDiscovery() {
        Log.d(tag, "startDiscovery()")
        repository.bluetoothStartDiscovery()
    }

    /**
     * Initiate pairing with the device identified by its MAC [address].
     * The bond-state update will arrive via [repository.btState] once pairing completes.
     */
    fun createBond(address: String) {
        Log.d(tag, "createBond($address)")
        repository.bluetoothCreateBond(address)
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val clusterApp = app as ClusterApplication
            return BluetoothViewModel(clusterApp.clusterRepository) as T
        }
    }
}
