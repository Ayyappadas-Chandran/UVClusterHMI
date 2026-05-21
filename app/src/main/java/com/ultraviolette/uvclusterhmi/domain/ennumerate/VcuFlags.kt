package com.ultraviolette.uvclusterhmi.domain.ennumerate

object VcuStatusFlags {

    /* -------- Lower Word Bits (0–31) -------- */

    const val STAT_VCU_LOG_UPLOAD_RUNNING = 0
    const val STAT_VCU_BMS_VCU_INCOMPATIBLE_VSN = 1
    const val STAT_VCU_FW_DL_RUNNING = 2
    const val STAT_VCU_KEY_EVENT = 3
    const val STAT_VCU_MOTOR_CON_KEY_SW_ON = 4
    const val STAT_VCU_MOTOR_CON_DIR_FWD = 5
    const val STAT_VCU_MOTOR_CON_DIR_REV = 6
    const val STAT_VCU_VEHICLE_KEY_OFF = 7
    const val STAT_VCU_FRONT_BRAKE_PRESS = 8
    const val STAT_VCU_REAR_BRAKE_PRESS = 9
    const val STAT_VCU_MOTOR_CON_FAULT = 10
    const val STAT_VCU_IMU_FAULT = 11
    const val STAT_VCU_IMU_DMP_FAULT = 12
    const val STAT_VCU_LAC_BUS_LOW_VOLTAGE_WARNING = 13
    const val STAT_VCU_MOTOR_OVER_TEMPERATURE = 14
    const val STAT_VCU_RTC_INIT_FAILURE = 15
    const val STAT_VCU_RTC_READ_FAILURE = 16
    const val STAT_VCU_ABS_REAR_WHEEL_SPEED_SENSOR_FAILURE = 17
    const val STAT_VCU_ABS_FRONT_WHEEL_SPEED_SENSOR_FAILURE = 18
    const val STAT_VCU_MC_SDO_UPDATE_SIG = 19
    const val STAT_VCU_IMU_OFS_CALIBRATION = 20
    const val STAT_VCU_NVM_TIMEOUT = 21
    const val STAT_VCU_FW_UPD_READY = 22
    const val STAT_VCU_SM_INVALID_STATE_ENTRY = 23
    const val STAT_VCU_BMS_CAN_MSG_TIMEOUT = 24
    const val STAT_VCU_BMS_CAN_LINK_FAIL = 25
    const val STAT_VCU_CHARGING_IN_PROGRESS = 26
    const val STAT_VCU_CHARGING_COMPLETE = 27
    const val STAT_VCU_CAN_MSG_EXEC_ERR = 28
    const val STAT_VCU_SIDE_STAND_DEPLOYED = 29
    const val STAT_VCU_MC_MODE_GLIDE = 30
    const val STAT_VCU_MC_MODE_COMBAT = 31

    /* -------- Upper Word Bits (32–63) -------- */

