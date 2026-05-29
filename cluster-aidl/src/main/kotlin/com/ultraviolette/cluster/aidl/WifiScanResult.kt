package com.ultraviolette.cluster.aidl

import android.os.Parcel
import android.os.Parcelable

/**
 * A single Wi-Fi network entry discovered during a scan cycle.
 *
 * @param ssid        Network SSID (already de-quoted / trimmed).
 * @param level       Signal strength normalised to 0–5 (via WifiManager.calculateSignalLevel).
 * @param isSecured   True if the network requires a credential (WEP/WPA/WPA2/WPA3).
 * @param isSaved     True if this SSID is in the device's configured-network list.
 */
data class WifiScanResult(
    val ssid: String = "",
    val level: Int = 0,
    val isSecured: Boolean = false,
    val isSaved: Boolean = false
) : Parcelable {

    constructor(parcel: Parcel) : this(
        ssid      = parcel.readString() ?: "",
        level     = parcel.readInt(),
        isSecured = parcel.readInt() != 0,
        isSaved   = parcel.readInt() != 0
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(ssid)
        parcel.writeInt(level)
        parcel.writeInt(if (isSecured) 1 else 0)
        parcel.writeInt(if (isSaved) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<WifiScanResult> {
        override fun createFromParcel(parcel: Parcel): WifiScanResult = WifiScanResult(parcel)
        override fun newArray(size: Int): Array<WifiScanResult?> = arrayOfNulls(size)
    }
}
