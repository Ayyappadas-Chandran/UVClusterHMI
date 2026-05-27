package com.ultraviolette.carproperty

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.car.Car
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.RemoteCallbackList
import android.util.Log
import com.ultraviolette.cluster.aidl.ICarPropertyCallback
import com.ultraviolette.cluster.aidl.ICarPropertyService
import com.ultraviolette.cluster.aidl.VehicleSnapshot
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Single source of vehicle state for the cluster.
 *
 * Two input paths feed one [VehicleSnapshot]:
 *
 *  • **Individual properties** (event-based, ~50ms latency on change):
 *    SPEED, INDICATOR, HIGH_BEAM, HAZARD, MOTOR_ARM, ABS, HILL_HOLD,
 *    RIDE_MODE, MTC_MODE, etc. Each fires when the underlying signal
 *    changes (with some periodic keepalive). These drive the live UI —
 *    a turn signal lights up within ~50ms of the rider flicking it.
 *
 *  • **PROP_ID_CUSTOM blob** (periodic, ~600–700ms cadence):
 *    Full VCU/IMX state dump. Covers everything in the individual
 *    properties PLUS signals with no individual representation
 *    (radar bits, side stand, thermal runaway, charging context,
 *    range/odometer/whPerKm, etc.).
 *
 * Precedence rule: both paths write to [lastVehicleSnapshot] via
 * `.copy()`. The later write wins. Because events are ~12× faster
 * than the blob, events dominate during normal operation; the blob
 * acts as a steady-state floor and provides cold-start values
 * before any event has fired.
 *
 * ── AIDL changes required to compile this file ────────────────────────────
 *
 * **ICarPropertyService.aidl**
 * ```
 *   interface ICarPropertyService {
 *       void registerCallback(in ICarPropertyCallback cb);
 *       void unregisterCallback(in ICarPropertyCallback cb);
 *       VehicleSnapshot getLastKnownSnapshot();
 *       void sendBoolean(int propId, boolean value);
 *       void sendByteArray(int propId, in byte[] value);
 *   }
 * ```
 *
 * **ICarPropertyCallback.aidl** (all `oneway`)
 * ```
 *   oneway interface ICarPropertyCallback {
 *       void onVehicleSnapshot(in VehicleSnapshot snapshot);
 *       void onHandlebarButton(int button);
 *       void onChargerEvent(int code);
 *       void onMotorNoArmEvent(in int[] reason);
 *       void onVehicleInfoRequest();
 *       void onHeartbeatControl(int value);
 *       void onFotaProgress(in int[] progress);
 *       void onSleepWake(int value);
 *   }
 * ```
 *
 * Delete: VehicleData.aidl, VehicleData.kt, and any onVehicleData(...)
 * declarations.
 *
 * **VehicleSnapshot fields to add** (Kotlin + .aidl Parcelable, defaults 0/0f):
 * ```
 *   val speedKph:                Float = 0f      // PROP_ID_VEHICLE_VALUE[0]
 *   val brightnessLevel:         Int   = 0       // PROP_ID_DISPLAY_BRIGHTNESS[0]
 *   val brightnessAuto:          Boolean = false // PROP_ID_DISPLAY_BRIGHTNESS[1]
 *   val absModeStatus:           Int   = 0       // PROP_ID_ABS_MODE_STATUS
 *   val hillHoldIcon:            Int   = 0       // PROP_ID_HILL_HOLD_ICON
 *   val indicator:               Int   = 0       // PROP_ID_INDICATOR raw (0/1/2)
 *   val lockdown:                Int   = 0       // PROP_ID_LOCKDOWN
 * ```
 * (Other fields like `vehicleSpeed`, `absMode`, `mtcMode`, `hillHold`,
 *  `rideMode`, `motorArmed`, `hazardLamps`, `highBeam`, `indicatorLeft`,
 *  `indicatorRight` already exist on VehicleSnapshot from the TellTales path.)
 */
class CarPropertyService : Service() {

    private val tag = "CarProp"

    private val callbackList = RemoteCallbackList<ICarPropertyCallback>()
    private var car: Car? = null
    private var carPropertyManager: CarPropertyManager? = null
    private lateinit var handlerThread: HandlerThread
    private lateinit var propHandler: Handler
    private var isCarConnected = false

    @Volatile private var lastVehicleSnapshot = VehicleSnapshot()

    private val registeredProps = mutableSetOf<Int>()

