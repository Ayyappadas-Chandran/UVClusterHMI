package com.ultraviolette.uvclusterhmi.ui.features.settings.debug

import android.Manifest
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.util.Log.d
import android.util.Log.e
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.dataModel.FaultItem
import com.ultraviolette.uvclusterhmi.domain.dataModel.Severity
import com.ultraviolette.uvclusterhmi.domain.dataModel.StatusItem
import com.ultraviolette.uvclusterhmi.domain.dataModel.TelemetryItem
import com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData.DevUid
import com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData.ImxDbgMsg
import com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData.VcuInfoMsg
import com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData.VcuMiscInfo
import com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData.VehicleMetaData
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import com.ultraviolette.uvclusterhmi.ui.adapter.FaultAdapter
import com.ultraviolette.uvclusterhmi.ui.adapter.StatusAdapter
import com.ultraviolette.uvclusterhmi.ui.adapter.TelemetryAdapter
import com.ultraviolette.uvclusterhmi.ui.viewModel.CarViewModel
import com.ultraviolette.uvclusterhmi.ui.viewModel.SharedViewModel
import com.ultraviolette.uvclusterhmi.utils.Utilities
import com.ultraviolette.uvclusterhmi.utils.Utilities.frameworkStartTime
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

import android.content.Intent
import android.content.pm.PackageManager
import android.location.GnssStatus
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.DocumentsContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class DebugFragment : Fragment() {
    private lateinit var gestureDetector: GestureDetector

    private val MAX_SINGLE_COLUMN_ITEMS = 11

    private lateinit var tvHeader: TextView
    private lateinit var tvTime: TextView
    private lateinit var tvImei: TextView
    private lateinit var tvSpeedValue: TextView
    private lateinit var tvRpmValue: TextView
    private lateinit var tvThrottleValue: TextView
    private lateinit var tvPowerValue: TextView
    private lateinit var tvPitchValue: TextView
    private lateinit var tvRollValue: TextView

    private var rvTelemetry: RecyclerView? = null
    private var rvStatus: RecyclerView? = null
    private var rvFaults: RecyclerView? = null
    private var locationManager: LocationManager? = null

    val carViewModel by activityViewModels<CarViewModel> {
        ViewModelFactory(context = requireContext())
    }
    private val sharedViewModel by activityViewModels<SharedViewModel> { ViewModelFactory(context = requireContext()) }

    // ---------------- FLAGS ----------------

    data class VcuFlag(val bit: Int, val label: String, val severity: Severity)
    data class BmsFlag(val bit: Int, val label: String, val severity: Severity)
    data class McuFlag(val bit: Int, val label: String, val severity: Severity)
    class McuPmicFlag(val bit: Int, val label: String, val severity: Severity)
    class MiscFlag(val bit: Int, val label: String, val severity: Severity)

    private var logProcess: Process? = null
    private var logThread: Thread? = null
    private var writer: BufferedWriter? = null
    private lateinit var  buttonStart : Button
    private lateinit var  buttonStop : Button
    private lateinit var buttonToNextPage: Button
    private var isHover=false
    private var isCHarging = false

    private val vcuFlags = listOf(
        VcuFlag(0, "STAT_VCU_LOG_UPLOAD_RUNNING", Severity.ERROR),
        VcuFlag(1, "STAT_VCU_BMS_VCU_INCOMPATIBLE_VSN", Severity.ERROR),
        VcuFlag(2, "STAT_VCU_FW_DL_RUNNING", Severity.ERROR),
        VcuFlag(3, "STAT_VCU_KEY_EVENT", Severity.ERROR),
        VcuFlag(4, "STAT_VCU_MOTOR_CON_KEY_SW_ON", Severity.ERROR),
        VcuFlag(5, "STAT_VCU_MOTOR_CON_DIR_FWD", Severity.ERROR),
        VcuFlag(6, "STAT_VCU_MOTOR_CON_DIR_REV", Severity.ERROR),
        VcuFlag(7, "STAT_VCU_VEHICLE_KEY_OFF", Severity.ERROR),
        VcuFlag(8, "STAT_VCU_FRONT_BRAKE_PRESS", Severity.ERROR),
        VcuFlag(9, "STAT_VCU_REAR_BRAKE_PRESS", Severity.ERROR),
        VcuFlag(10, "STAT_VCU_MOTOR_CON_FAULT", Severity.ERROR),
        VcuFlag(11, "STAT_VCU_IMU_FAULT", Severity.ERROR),
        VcuFlag(12, "STAT_VCU_IMU_DMP_FAULT", Severity.ERROR),
        VcuFlag(13, "STAT_VCU_LAC_BUS_LOW_VOLTAGE_WARNING", Severity.ERROR),
        VcuFlag(14, "STAT_VCU_MOTOR_OVER_TEMPERATURE", Severity.ERROR),
        VcuFlag(15, "STAT_VCU_RTC_INIT_FAILURE", Severity.ERROR),
        VcuFlag(16, "STAT_VCU_RTC_READ_FAILURE", Severity.ERROR),
        VcuFlag(17, "STAT_VCU_ABS_REAR_WHEEL_SPEED_SENSOR_FAILURE", Severity.ERROR),
        VcuFlag(18, "STAT_VCU_ABS_FRONT_WHEEL_SPEED_SENSOR_FAILURE", Severity.ERROR),
        VcuFlag(19, "STAT_VCU_MC_SDO_UPDATE_SIG", Severity.ERROR),
        VcuFlag(20, "STAT_VCU_IMU_OFS_CALIBRATION", Severity.ERROR),
        VcuFlag(21, "STAT_VCU_NVM_TIMEOUT", Severity.ERROR),
        VcuFlag(22, "STAT_VCU_FW_UPD_READY", Severity.ERROR),
        VcuFlag(23, "STAT_VCU_SM_INVALID_STATE_ENTRY", Severity.ERROR),
        VcuFlag(24, "STAT_VCU_BMS_CAN_MSG_TIMEOUT", Severity.ERROR),
        VcuFlag(25, "STAT_VCU_BMS_CAN_LINK_FAIL", Severity.ERROR),
        VcuFlag(26, "STAT_VCU_CHARGING_IN_PROGRESS", Severity.ERROR),
        VcuFlag(27, "STAT_VCU_CHARGING_COMPLETE", Severity.ERROR),
        VcuFlag(28, "STAT_VCU_CAN_MSG_EXEC_ERR", Severity.ERROR),
        VcuFlag(29, "STAT_VCU_SIDE_STAND_DEPLOYED", Severity.ERROR),
        VcuFlag(30, "STAT_VCU_MC_MODE_GLIDE", Severity.ERROR),
        VcuFlag(31, "STAT_VCU_MC_MODE_COMBAT", Severity.ERROR),
        VcuFlag(32, "STAT_VCU_MC_MODE_BALLISTIC", Severity.ERROR),
        VcuFlag(33, "STAT_VCU_MOTOR_HS_OVER_TEMPERATURE", Severity.ERROR),
        VcuFlag(34, "STAT_VCU_MC_TMAP_LOAD_FAIL", Severity.ERROR),
        VcuFlag(35, "STAT_VCU_MC_TMAP_UPDATED", Severity.ERROR),
        VcuFlag(36, "STAT_VCU_MC_TMAP_COMITTED", Severity.ERROR),
        VcuFlag(37, "STAT_VCU_MC_TMAP_FACT_RESET", Severity.ERROR),
        VcuFlag(38, "STAT_VCU_THROTTLE_ERROR", Severity.ERROR),
        VcuFlag(39, "STAT_VCU_SWIF_ERROR", Severity.ERROR),
        VcuFlag(40, "STAT_VCU_MC_REGEN", Severity.ERROR),
        VcuFlag(41, "STAT_VCU_BMS_SW_EXCEPTION", Severity.ERROR),
        VcuFlag(42, "STAT_VCU_ABS_MODE", Severity.ERROR),
        VcuFlag(43, "STAT_VCU_ABS_FCN_ACTIVE", Severity.ERROR),
        VcuFlag(44, "STAT_VCU_ABS_MODE_ERR", Severity.ERROR),
        VcuFlag(45, "STAT_VCU_CHARGING_ERROR", Severity.ERROR),
        VcuFlag(46, "STAT_VCU_PA_MODE_FWD", Severity.ERROR),
        VcuFlag(47, "STAT_VCU_PA_MODE_REV", Severity.ERROR),
        VcuFlag(48, "STAT_VCU_PA_MODE_ENTRY", Severity.ERROR),
        VcuFlag(49, "STAT_VCU_UP_HH_ACTIVE", Severity.ERROR),
        VcuFlag(50, "STAT_VCU_PA_MODE_ERROR", Severity.ERROR),
        VcuFlag(51, "STAT_VCU_MC_PA_ERROR", Severity.ERROR),
        VcuFlag(52, "STAT_VCU_VACATION_MODE", Severity.ERROR),
        VcuFlag(53, "STAT_VCU_PHY_LINK_RST_FAIL", Severity.ERROR),
        VcuFlag(54, "STAT_VCU_PHY_LINK_TIMEOUT", Severity.ERROR),
        VcuFlag(55, "STAT_VCU_MC_IN_BALLISTIC_DERATION", Severity.ERROR),
        VcuFlag(56, "STAT_VCU_MC_FACT_RESET", Severity.ERROR),
        VcuFlag(57, "STAT_VCU_MC_MODE_HOVER", Severity.ERROR),
        VcuFlag(58, "STAT_VCU_ODO_NVM_ERROR", Severity.ERROR),
        VcuFlag(59, "STAT_VCU_SWIF_INTERNAL_ERROR", Severity.ERROR),
        VcuFlag(60, "STAT_VCU_RE_UPDATED", Severity.ERROR),
        VcuFlag(61, "STAT_VCU_KILL_SW_ACTIVE", Severity.ERROR),
        VcuFlag(62, "STAT_VCU_MC_INCOMPATIBLE", Severity.ERROR),
        VcuFlag(63, "STAT_VCU_MQTT_CMD_ACK", Severity.ERROR),
    )

    private val bmcFlags = listOf(
        BmsFlag(0, "STAT_HSC_STATUS_FLAG", Severity.WARNING),
        BmsFlag(1, "STAT_LSC_STATUS_FLAG", Severity.WARNING),
        BmsFlag(2, "STAT_BAL_TIMER_STATUS_FLAG", Severity.WARNING),
        BmsFlag(3, "STAT_BAL_ACT_STATUS_FLAG", Severity.WARNING),
        BmsFlag(4, "STAT_LTC2946_DSG_ALERT_FLAG", Severity.WARNING),
        BmsFlag(5, "STAT_LTC2946_CHG_ALERT_FLAG", Severity.WARNING),
        BmsFlag(6, "STAT_PWR_MODE_CHARGE", Severity.WARNING),
        BmsFlag(7, "STAT_PWR_MODE_CHARGE_NX", Severity.WARNING),
        BmsFlag(8, "STAT_BMS_UNECOVERABLE_FAILURE", Severity.WARNING),
        BmsFlag(9, "STAT_UV_THR_FLAG", Severity.WARNING),
        BmsFlag(10, "STAT_OV_THR_FLAG", Severity.WARNING),
        BmsFlag(11, "STAT_LTC6812_WDT_SET_FLAG", Severity.WARNING),
        BmsFlag(12, "STAT_BATTERY_TEMP_OVER_MIN_THRESHOLD", Severity.WARNING),
        BmsFlag(13, "STAT_BATTERY_TEMP_OVER_MAX_THRESHOLD", Severity.WARNING),
        BmsFlag(14, "STAT_BATTERY_TEMP_TOO_LOW", Severity.WARNING),
        BmsFlag(15, "STAT_AFE_SAFETY_TIMER_FLAG", Severity.WARNING),
        BmsFlag(16, "STAT_BALANCER_ABORT_FLAG", Severity.WARNING),
        BmsFlag(17, "STAT_BALANCER_RESET_FLAG", Severity.WARNING),
        BmsFlag(18, "STAT_BALANCING_COMPLETE_FLAG", Severity.WARNING),
        BmsFlag(19, "STAT_AFE_PEC_ERROR", Severity.WARNING),
        BmsFlag(20, "STAT_UV_OV_THR_FOR_TURN_ON", Severity.WARNING),
        BmsFlag(21, "STAT_ECC_ERM_ERR_FLAG", Severity.WARNING),
        BmsFlag(22, "STAT_DSG_INA302_ALERT1", Severity.WARNING),
        BmsFlag(23, "STAT_DSG_INA302_ALERT2", Severity.WARNING),
        BmsFlag(24, "STAT_CONTACTOR_OVER_TMP_ALERT", Severity.WARNING),
        BmsFlag(25, "STAT_AFE_INVALID_CMD", Severity.WARNING),
        BmsFlag(26, "STAT_SHUNT_OVER_TMP_ALERT", Severity.WARNING),
        BmsFlag(27, "STAT_BIT_UNUSED_27", Severity.WARNING),
        BmsFlag(28, "STAT_BIT_UNUSED_28", Severity.WARNING),
        BmsFlag(29, "STAT_REL_HUMIDITY_OVERVALUE_ALERT", Severity.WARNING),
        BmsFlag(30, "STAT_FUSE_BLOWN_ALERT", Severity.WARNING),
        BmsFlag(31, "STAT_BIT_UNUSED_31", Severity.WARNING),
        BmsFlag(32, "STAT_CONT_TURN_ON_FAILURE", Severity.WARNING),
        BmsFlag(33, "STAT_CONT_TURN_OFF_FAILURE", Severity.WARNING),
        BmsFlag(34, "STAT_BAL_RES_OVER_TEMPERATURE", Severity.WARNING),
        BmsFlag(35, "STAT_LTC2946_COMM_FAILURE", Severity.WARNING),
        BmsFlag(36, "STAT_ADS1015_COMM_FAILURE", Severity.WARNING),
        BmsFlag(37, "STAT_BIT_UNUSED_37", Severity.WARNING),
        BmsFlag(38, "STAT_HW_OVER_TMP_SHUTDOWN", Severity.WARNING),
        BmsFlag(39, "STAT_BIT_UNUSED_39", Severity.WARNING),
        BmsFlag(40, "STAT_SYS_BOOT_FAILURE", Severity.WARNING),
        BmsFlag(41, "STAT_CAN_MSG_SIG_ERR", Severity.WARNING),
        BmsFlag(42, "STAT_HVIL_N_ERR", Severity.WARNING),
        BmsFlag(43, "STAT_HVIL_P_ERR", Severity.WARNING),
        BmsFlag(44, "STAT_BAT_TAMPER_DETECTED", Severity.WARNING),
        BmsFlag(45, "STAT_TMP_THR_FOR_TURN_ON", Severity.WARNING),
        BmsFlag(46, "STAT_CONT_OVER_TMP_WARN", Severity.WARNING),
        BmsFlag(47, "STAT_SHUNT_OVER_TMP_WARN", Severity.WARNING),
        BmsFlag(48, "STAT_MSD_ERR", Severity.WARNING),
        BmsFlag(49, "STAT_BIT_UNUSED_49", Severity.WARNING),
        BmsFlag(50, "STAT_BIT_UNUSED_50", Severity.WARNING),
        BmsFlag(51, "STAT_PM_CHG_CURRENT_LIMIT_UPDATE", Severity.WARNING),
        BmsFlag(52, "STAT_UNSAFE_COND_CONT_TURN_ON", Severity.WARNING),
        BmsFlag(53, "STAT_THRM_RUNAWAY_ALRT_V", Severity.WARNING),
        BmsFlag(54, "STAT_THRM_RUNAWAY_ALRT_T", Severity.WARNING),
        BmsFlag(55, "STAT_THRM_RUNAWAY_ALRT_H", Severity.WARNING),
        BmsFlag(56, "STAT_PRE_DISCHARGE_STRESSED", Severity.WARNING),
        BmsFlag(57, "STAT_LSCONT_SHORT_WARN", Severity.WARNING),
        BmsFlag(58, "STAT_HSCONT_SHORT_WARN", Severity.WARNING),
        BmsFlag(59, "STAT_BAT_MINUS_INS_FAULT", Severity.WARNING),
        BmsFlag(60, "STAT_BAT_PLUS_INS_FAULT", Severity.WARNING),
        BmsFlag(61, "STAT_FUSE_OVER_TMP_WARN", Severity.WARNING),
        BmsFlag(62, "STAT_FUSE_OVER_TMP_ALERT", Severity.WARNING),
        BmsFlag(63, "MAX_BMS_STATUS_FLAGS", Severity.WARNING)
    )

    private val mcuFlags = listOf(

        McuFlag(1, "SIN_COS_IDENTITY_ERROR", Severity.ERROR),
        McuFlag(2, "THROTTLE_ERROR_1", Severity.ERROR),
        McuFlag(3, "THROTTLE_ERROR_2", Severity.ERROR),
        McuFlag(4, "DRIVE_UNDERVOLTAGE_SW", Severity.ERROR),
        McuFlag(5, "CRITICAL_OVERVOLTAGE_SW", Severity.ERROR),
        McuFlag(6, "MOTOR_TEMPERATURE_ERROR", Severity.ERROR),
        McuFlag(7, "OVERCURRENT_SW", Severity.ERROR),
        McuFlag(8, "CONTROLLER_TEMPERATURE_ERROR", Severity.ERROR),
        McuFlag(9, "CURRENT_OFFSET_ERROR", Severity.ERROR),
        McuFlag(10, "OVERSPEED", Severity.ERROR),
        McuFlag(11, "INT_WATCHDOG_RESET", Severity.ERROR),
        McuFlag(12, "EXT_WATCHDOG_RESET", Severity.ERROR),
        McuFlag(13, "EEPROM_FLASH", Severity.ERROR),
        McuFlag(14, "EEPROM_STATE", Severity.ERROR),
        McuFlag(15, "WRITE_ONCE_WRITE", Severity.ERROR),
        McuFlag(16, "RPDO_TIMEOUT", Severity.ERROR),
        McuFlag(17, "CAN_PARITY", Severity.ERROR),
        McuFlag(18, "FLASH_API_INIT_ERROR", Severity.ERROR),
        McuFlag(19, "CONFIG_WRITE_INCOMPLETE", Severity.ERROR),
        McuFlag(20, "NMI_WATCHDOG_RESET", Severity.ERROR),
        McuFlag(21, "DCSM_SAFE_COPY_RESET", Severity.ERROR),
        McuFlag(22, "NMI", Severity.ERROR),
        McuFlag(23, "ITRAP", Severity.ERROR),
        McuFlag(24, "FLASH_ECC_SELF_TEST_FAILED", Severity.ERROR),
        McuFlag(25, "RAM_ECC_SELF_TEST_FAILED", Severity.ERROR),
        McuFlag(26, "ASSERT_CALLED", Severity.ERROR),
        McuFlag(27, "CLA_OC", Severity.ERROR),
        McuFlag(28, "ERAD_ISR_TIME", Severity.ERROR),
        McuFlag(29, "ERAD_STACK_OVR", Severity.ERROR),
        McuFlag(30, "MOSFET_U_HEALTH_ERROR", Severity.ERROR),
        McuFlag(31, "MOSFET_V_HEALTH_ERROR", Severity.ERROR),
        McuFlag(32, "MOSFET_W_HEALTH_ERROR", Severity.ERROR),
        McuFlag(33, "MOSFET_U_DRIVER_ERROR", Severity.ERROR),
        McuFlag(34, "MOSFET_V_DRIVER_ERROR", Severity.ERROR),
        McuFlag(35, "MOSFET_W_DRIVER_ERROR", Severity.ERROR),
        McuFlag(36, "FRWD_REV_ERROR", Severity.ERROR),
        McuFlag(37, "ADC_OCSC_SELF_TEST_FAILED", Severity.ERROR),
        McuFlag(38, "FWC_VS_ERROR", Severity.ERROR),
        McuFlag(39, "PIE_VECT_CORRUPT", Severity.ERROR),
        McuFlag(40, "PMIC_FAULT", Severity.ERROR),
        McuFlag(41, "OTP_EMPTY_INVALID", Severity.ERROR),
        McuFlag(42, "U_PHASE_IMBALANCE", Severity.ERROR),
        McuFlag(43, "V_PHASE_IMBALANCE", Severity.ERROR),
        McuFlag(44, "W_PHASE_IMBALANCE", Severity.ERROR),
        McuFlag(45, "AUTOTUNE_FAILURE", Severity.ERROR),
        McuFlag(46, "SIN_ERROR", Severity.ERROR),
        McuFlag(47, "COS_ERROR", Severity.ERROR),
        McuFlag(48, "UNUSED_48", Severity.ERROR),
        McuFlag(49, "UNUSED_49", Severity.ERROR),
        McuFlag(50, "UNUSED_50", Severity.ERROR),
        McuFlag(51, "UNUSED_51", Severity.ERROR),
        McuFlag(52, "UNUSED_52", Severity.ERROR),
        McuFlag(53, "UNUSED_53", Severity.ERROR),
        McuFlag(54, "UNUSED_54", Severity.ERROR),
        McuFlag(55, "UNUSED_55", Severity.ERROR),
        McuFlag(56, "UNUSED_56", Severity.ERROR),
        McuFlag(57, "UNUSED_57", Severity.ERROR),
        McuFlag(58, "MAX_FAULT_CODE", Severity.ERROR),
        McuFlag(59, "UNUSED_59", Severity.ERROR),
        McuFlag(60, "UNUSED_60", Severity.ERROR),
        McuFlag(61, "UNUSED_61", Severity.ERROR),
        McuFlag(62, "UNUSED_62", Severity.ERROR),
        McuFlag(63, "UNUSED_63", Severity.ERROR)
    )

    private val mcuPmicFlags = listOf(

        /* -------- Error Bits (1–32) -------- */

        McuPmicFlag(1, "VDD5_ILIM", Severity.ERROR),
        McuPmicFlag(2, "VDD3_5_ILIM", Severity.ERROR),
        McuPmicFlag(3, "VDD5_OT", Severity.ERROR),
        McuPmicFlag(4, "VDD_3_5_OT", Severity.ERROR),
        McuPmicFlag(5, "CFG_CRC_ERR", Severity.ERROR),
        McuPmicFlag(6, "EE_CRC_ERR", Severity.ERROR),
        McuPmicFlag(7, "WD_FAIL_CNT_ERROR", Severity.ERROR),
        McuPmicFlag(8, "ABIST_ERR", Severity.ERROR),
        McuPmicFlag(9, "LBIST_ERR", Severity.ERROR),
        McuPmicFlag(10, "NRES_ERR", Severity.ERROR),
        McuPmicFlag(11, "SPI_ERR", Severity.ERROR),
        McuPmicFlag(12, "LOCLK", Severity.ERROR),
        McuPmicFlag(13, "MCU_ERR", Severity.ERROR),
        McuPmicFlag(14, "WD_ERR", Severity.ERROR),
        McuPmicFlag(15, "ENDRV_ERR", Severity.ERROR),
        McuPmicFlag(16, "DEVICE_STATE_ERR", Severity.ERROR),
        McuPmicFlag(17, "VBATP_OV", Severity.ERROR),
        McuPmicFlag(18, "VBATP_UV", Severity.ERROR),
        McuPmicFlag(19, "VCP17_OV", Severity.ERROR),
        McuPmicFlag(20, "VCP12_OV", Severity.ERROR),
        McuPmicFlag(21, "VCP12_UV", Severity.ERROR),
        McuPmicFlag(22, "VDD6_OV", Severity.ERROR),
        McuPmicFlag(23, "VDD6_UV", Severity.ERROR),
        McuPmicFlag(24, "VDD5_OV", Severity.ERROR),
        McuPmicFlag(25, "VDD5_UV", Severity.ERROR),
        McuPmicFlag(26, "VDD3_5_OV", Severity.ERROR),
        McuPmicFlag(27, "VDD3_5_UV", Severity.ERROR),
        McuPmicFlag(28, "WD_CFG_ERR", Severity.ERROR),
        McuPmicFlag(29, "WD_RST_EN_ERR", Severity.ERROR),
        McuPmicFlag(30, "WD_WIN1_CFG_ERR", Severity.ERROR),
        McuPmicFlag(31, "WD_WIN2_CFG_ERR", Severity.ERROR),
        McuPmicFlag(32, "WD_SYNC_ERR", Severity.ERROR),

        /* -------- Error Bits (33–34) -------- */

        McuPmicFlag(33, "DIAG_EXIT_ERR", Severity.ERROR),
        McuPmicFlag(34, "TURN_ON_DIAG_STATE_ERR", Severity.ERROR),
        McuPmicFlag(35, "UNUSED_35", Severity.ERROR),
        McuPmicFlag(36, "UNUSED_36", Severity.ERROR),
        McuPmicFlag(37, "UNUSED_37", Severity.ERROR),
        McuPmicFlag(38, "UNUSED_38", Severity.ERROR),
        McuPmicFlag(39, "UNUSED_39", Severity.ERROR),
        McuPmicFlag(40, "UNUSED_40", Severity.ERROR),
        McuPmicFlag(41, "UNUSED_41", Severity.ERROR),
        McuPmicFlag(42, "UNUSED_42", Severity.ERROR),
        McuPmicFlag(43, "UNUSED_43", Severity.ERROR),
        McuPmicFlag(44, "UNUSED_44", Severity.ERROR),
        McuPmicFlag(45, "UNUSED_45", Severity.ERROR),
        McuPmicFlag(46, "UNUSED_46", Severity.ERROR),
        McuPmicFlag(47, "UNUSED_47", Severity.ERROR),
        McuPmicFlag(48, "UNUSED_48", Severity.ERROR),
        McuPmicFlag(49, "UNUSED_49", Severity.ERROR),
        McuPmicFlag(50, "UNUSED_50", Severity.ERROR),
        McuPmicFlag(51, "UNUSED_51", Severity.ERROR),
        McuPmicFlag(52, "UNUSED_52", Severity.ERROR),
        McuPmicFlag(53, "UNUSED_53", Severity.ERROR),
        McuPmicFlag(54, "UNUSED_54", Severity.ERROR),
        McuPmicFlag(55, "UNUSED_55", Severity.ERROR),
        McuPmicFlag(56, "UNUSED_56", Severity.ERROR),
        McuPmicFlag(57, "UNUSED_57", Severity.ERROR),
        McuPmicFlag(58, "UNUSED_58", Severity.ERROR),
        McuPmicFlag(59, "UNUSED_59", Severity.ERROR),
        McuPmicFlag(60, "UNUSED_60", Severity.ERROR),
        McuPmicFlag(61, "UNUSED_61", Severity.ERROR),
        McuPmicFlag(62, "UNUSED_62", Severity.ERROR),
        McuPmicFlag(63, "UNUSED_63", Severity.ERROR)
    )

    private val vcuMiscFlags = listOf(

        /* -------- Status Word Group 0 (0–31) -------- */

        MiscFlag(0, "MTC_FCN_ACTIVE", Severity.INFO),
        MiscFlag(1, "MTC_ERROR", Severity.INFO),
        MiscFlag(2, "MTC_CTRL_REFUSED", Severity.INFO),
        MiscFlag(3, "MTC_EN", Severity.INFO),
        MiscFlag(4, "MTC_MODE_SPORT", Severity.INFO),
        MiscFlag(5, "MTC_MODE_ROAD", Severity.INFO),
        MiscFlag(6, "MTC_MODE_RAIN", Severity.INFO),
        MiscFlag(7, "WOL_TIMEOUT", Severity.INFO),
        MiscFlag(8, "SLEEP_MODE", Severity.INFO),
        MiscFlag(9, "LAC_CHARGING", Severity.INFO),
        MiscFlag(10, "IMU_FALL_DETECTED", Severity.INFO),
        MiscFlag(11, "IMU_MOTION_DETECTED", Severity.INFO),
        MiscFlag(12, "MTC_CRC_ERROR", Severity.INFO),
        MiscFlag(13, "IMU_TOW_DETECTED", Severity.INFO),
        MiscFlag(14, "IMU_CRASH_DETECTED", Severity.INFO),
        MiscFlag(15, "LEFT_IND_ACTIVE", Severity.INFO),
        MiscFlag(16, "RIGHT_IND_ACTIVE", Severity.INFO),
        MiscFlag(17, "FORCE_RESET_EXEC", Severity.INFO),
        MiscFlag(18, "ABS_COMM_TIMEOUT", Severity.INFO),
        MiscFlag(19, "MC_MIL_ACTIVE", Severity.INFO),
        MiscFlag(20, "CSEC_OP_TIMEOUT", Severity.INFO),
        MiscFlag(21, "STN_CHG_TERM_EVENT", Severity.INFO),
        MiscFlag(22, "ABS_PROG_ACTIVE", Severity.INFO),
        MiscFlag(23, "FAST_CHARGER_CONNECTED", Severity.INFO),
        MiscFlag(24, "HORN_PRESSED", Severity.INFO),
        MiscFlag(25, "PRE_CRASH_TRIGGER", Severity.INFO),
        MiscFlag(26, "CLU_RDY", Severity.INFO),
        MiscFlag(27, "LAC_CHARGING_TIMEOUT", Severity.INFO),
        MiscFlag(28, "SLEEP_MODE_L2", Severity.INFO),
        MiscFlag(29, "LAC_DCDC_CHG_ERR", Severity.INFO),
        MiscFlag(30, "MC_TMP_SNS_ERR", Severity.INFO),
        MiscFlag(31, "VEH_IN_SVC_MODE", Severity.INFO),

        /* -------- Status Word Group 1 (32–63) -------- */

        MiscFlag(32, "LFX_SENTRY_AIRBUS_DISABLED", Severity.INFO),
        MiscFlag(33, "LFX_SENTRY_ON_AIRBUS_OFF", Severity.INFO),
        MiscFlag(34, "LFX_SENTRY_OFF_AIRBUS_ON", Severity.INFO),
        MiscFlag(35, "LFX_SENTRY_ON_AIRBUS_ON", Severity.INFO),
        MiscFlag(36, "IMU_FALL_DETECT_ON", Severity.INFO),
        MiscFlag(37, "IMU_TOW_DETECT_ON", Severity.INFO),
        MiscFlag(38, "IMU_SENTRY_MODE_ON", Severity.INFO),
        MiscFlag(39, "CHG_ENDPOINT_LIMITED", Severity.INFO),
        MiscFlag(40, "IMU_LOGGING_ENABLED", Severity.INFO),
        MiscFlag(41, "MC_LCA_AVAILABLE", Severity.INFO),
        MiscFlag(42, "MC_LCA_ENGAGED", Severity.INFO),
        MiscFlag(43, "MC_LCA_ACTIVE", Severity.INFO),
        MiscFlag(44, "MC_LCA_USR_EXIT", Severity.INFO),
        MiscFlag(45, "LC_LCA_COND_EXIT", Severity.INFO),
        MiscFlag(46, "LC_LCA_TIMEOUT", Severity.INFO),
        MiscFlag(47, "MC_TPDO_TIMEOUT", Severity.INFO),
        MiscFlag(48, "BL_PATCH_DONE", Severity.INFO),
        MiscFlag(49, "RDR_BSM_RUNNING", Severity.INFO),
        MiscFlag(50, "RDR_BSM_PAUSED", Severity.INFO),
        MiscFlag(51, "RDR_BSM_ERROR", Severity.INFO),
        MiscFlag(52, "RDR_BSM_DISABLED", Severity.INFO),
        MiscFlag(53, "RDR_BSM_LHS_WARN", Severity.INFO),
        MiscFlag(54, "RDR_BSM_LHS_ALRT", Severity.INFO),
        MiscFlag(55, "RDR_BSM_RHS_WARN", Severity.INFO),
        MiscFlag(56, "RDR_BSM_RHS_ALRT", Severity.INFO),
        MiscFlag(57, "RDR_RCW_ALRT", Severity.INFO),
        MiscFlag(58, "SWIF_SEM_WAIT", Severity.INFO),
        MiscFlag(59, "RDR_SENSOR_BLOCKED", Severity.INFO),
        MiscFlag(60, "RDR_SENSOR_DISABLED", Severity.INFO),
        MiscFlag(61, "RDR_SENSOR_LIMITED", Severity.INFO),
        MiscFlag(62, "RDR_SENSOR_ACTIVE", Severity.INFO),
        MiscFlag(63, "RDR_SENSOR_INSPECTION", Severity.INFO),

        /* -------- Status Word Group 2 (64–127) -------- */

        MiscFlag(64, "RDR_REAR_CRC_ERR", Severity.INFO),
        MiscFlag(65, "RDR_FRONT_CRC_ERR", Severity.INFO),
        MiscFlag(66, "MC_SURGE_MODE", Severity.INFO),
        MiscFlag(67, "MC_INVALID_ENCODER_OFFSET", Severity.INFO),
        MiscFlag(68, "MC_SDO_UPDATE_FAIL", Severity.INFO),
        MiscFlag(69, "RDR_BSM_TURNED_OFF", Severity.INFO),
        MiscFlag(70, "RDR_RCW_TURNED_OFF", Severity.INFO),
        MiscFlag(71, "INVALID_MC_FW", Severity.INFO),
        MiscFlag(72, "INVALID_MC_PROD_CODE", Severity.INFO),
        MiscFlag(73, "MC_DETAILS_NA", Severity.INFO),
        MiscFlag(74, "MC_SPEED_LIMIT_SET", Severity.INFO),
        MiscFlag(75, "MC_ENCODER_OFFSET_UPDATED", Severity.INFO),
        MiscFlag(76, "MC_IN_LOCKDOWN", Severity.INFO),
        MiscFlag(77, "CHG_INCOMPATIBLE", Severity.INFO),
        MiscFlag(78, "RE_LOAD_FAILED", Severity.INFO),
        MiscFlag(79, "CCG_INIT_FAIL", Severity.INFO),
        MiscFlag(80, "CCG_MCP_RX_ERROR", Severity.INFO),
        MiscFlag(81, "VNIC_RNDIS_CONNECTED", Severity.INFO),
        MiscFlag(82, "CCG_RX_ERR", Severity.INFO),
        MiscFlag(83, "CHARGER_FLAP_OPENED", Severity.INFO),
        MiscFlag(84, "MC_TMAP_FACT_RESET_AT_BOOT", Severity.INFO),
        MiscFlag(85, "CD_DETECTED", Severity.INFO),
        MiscFlag(86, "SHMEM_OOM", Severity.INFO),
        MiscFlag(87, "CPU_CORE_OVERTEMP_ALERT", Severity.INFO),
        MiscFlag(88, "MC_CC_OFF", Severity.INFO),
        MiscFlag(89, "MC_CC_STBY", Severity.INFO),
        MiscFlag(90, "MC_CC_ACTIVE", Severity.INFO),
        MiscFlag(91, "MC_CC_ERROR", Severity.INFO),
        MiscFlag(92, "IMU_WORLD_CAL_MISSING", Severity.INFO),
        MiscFlag(93, "IMU_FRAME_CAL_MISSING", Severity.INFO),
        MiscFlag(94, "MC_CC_FEAT_EN", Severity.INFO),
        MiscFlag(95, "ABS_WARNING_LAMP_ON", Severity.INFO),
        MiscFlag(96, "VNIC_USB_TX_ABORTED", Severity.INFO),
        MiscFlag(97, "USB_HBT_TIMEOUT", Severity.INFO),
        MiscFlag(98, "USB_HBT_DISABLED", Severity.INFO),
        MiscFlag(99, "ALS_INVALID_SCAN", Severity.INFO),
        MiscFlag(100, "MC_BAAS_LOCKDOWN_TRIGGERED", Severity.INFO),
        MiscFlag(101, "MC_IN_BAAS_LOCKDOWN", Severity.INFO),
        MiscFlag(102, "DSP_IMEI_UPD_EVENT", Severity.INFO),
        MiscFlag(103, "DSP_VIN_UPD_EVENT", Severity.INFO),

        /* -------- Remaining Unused (104–127) -------- */

        MiscFlag(104, "UNUSED_104", Severity.INFO),
        MiscFlag(105, "UNUSED_105", Severity.INFO),
        MiscFlag(106, "UNUSED_106", Severity.INFO),
        MiscFlag(107, "UNUSED_107", Severity.INFO),
        MiscFlag(108, "UNUSED_108", Severity.INFO),
        MiscFlag(109, "UNUSED_109", Severity.INFO),
        MiscFlag(110, "UNUSED_110", Severity.INFO),
        MiscFlag(111, "UNUSED_111", Severity.INFO),
        MiscFlag(112, "UNUSED_112", Severity.INFO),
        MiscFlag(113, "UNUSED_113", Severity.INFO),
        MiscFlag(114, "UNUSED_114", Severity.INFO),
        MiscFlag(115, "UNUSED_115", Severity.INFO),
        MiscFlag(116, "UNUSED_116", Severity.INFO),
        MiscFlag(117, "UNUSED_117", Severity.INFO),
        MiscFlag(118, "UNUSED_118", Severity.INFO),
        MiscFlag(119, "UNUSED_119", Severity.INFO),
        MiscFlag(120, "UNUSED_120", Severity.INFO),
        MiscFlag(121, "UNUSED_121", Severity.INFO),
        MiscFlag(122, "UNUSED_122", Severity.INFO),
        MiscFlag(123, "UNUSED_123", Severity.INFO),
        MiscFlag(124, "UNUSED_124", Severity.INFO),
        MiscFlag(125, "UNUSED_125", Severity.INFO),
        MiscFlag(126, "UNUSED_126", Severity.INFO),
        MiscFlag(127, "UNUSED_127", Severity.INFO),
        MiscFlag(128, "MAX_MISC_STATUS_FLAGS", Severity.INFO)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.fragment_debug, container, false)
        addSwipeGesture(rootView)
        return rootView
    }


    @RequiresPermission("android.permission.READ_PRIVILEGED_PHONE_STATE")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvHeader = view.findViewById(R.id.tvHeader)
        tvTime = view.findViewById(R.id.tvTime)
        tvImei = view.findViewById(R.id.tvImei)
        tvSpeedValue = view.findViewById(R.id.tvSpeedValue)
        tvRpmValue = view.findViewById(R.id.tvRpmValue)
        tvThrottleValue = view.findViewById(R.id.tvThrottleValue)
        tvPowerValue = view.findViewById(R.id.tvPowerValue)
        tvPitchValue = view.findViewById(R.id.tvPitchValue)
        tvRollValue = view.findViewById(R.id.tvRollValue)

        rvTelemetry = view.findViewById(R.id.rvTelemetry)
        rvStatus = view.findViewById(R.id.rvStatus)
        rvFaults = view.findViewById(R.id.rvFaults)
        buttonStart = view.findViewById(R.id.buttonStart)
        buttonStop = view.findViewById(R.id.buttonStop)
	buttonToNextPage= view.findViewById(R.id.nextPageBtn)

        rvTelemetry?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = TelemetryAdapter()
        }

        rvStatus?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = StatusAdapter()
        }

        rvFaults?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = FaultAdapter()
        }
        tvHeader.text = "DEBUG"
        tvImei.text = "IMEI : " + getImei()
        initObserver()
	startLogging(requireContext())
        buttonStart.setOnClickListener {
            folderPickerLauncher.launch(null)

            //startLogging(requireContext())
            d("DebugFragment", "Critical log capture STARTED")
        }

        buttonStop.setOnClickListener {
            // 1. Check if permission is actually granted
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

                // If not granted, request it from the user
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
                return@setOnClickListener
            }
            locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

            // 2. Start GPS Updates (Now safe from permission errors)
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                0f,
                locationListener
            )

            // 3. Register for Satellite metadata
            locationManager?.registerGnssStatusCallback(
                gnssStatusCallback,
                Handler(Looper.getMainLooper())
            )

            d("DebugFragment", "GPS tracking and GNSS metadata capture STARTED")
        }
	buttonToNextPage.setOnClickListener {
            d("DebugFragment", "action_debugFragment_to_checkSpeedFragment")
            findNavController().navigate(R.id.action_debugFragment_to_checkSpeedFragment)
        }

    }

    private val gnssStatusCallback = object : GnssStatus.Callback() {
        override fun onSatelliteStatusChanged(status: GnssStatus) {
            val totalSatellites = status.satelliteCount
            var usedInFixCount = 0

            for (i in 0 until totalSatellites) {
                if (status.usedInFix(i)) {
                    usedInFixCount++
                }
            }

            d("GPS", "onSatelliteStatusChanged")
            // This gives you:
            // totalSatellites (Visible)
            // usedInFixCount (Used)
        }

        override fun onFirstFix(ttffMillis: Int) {
            // ttffMillis is your "Time to First Fix"
            d("GPS", "Time to Fix: ${ttffMillis / 1000.0} seconds")
        }

        override fun onStarted() {
            d("GPS", "GNSS System Started - Hardware is now searching...")
        }

        override fun onStopped() {
            d("GPS", "GNSS System Stopped")
        }
    }

    private val locationListener = LocationListener { location ->
        val lat = location.latitude
        val long = location.longitude
        val hasAltitude = location.hasAltitude()
        d("GPS", "LocationListener")

        // Logic for 3D Fix:
        // A 3D fix technically requires at least 4 satellites and provides altitude.
        val fixType = if (location.hasAltitude()) "3D Fix" else "2D Fix"
    }

    @RequiresPermission("android.permission.READ_PRIVILEGED_PHONE_STATE")
    private fun getImei(): String {
        return try {
            val tm =
                requireContext().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            tm.getImei(0) ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun addSwipeGesture(rootView: View?) {
        gestureDetector = GestureDetector(
            requireContext(),
            object : GestureDetector.SimpleOnGestureListener() {

                override fun onDown(e: MotionEvent): Boolean {
                    return true   // REQUIRED for double tap to work
                }

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    val destination = when {
                        isHover -> R.id.hoverModeFragment
                        isCHarging -> R.id.chargingFragment
                        else -> R.id.dashboardFragment
                    }
                    findNavController().navigate(destination)
                    return true
                }
            }
        )

        rootView?.isClickable = true
        rootView?.isFocusable = true

        rootView?.setOnTouchListener { v, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }
    private fun setTimeAndDate(value: IntArray) {

        val day = value[0].toUByte().toInt()
        val hour = value[1].toUByte().toInt()
        val minute = value[2].toUByte().toInt()
        val second = value[3].toUByte().toInt()
        val monthRaw = value[4].toUByte().toInt()
        val year = 2000 + value[6].toUByte().toInt()

        // If firmware sends month 0–11
        val month = if (monthRaw in 0..11) monthRaw + 1 else monthRaw

        if (month !in 1..12) {
            e("DATE_CHECK", "Invalid month received: $monthRaw")
            return
        }

        if (day !in 1..31) {
            e("DATE_CHECK", "Invalid day received: $day")
            return
        }

        val dateTime = LocalDateTime.of(year, month, day, hour, minute, second)

        val formatter = DateTimeFormatter.ofPattern(
            "MMM dd, yyyy - HH:mm:ss",
            Locale.getDefault()
        )

        tvTime.text = dateTime.format(formatter)
    }


    private fun initObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            val categoryFaults = mutableMapOf<String, List<FaultItem>>(
                "VCU" to emptyList(),
                "BMS" to emptyList(),
                "MCU" to emptyList(),
                "MCU_PMIC" to emptyList(),
                "MISC" to emptyList()
            )
            fun refreshFaultUI() {
                // "First comes show first" defined by the order of addition here:
                val allFaults = mutableListOf<FaultItem>()
                allFaults.addAll(categoryFaults["VCU"] ?: emptyList())
                allFaults.addAll(categoryFaults["BMS"] ?: emptyList())
                allFaults.addAll(categoryFaults["MCU"] ?: emptyList())
		allFaults.addAll(categoryFaults["MCU_PMIC"] ?: emptyList())
                allFaults.addAll(categoryFaults["MISC"] ?: emptyList())

                updateFaultColumns(allFaults)
            }
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    carViewModel.vehicleValue.collect { vehicleValue ->
                        d("DebugFragment", "regen: ${vehicleValue.joinToString()}")

                        // Assuming index 0 is speed, and vehicleValue contains Ints
                        val speed = vehicleValue.getOrNull(0)
                        tvSpeedValue.text = speed?.toString() ?: "--"  // or "0", "", etc.
                    }
                }
                launch {
                    carViewModel.vcuInfoMsg.collect { vcu ->
                        //DUmmy value
                        /*val vcu = VcuInfoMsg(
                            apiVersion = byteArrayOf(
                                41,
                                2,
                                47,
                                70
                            ),           // probably "A./F" or version 41.2.47.70
                            msgSequence = 30983u,
                            millis = 12857372u,
                            statusH = 0u,
                            statusL = 1u,
                            vcuStatusH = 4194560u,
                            vcuStatusL = 1073741840u,
                            roll = 90.0f,
                            pitch = -15.912899f,
                            odometer = 0.2f,
                            bmsId = DevUid(
                                uidl = 806486018u,
                                uidml = 1314083936u,
                                uidmh = 4294967295u,      // = 0xFFFFFFFFu
                                uidh = 2621439u
                            ),
                            throttleVoltage = byteArrayOf(31, -123, 107, 63),
                            speed = byteArrayOf(74, 0, 0, 0),
                            actualSpeed = byteArrayOf(0, 0, 0, 0),
                            distance = byteArrayOf(-51, -52, -52, 61),
                            vehicleMetaData = VehicleMetaData(
                                model = 2u,
                                vcuHw = 1u,
                                batteryPackVariant = 0u,
                                mcuVariant = 2u,
                                motorType = 255u,
                                region = 1u,
                                reserved1 = 0u,
                                reserved2 = 0u,
                                reserved3 = 0u,
                                reserved4 = 0u,
                                reserved5 = 0u,
                                reserved6 = 0u
                            ),
                            miscInfo = byteArrayOf(93, 1, 0, 0),
                            whPerKm = 0.0f,
                            whPerKmRegen = 0.0f,
                            availableModes = 7u,
                            currentRideMode = 4u,
                            vehicleRangeType = 356909532u,
                            range = 119u,
                            rtc = byteArrayOf(-112, 16, 58, 11, 6, 6, 2, 6),
                            bus = 0u
                        )*/
                        val vcuStatus = (vcu.vcuStatusH.toULong() shl 32) or vcu.vcuStatusL.toULong()
                        val bmsStatus = (vcu.statusH.toULong() shl 32) or vcu.statusL.toULong()

                        // Extract VCU Faults
                        categoryFaults["VCU"] = vcuFlags
                            .filter { isBitSet(vcuStatus, it.bit) }
                            .map { FaultItem(it.label, it.severity) }

                        // Extract BMS Faults
                        categoryFaults["BMS"] = bmcFlags
                            .filter { isBitSet(bmsStatus, it.bit) }
                            .map { FaultItem(it.label, it.severity) }

                        // Update UI immediately
                        refreshFaultUI()

                        // Update other non-fault UI elements
                        tvThrottleValue.text = vcu.throttlePercent.toString()
                        tvPitchValue.text = "%.1f".format(vcu.pitch)
                        tvRollValue.text = "%.1f".format(vcu.roll)
                    }
                }

                launch {
                    carViewModel.imxDbgMsg.collect { imxDbgMsg ->
                        tvRpmValue.text = imxDbgMsg.shaftRpm.toString()

                        //dummy
                        /*val imxDbgMsg = ImxDbgMsg(
                            packVoltage = 105.47f,
                            packCurrent = 0.50012213f,
                            maxCellTemperature = 24.449999f,
                            maxCellVoltage = 3.7696f,
                            minCellVoltage = 3.7570999f,
                            motorTemperature = 22.0f,
                            motorHeatSinkTemperature = 24.0f,
                            fetTemp = 35222.48f,
                            shaftRpm = 0,
                            availableModes = 7u,
                            dischargeAh = 1.2587525f,
                            chargeAh = 5.4661028E-5f,
                            dischargeEnergy = 132.13126f,
                            chargeEnergy = 0.0045535024f,
                            chargeTtf = 0u
                        )*/
                        sharedViewModel.peakMotTemp = maxOf(sharedViewModel.peakMotTemp, imxDbgMsg.motorTemperature)
                        sharedViewModel.peakHeatSinkTemp =
                            maxOf(sharedViewModel.peakHeatSinkTemp, imxDbgMsg.motorHeatSinkTemperature)
                        val telemetryItems = listOf(
                            TelemetryItem.fromRaw(
                                "PACK VOLTAGE",
                                String.format(Locale.US, "%.4f", imxDbgMsg.packVoltage)
                            ),
                            TelemetryItem.fromRaw(
                                "PACK CURRENT",
                                String.format(Locale.US, "%.4f", imxDbgMsg.packCurrent)
                            ),
                            TelemetryItem.fromRaw(
                                "MAX CELL TEMP",
                                String.format(Locale.US, "%.4f", imxDbgMsg.maxCellTemperature)
                            ),
                            TelemetryItem.fromRaw(
                                "MAX CELL VOLTAGE",
                                String.format(Locale.US, "%.4f", imxDbgMsg.maxCellVoltage)
                            ),
                            TelemetryItem.fromRaw(
                                "MIN CELL VOLTAGE",
                                String.format(Locale.US, "%.4f", imxDbgMsg.minCellVoltage)
                            ),
                            TelemetryItem.fromRaw(
                                "IMDP RESIST",
                                String.format(Locale.US, "%.4f", imxDbgMsg.fetTemp)
                            ),
                            TelemetryItem.fromRaw(
                                "MOTOR TEMP",
                                String.format(Locale.US, "%.4f", imxDbgMsg.motorTemperature)
                            ),
                            TelemetryItem.fromRaw(
                                "PEAK MOTOR TEMP",
                                String.format(Locale.US, "%.4f", sharedViewModel.peakMotTemp)
                            ),
                            TelemetryItem.fromRaw(
                                "HEATSINK TEMP",
                                String.format(Locale.US, "%.4f", imxDbgMsg.motorHeatSinkTemperature)
                            ),
                            TelemetryItem.fromRaw(
                                "PEAK HEATSINK TEMP",
                                String.format(Locale.US, "%.4f", sharedViewModel.peakHeatSinkTemp)
                            ),
                        )
                        (rvTelemetry?.adapter as? TelemetryAdapter)?.submitList(telemetryItems)
                    }
                }

                launch {
                    carViewModel.rtcTime.collect { value ->
                        d("UI Update", "RTC Time: $value")
                        if (value.isEmpty()) return@collect
                        /*if (value.size < 8 || value[7].toUByte().toInt() == 0) {
                            return@collect
                        }*/

                        /*val dummyValue = intArrayOf(
                        24,    // [0] Day
                        14,    // [1] Hour (24h format)
                        30,    // [2] Minute
                        5,     // [3] Second
                        10,    // [4] Month (October)
                        2,     // [5] Weekday (e.g., Tuesday)
                        25,    // [6] Year offset (2000 + 25 = 2025)
                        1      // [7] Status/Validation flag (must be > 0)
                        )*/
                        //setTimeAndDate(value)
                    }
                }

		launch {
                    carViewModel.vcuMiscInfo.collect { miscInfo ->
                        categoryFaults["MISC"] = vcuMiscFlags
                            .filter { miscInfo.hasFlag(it.bit) }
                            .map { FaultItem(it.label, it.severity) }

                        refreshFaultUI()

                    }
                }
                launch {
                    carViewModel.mcuFaultData.collect { mcuFaultData ->
                        categoryFaults["MCU"] = mcuFlags
                            .filter { mcuFaultData.hasFlag(it.bit) }
                            .map { FaultItem(it.label, it.severity) }

                        refreshFaultUI()
                    }
                }

                launch {
                    carViewModel.mcuPmicFaultData.collect { mcuPmicFaultData ->
                        categoryFaults["MCU_PMIC"] = mcuPmicFlags
                            .filter { mcuPmicFaultData.hasFlag(it.bit) }
                            .map { FaultItem(it.label, it.severity) }

                        refreshFaultUI()
                    }
                }

                launch {
                    carViewModel.swiftButton.collect { swiftButton ->
                        val button = Utilities.getButtonState(swiftButton)
                         if (ButtonNavigation.Enter == button) {
                            val destination = when {
                                 isHover -> R.id.hoverModeFragment
                                 isCHarging -> R.id.chargingFragment
                                 else -> R.id.dashboardFragment
                             }
                             findNavController().navigate(destination)
                        }
                        if (ButtonNavigation.Right == button) {
                            findNavController().navigate(R.id.action_debugFragment_to_versionFragment)
                        }
                    }

                }
                launch {
                    // This loop runs as long as the Fragment is in the STARTED state
                    while (true) {
                        val (hwUptime, fwUptime) = getUptimeStrings()
                        tvTime.text = "HW: $hwUptime | SW: $fwUptime"

                        // Check for Soft Reboot (Framework restart)
                        // If Kernel is up for 10 hours but Framework only for 1 minute, show red
                        val kernelMs = android.os.SystemClock.elapsedRealtime()
                        val frameworkMs = kernelMs - frameworkStartTime
                        if (kernelMs > 60000 && frameworkMs < (kernelMs - 10000)) {
                            tvTime.setTextColor(Color.RED) // Visual alert that a crash occurred
                        } else {
                            tvTime.setTextColor(Color.WHITE)
                        }

                        kotlinx.coroutines.delay(1000) // Update every second
                    }
                }
               launch {
                    carViewModel.tellTales.collect { tellTales ->
                        isHover= tellTales.modeHover==1
			isCHarging = tellTales.charger == 1 || tellTales.charger == 2
                    }
                }
            }
        }
    }

    // ---------------- HELPERS ----------------

    private fun isBitSet(value: ULong, bit: Int): Boolean {
        return ((value shr bit) and 1uL) == 1uL
    }

    private fun ByteArray.toFloatFromBytes(order: ByteOrder = ByteOrder.LITTLE_ENDIAN): Float? =
        try {
            ByteBuffer.wrap(this).order(order).getFloat(0)
        } catch (_: Exception) {
            null
        }

    private fun ByteArray.toTrimmedAscii(): String =
        decodeToString(throwOnInvalidSequence = false)
            .trim { it <= ' ' || it.code == 0 }
            .ifBlank { "-" }

    private fun updateFaultColumns(items: List<FaultItem>) {
        if (items.size <= MAX_SINGLE_COLUMN_ITEMS) {
            rvStatus?.visibility = View.VISIBLE
            rvFaults?.visibility = View.GONE

            (rvStatus?.adapter as? StatusAdapter)?.submitList(
                items.map { StatusItem(it.message, it.severity) }
            )
        } else {
            rvStatus?.visibility = View.VISIBLE
            rvFaults?.visibility = View.VISIBLE

            val mid = (items.size + 1) / 2
            val left = items.take(mid)
            val right = items.drop(mid)

            (rvStatus?.adapter as? StatusAdapter)?.submitList(
                left.map { StatusItem(it.message, it.severity) }
            )

            (rvFaults?.adapter as? FaultAdapter)?.submitList(right)
        }
    }

    fun getUptimeStrings(): Pair<String, String> {
        val currentTime = android.os.SystemClock.elapsedRealtime()

        // 1. Kernel Uptime: Time since the SM6115 hardware powered on
        val kernelUptime = formatTime(currentTime)

        // 2. Framework Uptime: Time since this specific process started
        // This will grow as long as the system server doesn't crash
        val frameworkUptime = formatTime(currentTime - frameworkStartTime)

        return Pair(kernelUptime, frameworkUptime)
    }

    private fun formatTime(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = (millis / (1000 * 60 * 60))
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopLogging()
        rvTelemetry = null
        rvStatus = null
        rvFaults = null
    }
    // folder structure code
    private val folderPickerLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->

            if (uri == null) {
                Log.e("FOLDER_PICK", "No folder selected")
                return@registerForActivityResult
            }

            Log.d("FOLDER_PICK", "Selected URI: $uri")

            // Persist permission
            requireContext().contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            // Start reading files
            readFolderStructure(uri)
        }
    private fun readFolderStructure(uri: Uri) {

        Thread {
            try {
                val resolver = requireContext().contentResolver

                val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                    uri,
                    DocumentsContract.getTreeDocumentId(uri)
                )

                val cursor = resolver.query(
                    childrenUri,
                    arrayOf(
                        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                        DocumentsContract.Document.COLUMN_MIME_TYPE
                    ),
                    null,
                    null,
                    null
                )

                val file = File(requireContext().getExternalFilesDir(null), "folder_structure.txt")
                val writer = BufferedWriter(FileWriter(file, false))

                cursor?.use {
                    while (it.moveToNext()) {
                        val name = it.getString(0)
                        val mime = it.getString(1)

                        Log.d("FILES", name)
                        writer.write(name)
                        writer.newLine()
                    }
                }

                writer.flush()
                writer.close()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun startLogging(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
		d("LOGGING", "StartLogging")
                val process = Runtime.getRuntime().exec(arrayOf("sh", "/system/etc/onDemand_logcat.sh"))

                // Read stdout
                val stdout = process.inputStream.bufferedReader().readText()
                val stderr = process.errorStream.bufferedReader().readText()

                val exitCode = process.waitFor()

                d("LOGGING", "Exit code: $exitCode")
                d("LOGGING", "stdout: $stdout")
                e("LOGGING", "stderr: $stderr")
            } catch (e: Exception) {
                e("LOGGING", "Exception: ${e.message}", e)
            }
        }
    }
    fun stopLogging() {
    }
}


