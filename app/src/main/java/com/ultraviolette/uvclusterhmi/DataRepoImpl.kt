package com.ultraviolette.uvclusterhmi

import android.Manifest
import androidx.annotation.RequiresPermission
import android.util.Log.d
class DataRepoImpl(private val dataWrapperManager: DataWrapperManager): DataRepository {
    override fun registerReceiver() {
        dataWrapperManager.registerReceiver()
    }

    override fun unregisterReceiver() {
        dataWrapperManager.unregisterReceiver()
    }

    override fun dataState(callback: (Boolean) -> Unit) {
        dataWrapperManager.dataState(callback)
    }

    override fun setDataState(isEnabled: Boolean) {
            dataWrapperManager.setDataState(isEnabled)
    }

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    override fun isDataEnable(): Boolean {
        return dataWrapperManager.isLteEnabled()
    }
    override fun startListening(onSignalChanged: (Int) -> Unit) {
	d("LTESignal","startListening_VM")
        dataWrapperManager.startListening{ level ->
	d("LTESignal","startListening_VM")
            onSignalChanged(level)
        }
  }
    override fun stopListening() {
        dataWrapperManager.stop()
    }
}