    private val binder = object : ICarPropertyService.Stub() {
        override fun registerCallback(cb: ICarPropertyCallback?) {
            cb?.let { callbackList.register(it) }
        }
        override fun unregisterCallback(cb: ICarPropertyCallback?) {
            cb?.let { callbackList.unregister(it) }
        }
        override fun getLastKnownSnapshot(): VehicleSnapshot = lastVehicleSnapshot
        override fun sendBoolean(propId: Int, value: Boolean) {
            try {
                carPropertyManager?.setProperty(Boolean::class.java, propId, 0, value)
            } catch (e: Exception) {
                Log.e(tag, "sendBoolean $propId failed", e)
            }
        }
        override fun sendByteArray(propId: Int, value: ByteArray?) {
            if (value == null) return
            try {
                carPropertyManager?.setProperty(ByteArray::class.java, propId, 0, value)
            } catch (e: Exception) {
                Log.e(tag, "sendByteArray $propId failed", e)
            }
        }
    }

    private val propertyCallback = object : CarPropertyManager.CarPropertyEventCallback {
        override fun onChangeEvent(value: CarPropertyValue<Any>?) {
            value ?: return
            propHandler.post { handlePropertyChange(value) }
        }
        override fun onErrorEvent(propId: Int, status: Int) {
            Log.e(tag, "Property error propId=$propId status=$status")
        }
    }