    const val STAT_VCU_MC_MODE_BALLISTIC = 32
    const val STAT_VCU_MOTOR_HS_OVER_TEMPERATURE = 33
    const val STAT_VCU_MC_TMAP_LOAD_FAIL = 34
    const val STAT_VCU_MC_TMAP_UPDATED = 35
    const val STAT_VCU_MC_TMAP_COMITTED = 36
    const val STAT_VCU_MC_TMAP_FACT_RESET = 37
    const val STAT_VCU_THROTTLE_ERROR = 38
    const val STAT_VCU_SWIF_ERROR = 39
    const val STAT_VCU_MC_REGEN = 40
    const val STAT_VCU_BMS_SW_EXCEPTION = 41
    const val STAT_VCU_ABS_MODE = 42
    const val STAT_VCU_ABS_FCN_ACTIVE = 43
    const val STAT_VCU_ABS_MODE_ERR = 44
    const val STAT_VCU_CHARGING_ERROR = 45
    const val STAT_VCU_PA_MODE_FWD = 46
    const val STAT_VCU_PA_MODE_REV = 47
    const val STAT_VCU_PA_MODE_ENTRY = 48
    const val STAT_VCU_UP_HH_ACTIVE = 49
    const val STAT_VCU_PA_MODE_ERROR = 50
    const val STAT_VCU_MC_PA_ERROR = 51
    const val STAT_VCU_VACATION_MODE = 52
    const val STAT_VCU_PHY_LINK_RST_FAIL = 53
    const val STAT_VCU_PHY_LINK_TIMEOUT = 54
    const val STAT_VCU_MC_IN_BALLISTIC_DERATION = 55
    const val STAT_VCU_MC_FACT_RESET = 56
    const val STAT_VCU_MC_MODE_HOVER = 57
    const val STAT_VCU_ODO_NVM_ERROR = 58
    const val STAT_VCU_SWIF_INTERNAL_ERROR = 59
    const val STAT_VCU_RE_UPDATED = 60
        const val STAT_VCU_KILL_SW_ACTIVE = 61
    const val STAT_VCU_MC_INCOMPATIBLE = 62
    const val STAT_VCU_MQTT_CMD_ACK = 63

    /** Total number of valid VCU status flags */
    const val MAX_STATUS_FLAGS = 64
}


object VcuMiscFlags {

    /* -------- Status Word Group 0 (0–31) -------- */

    const val STAT_VCU_MISC_MTC_FCN_ACTIVE = 0
    const val STAT_VCU_MISC_MTC_ERROR = 1
    const val STAT_VCU_MISC_MTC_CTRL_REFUSED = 2
    const val STAT_VCU_MISC_MTC_EN = 3
    const val STAT_VCU_MISC_MTC_MODE_SPORT = 4
    const val STAT_VCU_MISC_MTC_MODE_ROAD = 5
    const val STAT_VCU_MISC_MTC_MODE_RAIN = 6
    const val STAT_VCU_MISC_WOL_TIMEOUT = 7
    const val STAT_VCU_MISC_SLEEP_MODE = 8
    const val STAT_VCU_MISC_LAC_CHARGING = 9
    const val STAT_VCU_IMU_FALL_DETECTED = 10
    const val STAT_VCU_IMU_MOTION_DETECTED = 11
    const val STAT_VCU_MISC_MTC_CRC_ERROR = 12
    const val STAT_VCU_IMU_TOW_DETECTED = 13
    const val STAT_VCU_IMU_CRASH_DETECTED = 14
    const val STAT_VCU_LEFT_IND_ACTIVE = 15
    const val STAT_VCU_RIGHT_IND_ACTIVE = 16
    const val STAT_VCU_FORCE_RESET_EXEC = 17
    const val STAT_VCU_ABS_COMM_TIMEOUT = 18
    const val STAT_VCU_MC_MIL_ACTIVE = 19
    const val STAT_VCU_CSEC_OP_TIMEOUT = 20
    const val STAT_VCU_STN_CHG_TERM_EVENT = 21
    const val STAT_VCU_ABS_PROG_ACTIVE = 22
    const val STAT_VCU_FAST_CHARGER_CONNNECTED = 23
    const val STAT_VCU_HORN_PRESSED = 24
    const val STAT_VCU_MISC_PRE_CRASH_TRIGGER = 25
    const val STAT_VCU_MISC_CLU_RDY = 26
    const val STAT_VCU_LAC_CHARGING_TIMEOUT = 27
    const val STAT_VCU_MISC_SLEEP_MODE_L2 = 28
    const val STAT_VCU_LAC_DCDC_CHG_ERR = 29
    const val STAT_VCU_MC_TMP_SNS_ERR = 30
    const val STAT_VCU_VEH_IN_SVC_MODE = 31

    /* -------- Status Word Group 1 (32–63) -------- */

