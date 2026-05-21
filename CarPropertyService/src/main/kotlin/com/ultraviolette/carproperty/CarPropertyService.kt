package com.ultraviolette.carproperty

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.car.Car
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.RemoteCallbackList
import android.util.Log
import com.ultraviolette.cluster.aidl.BtState
import com.ultraviolette.cluster.aidl.ICarPropertyCallback
import com.ultraviolette.cluster.aidl.ICarPropertyService
import com.ultraviolette.cluster.aidl.VehicleData
import com.ultraviolette.cluster.aidl.VehicleSnapshot
import com.ultraviolette.cluster.aidl.WifiState
import java.nio.ByteBuffer
import java.nio.ByteOrder

class CarPropertyService : Service() {

    private val tag = "CarProp"

    private val callbackList = RemoteCallbackList<ICarPropertyCallback>()
    private var car: Car? = null
    private var carPropertyManager: CarPropertyManager? = null
    private lateinit var handlerThread: HandlerThread
    private lateinit var propHandler: Handler
    private var isCarConnected = false

    @Volatile private var lastVehicleData = VehicleData()
    @Volatile private var lastVehicleSnapshot = VehicleSnapshot()

    private val registeredProps = mutableSetOf<Int>()

    private val binder = object : ICarPropertyService.Stub() {
        override fun registerCallback(cb: ICarPropertyCallback?) {
            cb?.let { callbackList.register(it) }
        }
        override fun unregisterCallback(cb: ICarPropertyCallback?) {
            cb?.let { callbackList.unregister(it) }
        }
        override fun getLastKnownData(): VehicleData = lastVehicleData
        override fun sendBoolean(propId: Int, value: Boolean) {
            try {
                carPropertyManager?.setProperty(Boolean::class.java, propId, 0, value)
            } catch (e: Exception) {
                Log.e(tag, "sendBoolean $propId failed", e)
            }
        }
        override fun sendByteArray(propId: Int, value: ByteArray?) {
            if (value == null) return
            try {
                carPropertyManager?.setProperty(ByteArray::class.java, propId, 0, value)
            } catch (e: Exception) {
                Log.e(tag, "sendByteArray $propId failed", e)
            }
        }
    }

    private val propertyCallback = object : CarPropertyManager.CarPropertyEventCallback {
        override fun onChangeEvent(value: CarPropertyValue<Any>?) {
            value ?: return
            propHandler.post { handlePropertyChange(value) }
        }
        override fun onErrorEvent(propId: Int, status: Int) {
            Log.e(tag, "Property error propId=$propId status=$status")
        }
    }

