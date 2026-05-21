package com.ultraviolette.uvclusterhmi.data.repository

import android.car.hardware.CarPropertyValue
import com.ultraviolette.uvclusterhmi.data.dataSource.CarServiceWrapper
import com.ultraviolette.uvclusterhmi.domain.repository.CarRepository

/**
 * Implementation of [CarRepository] that interacts with the vehicle's
 * car hardware services via [CarServiceWrapper].
 *
 * @property carServiceWrapper The wrapper that handles low-level car service interactions.
 */
class CarRepoImpl(private val carServiceWrapper: CarServiceWrapper) : CarRepository {

    /**
     * Establishes a connection to the car service.
     */
    override fun connect() {
        carServiceWrapper.connect()
    }

    /**
     * Disconnects from the car service.
     */
    override fun disconnect() {
        carServiceWrapper.disconnect()
    }

    /**
     * Sends a boolean property value to the car system.
     *
     * @param propertyId The ID of the car property.
     * @param value The boolean value to send.
     */
    override fun sendBoolean(propertyId: Int, value: Boolean) {
        carServiceWrapper.sendBooleanProperty(propertyId, value)
    }

    /**
     * Sends a byte array property value to the car system.
     *
     * @param propId The ID of the car property.
     * @param value The byte array value to send.
     */
    override fun sendByteArray(propId: Int, value: ByteArray) {
        carServiceWrapper.sendByteArrayProperty(propId, value)
    }

    /**
     * Observes car property updates and invokes the provided callback
     * when a property changes.
     *
     * @param callback A lambda function called with the updated [CarPropertyValue].
     */
    override fun observeProperties(callback: (CarPropertyValue<Any>) -> Unit) {
        carServiceWrapper.observe(callback)
    }
}
