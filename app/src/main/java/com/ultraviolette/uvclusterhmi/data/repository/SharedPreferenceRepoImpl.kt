package com.ultraviolette.uvclusterhmi.data.repository

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ultraviolette.uvclusterhmi.domain.repository.SharedPreferenceRepository
import com.ultraviolette.uvclusterhmi.domain.manager.PreferenceManager.Companion.PREF_CLUSTER_UV

/**
 * Implementation of [SharedPreferenceRepository] that handles read and write operations
 * for primitive data types using Android's SharedPreferences.
 *
 * @param context Application or Activity context used to initialize SharedPreferences.
 */
class SharedPreferenceRepoImpl(context: Context) : SharedPreferenceRepository {
    /**
     * Lazily initialized instance of SharedPreferences.
     * Uses the name [PREF_CLUSTER_UV] and private mode.
     */
    private val sharedPref by lazy {
        context.getSharedPreferences(PREF_CLUSTER_UV, Context.MODE_PRIVATE)
    }

    private val gson = Gson()


    /**
     * Retrieves a value from SharedPreferences.
     *
     * @param key The name of the preference.
     * @param defaultValue The default value to return if the preference does not exist.
     * @throws IllegalArgumentException if the type of [defaultValue] is not supported.
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T> getPref(key: String, defaultValue: T): T {
        return when (defaultValue) {
            is String -> sharedPref.getString(key, defaultValue) as T
            is Int -> sharedPref.getInt(key, defaultValue) as T
            is Long -> sharedPref.getLong(key, defaultValue) as T
            is Float -> sharedPref.getFloat(key, defaultValue) as T
            is Boolean -> sharedPref.getBoolean(key, defaultValue) as T
            else -> throw IllegalArgumentException("Unsupported Type")
        }
    }

    /**
     * Saves a value to SharedPreferences.
     *
     * @param key The name of the preference to modify.
     * @param value The value to store. Must be a supported type: String, Int, Float, Long, or Boolean.
     * @throws IllegalArgumentException if the type of [value] is not supported.
     */
    override fun <T> savePref(key: String, value: T) {
        sharedPref.edit {
            when (value) {
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Float -> putFloat(key, value)
                is Long -> putLong(key, value)
                is Boolean -> putBoolean(key, value)
                else -> throw IllegalArgumentException("Unsupported Type")
            }
        }
    }

    /**
     * Removes a specific preference entry.
     * @param key The name of the preference to remove.
     */
    override fun removePref(key: String) {
        sharedPref.edit { remove(key) }
    }

    override fun <T> saveModel(key: String, model: T) {
        val json = gson.toJson(model)
        sharedPref.edit {
            putString(key, json)
        }
    }

    override fun <T> getModel(
        key: String,
        typeToken: TypeToken<T>
    ): T? {
        val json = sharedPref.getString(key, null) ?: return null
        return gson.fromJson<T>(json, typeToken.type)
    }

    /**
     * Clears all stored preferences.
     */
    override fun clearPreferences() {
        sharedPref.edit { clear() }
    }
}