    const val STAT_VCU_LFX_SENTRY_AIRBUS_DISABLED = 32
    const val STAT_VCU_LFX_SENTRY_ON_AIRBUS_OFF = 33
    const val STAT_VCU_LFX_SENTRY_OFF_AIRBUS_ON = 34
    const val STAT_VCU_LFX_SENTRY_ON_AIRBUS_ON = 35
    const val STAT_VCU_IMU_FALL_DETECT_ON = 36
    const val STAT_VCU_IMU_TOW_DETECT_ON = 37
    const val STAT_VCU_IMU_SENTRY_MODE_ON = 38
    const val STAT_VCU_CHG_ENDPOINT_LIMITED = 39
    const val STAT_VCU_IMU_LOGGING_ENABLED = 40
    const val STAT_VCU_MC_LCA_AVAILABLE = 41
    const val STAT_VCU_MC_LCA_ENGAGED = 42
    const val STAT_VCU_MC_LCA_ACTIVE = 43
    const val STAT_VCU_MC_LCA_USR_EXIT = 44
    const val STAT_VCU_LC_LCA_COND_EXIT = 45
    const val STAT_VCU_LC_LCA_TIMEOUT = 46
    const val STAT_VCU_MC_TPDO_TIMEOUT = 47
    const val STAT_VCU_BL_PATCH_DONE = 48
    const val STAT_VCU_RDR_BSM_RUNNING = 49
    const val STAT_VCU_RDR_BSM_PAUSED = 50
    const val STAT_VCU_RDR_BSM_ERROR = 51
    const val STAT_VCU_RDR_BSM_DISABLED = 52
    const val STAT_VCU_RDR_BSM_LHS_WARN = 53
    const val STAT_VCU_RDR_BSM_LHS_ALRT = 54
    const val STAT_VCU_RDR_BSM_RHS_WARN = 55
    const val STAT_VCU_RDR_BSM_RHS_ALRT = 56
    const val STAT_VCU_RDR_RCW_ALRT = 57
    const val STAT_VCU_SWIF_SEM_WAIT = 58
    const val STAT_VCU_RDR_SENSOR_BLOCKED = 59
    const val STAT_VCU_RDR_SENSOR_DISABLED = 60
    const val STAT_VCU_RDR_SENSOR_LIMITED = 61
    const val STAT_VCU_RDR_SENSOR_ACTIVE = 62
    const val STAT_VCU_RDR_SENSOR_INSPECTION = 63

    /* -------- Status Word Group 2 (64–84) -------- */

