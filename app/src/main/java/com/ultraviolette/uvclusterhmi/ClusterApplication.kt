package com.ultraviolette.uvclusterhmi

import android.app.Application
import com.ultraviolette.uvclusterhmi.data.datasource.BusDataSource
import com.ultraviolette.uvclusterhmi.data.repository.ClusterRepository
import com.ultraviolette.uvclusterhmi.data.repository.SharedPreferenceRepoImpl
import com.ultraviolette.uvclusterhmi.domain.manager.PreferenceManager

// Manual DI: application holds singleton instances injected into ViewModels via Factory.
// Note: Hilt was not used because com.google.dagger.hilt.android Gradle plugin requires
// BaseExtension, removed in AGP 9.0. Switch to Hilt once a compatible version is available.
class ClusterApplication : Application() {

    val busDataSource: BusDataSource by lazy { BusDataSource(this) }
    val clusterRepository: ClusterRepository by lazy { ClusterRepository(busDataSource) }
    val preferenceManager: PreferenceManager by lazy {
        PreferenceManager(SharedPreferenceRepoImpl(this))
    }

    override fun onCreate() {
        super.onCreate()
        busDataSource.connect()
    }

    override fun onTerminate() {
        super.onTerminate()
        busDataSource.disconnect()
    }
}