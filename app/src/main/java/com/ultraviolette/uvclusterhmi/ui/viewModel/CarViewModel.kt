package com.ultraviolette.uvclusterhmi.ui.viewModel

import android.util.Log.d
import androidx.lifecycle.ViewModel
import com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData.ChargeCtx
import com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData.ChargerCtxObc
import com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData.DevUid
import com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData.ImuData
import com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData.ImxAuxMsg
import com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData.ImxDbgMsg
import com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData.ImxFwVersionMsg
import com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData.McuFaultData
import com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData.McuPmicFaultData
import com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData.TellTales
import com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData.TripInfo
import com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData.TripMeterDisp
import com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData.VcuInfoMsg
import com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData.VehicleMetaData
import com.ultraviolette.uvclusterhmi.domain.manager.PreferenceManager
import com.ultraviolette.uvclusterhmi.domain.repository.CarRepository
import com.ultraviolette.uvclusterhmi.utils.Utilities.PROP_ID_ABS_MODE
import com.ultraviolette.uvclusterhmi.utils.Utilities.PROP_ID_ABS_MODE_STATUS
import com.ultraviolette.uvclusterhmi.utils.Utilities.PROP_ID_CHARGER_EVT
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
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import com.ultraviolette.uvclusterhmi.domain.ennumerate.VcuStatusFlags
import com.ultraviolette.uvclusterhmi.domain.ennumerate.VcuMiscFlags
import com.ultraviolette.uvclusterhmi.domain.ennumerate.BmsStatusFlags
import com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData.VcuMiscInfo
import com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData.hasBmsFlag
import com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData.hasFlag
import com.ultraviolette.uvclusterhmi.domain.ennumerate.McuFaultFlag
import com.ultraviolette.uvclusterhmi.domain.ennumerate.McuPmicFaultFlag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.NonCancellable.isActive
import androidx.lifecycle.viewModelScope
import com.ultraviolette.uvclusterhmi.utils.Utilities.PROP_ID_CRUISE_CONTROL
import kotlinx.coroutines.launch


enum class RadarState {
    Off,
    Warn,
    Alert
}

enum class RadarTelltaleState {
    Off,
    On,
    Malfunction
}

enum class CruiseState {
    OFF,
    STANDBY,
    ACTIVE,
    ERROR
}

class CarViewModel(private val carRepository: CarRepository?) : ViewModel() {

    private val _vehicleValue = MutableStateFlow(floatArrayOf())
    val vehicleValue: StateFlow<FloatArray> = _vehicleValue.asStateFlow()

    private val _regen = MutableStateFlow(intArrayOf())
    val regen: StateFlow<IntArray> = _regen.asStateFlow()

    private val _absMode = MutableStateFlow(0)
    val absMode: StateFlow<Int> = _absMode.asStateFlow()

    private val _hillHoldState = MutableStateFlow(0)
    val hillHoldState: StateFlow<Int> = _hillHoldState.asStateFlow()

    private val _hillHoldIcon = MutableStateFlow(0)
    val hillHoldIcon: StateFlow<Int> = _hillHoldIcon.asStateFlow()

    private val _rtcTime = MutableStateFlow(intArrayOf())
    val rtcTime: StateFlow<IntArray> = _rtcTime.asStateFlow()

    private val _rideModes = MutableStateFlow(0)
    val rideModes: StateFlow<Int> = _rideModes.asStateFlow()

    private val _chargerEvt = MutableStateFlow(0)
    val chargerEvt: StateFlow<Int> = _chargerEvt.asStateFlow()


    private val _screenModes = MutableStateFlow(0)
    val screenModes: StateFlow<Int> = _screenModes.asStateFlow()

    private val _indicator = MutableStateFlow(0)
    val indicator: StateFlow<Int> = _indicator.asStateFlow()

    private val _highBeamTellTale = MutableStateFlow(0)
    val highBeamTellTale: StateFlow<Int> = _highBeamTellTale.asStateFlow()

   private val _heartBeatstatus = MutableStateFlow(0)
    val heartBeatstatus: StateFlow<Int> = _heartBeatstatus.asStateFlow()

    private val _hazardLightTellTale = MutableStateFlow(0)
    val hazardLightTellTale: StateFlow<Int> = _hazardLightTellTale.asStateFlow()

    private val _motorArmDisarmTellTale = MutableStateFlow(0)
    val motorArmDisarmTellTale: StateFlow<Int> = _motorArmDisarmTellTale.asStateFlow()