    const val STAT_VCU_RDR_REAR_CRC_ERR = 64
    const val STAT_VCU_RDR_FRONT_CRC_ERR = 65
    const val STAT_VCU_MC_SURGE_MODE = 66
    const val STAT_VCU_MC_INVALID_ENCODER_OFFSET = 67
    const val STAT_VCU_MC_SDO_UPDATE_FAIL = 68
    const val STAT_VCU_RDR_BSM_TURNED_OFF = 69
    const val STAT_VCU_RDR_RCW_TURNED_OFF = 70
    const val STAT_VCU_INVALID_MC_FW = 71
    const val STAT_VCU_INVALID_MC_PROD_CODE = 72
    const val STAT_VCU_MC_DETAILS_NA = 73
    const val STAT_VCU_MC_SPEED_LIMIT_SET = 74
    const val STAT_VCU_MC_ENCODER_OFFSET_UPDATED = 75
    const val STAT_VCU_MC_IN_LOCKDOWN = 76
    const val STAT_VCU_CHG_INCOMPATIBLE = 77
    const val STAT_VCU_RE_LOAD_FAILED = 78
    const val STAT_VCU_CCG_INIT_FAIL = 79
    const val STAT_VCU_CCG_MCP_RX_ERROR = 80
    const val STAT_VCU_VNIC_RNDIS_CONNECTED = 81
    const val STAT_VCU_CCG_RX_ERR = 82
    const val STAT_VCU_CHARGER_FLAP_OPENED = 83
    const val STAT_VCU_MC_TMAP_FACT_RESET_AT_BOOT = 84
    const val STAT_VCU_CD_DETECTED = 85
    const val STAT_VCU_SHMEM_OOM = 86
    const val STAT_VCU_CPU_CORE_OVERTEMP_ALERT = 87
    const val STAT_VCU_MC_CC_OFF = 88
    const val STAT_VCU_MC_CC_STBY = 89
    const val STAT_VCU_MC_CC_ACTIVE = 90
    const val STAT_VCU_MC_CC_ERROR = 91
    const val STAT_VCU_IMU_WORLD_CAL_MISSING = 92
    const val STAT_VCU_IMU_FRAME_CAL_MISSING = 93
    const val STAT_VCU_MC_CC_FEAT_EN = 94
    const val STAT_VCU_ABS_WARNING_LAMP_ON = 95
    const val STAT_VCU_VNIC_USB_TX_ABORTED = 96
    const val STAT_VCU_USB_HBT_TIMEOUT = 97
    const val STAT_VCU_USB_HBT_DISABLED = 98
    const val STAT_VCU_ALS_INVALID_SCAN = 99
    const val STAT_VCU_MC_BAAS_LOCKDOWN_TRIGGERED = 100
    const val STAT_VCU_MC_IN_BAAS_LOCKDOWN = 101
    const val STAT_VCU_DSP_IMEI_UPD_EVENT = 102
    const val STAT_VCU_DSP_VIN_UPD_EVENT = 103
    const val STAT_VCU_UNUSED_104 = 104
    const val STAT_VCU_UNUSED_105 = 105
    const val STAT_VCU_UNUSED_106 = 106
    const val STAT_VCU_UNUSED_107 = 107
    const val STAT_VCU_UNUSED_108 = 108
    const val STAT_VCU_UNUSED_109 = 109
    const val STAT_VCU_UNUSED_110 = 110
    const val STAT_VCU_UNUSED_111 = 111
    const val STAT_VCU_UNUSED_112 = 112
    const val STAT_VCU_UNUSED_113 = 113
    const val STAT_VCU_UNUSED_114 = 114
    const val STAT_VCU_UNUSED_115 = 115
    const val STAT_VCU_UNUSED_116 = 116
    const val STAT_VCU_UNUSED_117 = 117
    const val STAT_VCU_UNUSED_118 = 118
    const val STAT_VCU_UNUSED_119 = 119
    const val STAT_VCU_UNUSED_120 = 120
    const val STAT_VCU_UNUSED_121 = 121
    const val STAT_VCU_UNUSED_122 = 122
    const val STAT_VCU_UNUSED_123 = 123
    const val STAT_VCU_UNUSED_124 = 124
    const val STAT_VCU_UNUSED_125 = 125
    const val STAT_VCU_UNUSED_126 = 126
    const val STAT_VCU_UNUSED_127 = 127


    /** Total number of valid misc status flags */
    const val MAX_MISC_STATUS_FLAGS =128 //previously 85
}


object BmsStatusFlags {

    /* -------- Status Low Bits (0–31) -------- */

