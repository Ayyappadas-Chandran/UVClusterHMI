package com.ultraviolette.uvclusterhmi.ui.features.settings.wifi

import android.net.wifi.WifiConfiguration
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ultraviolette.uvclusterhmi.domain.repository.WifiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import kotlin.collections.map

/**
 * ViewModel responsible for managing Wi-Fi operations and exposing scan results to the UI.
 * Acts as a mediator between the UI and [com.ultraviolette.uvclusterhmi.domain.repository.WifiRepository].
 *
 * @param wifiRepository The repository handling Wi-Fi-related operations.
 */
class WifiViewModel(private val wifiRepository: WifiRepository): ViewModel() {
//    private var _scanResult  = MutableLiveData<List<String>>()
//
//    /**
//     * LiveData containing the list of discovered Wi-Fi SSIDs.
//     */
//    val scanResult : LiveData<List<String>>
//        get() = _scanResult

    private val TAG = "WifiViewModel"

    private val _currentSignalLevel = MutableStateFlow(0)
    val currentSignalLevel: StateFlow<Int> = _currentSignalLevel.asStateFlow()
    private val _scanResult = MutableLiveData<List<WifiUiModel>>()

    val scanResult: LiveData<List<WifiUiModel>>
        get() = _scanResult

    private val _saveNetworkList = MutableLiveData<List<WifiUiModel>>()

    val saveNetworkList: LiveData<List<WifiUiModel>>
        get() = _saveNetworkList

    //27/01/2026
    private var _connectionState = MutableStateFlow<Boolean>(false)

    val connectionState : StateFlow<Boolean>
            = _connectionState.asStateFlow()

    private var _wifiState = MutableStateFlow(isWifiEnabled())
    val onWifiStateChange : StateFlow<Boolean>
        get() = _wifiState.asStateFlow()

    private var _connectedSSID = MutableStateFlow<String?>(null)
    val connectedSSID : StateFlow<String?>
        get() = _connectedSSID.asStateFlow()

    private var _reconnectSSID = MutableStateFlow<String?>(null)
    val reconnectSSID : StateFlow<String?>
        get() = _reconnectSSID.asStateFlow()

    private val _selectedItem = MutableStateFlow<String?>(null)
    val selectedItem: StateFlow<String?> = _selectedItem.asStateFlow()


    fun startSignalMonitoring() {
        viewModelScope.launch {
            while (true) {
                if (wifiRepository.isWifiEnabled()) {
                    val level = wifiRepository.getCurrentSignalLevel()
                    _currentSignalLevel.value = level
                } else {
                    _currentSignalLevel.value = 0
                }
                delay(2000)
            }
        }
    }
    fun wifiStateChange(){
        wifiRepository.wifiStateChange {
            _wifiState.value = it
        }
    }

    fun registerWifiStateChangeReceiver(){
        wifiRepository.registerWifiStateChangeReceiver()
    }

    fun unregisterWifiStateChangeReceiver(){
        wifiRepository.unregisterWifiStateChangeReceiver()
    }



    /**
     * Checks whether Wi-Fi is currently enabled on the device.
     *
     * @return true if Wi-Fi is enabled, false otherwise.
     */
    fun isWifiEnabled(): Boolean = wifiRepository.isWifiEnabled()

    /**
     * Connects to a specified Wi-Fi hotspot using SSID and password.
     *
     * @param ssid The SSID of the hotspot.
     * @param password The password for the hotspot.
     */
    fun connectHotspot(ssid: String, password: String) = wifiRepository.connectHotspot(ssid, password, true)

    /**
     * Initiates a scan to discover available Wi-Fi networks.
     */
    fun startScan() = wifiRepository.startScan()

    /**
     * Returns the SSID of the currently connected Wi-Fi network.
     *
     * @return SSID as a string, or null if not connected.
     */
    fun getConnectedWifiSSID() = wifiRepository.getConnectedWifiSSID()

    /**
     * Forgets the currently connected or previously remembered Wi-Fi hotspot.
     */
    fun forgetHotspot() = wifiRepository.forgetHotspot()

    /**
     * Fetches the scan results asynchronously and updates [scanResult] LiveData.
     */
//    fun scanResult() {
//        //for bug no 42 - Fix the Duplicates wifi  network
//        wifiRepository.scanResult { result ->
//            _scanResult.value = result.distinct()
//        }
//    }
    fun scanResult() {
        wifiRepository.scanResult { results ->
            Log.d(TAG, "scanResult:  wifiRepository.scanResult :: $results")
            _scanResult.value = results.map { result ->
                WifiUiModel(
                    ssid = result.SSID,
                    level = android.net.wifi.WifiManager.calculateSignalLevel(result.level, 6), // normalize to 0–5
                    isSecured = result.capabilities.contains("WEP") ||
                            result.capabilities.contains("WPA") ||
                            result.capabilities.contains("WPA2") ||
                            result.capabilities.contains("WPA3")
                )
            }
        }
    }


    fun getSavedNetworkList(){
        wifiRepository.savedNetworkList{savedList->
            val tempSavedNetworkList= savedList.toUiModels()
            _saveNetworkList.value =  tempSavedNetworkList
        }
    }


    fun isSavedNetworkListEmpty(): Boolean {
        return _saveNetworkList.value.isNullOrEmpty()
    }

    fun connectToSavedNetwork(ssid: String){
        wifiRepository.connectToSavedNetwork(ssid)
    }



    fun List<WifiConfiguration>.toUiModels(): List<WifiUiModel> {
        return this.map { config ->
            val ssid = config.SSID.trim('"')   // remove quotes around SSID

            val isSecured = config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK) ||
                    config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP) ||
                    config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X) ||
                    config.wepKeys.any { !it.isNullOrBlank() }

            WifiUiModel(
                ssid = ssid,
                level = 0,
                isSecured = isSecured
            )
        }
    }

    fun isNetworkConnected(): Boolean {
        val ssid = _connectedSSID.value
        return ssid != null && ssid != "<unknown ssid>"
    }


    fun isConnectionStateActive(): Boolean{
        return _connectionState.value
    }

    fun enableWifi(enable:Boolean){
        wifiRepository.enableWifi(enable)
    }

    //27/01/2026
    fun wifiConnected(){
        Log.d(TAG, "wifiConnected: Entry")
        wifiRepository.connectionState {isConnected->
            Log.d(TAG, "wifiConnected: Connection state Changed :: $isConnected")
            _connectionState.value = isConnected
        }
        getConnectedSSID()
        getSavedNetworkList()
        startScan()
    }

    ///WIFFI
    fun autoConnectWifi() {
        wifiRepository.startScan()
    }

    fun getConnectedSSID() {
        wifiRepository.connectedSSID{ssid->
            _connectedSSID.value = ssid
        }

    }

    fun wifiReconnectRequest() {
        wifiRepository.reconnectRequestSSID{ssid->
            _reconnectSSID.value = ssid
        }

    }

    fun selectItem(ssid: String) {
        _selectedItem.value = ssid
    }

    ///END

}