   //SharedFlow for cmd-only (no data)
    private val _vehicleInfoRequest = MutableSharedFlow<Unit>( extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val vehicleInfoRequest: SharedFlow<Unit> = _vehicleInfoRequest.asSharedFlow()

    private val _lockdown = MutableStateFlow(0)
    val lockdown: StateFlow<Int> = _lockdown.asStateFlow()

    private val _swiftButton = MutableSharedFlow<Int>(
        replay = 0,
        extraBufferCapacity = 16
    )
    val swiftButton: SharedFlow<Int> = _swiftButton.asSharedFlow()

    private val _sleepWake = MutableStateFlow(0)
    val sleepWake: StateFlow<Int> = _sleepWake.asStateFlow()

    private val _displayBrightness = MutableStateFlow(intArrayOf())
    val displayBrightness: StateFlow<IntArray> = _displayBrightness.asStateFlow()

    private val _absModeStatus = MutableStateFlow(0)
    val absModeStatus: StateFlow<Int> = _absModeStatus.asStateFlow()

    private val _mcThermal = MutableStateFlow(floatArrayOf())
    val mcThermal: StateFlow<FloatArray> = _mcThermal.asStateFlow()

    private val _mcNoArm = MutableSharedFlow<IntArray>(replay = 0, extraBufferCapacity = 8)
    val mcNoArm: SharedFlow<IntArray> = _mcNoArm.asSharedFlow()
    private val _cruise = MutableStateFlow(0)
    val cruise: StateFlow<Int> = _cruise.asStateFlow()

    private val _fotaUpdate = MutableStateFlow(intArrayOf())
    val fotaUpdate: StateFlow<IntArray> = _fotaUpdate.asStateFlow()

    private val _mtcMode = MutableStateFlow(0)
    val mtcMode: StateFlow<Int> = _mtcMode.asStateFlow()

    private val _vcuInfoMsg = MutableStateFlow(VcuInfoMsg())
    val vcuInfoMsg: StateFlow<VcuInfoMsg> = _vcuInfoMsg.asStateFlow()

    private val _imxDbgMsg = MutableStateFlow(ImxDbgMsg())
    val imxDbgMsg: StateFlow<ImxDbgMsg> = _imxDbgMsg.asStateFlow()

    private val _tripMeter = MutableStateFlow(TripMeterDisp())
    val tripMeter: StateFlow<TripMeterDisp> = _tripMeter.asStateFlow()

    private val _imuData = MutableStateFlow(ImuData())
    val imuData: StateFlow<ImuData> = _imuData.asStateFlow()

    private val _imxFwVersionMsg = MutableStateFlow(ImxFwVersionMsg())
    val imxFwVersionMsg: StateFlow<ImxFwVersionMsg> = _imxFwVersionMsg.asStateFlow()

    private val _imxAuxMsg = MutableStateFlow(ImxAuxMsg())
    val imxAuxMsg: StateFlow<ImxAuxMsg> = _imxAuxMsg.asStateFlow()

    private val _chargeCtx = MutableStateFlow(ChargeCtx())
    val chargeCtx: StateFlow<ChargeCtx> = _chargeCtx.asStateFlow()

    private val _chargerCtxObc = MutableStateFlow(ChargerCtxObc())
    val chargerCtxObc: StateFlow<ChargerCtxObc> = _chargerCtxObc.asStateFlow()

    private val _tellTales = MutableStateFlow(TellTales())
    val tellTales: StateFlow<TellTales> = _tellTales.asStateFlow()

    private val _vcuMiscInfo = MutableStateFlow(VcuMiscInfo())
    val vcuMiscInfo: StateFlow<VcuMiscInfo> = _vcuMiscInfo.asStateFlow()

    private val _mcuFaultData = MutableStateFlow(McuFaultData())
    val mcuFaultData: StateFlow<McuFaultData> = _mcuFaultData.asStateFlow()

    private val _mcuPmicFaultData = MutableStateFlow(McuPmicFaultData())
    val mcuPmicFaultData: StateFlow<McuPmicFaultData> = _mcuPmicFaultData.asStateFlow()

    private val _leftRadarState = MutableStateFlow(RadarState.Off)
    val leftRadarState: StateFlow<RadarState> = _leftRadarState.asStateFlow()

    private val _rightRadarState = MutableStateFlow(RadarState.Off)
    val rightRadarState: StateFlow<RadarState> = _rightRadarState.asStateFlow()
    
    private val _rcwRadarState = MutableStateFlow(false)
    val rcwRadarState=_rcwRadarState.asStateFlow()

    private val _radarTelltaleState = MutableStateFlow(RadarTelltaleState.Off)
    val radarTellTaleState: StateFlow<RadarTelltaleState> = _radarTelltaleState.asStateFlow()

    private val _ballisticPlus = MutableStateFlow(false)
    val ballisticPlus = _ballisticPlus.asStateFlow()

    private val _paFwd = MutableStateFlow(false)
    val paFwd = _paFwd.asStateFlow()
    private val _paRev = MutableStateFlow(false)
    val paRev = _paRev.asStateFlow()
    private val _paEntry = MutableStateFlow(false)
    val paEntry: StateFlow<Boolean> = _paEntry

    private val _ccOff = MutableStateFlow(false)
    val ccOff = _ccOff.asStateFlow()
    private val _ccSTBY = MutableStateFlow(false)
    val ccSTBY = _ccSTBY.asStateFlow()
    private val _ccActive = MutableStateFlow(false)
    val ccActive = _ccActive.asStateFlow()
    private val _ccError = MutableStateFlow(false)
    val ccError = _ccError.asStateFlow()
    private val _thermalRunwayV = MutableStateFlow(false)
    val thermalRunwayV = _thermalRunwayV.asStateFlow()
    private val _thermalRunwayH = MutableStateFlow(false)
    val thermalRunwayH = _thermalRunwayH.asStateFlow()
    private val _thermalRunwayT = MutableStateFlow(false)
    val thermalRunwayT = _thermalRunwayT.asStateFlow()
    private val _keyOff= MutableStateFlow(false)
    val keyOff = _keyOff.asStateFlow()

    private var heartbeatJob: Job? = null
    private var heartbeatCounter = 0L
    private var isHeartbeatEnabled = true

    private val MSG_ID_USB_HEARTBEAT_IMX_S32 = 0x2170030F

    private var _bootCompleted = false
    val bootCompleted: Boolean
        get() = _bootCompleted


    fun connect() {
        carRepository?.connect()
        carRepository?.observeProperties { propertyValue ->
            when (propertyValue.propertyId) {
                PROP_ID_VEHICLE_VALUE -> {
                    val vehicleValue = toFloatArray(propertyValue.value)
                    _vehicleValue.value = vehicleValue
                }

                PROP_ID_REGEN -> {
                    val regen = toIntArray(propertyValue.value)
                    _regen.value = regen
                }

                PROP_ID_ABS_MODE -> {
                    val absModeStatus = toInt(propertyValue.value)
                    _absMode.value = absModeStatus
                }

                PROP_ID_HILL_HOLD_STATE -> {
                    val hillHoldState = toInt(propertyValue.value)
                    _hillHoldState.value = hillHoldState
                }

                PROP_ID_HILL_HOLD_ICON -> {
                    val hillHoldIcon = toInt(propertyValue.value)
                    _hillHoldIcon.value = hillHoldIcon
                }

                PROP_ID_FOTA_UPDATE -> {
                    val fotaUpdate = toIntArray(propertyValue.value)
                    _fotaUpdate.value = fotaUpdate
                }

                PROP_ID_RTC_TIME -> {
                    val dateAndTime = toIntArray(propertyValue.value)
                    _rtcTime.value = dateAndTime
                }

                PROP_ID_DISPLAY_BRIGHTNESS -> {
                    val displayBrightness = toIntArray(propertyValue.value)
                    _displayBrightness.value = displayBrightness
                }

                PROP_ID_RIDE_MODES -> {
                    val rideMode = toInt(propertyValue.value)
                    _rideModes.value = rideMode
                }

                PROP_ID_SCREEN_MODES -> {
                    val screenModes = toInt(propertyValue.value)
                    _screenModes.value = screenModes
                }

                PROP_ID_INDICATOR -> {
                    val indicator = toInt(propertyValue.value)
                    _indicator.value = indicator
                }

		PROP_ID_HIGH_BEAM_TELLTALE -> {
                    val highBeamTellTale = toInt(propertyValue.value)
                    _highBeamTellTale.value = highBeamTellTale
                }

		PROP_ID_HEARTBEAT_ENABLE_DISABLE -> {
                val heartBeatstatus = toInt(propertyValue.value)
                _heartBeatstatus.value = heartBeatstatus
        }
		 //VCU sends cmd only, no data
            PROP_ID_VEHICLE_INFO_REQ -> {
                d("CarViewModel", "vehicleInfoRequest cmd received from VCU")
                _vehicleInfoRequest.tryEmit(Unit)  // triggers every time, no dedup
            }

		PROP_ID_HAZARD_LIGHT_TELLTALE -> {
                    val hazardLightTellTale = toInt(propertyValue.value)
                    _hazardLightTellTale.value = hazardLightTellTale
                }

                PROP_ID_MOTOR_ARM_DISARM_TELLTALE -> {
                    val motorArmDisarmTellTale = toInt(propertyValue.value)
                    _motorArmDisarmTellTale.value = motorArmDisarmTellTale
                }

                PROP_ID_LOCKDOWN -> {
                    val lockdown = toInt(propertyValue.value)
                    _lockdown.value = lockdown
                }

                PROP_ID_SWIFT_BUTTON -> {
                    val swiftButton = toInt(propertyValue.value)
                    _swiftButton.tryEmit(swiftButton)
                    d("ButtonNavigation", "PROP_ID_SWIFT_BUTTON clickEvent: $swiftButton")
                }

                PROP_ID_SLEEP_WAKE -> {
                    val sleepWake = toInt(propertyValue.value)
                    _sleepWake.value = sleepWake
                }

                PROP_ID_ABS_MODE_STATUS -> {
                    val absMode = toInt(propertyValue.value)
                    _absModeStatus.value = absMode
                }

                PROP_ID_MTC_MODE -> {
                    val mtcMode = toInt(propertyValue.value)
                    _mtcMode.value = mtcMode
                }

                PROP_ID_CUSTOM -> {
                    (propertyValue.value as? ByteArray)?.let { data ->
                        d("VHALData", "ByteArraySize:${data.size}")
                        parseCustomData(data)
                    }
                }

                PROP_ID_MC_NO_ARM -> {
                    val mcNoArm = toIntArray(propertyValue.value)
                    d("VCU_ALERT_SYSTEM", "[VHAL] Event Received: mcNoArm -> ${mcNoArm.joinToString()}")
                    // Use tryEmit for SharedFlow
                    _mcNoArm.tryEmit(mcNoArm)
                }

                PROP_ID_CRUISE_CONTROL -> {
                    val cruise = toInt(propertyValue.value)
                    d("VHALData", "[VHAL] Event Received: CRUISE -> $cruise")
                    // Use tryEmit for SharedFlow
                    _cruise.tryEmit(cruise)
                }

                PROP_ID_MC_THERMAL -> {
                    val mcThermal = toFloatArray(propertyValue.value)
                    d("VHALData", "mcThermal: ${mcThermal.joinToString()}")
                    _mcThermal.value = mcThermal
                }

                  PROP_ID_CHARGER_EVT -> {
                    val chargerEvt = toInt(propertyValue.value)
                    _chargerEvt.value = chargerEvt
                    d("VHALData", "chargerEvt: $chargerEvt")
                }
            }
        }
    }

    fun sendByteArrayProperty(propertyId: Int, byteArray: ByteArray) =
        carRepository?.sendByteArray(propertyId, byteArray)

    fun sendBoolean(propertyId: Int, value: Boolean) = carRepository?.sendBoolean(propertyId, value)

    fun disconnect() = carRepository?.disconnect()

    fun <T> safeParse(tag: String, block: () -> T?): T? {
        return try {
            block().also {
                d("VHALData", "$tag: $it")
            }
        } catch (e: BufferUnderflowException) {
            d("VHALData", "$tag: Not enough data, skipping")
            null
        }
    }

    //27/01/2026
    fun parseCustomData(data: ByteArray) {
        val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
        d("VHALData", "buffer: ${buffer.position()}")
        safeParse("VCU Info") { parseVcuInfoMessage(buffer) }?.let { vcuInfo ->
            _vcuInfoMsg.value = vcuInfo
            d("VHALData", "VCU Info *** value: $vcuInfo  ***")
            if (vcuInfo.hasFlag(VcuStatusFlags.STAT_VCU_SIDE_STAND_DEPLOYED)) {
                d("VHALData", "TESTING: Side Stand is DOWN [ON]")
            }
            if (vcuInfo.hasBmsFlag(BmsStatusFlags.STAT_HSC_STATUS_FLAG)) {
                d("VHALData", "BMS: Hsc status")
            }
            val isEntry = vcuInfo.hasFlag(VcuStatusFlags.STAT_VCU_PA_MODE_ENTRY)
            _paEntry.value = isEntry

            _paFwd.value = vcuInfo.hasFlag(VcuStatusFlags.STAT_VCU_PA_MODE_FWD)
            _paRev.value = vcuInfo.hasFlag(VcuStatusFlags.STAT_VCU_PA_MODE_REV)

            _thermalRunwayV.value = vcuInfo.hasBmsFlag(BmsStatusFlags.STAT_THRM_RUNAWAY_ALRT_V)
            _thermalRunwayH.value = vcuInfo.hasBmsFlag(BmsStatusFlags.STAT_THRM_RUNAWAY_ALRT_H)
            _thermalRunwayT.value = vcuInfo.hasBmsFlag(BmsStatusFlags.STAT_THRM_RUNAWAY_ALRT_T)
            _keyOff.value=vcuInfo.hasFlag(VcuStatusFlags.STAT_VCU_VEHICLE_KEY_OFF)

            d("thermalRunawayH_CVM","thermalRunwayH: $thermalRunwayH")
            d("thermalRunawayV_CVM","thermalRunwayV: $thermalRunwayV")
            d("thermalRunawayT_CVM","thermalRunwayT: $thermalRunwayT")



            d(
                "VCU_STATUS",
                "VCU_STATUS ---- STAT_VCU_ABS_MODE--- ${vcuInfo.hasFlag(VcuStatusFlags.STAT_VCU_ABS_MODE)}"
            )
            d(
                "VCU_STATUS",
                "VCU_STATUS ---- SSTAT_VCU_ABS_MODE_ERR--- ${vcuInfo.hasFlag(VcuStatusFlags.STAT_VCU_ABS_MODE_ERR)}"
            )
            d(
                "VCU_STATUS",
                "VCU_STATUS ---- STAT_VCU_MC_MODE_BALLISTIC--- ${vcuInfo.hasFlag(VcuStatusFlags.STAT_VCU_MC_MODE_BALLISTIC)}"
            )
            d(
                "VCU_STATUS",
                "VCU_STATUS ---- STAT_VCU_MOTOR_HS_OVER_TEMPERATURE--- ${
                    vcuInfo.hasFlag(VcuStatusFlags.STAT_VCU_MOTOR_HS_OVER_TEMPERATURE)
                }"
            )
            d(
                "VCU_STATUS",
                "VCU_STATUS ---- STAT_VCU_SWIF_ERROR--- ${vcuInfo.hasFlag(VcuStatusFlags.STAT_VCU_SWIF_ERROR)}"
            )
            d(
                "VCU_STATUS",
                "VCU_STATUS ---- STAT_VCU_MC_REGEN--- ${vcuInfo.hasFlag(VcuStatusFlags.STAT_VCU_MC_REGEN)}"
            )
            d(
                "VCU_STATUS",
                "VCU_STATUS ---- STAT_VCU_ABS_FCN_ACTIVE--- ${vcuInfo.hasFlag(VcuStatusFlags.STAT_VCU_ABS_FCN_ACTIVE)}"
            )
            d(
                "VCU_STATUS",
                "VCU_STATUS ---- STAT_VCU_UP_HH_ACTIVE--- ${vcuInfo.hasFlag(VcuStatusFlags.STAT_VCU_UP_HH_ACTIVE)}"
            )
            d(
                "VCU_STATUS",
                "VCU_STATUS ---- STAT_VCU_KILL_SW_ACTIVE--- ${vcuInfo.hasFlag(VcuStatusFlags.STAT_VCU_KILL_SW_ACTIVE)}"
            )
            d(
                "VCU_STATUS",
                "VCU_STATUS ---- STAT_VCU_MC_MODE_HOVER--- ${vcuInfo.hasFlag(VcuStatusFlags.STAT_VCU_MC_MODE_HOVER)}"
            )
            d(
                "VCU_STATUS",
                "VCU_STATUS ---- STAT_VCU_SWIF_INTERNAL_ERROR--- ${vcuInfo.hasFlag(VcuStatusFlags.STAT_VCU_SWIF_INTERNAL_ERROR)}"
            )
            d(
                "VCU_STATUS",
                "VCU_STATUS ---- STAT_VCU_MC_IN_BALLISTIC_DERATION--- ${
                    vcuInfo.hasFlag(VcuStatusFlags.STAT_VCU_MC_IN_BALLISTIC_DERATION)
                }"
            )


            d(
                "VCU_STATUS",
                "VCU_MISC ---- STAT_VCU_MISC_MTC_FCN_ACTIVE--- ${vcuInfo.hasFlag(VcuMiscFlags.STAT_VCU_MISC_MTC_FCN_ACTIVE)}"
            )
            d(
                "VCU_STATUS",
                "VCU_MISC ---- STAT_VCU_MISC_MTC_ERROR--- ${vcuInfo.hasFlag(VcuMiscFlags.STAT_VCU_MISC_MTC_ERROR)}"
            )
            d(
                "VCU_STATUS",
                "VCU_MISC ---- STAT_VCU_MISC_MTC_CTRL_REFUSED--- ${vcuInfo.hasFlag(VcuMiscFlags.STAT_VCU_MISC_MTC_CTRL_REFUSED)}"
            )
            d(
                "VCU_STATUS",
                "VCU_MISC ---- STAT_VCU_MISC_MTC_MODE_SPORT--- ${vcuInfo.hasFlag(VcuMiscFlags.STAT_VCU_MISC_MTC_MODE_SPORT)}"
            )
            d(
                "VCU_STATUS",
                "VCU_MISC ---- STAT_VCU_MISC_MTC_EN--- ${vcuInfo.hasFlag(VcuMiscFlags.STAT_VCU_MISC_MTC_EN)}"
            )
            d(
                "VCU_STATUS",
                "VCU_MISC ---- STAT_VCU_MISC_MTC_MODE_ROAD--- ${vcuInfo.hasFlag(VcuMiscFlags.STAT_VCU_MISC_MTC_MODE_ROAD)}"
            )
            d(
                "VCU_STATUS",
                "VCU_MISC ---- STAT_VCU_MISC_MTC_MODE_RAIN--- ${vcuInfo.hasFlag(VcuMiscFlags.STAT_VCU_MISC_MTC_MODE_RAIN)}"
            )
            d(
                "VCU_STATUS",
                "VCU_MISC ---- STAT_VCU_MISC_SLEEP_MODE--- ${vcuInfo.hasFlag(VcuMiscFlags.STAT_VCU_MISC_SLEEP_MODE)}"
            )


        }
        buffer.position(buffer.position() + 1) // One byte for padding

