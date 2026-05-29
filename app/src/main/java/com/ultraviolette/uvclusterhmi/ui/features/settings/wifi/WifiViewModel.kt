package com.ultraviolette.uvclusterhmi.ui.features.settings.wifi

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ultraviolette.uvclusterhmi.ClusterApplication
import com.ultraviolette.uvclusterhmi.data.repository.ClusterRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for the Wi-Fi settings screen.
 *
 * All state is derived from [ClusterRepository] flows (backed by ClusterDataBus AIDL).
 * No Wi-Fi SDK calls are made from this class — all control commands are delegated to
 * ClusterDataBus via [ClusterRepository].
 */
class WifiViewModel(private val repository: ClusterRepository) : ViewModel() {

    private val tag = "HMI/WifiViewModel"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    // ── State flows ───────────────────────────────────────────────────────────

    /** True when the Wi-Fi adapter is enabled. */
    val isEnabled: StateFlow<Boolean> = repository.wifiState
        .map { it.isEnabled }
        .stateIn(scope, SharingStarted.Eagerly, false)

    /** Alias kept for ControlSectionFragment source compatibility. */
    val onWifiStateChange: StateFlow<Boolean> = isEnabled

    /** SSID of the currently connected network, or null when not connected / unknown. */
    val connectedSSID: StateFlow<String?> = repository.wifiState
        .map { state ->
            state.connectedSsid.trim('"').takeIf { it.isNotBlank() && it != "<unknown ssid>" }
        }
        .stateIn(scope, SharingStarted.Eagerly, null)

    /** True when the device has an active Wi-Fi data connection. */
    val connectionState: StateFlow<Boolean> = repository.wifiState
        .map { it.isConnected }
        .stateIn(scope, SharingStarted.Eagerly, false)

    /** Current signal level (0–5) of the connected network. 0 when not connected. */
    val signalLevel: StateFlow<Int> = repository.wifiState
        .map { it.signalLevel }
        .stateIn(scope, SharingStarted.Eagerly, 0)

    /**
     * Available Wi-Fi networks from the latest scan cycle.
     * Maps each [WifiScanResult] to a [WifiUiModel] for the adapter.
     */
    val scanResult: StateFlow<List<WifiUiModel>> = repository.wifiScanResults
        .map { results ->
            results.map { WifiUiModel(ssid = it.ssid, level = it.level, isSecured = it.isSecured) }
        }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    /**
     * Previously saved / configured networks that are currently in scan range.
     * Derived from [scanResult] by filtering on the [isSaved] flag set by ClusterDataBus.
     */
    val saveNetworkList: StateFlow<List<WifiUiModel>> = repository.wifiScanResults
        .map { results ->
            results
                .filter { it.isSaved }
                .map { WifiUiModel(ssid = it.ssid, level = it.level, isSecured = it.isSecured) }
        }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    /**
     * SSID of a saved network that is in scan range while the device is not connected,
     * making it a candidate for automatic re-connection prompting. Null when connected or
     * no saved network is in range.
     */
    val reconnectSSID: StateFlow<String?> = combine(connectionState, saveNetworkList) { isConnected, saved ->
        if (!isConnected && saved.isNotEmpty()) {
            saved.first().ssid.also { Log.d(tag, "reconnectSSID candidate: $it") }
        } else {
            null
        }
    }.stateIn(scope, SharingStarted.Eagerly, null)

    /** Currently highlighted item in the scan / saved-network list. */
    private val _selectedItem = MutableStateFlow<String?>(null)
    val selectedItem: StateFlow<String?> = _selectedItem.asStateFlow()

    // ── Synchronous accessors (for imperative fragment calls) ─────────────────

    /** Synchronous read of the current enabled state. */
    fun isWifiEnabled(): Boolean = isEnabled.value

    /** Synchronous SSID accessor used when filtering scan results against the connected network. */
    fun getConnectedWifiSSID(): String? = connectedSSID.value

    /** True when the device has an active Wi-Fi connection. */
    fun isConnectionStateActive(): Boolean = connectionState.value

    /** True when the saved-network list is empty. */
    fun isSavedNetworkListEmpty(): Boolean = saveNetworkList.value.isEmpty()

    /** True when a valid (non-placeholder) SSID is connected. */
    fun isNetworkConnected(): Boolean {
        val ssid = connectedSSID.value
        return ssid != null && ssid != "<unknown ssid>"
    }

    // ── Control commands ──────────────────────────────────────────────────────

    /**
     * Enable or disable the Wi-Fi adapter.
     * @param enable true to enable, false to disable.
     */
    fun enableWifi(enable: Boolean) {
        if (enable) {
            Log.d(tag, "enableWifi()")
            repository.wifiEnable()
        } else {
            Log.d(tag, "disableWifi()")
            repository.wifiDisable()
        }
    }

    /** Trigger a new Wi-Fi scan. Results arrive via [scanResult] and [saveNetworkList]. */
    fun startScan() {
        Log.d(tag, "startScan()")
        repository.wifiStartScan()
    }

    /**
     * No-op in the ClusterDataBus architecture.
     * Scan results arrive automatically via [scanResult] after each scan cycle.
     * Kept for source-compatibility with [WifiFragment] call sites.
     */
    fun scanResult() {
        Log.d(tag, "scanResult() — scan results delivered via flow; triggering wifiStartScan")
        repository.wifiStartScan()
    }

    /**
     * Connect to a Wi-Fi network using the provided credentials.
     * @param ssid     Network name.
     * @param password Network password.
     */
    fun connectHotspot(ssid: String, password: String) {
        Log.d(tag, "connectHotspot($ssid)")
        repository.wifiConnect(ssid, password)
    }

    /** Connect to an already-configured (saved) network by SSID without entering a password. */
    fun connectToSavedNetwork(ssid: String) {
        Log.d(tag, "connectToSavedNetwork($ssid)")
        repository.wifiConnectToSaved(ssid)
    }

    /** Remove the current Wi-Fi connection from the configured-network list. */
    fun forgetHotspot() {
        Log.d(tag, "forgetHotspot()")
        repository.wifiForget()
    }

    /**
     * Refresh the saved-network list.
     * In the ClusterDataBus architecture, saved networks arrive via [saveNetworkList] after
     * each scan cycle. Triggers a new scan so the list is up-to-date.
     */
    fun getSavedNetworkList() {
        Log.d(tag, "getSavedNetworkList() — triggering wifiStartScan")
        repository.wifiStartScan()
    }

    /** Mark [ssid] as the currently highlighted item in the adapter. */
    fun selectItem(ssid: String) {
        Log.d(tag, "selectItem($ssid)")
        _selectedItem.value = ssid
    }

    /**
     * Previously registered broadcast-receiver callbacks in the old WifiRepository.
     * No-op in the ClusterDataBus architecture — connection state arrives via [connectionState] flow.
     * Triggers a scan so UI is populated immediately on attach.
     */
    fun wifiConnected() {
        Log.d(tag, "wifiConnected() — triggering initial scan")
        repository.wifiStartScan()
    }

    /**
     * Previously set up a reconnect-request listener in the old WifiRepository.
     * No-op in the ClusterDataBus architecture — reconnect candidates are derived via [reconnectSSID].
     */
    fun wifiReconnectRequest() {
        Log.d(tag, "wifiReconnectRequest() — reconnect candidates derived from saveNetworkList + connectionState")
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
            return WifiViewModel(clusterApp.clusterRepository) as T
        }
    }
}
