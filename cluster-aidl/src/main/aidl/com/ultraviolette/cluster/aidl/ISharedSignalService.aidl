package com.ultraviolette.cluster.aidl;

import com.ultraviolette.cluster.aidl.ISharedSignalCallback;
import com.ultraviolette.cluster.aidl.VehicleSnapshot;
import com.ultraviolette.cluster.aidl.BtState;
import com.ultraviolette.cluster.aidl.WifiState;

interface ISharedSignalService {
    void registerCallback(ISharedSignalCallback cb);
    void unregisterCallback(ISharedSignalCallback cb);
    VehicleSnapshot getLastVehicleSnapshot();
    BtState getLastBtState();
    WifiState getLastWifiState();

    // ── Bluetooth control ─────────────────────────────────────────────────────
    /** Enable the Bluetooth adapter. Requires BLUETOOTH_CONNECT permission on the bus process. */
    void bluetoothEnable();
    /** Disable the Bluetooth adapter. */
    void bluetoothDisable();
    /** Start device discovery; discovered devices are pushed via onBluetoothScanResult. */
    void bluetoothStartDiscovery();
    /** Initiate bonding with the device identified by MAC [address]. */
    void bluetoothCreateBond(String address);

    // ── Wi-Fi control ─────────────────────────────────────────────────────────
    /** Enable Wi-Fi. */
    void wifiEnable();
    /** Disable Wi-Fi. */
    void wifiDisable();
    /** Connect to a new network with the given SSID and password. */
    void wifiConnect(String ssid, String password);
    /** Connect to an already-configured (saved) network by SSID. */
    void wifiConnectToSaved(String ssid);
    /** Forget (remove) the currently connected or configured network. */
    void wifiForget();
    /** Trigger a new scan; results arrive via onWifiScanResult. */
    void wifiStartScan();
}