        safeParse("imxDbg") { parseImxDbgMsg(buffer) }?.let { imxDbgMsg ->
            _imxDbgMsg.value = imxDbgMsg
            d("VHALData", "***** imxDbg Value: $imxDbgMsg *****")
        }

        safeParse("Trips") { parseTrip(buffer) }?.let { trips ->
            _tripMeter.value = trips
            d("VHALData", "***** Trips Value: $trips *****")
        }

        safeParse("IMU") { parseImuData(buffer) }?.let { imuData ->
            _imuData.value = imuData
            d("VHALData", "***** IMU Value: $imuData *****")
        }

        safeParse("IMX Fw Version") { parseImxFwVersionMsg(buffer) }?.let { imxFwVersionMsg ->
            _imxFwVersionMsg.value = imxFwVersionMsg
            val bmsFw = imxFwVersionMsg.bmsFw.toReadableString()
            d("VHALData", "bmsFw: $bmsFw")
            val vcuFw = imxFwVersionMsg.vcuFw.toReadableString()
            d("VHALData", "vcuFw: $vcuFw")
            d("VHALData", "***** FW Value: $imxFwVersionMsg *****")
        }

        buffer.position(buffer.position() + 2) // Two byte for padding

        d("VHALData", "miscWords: ${buffer.position()}")
        safeParse("MiscWords") {
            IntArray(4) { buffer.int }
        }?.let { miscArray ->
            // Wrap the raw array in our new Data Model
            val miscInfo = VcuMiscInfo(miscArray)
            _vcuMiscInfo.value = miscInfo

            d("VHALData", " ***** MiscWords Value: ${miscArray.joinToString()} *****")

            // Example Debugging using the new Flags
            if (miscInfo.hasFlag(VcuMiscFlags.STAT_VCU_HORN_PRESSED)) {
                d("VHALData", "BEEP! Horn is currently pressed")
            }
            val isLeftWarn = miscInfo.hasFlag(VcuMiscFlags.STAT_VCU_RDR_BSM_LHS_WARN)
            val isLeftAlert = miscInfo.hasFlag(VcuMiscFlags.STAT_VCU_RDR_BSM_LHS_ALRT)

            _leftRadarState.value = when {
                isLeftAlert -> RadarState.Alert
                isLeftWarn -> RadarState.Warn
                else -> RadarState.Off
            }

            val isRightWarn = miscInfo.hasFlag(VcuMiscFlags.STAT_VCU_RDR_BSM_RHS_WARN)
            val isRightAlert = miscInfo.hasFlag(VcuMiscFlags.STAT_VCU_RDR_BSM_RHS_ALRT)
            _rightRadarState.value = when {
                isRightWarn -> RadarState.Warn
                isRightAlert -> RadarState.Alert
                else -> RadarState.Off
            }
          
            _rcwRadarState.value = miscInfo.hasFlag(VcuMiscFlags.STAT_VCU_RDR_RCW_ALRT)
            
            _ballisticPlus.value = miscInfo.hasFlag(VcuMiscFlags.STAT_VCU_MC_SURGE_MODE)
            val radarSensorBlocked = miscInfo.hasFlag(VcuMiscFlags.STAT_VCU_RDR_SENSOR_BLOCKED)
            val radarSensorDisabled = miscInfo.hasFlag(VcuMiscFlags.STAT_VCU_RDR_SENSOR_DISABLED)
            val radarSensorLimited = miscInfo.hasFlag(VcuMiscFlags.STAT_VCU_RDR_SENSOR_LIMITED)
            val radarSensorActive = miscInfo.hasFlag(VcuMiscFlags.STAT_VCU_RDR_SENSOR_ACTIVE)
            val radarSensorInspection =
                miscInfo.hasFlag(VcuMiscFlags.STAT_VCU_RDR_SENSOR_INSPECTION)
            val radarSensorBSMRightAlert = miscInfo.hasFlag(VcuMiscFlags.STAT_VCU_RDR_BSM_RHS_ALRT)
            val radarSensorBSMRightWarn = miscInfo.hasFlag(VcuMiscFlags.STAT_VCU_RDR_BSM_RHS_WARN)
            val radarSensorBSMLeftWarn = miscInfo.hasFlag(VcuMiscFlags.STAT_VCU_RDR_BSM_LHS_WARN)
            val radarSensorBSMLeftAlert = miscInfo.hasFlag(VcuMiscFlags.STAT_VCU_RDR_BSM_LHS_ALRT)
            val radarSensorRCWAlert = miscInfo.hasFlag(VcuMiscFlags.STAT_VCU_RDR_RCW_ALRT)
            val _ballisticPlusEnable = miscInfo.hasFlag(VcuMiscFlags.STAT_VCU_MC_SURGE_MODE)

            _ccOff.value = miscInfo.hasFlag(VcuMiscFlags.STAT_VCU_MC_CC_OFF)
            _ccActive.value = miscInfo.hasFlag(VcuMiscFlags.STAT_VCU_MC_CC_ACTIVE)
            _ccSTBY.value = miscInfo.hasFlag(VcuMiscFlags.STAT_VCU_MC_CC_STBY)
            _ccError.value = miscInfo.hasFlag(VcuMiscFlags.STAT_VCU_MC_CC_ERROR)
            val radarState = miscInfo.hasFlag(VcuMiscFlags.STAT_VCU_RDR_BSM_TURNED_OFF)
            val radarMalFunction =
                miscInfo.hasFlag(VcuMiscFlags.STAT_VCU_RDR_SENSOR_LIMITED) || miscInfo.hasFlag(
                    VcuMiscFlags.STAT_VCU_RDR_SENSOR_INSPECTION
                )
                        || miscInfo.hasFlag(VcuMiscFlags.STAT_VCU_RDR_SENSOR_BLOCKED) || miscInfo.hasFlag(
                    VcuMiscFlags.STAT_VCU_RDR_SENSOR_INSPECTION
                ) || miscInfo.hasFlag(VcuMiscFlags.STAT_VCU_RDR_BSM_ERROR)
                        || miscInfo.hasFlag(VcuMiscFlags.STAT_VCU_RDR_BSM_DISABLED) || miscInfo.hasFlag(
                    VcuMiscFlags.STAT_VCU_RDR_FRONT_CRC_ERR
                ) || miscInfo.hasFlag(VcuMiscFlags.STAT_VCU_RDR_REAR_CRC_ERR)

            _radarTelltaleState.value = when {
                radarMalFunction && radarState -> RadarTelltaleState.Malfunction
                radarState -> RadarTelltaleState.On
                else -> RadarTelltaleState.Off
            }
            d("Cruise_CVM","ccOff: $ccOff")
            d("Cruise_CVM","ccActive: $ccActive")
            d("Cruise_CVM","ccSTBY: $ccSTBY")
            d("Cruise_CVM","ccError: $ccError")
            d("RADAR_DATA", "RadarSensorBlocked:$radarSensorBlocked")
            d("RADAR_DATA", "RadarSensorDisabled:$radarSensorDisabled")
            d("RADAR_DATA", "RadarSensorLimited:$radarSensorLimited")
            d("RADAR_DATA", "RadarSensorActive:$radarSensorActive")
            d("RADAR_DATA", "RadarSensorInspection:$radarSensorInspection")
            d("RADAR_DATA", "RadarSensorRightAlert:$radarSensorBSMRightAlert")
            d("RADAR_DATA", "RadarSensorRightWarn:$radarSensorBSMRightWarn")
            d("RADAR_DATA", "RadarSensorLeftAlert:$radarSensorBSMLeftAlert")
            d("RADAR_DATA", "RadarSensorLeftWarn:$radarSensorBSMLeftWarn")
            d("RADAR_DATA", "RadarLeft:$radarSensorInspection")
            d("RADAR_DATA", "RadaSensorRCW:$radarSensorRCWAlert")
            d("BALLISTIC_PLUS", "surgeModeEnable:$_ballisticPlusEnable")


        }

