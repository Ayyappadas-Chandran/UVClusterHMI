package com.ultraviolette.clusterdatabus

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat

/** Checks whether required upstream service packages are installed and bindable. */
class PermissionManager(private val context: Context) {

    private val tag = "ClusterBus.PermissionManager"

    // ── Car-property service ──────────────────────────────────────────────────

    fun canBindCarPropertyService(): Boolean =
        isPackageInstalled(PKG_CAR_PROPERTY).also {
            if (!it) Log.w(tag, "CarPropertyService package not found: $PKG_CAR_PROPERTY")
        }

    // ── WiFi permissions ──────────────────────────────────────────────────────

    /**
     * Returns true if this process can read Wi-Fi scan results.
     *
     * - API 29–30: requires ACCESS_FINE_LOCATION at runtime.
     * - API 31+  : NEARBY_WIFI_DEVICES (with neverForLocation in manifest) is sufficient.
     *              ACCESS_FINE_LOCATION is accepted as fallback.
     */
    fun canReadWifiScanResults(): Boolean {
        val hasLocation = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        val hasNearbyWifi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasPermission(Manifest.permission.NEARBY_WIFI_DEVICES)
        } else false
        return hasLocation || hasNearbyWifi
    }

    /**
     * Returns true if this process can read the connected network's SSID.
     * Same requirements as [canReadWifiScanResults].
     */
    fun canReadConnectedSsid(): Boolean = canReadWifiScanResults()

    /**
     * Logs the runtime permission status for all Wi-Fi and Bluetooth permissions.
     * Call once on service start for a startup audit.
     */
    fun logPermissionAudit() {
        val permissions = buildMap<String, String> {
            put("ACCESS_FINE_LOCATION",   Manifest.permission.ACCESS_FINE_LOCATION)
            put("ACCESS_COARSE_LOCATION", Manifest.permission.ACCESS_COARSE_LOCATION)
            put("BLUETOOTH_SCAN",         Manifest.permission.BLUETOOTH_SCAN)
            put("BLUETOOTH_CONNECT",      Manifest.permission.BLUETOOTH_CONNECT)
            put("CHANGE_WIFI_STATE",      Manifest.permission.CHANGE_WIFI_STATE)
            put("ACCESS_WIFI_STATE",      Manifest.permission.ACCESS_WIFI_STATE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                put("NEARBY_WIFI_DEVICES", Manifest.permission.NEARBY_WIFI_DEVICES)
            }
        }
        permissions.forEach { (name, perm) ->
            val granted = hasPermission(perm)
            if (granted) Log.d(tag, "PERMISSION ✓ $name")
            else         Log.w(tag, "PERMISSION ✗ $name — not granted to this process; " +
                "Wi-Fi scan results will be read by the HMI app process instead.")
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    fun hasPermission(permission: String): Boolean =
        ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    private fun isPackageInstalled(packageName: String): Boolean = try {
        context.packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }

    companion object {
        const val PKG_CAR_PROPERTY = "com.ultraviolette.carproperty"
        const val ACTION_CAR_PROPERTY = "com.ultraviolette.carproperty.ICarPropertyService"
    }
}
