package com.ultraviolette.uvclusterhmi.domain.manager

import androidx.annotation.StyleRes
import com.google.gson.reflect.TypeToken
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.dataModel.TripDetails
import com.ultraviolette.uvclusterhmi.domain.repository.SharedPreferenceRepository

class PreferenceManager(val sharedPreferenceRepository: SharedPreferenceRepository) {

    /** getCharger State */
    val isChargeEnable:Boolean
        get()= sharedPreferenceRepository.getPref(PREF_CHARGE_STATE,false)

    /** get brightness value */
    val brightnessValue: Int
        get() = sharedPreferenceRepository.getPref(PREF_BRIGHTNESS, 0)

    /** get brightness state */
    val isAutoBrightnessEnabled: Boolean
        get() = sharedPreferenceRepository.getPref(PREF_BRIGHTNESS_STATE, true)

    /** get Volume Value*/
    val volumeValue: Int
        get() = sharedPreferenceRepository.getPref(PREF_VOLUME, 0)

    /** get Theme state*/
    val isParallaxEnabled: Boolean
        get() = sharedPreferenceRepository.getPref(PREF_THEME, true)

    /** get Mode state */
    val mode: String
        get() = sharedPreferenceRepository.getPref(PREF_MODE, "Auto")

    val playUpdate :Boolean
        get() = sharedPreferenceRepository.getPref(PREF_UPDATE,true)

    /** get date and time state */
    val isAutoDateTimeEnabled: Boolean
        get() = sharedPreferenceRepository.getPref(PREF_DATE_TIME, true)

    /** get language*/
    val language: String
        get() = sharedPreferenceRepository.getPref(PREF_LANGUAGE, "English")

    /** batteryLimit */
    val batteryLimit: Int
        get() = sharedPreferenceRepository.getPref(PREF_BATTERY_LIMIT, 80)

    /** get Camera */
    val isCameraOn: Boolean
        get() = sharedPreferenceRepository.getPref(PREF_CAMERA, false)

    /** get Radar */
    val isRadarOn: Boolean
        get() = sharedPreferenceRepository.getPref(PREF_RADAR, false)

    /** TimeFormat */
    val isNormalTimeFormat: Boolean
        get() = sharedPreferenceRepository.getPref(PREF_TIME_FORMAT, true)

    /** Console Alerts */
    val isConsoleAlertsOn: Boolean
        get() = sharedPreferenceRepository.getPref(PREF_CONSOLE_ALERTS, false)

    /** Mirror Alerts */
    val isMirrorAlertsOn: Boolean
        get() = sharedPreferenceRepository.getPref(PREF_MIRROR_ALERTS, false)

    /** Audio Alerts */
    val isAudioAlertsOn: Boolean
        get() = sharedPreferenceRepository.getPref(PREF_AUDIO_ALERTS, false)

    /** Abs */
    val isMonoAbs: Boolean
        get() = sharedPreferenceRepository.getPref(PREF_CUSTOM_MODE_ABS, true)

    /** Cruise */
    val isCruise: Boolean
        get() = sharedPreferenceRepository.getPref(PREF_CUSTOM_CRUISE, true)

    /** Traction Control */
    val tractionControlLevel: String
        get() = sharedPreferenceRepository.getPref(PREF_CUSTOM_MODE_TC, "Off")
    val isOtaComplete: Boolean
        get() = sharedPreferenceRepository.getPref(PREF_OTA_PENDING, false)

    val isOldBuild: String?
        get() = sharedPreferenceRepository.getPref(PREF_OTA_OLD_BUILD, null)

    /** Trip Details 1 */
    val tripDetails1: TripDetails?
        get() = sharedPreferenceRepository.getModel(PREF_TRIP_1, object : TypeToken<TripDetails>() {})

    /** Trip Details 2 */
    val tripDetails2: TripDetails?
        get() = sharedPreferenceRepository.getModel(PREF_TRIP_2, object : TypeToken<TripDetails>() {})

    /** Trip Details 3 */
    val tripDetails3: TripDetails?
        get() = sharedPreferenceRepository.getModel(PREF_TRIP_3, object : TypeToken<TripDetails>() {})

