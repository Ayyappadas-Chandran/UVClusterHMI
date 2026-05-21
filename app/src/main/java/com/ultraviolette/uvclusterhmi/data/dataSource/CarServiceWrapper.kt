package com.ultraviolette.uvclusterhmi.data.dataSource

import android.car.Car
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log.d
import android.util.Log.e
import com.ultraviolette.uvclusterhmi.utils.Utilities.PROP_ID_ABS_MODE
import com.ultraviolette.uvclusterhmi.utils.Utilities.PROP_ID_ABS_MODE_STATUS
import com.ultraviolette.uvclusterhmi.utils.Utilities.PROP_ID_CHARGER_EVT
import com.ultraviolette.uvclusterhmi.utils.Utilities.PROP_ID_CRUISE_CONTROL
import com.ultraviolette.uvclusterhmi.utils.Utilities.PROP_ID_DISPLAY_BRIGHTNESS
import com.ultraviolette.uvclusterhmi.utils.Utilities.PROP_ID_FOTA_UPDATE
import com.ultraviolette.uvclusterhmi.utils.Utilities.PROP_ID_HILL_HOLD_ICON
import com.ultraviolette.uvclusterhmi.utils.Utilities.PROP_ID_HILL_HOLD_STATE
import com.ultraviolette.uvclusterhmi.utils.Utilities.PROP_ID_INDICATOR
import com.ultraviolette.uvclusterhmi.utils.Utilities.PROP_ID_HIGH_BEAM_TELLTALE
import com.ultraviolette.uvclusterhmi.utils.Utilities.PROP_ID_HAZARD_LIGHT_TELLTALE
import com.ultraviolette.uvclusterhmi.utils.Utilities.PROP_ID_MOTOR_ARM_DISARM_TELLTALE
import com.ultraviolette.uvclusterhmi.utils.Utilities.PROP_ID_HEARTBEAT_ENABLE_DISABLE
import com.ultraviolette.uvclusterhmi.utils.Utilities.PROP_ID_VEHICLE_INFO_REQ
import com.ultraviolette.uvclusterhmi.utils.Utilities.PROP_ID_CUSTOM
import com.ultraviolette.uvclusterhmi.utils.Utilities.PROP_ID_LOCKDOWN
import com.ultraviolette.uvclusterhmi.utils.Utilities.PROP_ID_MC_NO_ARM
import com.ultraviolette.uvclusterhmi.utils.Utilities.PROP_ID_MC_THERMAL
import com.ultraviolette.uvclusterhmi.utils.Utilities.PROP_ID_MTC_MODE
import com.ultraviolette.uvclusterhmi.utils.Utilities.PROP_ID_REGEN
import com.ultraviolette.uvclusterhmi.utils.Utilities.PROP_ID_RIDE_MODES
import com.ultraviolette.uvclusterhmi.utils.Utilities.PROP_ID_RTC_TIME
import com.ultraviolette.uvclusterhmi.utils.Utilities.PROP_ID_SCREEN_MODES
import com.ultraviolette.uvclusterhmi.utils.Utilities.PROP_ID_SLEEP_WAKE
import com.ultraviolette.uvclusterhmi.utils.Utilities.PROP_ID_SWIFT_BUTTON
import com.ultraviolette.uvclusterhmi.utils.Utilities.PROP_ID_VEHICLE_VALUE

class CarServiceWrapper(private val context: Context) {
    private var car: Car? = null
    private var carPropertyManager: CarPropertyManager? = null
    private var observer: ((CarPropertyValue<Any>) -> Unit)? = null
    private val registeredProps = mutableSetOf<Int>()
    private var isConnected = false
    private val initializedProps = mutableSetOf<Int>()

    private val propertyCallback = object : CarPropertyManager.CarPropertyEventCallback {
        override fun onChangeEvent(value: CarPropertyValue<Any>?) {
            value?.let {
                d("VHALData", "Property changed ${it.propertyId}, value: ${it.value}")
                if (!initializedProps.contains(value.propertyId)) {
                    if (value.timestamp == 0L || isDefaultValue(value.value)) {
                        return
                    }
                    initializedProps.add(value.propertyId)
                }
                observer?.invoke(it)
            }
        }

        override fun onErrorEvent(propId: Int, status: Int) {
            e("VHALData", "Error event $propId, status $status")
        }
    }

    private val carServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
	    isConnected = true
            carPropertyManager = car?.getCarManager(Car.PROPERTY_SERVICE) as? CarPropertyManager