        safeParse("IMX AUX") { parseImxAuxMsg(buffer) }?.let { imxAuxMsg ->
            _imxAuxMsg.value = imxAuxMsg
            d("VHALData", " ***** IMX AUX Value: $imxAuxMsg *****")
        }

        safeParse("ChargeCTX") { parseChargeCtx(buffer) }?.let { chargeCtx ->
            _chargeCtx.value = chargeCtx
            d("VHALData", "***** IMX AUX Value: $chargeCtx *****")
            d("Muthuuuuu", "***** charging time: ${chargeCtx.chargerRemainingTime} *****")
        }

        safeParse("ChargerCtxObc") { parseChargerCtxObc(buffer) }?.let { chargerObc ->
            _chargerCtxObc.value = chargerObc
            d("VHALData", "***** ChargerCtxObc Value: $chargerObc *****")
        }

        try {
            d("VHALData", "parseTellTales: ${buffer.position()}")
            parseTellTales(buffer.long).let { tellTales ->
                _tellTales.value = tellTales
                d("VCU_STATUS", "tellTales:$tellTales")
            }
        } catch (e: Exception) {
            d("VHALData", "tellTale exception: ${e.message}")
        }

        d("VHALData", "mcu_fault_data: ${buffer.position()}")
        safeParse("McuFaultData") {
            IntArray(2) { buffer.int }
        }?.let { mcuFaultArray ->
            // Wrap the raw array in our new Data Model
            val mcuFault = McuFaultData(mcuFaultArray)
            _mcuFaultData.value = mcuFault

            d("VHALData", " ***** McuFaultsData Value: ${mcuFaultArray.joinToString()} *****")


            val overspeed = mcuFault.hasFlag(McuFaultFlag.OVERSPEED)
            d("MCU_FAULT_DATA", "overspeed:$overspeed")
        }