    /** Regen Modes*/
    val is10Levels: Boolean
        get() = sharedPreferenceRepository.getPref(PREF_REGEN_MODES, true)

    /** Regen Value */
    val regenValue: Int
        get() = sharedPreferenceRepository.getPref(PREF_REGEN_VALUE, 0)

    /** Hill Hold */
    val isHillHold: Boolean
        get() = sharedPreferenceRepository.getPref(PREF_HILL_HOLD, false)

    /** Data */
    val isDataEnabled: Boolean
        get() = sharedPreferenceRepository.getPref(PREF_DATA, false)

    /** Incognito */
    val isIncognitoEnabled: Boolean
        get() = sharedPreferenceRepository.getPref(PREF_INCOGNITO, false)

    /** Ride Mode */
    val rideMode
        get() = sharedPreferenceRepository.getPref(PREF_RIDE_MODE, "Street")

    val themeMode
        get() = sharedPreferenceRepository.getPref(PREF_THEME_MODE, R.style.Theme_Glide)

    val distanceUnit
        get() = sharedPreferenceRepository.getPref(PREF_DISTANCE_UNIT,"km")
    
    fun saveDistanceUnit(unit:String){
        sharedPreferenceRepository.savePref(PREF_DISTANCE_UNIT,unit)
    }
    val isBallisticPlus
        get() = sharedPreferenceRepository.getPref(PREF_BALLISTIC_PLUS,false)
    
    val isHapticOn
        get() = sharedPreferenceRepository.getPref(PREF_HAPTIC, true)

    val trips
        get() = sharedPreferenceRepository.getPref(PREF_TRIPS, 1)
    
    val vinNumber
        get() = sharedPreferenceRepository.getPref(PREF_VIN_VALUE, "LOCALVIN00000001")

    val imeiNumber
        get() = sharedPreferenceRepository.getPref(PREF_IMEI_NUMBER, "000000000000001")

    val chargerFwExt: String
        get() = sharedPreferenceRepository.getPref(PREF_CHARGER_FW_EXT, "")

    val chargerFwObc: String
        get() = sharedPreferenceRepository.getPref(PREF_CHARGER_FW_OBC, "")

    val chargerVersion: String
        get() = sharedPreferenceRepository.getPref(PREF_CHARGER_VERSION, "")

    val chargerTypeValue: String
        get() = sharedPreferenceRepository.getPref(PREF_CHARGER_TYPE_VALUE, "")    

    fun saveIemiNumber(imeiNumber: String) {
        sharedPreferenceRepository.savePref(PREF_IMEI_NUMBER, imeiNumber)
    }

    fun saveVinNumber(vinNumber: String) {
        sharedPreferenceRepository.savePref(PREF_VIN_VALUE, vinNumber)
    }

    fun saveHaptic(isEnabled: Boolean) {
        sharedPreferenceRepository.savePref(PREF_HAPTIC, isEnabled)
    }

    /** save brightness */
    fun saveBrightness(value: Int) {
        sharedPreferenceRepository.savePref(PREF_BRIGHTNESS, value)
    }

    fun saveTrip(trips:Int){
        sharedPreferenceRepository.savePref(PREF_TRIPS,trips)
    }

    fun saveChargeEnable(isEnable:Boolean){
        sharedPreferenceRepository.savePref(PREF_CHARGE_STATE,isEnable)
    }

    fun saveUpdate(value: Boolean) {
        sharedPreferenceRepository.savePref(PREF_UPDATE, value)
    }

    fun saveThemeMode(@StyleRes mode:Int){
        sharedPreferenceRepository.savePref(PREF_THEME_MODE,mode)
    }

    /** save mode */
    fun saveMode(theme: String) {
        sharedPreferenceRepository.savePref(PREF_MODE, theme)
    }

    /** save theme */
    fun saveTheme(isParallax: Boolean) {
        sharedPreferenceRepository.savePref(PREF_THEME, isParallax)
    }

    /** save brightness state */
    fun saveBrightnessState(state: Boolean) {
        sharedPreferenceRepository.savePref(PREF_BRIGHTNESS_STATE, state)
    }

