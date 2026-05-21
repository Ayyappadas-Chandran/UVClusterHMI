package com.ultraviolette.uvclusterhmi.utils

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Build.VERSION_CODES
import android.util.Log.d
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.dataModel.RangeLimit
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipFile


object Utilities {

    /** Permission constants for Android version R */
    val permissionsForSDKR = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.SYSTEM_ALERT_WINDOW,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_CONTACTS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_SMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ANSWER_PHONE_CALLS,
        Manifest.permission.CALL_PHONE,
        android.car.Car.PERMISSION_SPEED,
        android.car.Car.PERMISSION_ENERGY,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.MANAGE_EXTERNAL_STORAGE
    )


    /** Permission constants for Android version S*/
    @RequiresApi(VERSION_CODES.S)
    val permissionsForSDKAboveR = arrayOf(
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
    )

    /** Constants*/
    const val PROP_ID_VEHICLE_VALUE = 0x21610310
    const val PROP_ID_REGEN = 0x21410320
    const val PROP_ID_ABS_MODE = 0x21400330
    const val PROP_ID_ABS_MODE_STATUS = 0x21400411
    const val PROP_ID_HILL_HOLD_STATE = 0x21400340
    const val PROP_ID_HILL_HOLD_ICON = 0x21400341
    const val PROP_ID_FOTA_UPDATE = 0x21410350
    const val PROP_ID_RTC_TIME = 0x21410370
    const val PROP_ID_DISPLAY_BRIGHTNESS = 0x214103B0
    const val PROP_ID_RIDE_MODES = 0x21400412
    const val PROP_ID_SCREEN_MODES = 0x21400413
    const val PROP_ID_INDICATOR = 0x21400414
    const val PROP_ID_HIGH_BEAM_TELLTALE = 0x21400418
    const val PROP_ID_HAZARD_LIGHT_TELLTALE = 0x2140041A
    const val PROP_ID_MOTOR_ARM_DISARM_TELLTALE = 0x21400419
    const val PROP_ID_HEARTBEAT_ENABLE_DISABLE = 0x2140041B
    const val PROP_ID_VEHICLE_INFO_REQ = 0x2140041C
    const val PROP_ID_LOCKDOWN = 0x21400415
    const val PROP_ID_CUSTOM = 0x21700312
    const val PROP_ID_SWIFT_BUTTON = 0x214003A0
    const val PROP_ID_SLEEP_WAKE = 0x21400390
    const val PROP_ID_MTC_MODE = 0x21400416
    const val PROP_ID_MC_THERMAL = 0x21610360
    const val PROP_ID_MC_NO_ARM = 0x21410417
    const val PROP_ID_ALS_INFO = 0x2170031F
    const val PROP_ID_CHARGER_EVT = 0x214003A1
    const val PROP_ID_CRUISE_CONTROL = 0x2140041D
    const val SHOULD_INCLUDE_CAR_SERVICE = true
    const val IMAGE_PORTRAIT = "portrait"
    const val IMAGE_LANDSCAPE = "landscape"
    const val ARG_DOCUMENT_TYPE = "document_type"
    const val ARG_CHARGING_STATUS = "charging_status"
    const val ARG_DASH_CAM ="have_Dash_cam"
    const val ARG_BALLISTIC_PLUS = "ballistic_plus"
    var frameworkStartTime: Long = 0

    /** lux conversion */
    fun getBrightnessLevelFromLux(value: Float): Float {
        return when {
            value < 10 -> 0.1f
            value < 100 -> 0.3f
            value < 1000 -> 0.5f
            value < 10000 -> 0.7f
            else -> 1.0f
        }
    }

    /**
     * Sets a click listener on the View that plays a sound before executing the given [onClick] action.
     *
     * @param context The context to access resources and MediaPlayer.
     * @param soundResId Resource ID of the sound to play. Defaults to R.raw.music_sound_setting.
     * @param onClick Lambda to execute after playing the sound.
     */
    fun View.setOnSoundClickListener(
        context: Context,
        soundResId: Int = R.raw.music_sound_setting,
        onClick: (View) -> Unit
    ) {
        this.setOnClickListener {
            playClickSound(context, soundResId)
            onClick(it)
        }
    }

    /**
     * Plays a short sound using MediaPlayer.
     *
     * @param context The context to access the sound resource.
     * @param soundResId Resource ID of the sound file to play.
     */
    fun playClickSound(context: Context, soundResId: Int) {
        try {
            val mediaPlayer = MediaPlayer.create(context, soundResId)
            mediaPlayer?.apply {
                setAudioStreamType(AudioManager.STREAM_MUSIC)
                setOnCompletionListener { it.release() }
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Returns the name of a [BluetoothDevice], handling permission check for Android 12+.
     *
     * @param device The [BluetoothDevice] whose name is to be retrieved.
     * @param context The [Context] used for permission checking.
     * @return The device name, or an empty string (`""`) if permission is not granted (Android 12+).
     */
    fun getBluetoothDevice(device: BluetoothDevice, context: Context): String? {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.S &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) return ""
        return device.name
    }

    /**
     * Calculates corresponding level-4 regen value from a 10-level regen value.
     *
     * @param regenValue Regen value in 10-level mode.
     * @return Corresponding level-4 regen value (0, 3, 6, or 9).
     */
    fun getRegenValueForLevel4(regenValue: Int): Int {
        return when (regenValue) {
            in 0..2 -> 0
            in 3..5 -> 3
            in 6..8 -> 6
            else -> 9
        }
    }


    fun Int.applyMinMax(limit: RangeLimit?): Int {
        if (limit == null) return this
        return this.coerceIn(limit.min, limit.max)
    }

    fun getButtonState(button: Int): ButtonNavigation {
        return when (button) {
            0x31 -> ButtonNavigation.Left
            0xC1 -> ButtonNavigation.Right
            0xB7 -> ButtonNavigation.Top
            0xC5 -> ButtonNavigation.Bottom
            0xE1 -> ButtonNavigation.Enter
            0x7B -> ButtonNavigation.Back
            else -> ButtonNavigation.None
        }
    }

    fun View.visible() {
        visibility = View.VISIBLE
    }

    fun View.invisible() {
        visibility = View.INVISIBLE
    }

    @Throws(IOException::class)
    fun unzip(zipFilePath: File, destinationDir: String) {
        File(destinationDir).run {
            if (!exists()) mkdirs()
        }
        ZipFile(zipFilePath).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                zip.getInputStream(entry).use { input ->
                    val filePath = destinationDir + File.separator + entry.name
                    if (!entry.isDirectory) {
                        extractFile(input, filePath)
                    } else {
                        d("OTA Update", "Directory exist and extract File :$filePath")
                        val dir = File(filePath)
                        dir.mkdir()
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun extractFile(inputStream: InputStream, destFilePath: String) {
        d("OTAUpdate", "File exist and extract File :$destFilePath")
        val bos = BufferedOutputStream(FileOutputStream(destFilePath))
        val bytesIn = ByteArray(BUFFER_SIZE)
        var read: Int
        while (inputStream.read(bytesIn).also { read = it } != -1) {
            bos.write(bytesIn, 0, read)

        }
        bos.close()
    }

    fun readTextFileLineByLine(dirPath: String, filePath: String): Array<String> {
        val file = File(dirPath, filePath)
        if (!file.exists() || !file.isFile) {
            d("OTAUpdate", "File not found: $filePath")
            return emptyArray<String>()
        }
        return file.bufferedReader().useLines { lines ->
            lines.toList().toTypedArray()
        }
    }

    fun readBinSize():Long{
        val file = File("/data/ota_file/update/","payload.bin")
        return file.length()
    }

    fun isOtaAvailable():Boolean{
        val folder = File("/data/ota_file")
        return folder.exists()
    }

    private const val BUFFER_SIZE = 4096

    enum class ChargeStatusFlag(val bit: Int) {

        SYS_CHARGING_IN_PROGRESS(0),
        SYS_OVER_CHRG_FAULT(18),
        EXT_CHRG_ACV_DERATION_FLAG(19),
        CELL_VOLT_DERATION_FLAG(20),
        CELL_TEMP_DERATION_FLAG(21),
        DISCHARGE_IN_PROGRESS_FLAG(22),
        VCU_HEARTBEAT_FAULT(23),
        EXT_CHG_AUTH(24),
        EXT_CHRG_CP_VALID_FAULT(25),
        EXT_CHRG_TIMEOUT_ERROR(26),
        EXT_CHRG_HARDWARE_FAILURE(27),
        EXT_CHRG_OVER_TEMPERATURE(28),
        EXT_CHRG_INPUT_VOLTAGE_ERROR(29),
        EXT_CHRG_BATTERY_DISCONNECTION(30),
        EXT_CHRG_COMMUNICATION_TIMEOUT(31),
    }

    fun ByteArray.toFloat(): Float {
        return java.nio.ByteBuffer
            .wrap(this)
            .order(java.nio.ByteOrder.LITTLE_ENDIAN)
            .float
    }
}


