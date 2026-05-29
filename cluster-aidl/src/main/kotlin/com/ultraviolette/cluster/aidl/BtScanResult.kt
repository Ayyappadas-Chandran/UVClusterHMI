package com.ultraviolette.cluster.aidl

import android.os.Parcel
import android.os.Parcelable

/**
 * A discovered Bluetooth device — safe to parcel across process boundaries.
 * Carries only strings; the raw [android.bluetooth.BluetoothDevice] stays in ClusterDataBus.
 *
 * @param name        Device display name, or empty string if unavailable.
 * @param address     MAC address (always available, never empty).
 * @param bondState   [android.bluetooth.BluetoothDevice].BOND_* constant:
 *                    BOND_NONE=10, BOND_BONDING=11, BOND_BONDED=12.
 */
data class BtScanResult(
    val name: String = "",
    val address: String = "",
    /** BluetoothDevice.BOND_* constant */
    val bondState: Int = 10  // BOND_NONE
) : Parcelable {

    constructor(parcel: Parcel) : this(
        name      = parcel.readString() ?: "",
        address   = parcel.readString() ?: "",
        bondState = parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(address)
        parcel.writeInt(bondState)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<BtScanResult> {
        override fun createFromParcel(parcel: Parcel): BtScanResult = BtScanResult(parcel)
        override fun newArray(size: Int): Array<BtScanResult?> = arrayOfNulls(size)
    }
}