    fun saveOtaCompleted(isFinish: Boolean) {
        sharedPreferenceRepository.savePref(PREF_OTA_PENDING,isFinish)
    }

    fun saveOldBuild(isOld: String){
        sharedPreferenceRepository.savePref(PREF_OTA_OLD_BUILD, isOld)
    }

    /** save date and time settings*/
    fun saveDateAndTime(isAutoEnabled: Boolean) {
        sharedPreferenceRepository.savePref(PREF_DATE_TIME, isAutoEnabled)
    }

    /** save language */
    fun saveLanguage(language: String) {
        sharedPreferenceRepository.savePref(PREF_LANGUAGE, language)
    }

    /** save battery limit */
    fun saveBatteryLimit(limit: Int) {
        sharedPreferenceRepository.savePref(PREF_BATTERY_LIMIT, limit)
    }

    /** save time format */
    fun saveTimeFormat(isNormalTimeFormat: Boolean) {
        sharedPreferenceRepository.savePref(PREF_TIME_FORMAT, isNormalTimeFormat)
    }

    /** save camera */
    fun saveCamera(isCameraOn: Boolean) {
        sharedPreferenceRepository.savePref(PREF_CAMERA, isCameraOn)
    }

    /** save radar */
    fun saveRadar(isRadarOn: Boolean) {
        sharedPreferenceRepository.savePref(PREF_RADAR, isRadarOn)
    }

    /** save console alerts */
    fun saveConsoleAlerts(isConsoleAlertsOn: Boolean) {
        sharedPreferenceRepository.savePref(PREF_CONSOLE_ALERTS, isConsoleAlertsOn)
    }

    /** save mirror alerts */
    fun saveMirrorAlerts(isMirrorAlertsOn: Boolean) {
        sharedPreferenceRepository.savePref(PREF_MIRROR_ALERTS, isMirrorAlertsOn)
    }

    /** save audio alerts */
    fun saveAudioAlerts(isAudioAlertsOn: Boolean) {
        sharedPreferenceRepository.savePref(PREF_AUDIO_ALERTS, isAudioAlertsOn)
    }

    /** Save Trip Details 1 */
    fun saveTripDetails1(tripDetails: TripDetails) {
        sharedPreferenceRepository.saveModel(PREF_TRIP_1, tripDetails)
    }

   /** Save Trip Details 2 */
    fun saveTripDetails2(tripDetails: TripDetails) {
        sharedPreferenceRepository.saveModel(PREF_TRIP_2, tripDetails)
    }

   /** Save Trip Details 3 */
    fun saveTripDetails3(tripDetails: TripDetails) {
        sharedPreferenceRepository.saveModel(PREF_TRIP_3, tripDetails)
    }

    /** Save ABS mode */
    fun saveCustomModeAbs(isMono: Boolean) {
        sharedPreferenceRepository.savePref(PREF_CUSTOM_MODE_ABS, isMono)
    }

    /** Save ABS mode */
    fun saveCruise(isCruise: Boolean) {
        sharedPreferenceRepository.savePref(PREF_CUSTOM_CRUISE, isCruise)
    }

    /** Save Traction Control */
    fun saveTractionControl(tc: String) {
        sharedPreferenceRepository.savePref(PREF_CUSTOM_MODE_TC, tc)
    }

    /** Save Regen Modes */
    fun saveRegenType(is10Levels: Boolean) {
        sharedPreferenceRepository.savePref(PREF_REGEN_MODES, is10Levels)
    }

    /** Save Regen Value */
    fun saveRegenValue(value: Int) {
        sharedPreferenceRepository.savePref(PREF_REGEN_VALUE, value)
    }

    /** Save Hill hold */
    fun saveHillHold(isHillHold: Boolean) {
        sharedPreferenceRepository.savePref(PREF_HILL_HOLD, isHillHold)
    }

    /** Save Data */
    fun saveData(isDataEnabled: Boolean) {
        sharedPreferenceRepository.savePref(PREF_DATA, isDataEnabled)
    }