    const val STAT_HSC_STATUS_FLAG = 0
    const val STAT_LSC_STATUS_FLAG = 1
    const val STAT_BAL_TIMER_STATUS_FLAG = 2
    const val STAT_BAL_ACT_STATUS_FLAG = 3
    const val STAT_LTC2946_DSG_ALERT_FLAG = 4
    const val STAT_LTC2946_CHG_ALERT_FLAG = 5
    const val STAT_PWR_MODE_CHARGE = 6
    const val STAT_PWR_MODE_CHARGE_NX = 7
    const val STAT_BMS_UNECOVERABLE_FAILURE = 8
    const val STAT_UV_THR_FLAG = 9
    const val STAT_OV_THR_FLAG = 10
    const val STAT_LTC6812_WDT_SET_FLAG = 11
    const val STAT_BATTERY_TEMP_OVER_MIN_THRESHOLD = 12
    const val STAT_BATTERY_TEMP_OVER_MAX_THRESHOLD = 13
    const val STAT_BATTERY_TEMP_TOO_LOW = 14
    const val STAT_AFE_SAFETY_TIMER_FLAG = 15
    const val STAT_BALANCER_ABORT_FLAG = 16
    const val STAT_BALANCER_RESET_FLAG = 17
    const val STAT_BALANCING_COMPLETE_FLAG = 18
    const val STAT_AFE_PEC_ERROR = 19
    const val STAT_UV_OV_THR_FOR_TURN_ON = 20
    const val STAT_ECC_ERM_ERR_FLAG = 21
    const val STAT_DSG_INA302_ALERT1 = 22
    const val STAT_DSG_INA302_ALERT2 = 23
    const val STAT_CONTACTOR_OVER_TMP_ALERT = 24
    const val STAT_AFE_INVALID_CMD = 25
    const val STAT_SHUNT_OVER_TMP_ALERT = 26
    const val STAT_BIT_UNUSED_27 = 27
    const val STAT_BIT_UNUSED_28 = 28
    const val STAT_REL_HUMIDITY_OVERVALUE_ALERT = 29
    const val STAT_FUSE_BLOWN_ALERT = 30
    const val STAT_BIT_UNUSED_31 = 31

    /* -------- Status High Bits (32–62) -------- */

    const val STAT_CONT_TURN_ON_FAILURE = 32
    const val STAT_CONT_TURN_OFF_FAILURE = 33
    const val STAT_BAL_RES_OVER_TEMPERATURE = 34
    const val STAT_LTC2946_COMM_FAILURE = 35
    const val STAT_ADS1015_COMM_FAILURE = 36
    const val STAT_BIT_UNUSED_37 = 37
    const val STAT_HW_OVER_TMP_SHUTDOWN = 38
    const val STAT_BIT_UNUSED_39 = 39
    const val STAT_SYS_BOOT_FAILURE = 40
    const val STAT_CAN_MSG_SIG_ERR = 41
    const val STAT_HVIL_N_ERR = 42
    const val STAT_HVIL_P_ERR = 43
    const val STAT_BAT_TAMPER_DETECTED = 44
    const val STAT_TMP_THR_FOR_TURN_ON = 45
    const val STAT_CONT_OVER_TMP_WARN = 46
    const val STAT_SHUNT_OVER_TMP_WARN = 47
    const val STAT_MSD_ERR = 48
    const val STAT_BIT_UNUSED_49 = 49
    const val STAT_BIT_UNUSED_50 = 50
    const val STAT_PM_CHG_CURRENT_LIMIT_UPDATE = 51
    const val STAT_UNSAFE_COND_CONT_TURN_ON = 52
    const val STAT_THRM_RUNAWAY_ALRT_V = 53
    const val STAT_THRM_RUNAWAY_ALRT_T = 54
    const val STAT_THRM_RUNAWAY_ALRT_H = 55
    const val STAT_PRE_DISCHARGE_STRESSED = 56
    const val STAT_LSCONT_SHORT_WARN = 57
    const val STAT_HSCONT_SHORT_WARN = 58
    const val STAT_BAT_MINUS_INS_FAULT = 59
    const val STAT_BAT_PLUS_INS_FAULT = 60
    const val STAT_FUSE_OVER_TMP_WARN = 61
    const val STAT_FUSE_OVER_TMP_ALERT = 62

    /** Total number of valid BMS status flags */
    const val MAX_BMS_STATUS_FLAGS = 63
}

object McuFaultFlag {

    /* -------- Error Bits (1–32) -------- */