        d("VHALData", "mcu_pmic_fault_data: ${buffer.position()}")
        safeParse("McuPmicFaultData") {
            IntArray(2) { buffer.int }
        }?.let { mcuPmicFaultArray ->
            // Wrap the raw array in our new Data Model
            val mcuPmicFault = McuPmicFaultData(mcuPmicFaultArray)
            _mcuPmicFaultData.value = mcuPmicFault

            d("VHALData", " ***** McuFaultsData Value: ${mcuPmicFaultArray.joinToString()} *****")


            val diagStateError = mcuPmicFault.hasFlag(McuPmicFaultFlag.TURN_ON_DIAG_STATE_ERR)
            d("MCU_PMIC_FAULT_DATA", "diagStateError:$diagStateError")
        }
    }

    private fun parseTrip(data: ByteBuffer): TripMeterDisp {
        d("VHALData", "Trip: ${data.position()}")
        val tripList = List(3) {
            TripInfo(
                distance = data.float,
                wattHour = data.float,
                tripDuration = data.float,
                averageSpeed = data.float
            )
        }
        return TripMeterDisp(tripList)
    }

    private fun parseImuData(data: ByteBuffer): ImuData {
        d("VHALData", "IMU: ${data.position()}")
        val millis = data.int.toUInt()
        val accelX = data.float
        val accelY = data.float
        val accelZ = data.float
        val gyroX = data.float
        val gyroY = data.float
        val gyroZ = data.float
        val magX = data.float
        val magY = data.float
        val magZ = data.float
        val quatW = data.float
        val quatX = data.float
        val quatY = data.float
        val quatZ = data.float
        val orientationX = data.float
        val orientationY = data.float
        val orientationZ = data.float
        return ImuData(
            millis,
            accelX,
            accelY,
            accelZ,
            gyroX,
            gyroY,
            gyroZ,
            magX,
            magY,
            magZ,
            quatW,
            quatX,
            quatY,
            quatZ,
            orientationX,
            orientationY,
            orientationZ
        )
    }

    private fun parseVcuInfoMessage(data: ByteBuffer): VcuInfoMsg {
        d("VHALData", "VcuInfoMessage: ${data.position()}")
        val apiVersion = ByteArray(4).apply { data.get(this) }
        val msgSequence = data.int.toUInt()
        val millis = data.int.toUInt()
        val statusH = data.int.toUInt()
        val statusL = data.int.toUInt()
        val vcuStatusH = data.int.toUInt()
        val vcuStatusL = data.int.toUInt()
        val roll = data.float
        val pitch = data.float
        val odometer = data.float
        val bmsId = parseDevUid(data)
        val throttlePercent = data.get().toUByte()
        val mcAutotuneVcuStatus = data.get().toUByte()
        val mcAutotuneMcuStatus = data.get().toUByte()
        val swifCode = data.get().toUByte()
        val speed = ByteArray(4).apply { data.get(this) }
        val actualSpeed = ByteArray(4).apply { data.get(this) }
        val distance = ByteArray(4).apply { data.get(this) }
        val vehicleMetaData = parseVehicleMetaData(data)
        val miscInfo = ByteArray(4).apply { data.get(this) }
        val whPerKm = data.float
        val whPerKmRegen = data.float
        val availableModes = data.int.toUInt()
        val currentRideMode = data.int.toUInt()
        val vehicleRangeType = data.int.toUInt()
        val range = data.short.toUShort()
        val rtc = ByteArray(8).apply { data.get(this) }
        val bus = data.get().toUByte()
        return VcuInfoMsg(
            apiVersion, msgSequence, millis, statusH, statusL,
            vcuStatusH, vcuStatusL, roll, pitch, odometer, bmsId,
            throttlePercent, mcAutotuneVcuStatus, mcAutotuneMcuStatus, swifCode, // Updated
            speed, actualSpeed, distance, vehicleMetaData, miscInfo,
            whPerKm, whPerKmRegen, availableModes, currentRideMode,
            vehicleRangeType, range, rtc, bus
        )
    }

    private fun parseDevUid(data: ByteBuffer): DevUid {
        return DevUid(
            uidl = data.int.toUInt(),
            uidml = data.int.toUInt(),
            uidmh = data.int.toUInt(),
            uidh = data.int.toUInt()
        )
    }

    private fun parseVehicleMetaData(data: ByteBuffer): VehicleMetaData {
        return VehicleMetaData(
            model = data.get().toUByte(),
            vcuHw = data.get().toUByte(),
            batteryPackVariant = data.get().toUByte(),
            mcuVariant = data.get().toUByte(),
            motorType = data.get().toUByte(),
            region = data.get().toUByte(),
            reserved1 = data.get().toUByte(),
            reserved2 = data.get().toUByte(),
            reserved3 = data.get().toUByte(),
            reserved4 = data.get().toUByte(),
            reserved5 = data.get().toUByte(),
            reserved6 = data.get().toUByte()
        )
    }

    private fun parseImxFwVersionMsg(data: ByteBuffer): ImxFwVersionMsg {
        d("VHALData", "ImxFwVersionMsg: ${data.position()}")
        val bmsFw = ByteArray(64).apply { data.get(this) }
        val vcuFw = ByteArray(64).apply { data.get(this) }
        val mcSwVersion = ByteArray(10).apply { data.get(this) }
        val mcHwVersion = ByteArray(11).apply { data.get(this) }

        val padding = ByteArray(3).apply { data.get(this) }

        val mcProductCode = data.int.toUInt()
        val mcDcfChecksum = data.short.toUShort()

        val displayFw = ByteArray(2).apply { data.get(this) }
        val chgFw = ByteArray(4).apply { data.get(this) }

        val chargerType = data.get().toUByte()
        val extChgFwVersion = data.get().toUByte()

        return ImxFwVersionMsg(
            bmsFw,
            vcuFw,
            mcSwVersion,
            mcHwVersion,
            padding,
            mcProductCode,
            mcDcfChecksum,
            displayFw,
            chgFw,
            chargerType,
            extChgFwVersion
        )
    }

    private fun parseImxAuxMsg(data: ByteBuffer): ImxAuxMsg {
        d("VHALData", "ImxAuxMsg: ${data.position()}")
        return ImxAuxMsg(
            chargeLimit = data.int.toUInt(),
            lightFx = data.int.toUInt(),
            sentryCtrl = data.int.toUInt()
        )
    }

    private fun parseChargeCtx(data: ByteBuffer): ChargeCtx {
        d("VHALData", "ChargeCtx: ${data.position()}")
        val chargerBoundary = data.int.toUInt()
        val connectionState = data.int.toUInt()
        val chargerStatus = data.int.toUInt()
        val chargerType = data.int.toUInt()
        val chargerFwMajorNum = data.int.toUInt()
        val chargerFwMinorNum = data.int.toUInt()
        val chargerRemainingTime = data.int.toUInt()
        val chargerRangeValue = data.int.toUInt()
        val acInputVoltage = data.float
        val chargeTemp = data.float
        val acInputCurrent = data.float
        val pfcFetTEmp = data.float
        val vofbVolts = data.float
        val iofbVolts = data.float
        val chargeViRequest = data.int.toUInt()
        return ChargeCtx(
            chargerBoundary = chargerBoundary,
            connectionState = connectionState,
            chargerStatus = chargerStatus,
            chargerType = chargerType,
            chargerFwMajorNum = chargerFwMajorNum,
            chargerFwMinorNum = chargerFwMinorNum,
            chargerRemainingTime = chargerRemainingTime,
            chargerRangeValue = chargerRangeValue,
            acInputVoltage = acInputVoltage,
            chargeTemp = chargeTemp,
            acInputCurrent = acInputCurrent,
            pfcFetTEmp = pfcFetTEmp,
            vofbVolts = vofbVolts,
            iofbVolts = iofbVolts,
            chargeViRequest = chargeViRequest
        )
    }

    private fun parseChargerCtxObc(data: ByteBuffer): ChargerCtxObc {
        d("VHALData", "ChargeCtxOBC: ${data.position()}")
        val chargerBoundary = data.int.toUInt()
        data.int  // 4-byte padding
        val obcStatus = data.long.toULong()
        val temperature01 = data.float
        val temperature02 = data.float
        val temperature03 = data.float
        val temperature04 = data.float
        val ipAcRmsVoltage = data.float
        val ipAcRmsCurrent = data.float
        val fanFrequency = data.float
        val opFbVoltage = data.float
        val opFbCurrent = data.float
        val ipAcSignalFreq = data.float
        val dcFbVoltage = data.float
        val llcFreq = data.float
        val opRippleCurrent = data.float
        val acVoltageThd = data.float
        val chargerConnectionState = data.int.toUInt()
        val chargerType = data.int.toUInt()
        val chargerFwMajorNum: UInt = data.int.toUInt()
        val chargerFwMinorNum: UInt = data.int.toUInt()
        val chargerViReq = data.int.toUInt()
        val chargerRangeValue = data.int.toUInt()
        return ChargerCtxObc(
            chargerLogBoundary = chargerBoundary,
            obcStatus = obcStatus,
            temperature01 = temperature01,
            temperature02 = temperature02,
            temperature03 = temperature03,
            temperature04 = temperature04,
            ipAcRmsVoltage = ipAcRmsVoltage,
            ipAcRmsCurrent = ipAcRmsCurrent,
            fanFrequency = fanFrequency,
            opFbVoltage = opFbVoltage,
            opFbCurrent = opFbCurrent,
            ipAcSignalFreq = ipAcSignalFreq,
            dcFbVoltage = dcFbVoltage,
            llcFreq = llcFreq,
            opRippleCurrent = opRippleCurrent,
            acVoltageThd = acVoltageThd,
            chargerConnectionState = chargerConnectionState,
            chargerType = chargerType,
            chargerFwMajorNum = chargerFwMajorNum,
            chargerFwMinorNum = chargerFwMinorNum,
            chargerViReq = chargerViReq,
            chargerRangeValue = chargerRangeValue
        )
    }

    private fun parseImxDbgMsg(data: ByteBuffer): ImxDbgMsg {
        d("VHALData", "ImxDbgMsg: ${data.position()}")
        val soc = data.int.toUInt()
        val packVoltage = data.float
        val packCurrent = data.float
        val maxCellTemperature = data.float
        val maxCellVoltage = data.float
        val minCellVoltage = data.float
        val motorTemperature = data.float
        val motorHeatSinkTemperature = data.float
        val fetTemp = data.float
        val shaftRpm = data.int
        val availableModes = data.int.toUInt()
        val dischargeAh = data.float
        val chargeAh = data.float
        val dischargeEnergy = data.float
        val chargeEnergy = data.float
        val chargeTtf = data.int.toUInt()
        return ImxDbgMsg(
            soc = soc,
            packVoltage = packVoltage,
            packCurrent = packCurrent,
            maxCellTemperature = maxCellTemperature,
            maxCellVoltage = maxCellVoltage,
            minCellVoltage = minCellVoltage,
            motorTemperature = motorTemperature,
            motorHeatSinkTemperature = motorHeatSinkTemperature,
            fetTemp = fetTemp,
            shaftRpm = shaftRpm,
            availableModes = availableModes,
            dischargeAh = dischargeAh,
            chargeAh = chargeAh,
            dischargeEnergy = dischargeEnergy,
            chargeEnergy = chargeEnergy,
            chargeTtf = chargeTtf
        )
    }

    fun parseTellTales(data: Long): TellTales {

        val tellTales = TellTales(
            hillHold = extract(data, 0, 3),
            motorTempIcon = extract(data, 3, 3),
            absWarningLamp = extract(data, 6, 2),
            mtcMode = extract(data, 8, 3),
            mtcState = extract(data, 11, 3),
            charger = extract(data, 14, 2),
            rideMode = extract(data, 16, 2),
            modeHover = extract(data, 18, 1),
            milState = extract(data, 19, 2),
            absMode = extract(data, 21, 2),
            batteryError = extract(data, 23, 1),
            batteryOverTemp = extract(data, 24, 1),
            highBeam = extract(data, 25, 1),
            indicatorLeft = extract(data, 26, 1),
            indicatorRight = extract(data, 27, 1),
            milIcon = extract(data, 28, 1),
            motorArmed = extract(data, 29, 1),
            otaPending = extract(data, 30, 1),
            regenUnavailable = extract(data, 31, 1),
            vehicleSpeed = extract(data, 32, 9),
            batterySoc = extract(data, 41, 7),
            hazardLamps = extract(data, 48, 1),
            regenLevel = extract(data, 49, 4),
            criticalMalfunction = extract(data, 53, 1),
            radarIndicator = extract(data, 54, 2),
            motorNoArmCause = extract(data,56,4),
            availableRideModes=extract(data,60,3),
            thermalRunway=extract(data,63,1),
            reserved = extract(data, 64, 1)
        )
        return tellTales
    }

    fun extract(value: Long, start: Int, length: Int): Int {
        val mask = (1L shl length) - 1L
        return ((value ushr start) and mask).toInt()
    }

    fun ByteArray.toReadableString(): String {
        return this.toString(Charsets.UTF_8)
            .trimEnd('\u0000')
            .trim()
    }

    fun toIntArray(rawValue: Any?): IntArray {
        return when (rawValue) {
            is IntArray -> rawValue
            is Array<*> -> rawValue.filterIsInstance<Int>().toIntArray()
            is List<*> -> rawValue.filterIsInstance<Int>().toIntArray()
            else -> intArrayOf()
        }
    }

    fun toFloatArray(rawValue: Any?): FloatArray {
        return when (rawValue) {
            is FloatArray -> rawValue
            is Array<*> -> rawValue.filterIsInstance<Float>().toFloatArray()
            is List<*> -> rawValue.filterIsInstance<Float>().toFloatArray()
            else -> floatArrayOf()
        }
    }

     fun toInt(rawValue: Any?): Int {
        return when (rawValue) {
            is Byte -> rawValue.toInt() and 0xFF
            is Int -> rawValue
            is IntArray -> rawValue.getOrNull(0) ?: 0
            is Array<*> -> rawValue.filterIsInstance<Int>().firstOrNull() ?: 0
            else -> 0
        }
    }


     fun onBootCompleted() {
         d("onBootCompleted","CarViewMode :: DefaultNightMode Entry")
        _bootCompleted = true
    }

    fun startHeartbeat() {
        d("Heartbeat", "AkhilNewLog Heartbeat Entry")

        if (heartbeatJob?.isActive == true) {
            d("Heartbeat", "already running, skipping restart")
            return
        }

        heartbeatCounter = 0L
        isHeartbeatEnabled = true

        heartbeatJob = viewModelScope.launch(Dispatchers.IO) {
            d("Heartbeat", " AkhilNewLog  started")
            while (isActive) {
                if (isHeartbeatEnabled) {
                    val payload = buildHeartbeatPayload()
                    sendByteArrayProperty(MSG_ID_USB_HEARTBEAT_IMX_S32, payload)
                    d("Heartbeat", "Heartbeat sent | counter=${heartbeatCounter++} epoch=${System.currentTimeMillis() / 1000}")
                } else {
                    d("Heartbeat", "Heartbeat suppressed | enabled=$isHeartbeatEnabled")
                }
                delay(1000L)
            }
            d("Heartbeat", "stopped")
        }
    }

    fun stopHeartbeat() {
        isHeartbeatEnabled = false
        heartbeatJob?.cancel()
        heartbeatJob = null
    }

    fun buildHeartbeatPayload(): ByteArray {
        val counter = (heartbeatCounter and 0xFFFFFFFFL).toInt()
        heartbeatCounter++

        val unixEpochSeconds = System.currentTimeMillis() / 1000L

        val buffer = java.nio.ByteBuffer.allocate(28)
        buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(counter)           // uint32_t counter  (4 bytes)
        buffer.putLong(unixEpochSeconds) // uint64_t unix_epoch (8 bytes)
        buffer.putLong(0L)               // uint64_t reserved0  (8 bytes)
        buffer.putLong(0L)               // uint64_t reserved1  (8 bytes)
        return buffer.array()
    }


}