    /** Save Incognito */
    fun saveIncognito(isIncognitoEnabled: Boolean) {
        sharedPreferenceRepository.savePref(PREF_INCOGNITO, isIncognitoEnabled)
    }

    /** Save Ride modes */
    fun saveRideMode(rideMode: String) {
        sharedPreferenceRepository.savePref(PREF_RIDE_MODE, rideMode)
    }
    fun saveBallisticPlus(isBallisticPlus:Boolean){
        sharedPreferenceRepository.savePref(PREF_BALLISTIC_PLUS,isBallisticPlus)
    }

    fun saveChargerFwExt(value: String)      = sharedPreferenceRepository.savePref(PREF_CHARGER_FW_EXT, value)
    fun saveChargerFwObc(value: String)      = sharedPreferenceRepository.savePref(PREF_CHARGER_FW_OBC, value)
    fun saveChargerVersion(value: String)    = sharedPreferenceRepository.savePref(PREF_CHARGER_VERSION, value)
    fun saveChargerTypeValue(value: String)  = sharedPreferenceRepository.savePref(PREF_CHARGER_TYPE_VALUE, value)

//

    companion object{
        const val PREF_TRIP_1 = "pref_trip_1"
        const val PREF_TRIP_2 = "pref_trip_2"
        const val PREF_TRIP_3 = "pref_trip_3"
        const val PREF_CLUSTER_UV = "pref_cluster_uv"
        private const val PREF_BRIGHTNESS = "pref_brightness"
        private const val PREF_CHARGE_STATE = "pref_charge_state"
        private const val PREF_VOLUME = "pref_volume"
        private const val PREF_THEME = "pref_theme"
        private const val PREF_MODE = "pref_mode"
        private const val PREF_UPDATE = "pref_play_update"
        private const val PREF_BRIGHTNESS_STATE = "pref_brightness_state"
        private const val PREF_LANGUAGE = "pref_language"
        private const val PREF_DATE_TIME = "pref_date_time"
        const val PREF_BATTERY_LIMIT = "pref_battery_limit"
        const val PREF_TIME_FORMAT = "pref_time_format"
        private const val PREF_CAMERA = "pref_camera"
        private const val PREF_RADAR = "pref_radar"
        private const val PREF_CONSOLE_ALERTS = "pref_console_alerts"
        private const val PREF_MIRROR_ALERTS = "pref_mirror_alerts"
        private const val PREF_AUDIO_ALERTS = "pref_audio_alerts"
        private const val PREF_CUSTOM_MODE_ABS = "pref_custom_mode_abs"
        private const val PREF_CUSTOM_CRUISE = "pref_custom_cruise"
        private const val PREF_CUSTOM_MODE_TC = "pref_custom_mode_tc"
        private const val PREF_REGEN_MODES = "pref_regen_modes"
        private const val PREF_REGEN_VALUE = "pref_regen_value"
        private const val PREF_HILL_HOLD = "pref_hill_hold"
        private const val PREF_DATA = "pref_data"
        private const val PREF_INCOGNITO = "pref_incognito"
        private const val PREF_HAPTIC = "pref_haptic"    
        private const val PREF_RIDE_MODE = "pref_ride_modes"
        private const val PREF_THEME_MODE = "pref_theme_mode"
        private const val PREF_DISTANCE_UNIT = "pref_distance_unit"
        private const val PREF_OTA_PENDING = "pref_ota_pending"
        private const val PREF_OTA_OLD_BUILD = "pref_ota_old_build"
        private const val PREF_BALLISTIC_PLUS = "pref_surge_alert"
        private const val PREF_TRIPS = "pref_trips"

	 private const val PREF_VIN_VALUE = "pref_vin_value"
        private const val PREF_IMEI_NUMBER = "imeiNumber"
	
	private const val PREF_CHARGER_FW_EXT     = "pref_charger_fw_ext"
        private const val PREF_CHARGER_FW_OBC     = "pref_charger_fw_obc"
        private const val PREF_CHARGER_VERSION    = "pref_charger_version"
        private const val PREF_CHARGER_TYPE_VALUE = "pref_charger_type_value"

    }
}