    const val SINCOS_ERROR_1 = 1
    const val THROTTLE_ERROR_1 = 2
    const val THROTTLE_ERROR_2 = 3
    const val DRIVE_UNDERVOLTAGE_SW = 4
    const val CRITICAL_OVERVOLTAGE_SW = 5
    const val MOTOR_TEMPERATURE_ERROR = 6
    const val OVERCURRENT_SW = 7
    const val CONTROLLER_TEMPERATURE_ERROR = 8
    const val CURRENT_OFFSET_ERROR = 9
    const val OVERSPEED = 10
    const val INT_WATCHDOG_RESET = 11
    const val EXT_WATCHDOG_RESET = 12
    const val EEPROM_FLASH = 13
    const val EEPROM_STATE = 14
    const val WRITE_ONCE_WRITE = 15
    const val RPDO_TIMEOUT = 16
    const val CAN_PARITY = 17
    const val FLASH_API_INIT_ERROR = 18
    const val RUNTIME_ERROR = 19
    const val NMI_WATCHDOG_RESET = 20
    const val DCSM_SAFE_COPY_RESET = 21
    const val NMI = 22
    const val ITRAP = 23
    const val FLASH_ECC_SELF_TEST_FAILED = 24
    const val RAM_ECC_SELF_TEST_FAILED = 25
    const val ASSERT_CALLED = 26
    const val CLA_OC = 27
    const val ERAD_ISR_TIME = 28
    const val ERAD_STACK_OVR = 29
    const val MOSFET_U_HEALTH_ERROR = 30
    const val MOSFET_V_HEALTH_ERROR = 31
    const val MOSFET_W_HEALTH_ERROR = 32

    /* -------- Error Bits (33–44) -------- */

    const val MOSFET_U_DRIVER_ERROR = 33
    const val MOSFET_V_DRIVER_ERROR = 34
    const val MOSFET_W_DRIVER_ERROR = 35
    const val FRWD_REV_ERROR = 36
    const val ADC_OCSC_SELF_TEST_FAILED = 37
    const val FWC_VS_ERROR = 38
    const val PIE_VECT_CORRUPT = 39
    const val PMIC_FAULT = 40
    const val OTP_EMPTY_INVALID = 41
    const val U_PHASE_IMBALANCE = 42
    const val V_PHASE_IMBALANCE = 43
    const val W_PHASE_IMBALANCE = 44

    /** Total number of valid VCU misc flags */
    const val MAX_VCU_MISC_FLAGS = 44
}

object McuPmicFaultFlag {

    /* -------- Error Bits (1–32) -------- */

    const val VDD5_ILIM = 1
    const val VDD3_5_ILIM = 2
    const val VDD5_OT = 3
    const val VDD_3_5_OT = 4
    const val CFG_CRC_ERR = 5
    const val EE_CRC_ERR = 6
    const val WD_FAIL_CNT_ERROR = 7
    const val ABIST_ERR = 8
    const val LBIST_ERR = 9
    const val NRES_ERR = 10
    const val SPI_ERR = 11
    const val LOCLK = 12
    const val MCU_ERR = 13
    const val WD_ERR = 14
    const val ENDRV_ERR = 15
    const val DEVICE_STATE_ERR = 16
    const val VBATP_OV = 17
    const val VBATP_UV = 18
    const val VCP17_OV = 19
    const val VCP12_OV = 20
    const val VCP12_UV = 21
    const val VDD6_OV = 22
    const val VDD6_UV = 23
    const val VDD5_OV = 24
    const val VDD5_UV = 25
    const val VDD3_5_OV = 26
    const val VDD3_5_UV = 27
    const val WD_CFG_ERR = 28
    const val WD_RST_EN_ERR = 29
    const val WD_WIN1_CFG_ERR = 30
    const val WD_WIN2_CFG_ERR = 31
    const val WD_SYNC_ERR = 32

    /* -------- Error Bits (33–34) -------- */

    const val DIAG_EXIT_ERR = 33
    const val TURN_ON_DIAG_STATE_ERR = 34

    /** Total number of valid PMIC diagnostic flags */
    const val MAX_PMIC_DIAG_FLAGS = 34
}

