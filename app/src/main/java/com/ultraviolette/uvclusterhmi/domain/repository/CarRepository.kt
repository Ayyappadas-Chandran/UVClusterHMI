package com.ultraviolette.uvclusterhmi.domain.repository

import android.car.hardware.CarPropertyValue

/**
 * Interface defining the contract for car hardware interaction.
 * Provides methods to connect, disconnect, send data, and observe car properties.
 */
interface CarRepository {

    /**
     * Establishes a connection to the car hardware service.
     */
    fun connect()

    /**
     * Disconnects from the car hardware service.
     */
    fun disconnect()

    /**
     * Sends a boolean property value to the car system.
     *
     * @param propertyId The ID of the car property to update.
     * @param value The boolean value to send.
     */
    fun sendBoolean(propertyId: Int, value: Boolean)

    /**
     * Sends a byte array property value to the car system.
     *
     * @param propId The ID of the car property to update.
     * @param value The byte array data to send.
     */
    fun sendByteArray(propId: Int, value: ByteArray)

    /**
     * Observes updates to car properties and notifies via the callback.
     *
     * @param callback Lambda invoked when a property update occurs, providing [CarPropertyValue].
     */
    fun observeProperties(callback: (CarPropertyValue<Any>) -> Unit)
}
