package com.ultraviolette.cluster.aidl

import android.os.Parcel
import android.os.Parcelable

data class BtState(
    val isEnabled: Boolean = false,
    val pairedDeviceName: String = "",
    val pairedDeviceAddress: String = "",
    /** BluetoothDevice.BOND_* constant (BOND_NONE = 10) */
    val bondState: Int = 10,
    /** 0=idle, 1=ringing, 2=offhook */
    val callState: Int = 0,
    val a2dpConnected: Boolean = false,
    val hfpConnected: Boolean = false
) : Parcelable {

    constructor(parcel: Parcel) : this(
        isEnabled = parcel.readByte() != 0.toByte(),
        pairedDeviceName = parcel.readString() ?: "",
        pairedDeviceAddress = parcel.readString() ?: "",
        bondState = parcel.readInt(),
        callState = parcel.readInt(),
        a2dpConnected = parcel.readByte() != 0.toByte(),
        hfpConnected = parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (isEnabled) 1 else 0)
        parcel.writeString(pairedDeviceName)
        parcel.writeString(pairedDeviceAddress)
        parcel.writeInt(bondState)
        parcel.writeInt(callState)
        parcel.writeByte(if (a2dpConnected) 1 else 0)
        parcel.writeByte(if (hfpConnected) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<BtState> {
        override fun createFromParcel(parcel: Parcel): BtState = BtState(parcel)
        override fun newArray(size: Int): Array<BtState?> = arrayOfNulls(size)
    }
}