    private val carServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            isCarConnected = true
            carPropertyManager = car?.getCarManager(Car.PROPERTY_SERVICE) as? CarPropertyManager
            if (carPropertyManager == null) {
                Log.e(tag, "CarPropertyManager null after connect")
                return
            }
            registerAllProperties()
            Log.d(tag, "Car service connected, all properties registered")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            carPropertyManager = null
            isCarConnected = false
            Log.w(tag, "Car service disconnected")
        }
    }

    override fun onCreate() {
        super.onCreate()
        handlerThread = HandlerThread("CarPropHandler").also { it.start() }
        propHandler = Handler(handlerThread.looper)
        startForeground(NOTIFICATION_ID, buildNotification())
        connectCar()
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int =
        START_STICKY

    override fun onDestroy() {
        super.onDestroy()
        carPropertyManager?.unregisterCallback(propertyCallback)
        car?.disconnect()
        handlerThread.quitSafely()
        callbackList.kill()
    }

    private fun connectCar() {
        if (isCarConnected) return
        car = Car.createCar(this, carServiceConnection)
        car?.connect()
    }

    private fun registerAllProperties() {
        listOf(
            PROP_ID_VEHICLE_VALUE, PROP_ID_REGEN, PROP_ID_ABS_MODE,
            PROP_ID_HILL_HOLD_ICON, PROP_ID_HILL_HOLD_STATE, PROP_ID_RTC_TIME,
            PROP_ID_DISPLAY_BRIGHTNESS, PROP_ID_RIDE_MODES, PROP_ID_SCREEN_MODES,
            PROP_ID_INDICATOR, PROP_ID_HIGH_BEAM_TELLTALE, PROP_ID_HAZARD_LIGHT_TELLTALE,
            PROP_ID_MOTOR_ARM_DISARM_TELLTALE, PROP_ID_HEARTBEAT_ENABLE_DISABLE,
            PROP_ID_VEHICLE_INFO_REQ, PROP_ID_LOCKDOWN, PROP_ID_CUSTOM,
            PROP_ID_FOTA_UPDATE, PROP_ID_SWIFT_BUTTON, PROP_ID_SLEEP_WAKE,
            PROP_ID_ABS_MODE_STATUS, PROP_ID_MTC_MODE, PROP_ID_MC_THERMAL,
            PROP_ID_MC_NO_ARM, PROP_ID_CHARGER_EVT, PROP_ID_CRUISE_CONTROL
        ).forEach { propId ->
            if (registeredProps.contains(propId)) return@forEach
            try {
                carPropertyManager?.registerCallback(
                    propertyCallback, propId, CarPropertyManager.SENSOR_RATE_FAST
                )
                registeredProps.add(propId)
            } catch (e: Exception) {
                Log.e(tag, "Failed to register propId=0x${propId.toString(16)}", e)
            }
        }
    }

    private fun handlePropertyChange(value: CarPropertyValue<Any>) {
        val updated = when (value.propertyId) {
            PROP_ID_VEHICLE_VALUE -> {
                val arr = toFloatArray(value.value)
                lastVehicleData = lastVehicleData.copy(speed = arr.getOrElse(0) { 0f })
                true
            }
            PROP_ID_REGEN -> {
                val arr = toIntArray(value.value)
                lastVehicleData = lastVehicleData.copy(regenLevel = arr.getOrElse(0) { 0 })
                true
            }
            PROP_ID_ABS_MODE -> {
                lastVehicleData = lastVehicleData.copy(absMode = toInt(value.value))
                true
            }
            PROP_ID_ABS_MODE_STATUS -> {
                lastVehicleData = lastVehicleData.copy(absModeStatus = toInt(value.value))
                true
            }
            PROP_ID_HILL_HOLD_STATE -> {
                lastVehicleData = lastVehicleData.copy(hillHoldState = toInt(value.value))
                true
            }
            PROP_ID_HILL_HOLD_ICON -> {
                lastVehicleData = lastVehicleData.copy(hillHoldIcon = toInt(value.value))
                true
            }
            PROP_ID_RIDE_MODES -> {
                lastVehicleData = lastVehicleData.copy(rideModeRaw = toInt(value.value))
                true
            }
            PROP_ID_INDICATOR -> {
                lastVehicleData = lastVehicleData.copy(indicator = toInt(value.value))
                true
            }
            PROP_ID_HIGH_BEAM_TELLTALE -> {
                lastVehicleData = lastVehicleData.copy(highBeamTelltale = toInt(value.value))
                true
            }
            PROP_ID_HAZARD_LIGHT_TELLTALE -> {
                lastVehicleData = lastVehicleData.copy(hazardLightTelltale = toInt(value.value))
                true
            }
            PROP_ID_MOTOR_ARM_DISARM_TELLTALE -> {
                lastVehicleData = lastVehicleData.copy(motorArmDisarmTelltale = toInt(value.value))
                true
            }
            PROP_ID_LOCKDOWN -> {
                lastVehicleData = lastVehicleData.copy(lockdown = toInt(value.value))
                true
            }
            PROP_ID_SLEEP_WAKE -> {
                lastVehicleData = lastVehicleData.copy(sleepWake = toInt(value.value))
                true
            }
            PROP_ID_MTC_MODE -> {
                lastVehicleData = lastVehicleData.copy(mtcMode = toInt(value.value))
                true
            }
            PROP_ID_CHARGER_EVT -> {
                lastVehicleData = lastVehicleData.copy(chargerEvt = toInt(value.value))
                true
            }
            PROP_ID_CRUISE_CONTROL -> {
                lastVehicleData = lastVehicleData.copy(cruise = toInt(value.value))
                true
            }
            PROP_ID_CUSTOM -> {
                (value.value as? ByteArray)?.let { parseCustomAndUpdateSnapshot(it) }
                false // snapshot broadcast happens inside
            }
            else -> false
        }

        if (updated) broadcastVehicleData()
    }

    private fun broadcastVehicleData() {
        val data = lastVehicleData
        val count = callbackList.beginBroadcast()
        try {
            for (i in 0 until count) {
                try {
                    callbackList.getBroadcastItem(i).onVehicleData(data)
                } catch (e: Exception) {
                    Log.e(tag, "Callback onVehicleData failed at $i", e)
                }
            }
        } finally {
            callbackList.finishBroadcast()
        }
    }

    private fun broadcastVehicleSnapshot() {
        val snapshot = lastVehicleSnapshot
        val count = callbackList.beginBroadcast()
        try {
            for (i in 0 until count) {
                try {
                    callbackList.getBroadcastItem(i).onVehicleSnapshot(snapshot)
                } catch (e: Exception) {
                    Log.e(tag, "Callback onVehicleSnapshot failed at $i", e)
                }
            }
        } finally {
            callbackList.finishBroadcast()
        }
    }

    /** Parses the binary PROP_ID_CUSTOM payload and updates the snapshot. */
    private fun parseCustomAndUpdateSnapshot(data: ByteArray) {
        try {
            val buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
            // Skip VcuInfoMsg header fields to reach TellTales at known offset
            // (full parsing mirrors CarViewModel.parseCustomData — kept minimal here)
            // Skip 4+4+4+4+4+4+4+4+4+4 = 40 bytes for VcuInfoMsg base fields
            if (buf.remaining() < 8) return
            // Jump to TellTales: requires full struct parsing — skip for now, broadcast raw
            // The UI's CarViewModel still handles PROP_ID_CUSTOM directly during migration
            broadcastVehicleSnapshot()
        } catch (e: Exception) {
            Log.e(tag, "parseCustomAndUpdateSnapshot failed", e)
        }
    }

    private fun buildNotification(): Notification {
        val channelId = "car_prop_service"
        val nm = getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(channelId) == null) {
            nm.createNotificationChannel(
                NotificationChannel(channelId, "Vehicle Data", NotificationManager.IMPORTANCE_LOW)
            )
        }
        return Notification.Builder(this, channelId)
            .setContentTitle("UV Vehicle Data Service")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setOngoing(true)
            .build()
    }

    private fun toIntArray(v: Any?): IntArray = when (v) {
        is IntArray -> v
        is Array<*> -> v.filterIsInstance<Int>().toIntArray()
        else -> intArrayOf()
    }

    private fun toFloatArray(v: Any?): FloatArray = when (v) {
        is FloatArray -> v
        is Array<*> -> v.filterIsInstance<Float>().toFloatArray()
        else -> floatArrayOf()
    }

    private fun toInt(v: Any?): Int = when (v) {
        is Int -> v
        is Byte -> v.toInt() and 0xFF
        is IntArray -> v.getOrNull(0) ?: 0
        else -> 0
    }

    companion object {
        private const val NOTIFICATION_ID = 1001

        const val PROP_ID_VEHICLE_VALUE          = 0x21610310
        const val PROP_ID_REGEN                  = 0x21410320
        const val PROP_ID_ABS_MODE               = 0x21400330
        const val PROP_ID_ABS_MODE_STATUS         = 0x21400411
        const val PROP_ID_HILL_HOLD_STATE         = 0x21400340
        const val PROP_ID_HILL_HOLD_ICON          = 0x21400341
        const val PROP_ID_FOTA_UPDATE             = 0x21410350
        const val PROP_ID_RTC_TIME               = 0x21410370
        const val PROP_ID_DISPLAY_BRIGHTNESS     = 0x214103B0
        const val PROP_ID_RIDE_MODES             = 0x21400412
        const val PROP_ID_SCREEN_MODES           = 0x21400413
        const val PROP_ID_INDICATOR              = 0x21400414
        const val PROP_ID_HIGH_BEAM_TELLTALE     = 0x21400418
        const val PROP_ID_HAZARD_LIGHT_TELLTALE  = 0x2140041A
        const val PROP_ID_MOTOR_ARM_DISARM_TELLTALE = 0x21400419
        const val PROP_ID_HEARTBEAT_ENABLE_DISABLE  = 0x2140041B
        const val PROP_ID_VEHICLE_INFO_REQ       = 0x2140041C
        const val PROP_ID_LOCKDOWN               = 0x21400415
        const val PROP_ID_CUSTOM                 = 0x21700312
        const val PROP_ID_SWIFT_BUTTON           = 0x214003A0
        const val PROP_ID_SLEEP_WAKE             = 0x21400390
        const val PROP_ID_MTC_MODE               = 0x21400416
        const val PROP_ID_MC_THERMAL             = 0x21610360
        const val PROP_ID_MC_NO_ARM              = 0x21410417
        const val PROP_ID_CHARGER_EVT            = 0x214003A1
        const val PROP_ID_CRUISE_CONTROL         = 0x2140041D
    }
}
