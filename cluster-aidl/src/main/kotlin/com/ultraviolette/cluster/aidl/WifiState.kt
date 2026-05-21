package com.ultraviolette.cluster.aidl

import android.os.Parcel
import android.os.Parcelable

data class WifiState(
    val isEnabled: Boolean = false,
    val connectedSsid: String = "",
    /** Signal level 0–5 */
    val signalLevel: Int = 0,
    val isConnected: Boolean = false
) : Parcelable {

    constructor(parcel: Parcel) : this(
        isEnabled = parcel.readByte() != 0.toByte(),
        connectedSsid = parcel.readString() ?: "",
        signalLevel = parcel.readInt(),
        isConnected = parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (isEnabled) 1 else 0)
        parcel.writeString(connectedSsid)
        parcel.writeInt(signalLevel)
        parcel.writeByte(if (isConnected) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<WifiState> {
        override fun createFromParcel(parcel: Parcel): WifiState = WifiState(parcel)
        override fun newArray(size: Int): Array<WifiState?> = arrayOfNulls(size)
    }
}