    private val carServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            isCarConnected = true
            carPropertyManager = car?.getCarManager(Car.PROPERTY_SERVICE) as? CarPropertyManager
            if (carPropertyManager == null) {
                Log.e(tag, "CarPropertyManager null after connect")
                return
            }
            registerAllProperties()
            Log.d(tag, "Car service connected, all properties registered")
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            carPropertyManager = null
            isCarConnected = false
            Log.w(tag, "Car service disconnected")
        }
    }

    override fun onCreate() {
        super.onCreate()
        handlerThread = HandlerThread("CarPropHandler").also { it.start() }
        propHandler = Handler(handlerThread.looper)
        startForeground(NOTIFICATION_ID, buildNotification())
        connectCar()
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        super.onDestroy()
        carPropertyManager?.unregisterCallback(propertyCallback)
        car?.disconnect()
        handlerThread.quitSafely()
        callbackList.kill()
    }

    private fun connectCar() {
        if (isCarConnected) return
        car = Car.createCar(this, carServiceConnection)
        car?.connect()
    }

    private fun registerAllProperties() {
        listOf(
            // State (writes to VehicleSnapshot)
            PROP_ID_VEHICLE_VALUE, PROP_ID_INDICATOR, PROP_ID_HIGH_BEAM_TELLTALE,
            PROP_ID_HAZARD_LIGHT_TELLTALE, PROP_ID_MOTOR_ARM_DISARM_TELLTALE,
            PROP_ID_ABS_MODE, PROP_ID_ABS_MODE_STATUS,
            PROP_ID_HILL_HOLD_STATE, PROP_ID_HILL_HOLD_ICON,
            PROP_ID_RIDE_MODES, PROP_ID_MTC_MODE,
            PROP_ID_DISPLAY_BRIGHTNESS, PROP_ID_LOCKDOWN,
            PROP_ID_CUSTOM,
            // Events (broadcast through ICarPropertyCallback)
            PROP_ID_SWIFT_BUTTON, PROP_ID_CHARGER_EVT, PROP_ID_MC_NO_ARM,
            PROP_ID_VEHICLE_INFO_REQ, PROP_ID_HEARTBEAT_ENABLE_DISABLE,
            PROP_ID_FOTA_UPDATE, PROP_ID_SLEEP_WAKE,
            // Not yet handled — register so the VHAL doesn't complain;
            // wire into snapshot or events when a consumer needs them.
            PROP_ID_RTC_TIME, PROP_ID_SCREEN_MODES, PROP_ID_MC_THERMAL,
            PROP_ID_REGEN, PROP_ID_CRUISE_CONTROL,
        ).forEach { propId ->
            if (registeredProps.contains(propId)) return@forEach
            try {
                carPropertyManager?.registerCallback(
                    propertyCallback, propId, CarPropertyManager.SENSOR_RATE_FAST,
                )
                registeredProps.add(propId)
            } catch (e: Exception) {
                Log.e(tag, "Failed to register propId=0x${propId.toString(16)}", e)
            }
        }
    }

    // ── Dispatch ──────────────────────────────────────────────────────────────

    private fun handlePropertyChange(value: CarPropertyValue<Any>) {
        when (value.propertyId) {

            // ── Snapshot writes ──────────────────────────────────────────────
            // Event-based, ~50ms latency. These overwrite the corresponding
            // blob-derived fields (the later-arriving write wins).

            PROP_ID_VEHICLE_VALUE -> {
                val arr = toFloatArray(value.value)
                if (arr.size < 4) {
                    Log.w(tag, "PROP_ID_VEHICLE_VALUE: bad size ${arr.size}, skipping")
                    return
                }
                lastVehicleSnapshot = lastVehicleSnapshot.copy(
                    speedKph   = arr[0],
                    // arr[1] = motor power in raw watts (negative = regeneration)
                    motorPower = if (arr.size > 1) arr[1] else 0f,
                )
                broadcastSnapshot()
            }

            PROP_ID_INDICATOR -> {
                // Raw byte: 0=off, 1=right, 2=left.
                // Also derive 1-bit indicatorLeft/indicatorRight so consumers
                // reading either shape stay consistent with the TellTales path.
                val raw = toInt(value.value)
                lastVehicleSnapshot = lastVehicleSnapshot.copy(
                    indicator      = raw,
                    indicatorLeft  = if (raw == 2) 1 else 0,
                    indicatorRight = if (raw == 1) 1 else 0,
                )
                broadcastSnapshot()
            }

            PROP_ID_HIGH_BEAM_TELLTALE -> {
                lastVehicleSnapshot = lastVehicleSnapshot.copy(
                    highBeam = if (toInt(value.value) == 1) 1 else 0,
                )
                broadcastSnapshot()
            }

            PROP_ID_HAZARD_LIGHT_TELLTALE -> {
                lastVehicleSnapshot = lastVehicleSnapshot.copy(
                    hazardLamps = if (toInt(value.value) == 1) 1 else 0,
                )
                broadcastSnapshot()
            }

            PROP_ID_MOTOR_ARM_DISARM_TELLTALE -> {
                lastVehicleSnapshot = lastVehicleSnapshot.copy(
                    motorArmed = if (toInt(value.value) == 1) 1 else 0,
                )
                broadcastSnapshot()
            }

            PROP_ID_ABS_MODE -> {
                lastVehicleSnapshot = lastVehicleSnapshot.copy(absMode = toInt(value.value))
                broadcastSnapshot()
            }

            PROP_ID_ABS_MODE_STATUS -> {
                lastVehicleSnapshot = lastVehicleSnapshot.copy(absModeStatus = toInt(value.value))
                broadcastSnapshot()
            }

            PROP_ID_HILL_HOLD_STATE -> {
                lastVehicleSnapshot = lastVehicleSnapshot.copy(hillHold = toInt(value.value))
                broadcastSnapshot()
            }

            PROP_ID_HILL_HOLD_ICON -> {
                lastVehicleSnapshot = lastVehicleSnapshot.copy(hillHoldIcon = toInt(value.value))
                broadcastSnapshot()
            }

            PROP_ID_RIDE_MODES -> {
                lastVehicleSnapshot = lastVehicleSnapshot.copy(rideMode = toInt(value.value))
                broadcastSnapshot()
            }

            PROP_ID_MTC_MODE -> {
                lastVehicleSnapshot = lastVehicleSnapshot.copy(mtcMode = toInt(value.value))
                broadcastSnapshot()
            }

            PROP_ID_DISPLAY_BRIGHTNESS -> {
                val arr = toIntArray(value.value)
                if (arr.size < 2) {
                    Log.w(tag, "PROP_ID_DISPLAY_BRIGHTNESS: bad size ${arr.size}, skipping")
                    return
                }
                lastVehicleSnapshot = lastVehicleSnapshot.copy(
                    brightnessLevel = arr[0],
                    brightnessAuto  = arr[1] == 1,
                )
                broadcastSnapshot()
            }

            PROP_ID_LOCKDOWN -> {
                lastVehicleSnapshot = lastVehicleSnapshot.copy(lockdown = toInt(value.value))
                broadcastSnapshot()
            }

            PROP_ID_REGEN -> {
                lastVehicleSnapshot = lastVehicleSnapshot.copy(regenLevel = toInt(value.value))
                broadcastSnapshot()
            }
            PROP_ID_CRUISE_CONTROL -> {
                lastVehicleSnapshot = lastVehicleSnapshot.copy(cruiseValue = toInt(value.value))
                broadcastSnapshot()
            }
            PROP_ID_RTC_TIME -> {
                lastVehicleSnapshot = lastVehicleSnapshot.copy(rtcTime = toIntArray(value.value))
                broadcastSnapshot()
            }
            PROP_ID_SCREEN_MODES -> {
                lastVehicleSnapshot = lastVehicleSnapshot.copy(lockdown = toInt(value.value))
                broadcastSnapshot()
            }
            PROP_ID_MC_THERMAL -> {
            }

            PROP_ID_CUSTOM -> {
                (value.value as? ByteArray)?.let { parseCustomAndUpdateSnapshot(it) }
                // parseCustomAndUpdateSnapshot broadcasts internally
            }

            // ── Event broadcasts (no state write) ────────────────────────────

            PROP_ID_SWIFT_BUTTON -> {
                broadcastHandlebarButton(toInt(value.value))
            }

            PROP_ID_CHARGER_EVT -> {
                // Codes 192=unplugged, 193..196=charging states.
                // Drives screen-mode navigation in ClusterViewModel.
                broadcastChargerEvent(toInt(value.value))
            }

            PROP_ID_MC_NO_ARM -> {
                broadcastMotorNoArmEvent(toIntArray(value.value))
            }

            PROP_ID_VEHICLE_INFO_REQ -> {
                // VCU asks cluster to send its IMEI/VIN/firmware payload.
                // No data — pure command signal.
                broadcastVehicleInfoRequest()
            }

            PROP_ID_HEARTBEAT_ENABLE_DISABLE -> {
                // 1 = enable, 2 = disable. Controls the cluster→VCU heartbeat
                // loop that lives in MainActivity (move to a service later).
                broadcastHeartbeatControl(toInt(value.value))
            }

            PROP_ID_FOTA_UPDATE -> {
                broadcastFotaProgress(toIntArray(value.value))
            }

            PROP_ID_SLEEP_WAKE -> {
                broadcastSleepWake(toInt(value.value))
            }

            // ── Not yet handled (registered above to suppress VHAL warnings) ──
            // Add a branch here when a consumer needs the value.

            else -> {
                Log.w(tag, "Unhandled propId=0x${value.propertyId.toString(16)}")
            }
        }
    }

    // ── VehicleSnapshot population from PROP_ID_CUSTOM blob ───────────────────

    /**
     * Parses the PROP_ID_CUSTOM binary blob and populates [lastVehicleSnapshot].
     *
     * Binary layout (sequential, LITTLE_ENDIAN). Cumulative offsets verified
     * against the original CarViewModel.parseCustomData():
     * ```
     *   offset  bytes  field
     *   [0]     120    VcuInfoMsg (119 bytes) + 1 padding
     *   [120]    64    ImxDbgMsg
     *   [184]    48    TripMeterDisp (skipped)
     *   [232]    68    ImuData (skipped)
     *   [300]   168    ImxFwVersionMsg + 2 padding (skipped)
     *   [468]    16    VcuMiscInfo (4 × Int)
     *   [484]    12    ImxAuxMsg (3 × UInt)
     *   [496]    60    ChargeCtx (15 × Int)
     *   [556]    96    ChargerCtxObc (skipped)
     *   [652]     8    TellTales (Long bitfield)
     *   [660]     8    McuFaultData     — not parsed yet
     *   [668]     8    McuPmicFaultData — not parsed yet
     * ```
     * Minimum blob size to reach end of TellTales: 660 bytes.
     *
     * Fields with individual-property equivalents (vehicleSpeed, absMode,
     * mtcMode, hillHold, rideMode, motorArmed, hazardLamps, indicatorLeft,
     * indicatorRight, highBeam) are also written here — they serve as
     * cold-start values and a steady-state floor. At runtime the ~50ms
     * events overwrite these within one cycle.
     *
     * Note on speed: the blob's TellTales.vehicleSpeed (9-bit, max 511) is
     * NOT written here. The authoritative speed comes from PROP_ID_VEHICLE_VALUE
     * as a Float into [VehicleSnapshot.speedKph]. The Compose UI reads
     * `speedKph` and ignores the bitfield.
     */
    private fun parseCustomAndUpdateSnapshot(data: ByteArray) {
        if (data.size < 660) {
            Log.w(tag, "parseCustom: blob too small (${data.size} < 660), skipping")
            return
        }
        try {
            val buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)

            // ── 1. VcuInfoMsg (119 bytes) + 1 byte padding → end at offset 120
            buf.position(buf.position() + 4)    // apiVersion[4]
            buf.position(buf.position() + 4)    // msgSequence
            buf.position(buf.position() + 4)    // millis
            val statusH    = buf.int            // BMS flags high  (bits 32–63)
            val statusL    = buf.int            // BMS flags low   (bits 0–31)
            val vcuStatusH = buf.int            // VCU status flags high
            val vcuStatusL = buf.int            // VCU status flags low
            val rollAngleRaw  = buf.float          // roll (lean angle, degrees)
            buf.position(buf.position() + 4)    // pitch (skip)
            val odoRaw     = buf.float
            buf.position(buf.position() + 16)   // bmsId
            buf.position(buf.position() + 4)    // throttle, mcAutotune×2, swif
            buf.position(buf.position() + 4)    // speed (skip — use speedKph from PROP_ID_VEHICLE_VALUE)
            buf.position(buf.position() + 4)    // actualSpeed (skip)
            val tripDistanceRaw = buf.float     // trip distance (km)
            buf.position(buf.position() + 12)   // vehicleMetaData
            buf.position(buf.position() + 4)    // miscInfo[4]
            val whPerKmRaw = buf.float
            buf.position(buf.position() + 4)    // whPerKmRegen
            buf.position(buf.position() + 4)    // availableModes
            buf.position(buf.position() + 4)    // currentRideMode
            buf.position(buf.position() + 4)    // vehicleRangeType
            val rangeRaw   = buf.short.toInt()  // range (unsigned short)
            buf.position(buf.position() + 8)    // rtc
            buf.position(buf.position() + 1)    // bus
            buf.position(buf.position() + 1)    // padding → pos 120

            // Bit positions are hardcoded literals.
            // WARNING: original CarViewModel used named constants from
            // VcuStatusFlags / BmsStatusFlags. If those enum values change,
            // these reads silently break. Cross-check against the enum.
            val sideStandDeployed = if (vcuStatusL.hasBit(29)) 1 else 0
            val keyOff            = if (vcuStatusL.hasBit(7))  1 else 0
            val paFwd             = if (vcuStatusH.hasBit(46 - 32)) 1 else 0
            val paRev             = if (vcuStatusH.hasBit(47 - 32)) 1 else 0
            val paEntry           = if (vcuStatusH.hasBit(48 - 32)) 1 else 0

            val thermalRunwayV = if (statusH.hasBit(53 - 32)) 1 else 0
            val thermalRunwayT = if (statusH.hasBit(54 - 32)) 1 else 0
            val thermalRunwayH = if (statusH.hasBit(55 - 32)) 1 else 0

            // ── 2. ImxDbgMsg (64 bytes) → end at offset 184
            buf.position(buf.position() + 4)    // soc (use TellTales.batterySoc)
            val packVoltage = buf.float
            val packCurrent = buf.float
            val maxCellTemp = buf.float
            buf.position(buf.position() + 4)    // maxCellVoltage
            buf.position(buf.position() + 4)    // minCellVoltage
            val motorTemp   = buf.float
            buf.position(buf.position() + 4)    // motorHeatSinkTemperature
            buf.position(buf.position() + 4)    // fetTemp
            buf.position(buf.position() + 4)    // shaftRpm
            buf.position(buf.position() + 4)    // availableModes
            buf.position(buf.position() + 4)    // dischargeAh
            buf.position(buf.position() + 4)    // chargeAh
            buf.position(buf.position() + 4)    // dischargeEnergy
            buf.position(buf.position() + 4)    // chargeEnergy
            val chargeTtf   = buf.int and 0x7FFF_FFFF

            // ── 3. TripMeterDisp (48 bytes = 3 × TripInfo, each: distance/wattHour/duration/avgSpeed)
            //        then skip ImuData (68) + ImxFwVersionMsg+pad (168) → pos 468
            val trip1Distance   = buf.float
            val trip1WattHour   = buf.float
            val trip1Duration   = buf.float   // seconds as Float
            val trip1AvgSpeed   = buf.float
            val trip2Distance   = buf.float
            val trip2WattHour   = buf.float
            val trip2Duration   = buf.float
            val trip2AvgSpeed   = buf.float
            val trip3Distance   = buf.float
            val trip3WattHour   = buf.float
            val trip3Duration   = buf.float
            val trip3AvgSpeed   = buf.float
            buf.position(buf.position() + 68 + 168)       // → pos 468

            // ── 4. VcuMiscInfo (4 ints = 16 bytes) → end at offset 484
            @Suppress("UNUSED_VARIABLE")
            val misc0 = buf.int
            val misc1 = buf.int
            val misc2 = buf.int
            @Suppress("UNUSED_VARIABLE")
            val misc3 = buf.int

            // misc1 = bits 32–63 → bit index inside word = flag % 32
            val radarLeftWarn   = if (misc1.hasBit(53 % 32)) 1 else 0
            val radarLeftAlert  = if (misc1.hasBit(54 % 32)) 1 else 0
            val radarRightWarn  = if (misc1.hasBit(55 % 32)) 1 else 0
            val radarRightAlert = if (misc1.hasBit(56 % 32)) 1 else 0
            val rcwAlert        = if (misc1.hasBit(57 % 32)) 1 else 0

            // misc2 = bits 64–95
            val isBallisticPlus = if (misc2.hasBit(66 % 32)) 1 else 0
            val cruiseOff       = if (misc2.hasBit(88 % 32)) 1 else 0
            val cruiseStandby   = if (misc2.hasBit(89 % 32)) 1 else 0
            val cruiseActive    = if (misc2.hasBit(90 % 32)) 1 else 0
            val cruiseError     = if (misc2.hasBit(91 % 32)) 1 else 0

            // ── 5. ImxAuxMsg (12 bytes) → end at offset 496
            val chargeLimit = buf.int and 0x7FFF_FFFF
            buf.position(buf.position() + 8)    // lightFx + sentryCtrl

            // ── 6. ChargeCtx (60 bytes) → end at offset 556
            buf.position(buf.position() + 4)    // chargerBoundary
            val connectionState      = buf.int
            val chargerStatus        = buf.int
            val chargerType          = buf.int
            buf.position(buf.position() + 4)    // chargerFwMajorNum
            buf.position(buf.position() + 4)    // chargerFwMinorNum
            val chargerRemainingTime = buf.int and 0x7FFF_FFFF
            buf.position(buf.position() + 32)   // remaining 8 fields

            // ── 7. Skip ChargerCtxObc (96 bytes) → end at offset 652
            buf.position(buf.position() + 96)

            // ── 8. TellTales (8-byte Long bitfield) → end at offset 660
            val tt = buf.long

            lastVehicleSnapshot = lastVehicleSnapshot.copy(
                // TellTales bitfield. Fields that also have individual-property
                // events (PROP_ID_HIGH_BEAM_TELLTALE, PROP_ID_HAZARD_LIGHT_TELLTALE,
                // PROP_ID_MOTOR_ARM_DISARM_TELLTALE, PROP_ID_INDICATOR) are
                // intentionally NOT written from the blob.
                //
                // Reason: the blob is a periodic snapshot (~600–700 ms cadence)
                // captured by the VCU at a fixed point in time. When the VCU
                // sends the individual telltale event (~50 ms latency), the
                // cluster may receive the next blob with the PRE-CHANGE value
                // still encoded in the bitfield. Writing that stale value would
                // overwrite the correct event-driven value, producing visible
                // flicker (the "motorArmed shows/hides/shows" symptom).
                //
                // These fields are owned exclusively by their individual events:
                //   highBeam       ← PROP_ID_HIGH_BEAM_TELLTALE
                //   hazardLamps    ← PROP_ID_HAZARD_LIGHT_TELLTALE
                //   motorArmed     ← PROP_ID_MOTOR_ARM_DISARM_TELLTALE
                //   indicatorLeft  ← derived from PROP_ID_INDICATOR
                //   indicatorRight ← derived from PROP_ID_INDICATOR
                //   indicator      ← PROP_ID_INDICATOR (raw)
                //
                // vehicleSpeed is omitted for a different reason:
                // the authoritative speed is speedKph from PROP_ID_VEHICLE_VALUE.
                hillHold            = tt.bits(0, 3),
                motorTempIcon       = tt.bits(3, 3),
                absWarningLamp      = tt.bits(6, 2),
                mtcMode             = tt.bits(8, 3),
                mtcState            = tt.bits(11, 3),
                charger             = tt.bits(14, 2),
                rideMode            = tt.bits(16, 2),
                modeHover           = tt.bits(18, 1),
                milState            = tt.bits(19, 2),
                absMode             = tt.bits(21, 2),
                batteryError        = tt.bits(23, 1),
                batteryOverTemp     = tt.bits(24, 1),
                // highBeam    — omitted: owned by PROP_ID_HIGH_BEAM_TELLTALE event
                // indicatorLeft  — omitted: owned by PROP_ID_INDICATOR event
                // indicatorRight — omitted: owned by PROP_ID_INDICATOR event
                milIcon             = tt.bits(28, 1),
                // motorArmed  — omitted: owned by PROP_ID_MOTOR_ARM_DISARM_TELLTALE event
                otaPending          = tt.bits(30, 1),
                regenUnavailable    = tt.bits(31, 1),
                // (vehicleSpeed bits 32–41 omitted — speedKph wins)
                batterySoc          = tt.bits(41, 7),
                // hazardLamps — omitted: owned by PROP_ID_HAZARD_LIGHT_TELLTALE event
                regenLevel          = tt.bits(49, 4),
                criticalMalfunction = tt.bits(53, 1),
                radarIndicator      = tt.bits(54, 2),
                motorNoArmCause     = tt.bits(56, 4),
                availableRideModes  = tt.bits(60, 3),
                thermalRunway       = tt.bits(63, 1),
                // VcuInfoMsg scalars
                odometer            = odoRaw,
                range               = rangeRaw,
                whPerKm             = whPerKmRaw,
                rollAngle           = rollAngleRaw,
                tripDistance        = tripDistanceRaw,
                // VcuStatus + BMS flags
                sideStandDeployed   = sideStandDeployed,
                thermalRunwayV      = thermalRunwayV,
                thermalRunwayH      = thermalRunwayH,
                thermalRunwayT      = thermalRunwayT,
                keyOff              = keyOff,
                paFwd               = paFwd,
                paRev               = paRev,
                paEntry             = paEntry,
                // VcuMiscInfo flags
                radarLeftWarn       = radarLeftWarn,
                radarLeftAlert      = radarLeftAlert,
                radarRightWarn      = radarRightWarn,
                radarRightAlert     = radarRightAlert,
                rcwAlert            = rcwAlert,
                isBallisticPlus     = isBallisticPlus,
                cruiseOff           = cruiseOff,
                cruiseStandby       = cruiseStandby,
                cruiseActive        = cruiseActive,
                cruiseError         = cruiseError,
                // ImxDbgMsg battery telemetry
                chargeTtf           = chargeTtf,
                packVoltage         = packVoltage,
                packCurrent         = packCurrent,
                maxCellTemperature  = maxCellTemp,
                motorTemperature    = motorTemp,
                // Charger context
                chargerRemainingTime = chargerRemainingTime,
                connectionState     = connectionState,
                chargerStatus       = chargerStatus,
                chargerType         = chargerType,
                chargeLimit         = chargeLimit,
                // TripMeterDisp (3 trips)
                trip1Distance       = trip1Distance,
                trip1WattHour       = trip1WattHour,
                trip1Duration       = trip1Duration,
                trip1AvgSpeed       = trip1AvgSpeed,
                trip2Distance       = trip2Distance,
                trip2WattHour       = trip2WattHour,
                trip2Duration       = trip2Duration,
                trip2AvgSpeed       = trip2AvgSpeed,
                trip3Distance       = trip3Distance,
                trip3WattHour       = trip3WattHour,
                trip3Duration       = trip3Duration,
                trip3AvgSpeed       = trip3AvgSpeed,
            )

            broadcastSnapshot()

        } catch (e: BufferUnderflowException) {
            Log.w(tag, "parseCustom: buffer underflow — blob may be truncated")
        } catch (e: Exception) {
            Log.e(tag, "parseCustom: unexpected error", e)
        }
    }

    // ── Broadcast helpers ─────────────────────────────────────────────────────

    private fun broadcastSnapshot() {
        val snapshot = lastVehicleSnapshot
        val count = callbackList.beginBroadcast()
        try {
            for (i in 0 until count) {
                try {
                    callbackList.getBroadcastItem(i).onVehicleSnapshot(snapshot)
                } catch (e: Exception) {
                    Log.e(tag, "Callback onVehicleSnapshot failed at $i", e)
                }
            }
        } finally {
            callbackList.finishBroadcast()
        }
    }

    private fun broadcastHandlebarButton(button: Int) {
        val count = callbackList.beginBroadcast()
        try {
            for (i in 0 until count) {
                try { callbackList.getBroadcastItem(i).onHandlebarButton(button) }
                catch (e: Exception) { Log.e(tag, "onHandlebarButton failed at $i", e) }
            }
        } finally { callbackList.finishBroadcast() }
    }

    private fun broadcastChargerEvent(code: Int) {
        val count = callbackList.beginBroadcast()
        try {
            for (i in 0 until count) {
                try { callbackList.getBroadcastItem(i).onChargerEvent(code) }
                catch (e: Exception) { Log.e(tag, "onChargerEvent failed at $i", e) }
            }
        } finally { callbackList.finishBroadcast() }
    }

    private fun broadcastMotorNoArmEvent(reason: IntArray) {
        val count = callbackList.beginBroadcast()
        try {
            for (i in 0 until count) {
                try { callbackList.getBroadcastItem(i).onMotorNoArmEvent(reason) }
                catch (e: Exception) { Log.e(tag, "onMotorNoArmEvent failed at $i", e) }
            }
        } finally { callbackList.finishBroadcast() }
    }

    private fun broadcastVehicleInfoRequest() {
        val count = callbackList.beginBroadcast()
        try {
            for (i in 0 until count) {
                try { callbackList.getBroadcastItem(i).onVehicleInfoRequest() }
                catch (e: Exception) { Log.e(tag, "onVehicleInfoRequest failed at $i", e) }
            }
        } finally { callbackList.finishBroadcast() }
    }

    private fun broadcastHeartbeatControl(value: Int) {
        val count = callbackList.beginBroadcast()
        try {
            for (i in 0 until count) {
                try { callbackList.getBroadcastItem(i).onHeartbeatControl(value) }
                catch (e: Exception) { Log.e(tag, "onHeartbeatControl failed at $i", e) }
            }
        } finally { callbackList.finishBroadcast() }
    }

    private fun broadcastFotaProgress(progress: IntArray) {
        val count = callbackList.beginBroadcast()
        try {
            for (i in 0 until count) {
                try { callbackList.getBroadcastItem(i).onFotaProgress(progress) }
                catch (e: Exception) { Log.e(tag, "onFotaProgress failed at $i", e) }
            }
        } finally { callbackList.finishBroadcast() }
    }

    private fun broadcastSleepWake(value: Int) {
        val count = callbackList.beginBroadcast()
        try {
            for (i in 0 until count) {
                try { callbackList.getBroadcastItem(i).onSleepWake(value) }
                catch (e: Exception) { Log.e(tag, "onSleepWake failed at $i", e) }
            }
        } finally { callbackList.finishBroadcast() }
    }

    // ── Bit / type helpers ────────────────────────────────────────────────────

    /** Extracts [length] bits at [start] (LSB-first) from this Long. */
    private fun Long.bits(start: Int, length: Int): Int {
        val mask = (1L shl length) - 1L
        return ((this ushr start) and mask).toInt()
    }

    /** True iff [bit] (0-based, LSB) is set in this Int. */
    private fun Int.hasBit(bit: Int): Boolean = ((this ushr bit) and 1) == 1

    private fun toIntArray(v: Any?): IntArray = when (v) {
        is IntArray -> v
        is Array<*> -> v.filterIsInstance<Int>().toIntArray()
        else        -> intArrayOf()
    }

    private fun toFloatArray(v: Any?): FloatArray = when (v) {
        is FloatArray -> v
        is Array<*>   -> v.filterIsInstance<Float>().toFloatArray()
        else          -> floatArrayOf()
    }

    private fun toInt(v: Any?): Int = when (v) {
        is Int      -> v
        is Byte     -> v.toInt() and 0xFF
        is IntArray -> v.getOrNull(0) ?: 0
        else        -> 0
    }

    // ── Notification ─────────────────────────────────────────────────────────

    private fun buildNotification(): Notification {
        val channelId = "car_prop_service"
        val nm = getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(channelId) == null) {
            nm.createNotificationChannel(
                NotificationChannel(channelId, "Vehicle Data", NotificationManager.IMPORTANCE_LOW),
            )
        }
        return Notification.Builder(this, channelId)
            .setContentTitle("UV Vehicle Data Service")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1001

        const val PROP_ID_VEHICLE_VALUE             = 0x21610310
        const val PROP_ID_REGEN                     = 0x21410320
        const val PROP_ID_ABS_MODE                  = 0x21400330
        const val PROP_ID_ABS_MODE_STATUS           = 0x21400411
        const val PROP_ID_HILL_HOLD_STATE           = 0x21400340
        const val PROP_ID_HILL_HOLD_ICON            = 0x21400341
        const val PROP_ID_FOTA_UPDATE               = 0x21410350
        const val PROP_ID_RTC_TIME                  = 0x21410370
        const val PROP_ID_DISPLAY_BRIGHTNESS        = 0x214103B0
        const val PROP_ID_RIDE_MODES                = 0x21400412
        const val PROP_ID_SCREEN_MODES              = 0x21400413
        const val PROP_ID_INDICATOR                 = 0x21400414
        const val PROP_ID_HIGH_BEAM_TELLTALE        = 0x21400418
        const val PROP_ID_HAZARD_LIGHT_TELLTALE     = 0x2140041A
        const val PROP_ID_MOTOR_ARM_DISARM_TELLTALE = 0x21400419
        const val PROP_ID_HEARTBEAT_ENABLE_DISABLE  = 0x2140041B
        const val PROP_ID_VEHICLE_INFO_REQ          = 0x2140041C
        const val PROP_ID_LOCKDOWN                  = 0x21400415
        const val PROP_ID_CUSTOM                    = 0x21700312
        const val PROP_ID_SWIFT_BUTTON              = 0x214003A0
        const val PROP_ID_SLEEP_WAKE                = 0x21400390
        const val PROP_ID_MTC_MODE                  = 0x21400416
        const val PROP_ID_MC_THERMAL                = 0x21610360
        const val PROP_ID_MC_NO_ARM                 = 0x21410417
        const val PROP_ID_CHARGER_EVT               = 0x214003A1
        const val PROP_ID_CRUISE_CONTROL            = 0x2140041D
    }
}