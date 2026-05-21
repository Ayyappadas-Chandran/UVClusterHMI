package com.ultraviolette.uvclusterhmi.domain.repository

import com.google.gson.reflect.TypeToken

interface SharedPreferenceRepository {

    /**
     * Retrieves a value from shared preferences.
     *
     * @param key The key identifying the preference.
     * @param defaultValue The value to return if the key doesn't exist.
     * @return The stored value, or [defaultValue] if not found.
     */
    fun <T> getPref(key: String, defaultValue: T): T

    /**
     * Saves a value to shared preferences.
     *
     * @param key The key to store the value under.
     * @param value The value to be saved.
     */
    fun <T> savePref(key: String, value: T)

    /**
     * Clears all entries in shared preferences.
     */
    fun clearPreferences()

    /**
     * Removes a specific entry from shared preferences.
     *
     * @param key The key of the preference to remove.
     */
    fun removePref(key: String)

    fun <T> saveModel(key: String, model: T)
    fun <T> getModel(key: String, typeToken: TypeToken<T>): T?
}
