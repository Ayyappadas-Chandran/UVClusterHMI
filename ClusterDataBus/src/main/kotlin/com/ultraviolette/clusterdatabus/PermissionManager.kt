package com.ultraviolette.clusterdatabus

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

/** Checks whether required upstream service packages are installed and bindable. */
class PermissionManager(private val context: Context) {

    private val tag = "ClusterBus.PermissionManager"

    fun canBindCarPropertyService(): Boolean =
        isPackageInstalled(PKG_CAR_PROPERTY).also {
            if (!it) Log.w(tag, "CarPropertyService package not found: $PKG_CAR_PROPERTY")
        }

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
