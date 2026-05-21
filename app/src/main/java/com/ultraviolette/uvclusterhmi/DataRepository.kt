
package com.ultraviolette.uvclusterhmi

interface DataRepository {

    fun registerReceiver()
    fun unregisterReceiver()
    fun dataState(callback: (Boolean) -> Unit)
    fun setDataState(isEnabled: Boolean)

    fun isDataEnable():Boolean
    fun startListening(onSignalChanged: (Int) -> Unit)
    fun stopListening()
}