            if (carPropertyManager == null) {
                e("VHALData", "CarPropertyManager is null")
                return
            }

            try {
                registerProperId(PROP_ID_VEHICLE_VALUE)
                registerProperId(PROP_ID_REGEN)
                registerProperId(PROP_ID_ABS_MODE)
                registerProperId(PROP_ID_HILL_HOLD_ICON)
                registerProperId(PROP_ID_HILL_HOLD_STATE)
                registerProperId(PROP_ID_RTC_TIME)
                registerProperId(PROP_ID_DISPLAY_BRIGHTNESS)
                registerProperId(PROP_ID_RIDE_MODES)
                registerProperId(PROP_ID_SCREEN_MODES)
                registerProperId(PROP_ID_INDICATOR)
		registerProperId(PROP_ID_HIGH_BEAM_TELLTALE)
		registerProperId(PROP_ID_HAZARD_LIGHT_TELLTALE)
                registerProperId(PROP_ID_MOTOR_ARM_DISARM_TELLTALE)
		registerProperId(PROP_ID_HEARTBEAT_ENABLE_DISABLE)
		 registerProperId(PROP_ID_VEHICLE_INFO_REQ)
                registerProperId(PROP_ID_LOCKDOWN)
                registerProperId(PROP_ID_CUSTOM)
                registerProperId(PROP_ID_FOTA_UPDATE)
                registerProperId(PROP_ID_SWIFT_BUTTON)
                registerProperId(PROP_ID_SLEEP_WAKE)
                registerProperId(PROP_ID_ABS_MODE_STATUS)
                registerProperId(PROP_ID_MTC_MODE)
                registerProperId(PROP_ID_MC_THERMAL)
                registerProperId(PROP_ID_MC_NO_ARM)
		registerProperId(PROP_ID_CHARGER_EVT)
                registerProperId(PROP_ID_CRUISE_CONTROL)
                d("VHALData", "All properties registered safely")
            } catch (ex: Exception) {
                e("VHALData", "Exception registering props: ${ex.message}")
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            carPropertyManager = null
            isConnected = false
        }
    }

    /** Public method to register observers */
    fun observe(callback: (CarPropertyValue<Any>) -> Unit) {
        observer = callback
    }

    /** Register prop only once */
    private fun registerProperId(propId: Int) {
        if (registeredProps.contains(propId)) {
            d("VHALData", "Already registered $propId — skipping")
            return
        }

        try {
            carPropertyManager?.registerCallback(
                propertyCallback,
                propId,
                CarPropertyManager.SENSOR_RATE_FAST
            )
            registeredProps.add(propId)
            d("VHALData", "Registered propId $propId")
        } catch (e: Exception) {
            e("VHALData", "Error registering propId $propId", e)
        }
    }

    fun connect() {
        if (isConnected) {
            d("VHALData", "Already connected — skipping")
            return
        }
        car = Car.createCar(context, carServiceConnection)
        car?.connect()
    }

    fun disconnect() {
        carPropertyManager?.unregisterCallback(propertyCallback)
        registeredProps.clear()
        car?.disconnect()
        isConnected = false
    }


    fun sendBooleanProperty(propId: Int, value: Boolean) {
        try {
            carPropertyManager?.setProperty(Boolean::class.java, propId, 0, value)
        } catch (e: Exception) {
            e("VHALData", "Error sending boolean property", e)
        }
    }

    fun sendByteArrayProperty(propId: Int, value: ByteArray) {
        if (carPropertyManager == null) {
        e("VHALData", "carPropertyManager NULL")
    }
        try {
	    e("VHALData", "sendByteArrayProperty propertyId : $propId")
            carPropertyManager?.setProperty(ByteArray::class.java, propId, 0, value)
	    e("VHALDATA", "carPropertyManager SUCCESS")
        } catch (e: Exception) {
            e("VHALData", "Error sending byte array", e)
        }
    }

    fun sendFloatProperty(propId: Int, value: Float) {
        try {
            carPropertyManager?.setProperty(Float::class.java, propId, 0, value)
        } catch (e: Exception) {
            e("VHALData", "Error sending float property", e)
        }
    }

        private fun isDefaultValue(v: Any?): Boolean {
            return when (v) {
                is Int -> v == 0
                is Float -> v == 0f
                is Long -> v == 0L
                is Double -> v == 0.0
                else -> v == null
            }
        }

    }
