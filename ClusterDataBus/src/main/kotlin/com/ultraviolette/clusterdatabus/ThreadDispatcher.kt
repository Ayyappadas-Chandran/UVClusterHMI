package com.ultraviolette.clusterdatabus

import android.os.Handler
import android.os.HandlerThread

/** Single background HandlerThread for all off-Binder-thread dispatching. */
class ThreadDispatcher(name: String = "ClusterBusDispatcher") {

    private val handlerThread = HandlerThread(name).also { it.start() }
    private val handler = Handler(handlerThread.looper)

    fun post(action: Runnable) {
        handler.post(action)
    }

    fun shutdown() {
        handlerThread.quitSafely()
    }
}
