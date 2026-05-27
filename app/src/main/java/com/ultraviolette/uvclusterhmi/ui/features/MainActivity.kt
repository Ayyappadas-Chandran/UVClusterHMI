package com.ultraviolette.uvclusterhmi.ui.features

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.AlertDialog
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log.d
import android.util.Log.e
import android.util.TypedValue
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.ultraviolette.uvclusterhmi.ClusterNotification
import com.ultraviolette.uvclusterhmi.FotaReceiver
import com.ultraviolette.uvclusterhmi.NotificationManager
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData.TellTales
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import com.ultraviolette.uvclusterhmi.ui.features.controls.advancedFeatures.AdvancedFeaturesFragment
import com.ultraviolette.uvclusterhmi.ui.features.controls.performance.PerformanceFragment
import com.ultraviolette.uvclusterhmi.ui.features.controls.rideModes.RideModesFragment
import com.ultraviolette.uvclusterhmi.ui.features.controls.trips.TripsFragment
import com.ultraviolette.uvclusterhmi.ui.features.controls.violette.VioletteFragment
import com.ultraviolette.uvclusterhmi.ui.features.menu.MenuFragment
import com.ultraviolette.uvclusterhmi.ui.features.menus.battery.BatteryFragment
import com.ultraviolette.uvclusterhmi.ui.features.menus.battery.winter.WinterModeFragment
import com.ultraviolette.uvclusterhmi.ui.features.menus.control.ControlMenuFragment
import com.ultraviolette.uvclusterhmi.ui.features.menus.myF77.MyF77MenuFragment
import com.ultraviolette.uvclusterhmi.ui.features.menus.setting.SettingsFragment
import com.ultraviolette.uvclusterhmi.ui.features.menus.tpms.TpmsFragment
import com.ultraviolette.uvclusterhmi.ui.features.myF77.EmergencyFragment
import com.ultraviolette.uvclusterhmi.ui.features.myF77.InfoFragment
import com.ultraviolette.uvclusterhmi.ui.features.myF77.document.DocumentFragment
import com.ultraviolette.uvclusterhmi.ui.features.myF77.document.DocumentMenuFragment
import com.ultraviolette.uvclusterhmi.ui.features.myF77.tutorial.TutorialFragment
import com.ultraviolette.uvclusterhmi.ui.features.myF77.tutorial.TutorialPlayerFragment
import com.ultraviolette.uvclusterhmi.ui.features.settings.bluetooth.BluetoothViewModel
import com.ultraviolette.uvclusterhmi.ui.features.settings.data.DataViewModel
import com.ultraviolette.uvclusterhmi.ui.features.settings.wifi.WifiViewModel
import com.ultraviolette.uvclusterhmi.ui.viewModel.CarViewModel
import com.ultraviolette.uvclusterhmi.ui.viewModel.RadarTelltaleState
import com.ultraviolette.uvclusterhmi.ui.viewModel.SharedViewModel
import com.ultraviolette.uvclusterhmi.utils.ClusterAlertManager
import com.ultraviolette.uvclusterhmi.utils.Utilities
import com.ultraviolette.uvclusterhmi.utils.Utilities.ARG_CHARGING_STATUS
import com.ultraviolette.uvclusterhmi.utils.Utilities.SHOULD_INCLUDE_CAR_SERVICE
import com.ultraviolette.uvclusterhmi.utils.Utilities.applyMinMax
import com.ultraviolette.uvclusterhmi.utils.Utilities.frameworkStartTime
import com.ultraviolette.uvclusterhmi.utils.Utilities.getBrightnessLevelFromLux
import com.ultraviolette.uvclusterhmi.utils.Utilities.permissionsForSDKAboveR
import com.ultraviolette.uvclusterhmi.utils.Utilities.permissionsForSDKR
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
import android.os.UserHandle
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.viewModels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import com.ultraviolette.uvclusterhmi.ui.features.settings.wifi.WifiAutoConnector
import java.time.ZoneId
import com.ultraviolette.uvclusterhmi.MyViewModelProvider
import com.ultraviolette.uvclusterhmi.ui.features.controls.performance.PerformanceViewModel
import kotlinx.coroutines.flow.drop
// ── Compose host bridge ───────────────────────────────────────────────────────
import android.view.KeyEvent
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ultraviolette.uvclusterhmi.ClusterApplication
import com.ultraviolette.uvclusterhmi.domain.model.ClusterUiState
import com.ultraviolette.uvclusterhmi.domain.model.MenuPosition
import com.ultraviolette.uvclusterhmi.domain.model.ScreenMode
import com.ultraviolette.uvclusterhmi.ui.composable.screen.SplashScreen
import com.ultraviolette.uvclusterhmi.ui.viewModel.ClusterViewModel
import com.ultraviolette.uvclusterhmi.domain.model.RideMode
import com.ultraviolette.uvclusterhmi.domain.model.ToolbarUiState

class MainActivity : AppCompatActivity() { 

    private lateinit var wifiAutoConnector: WifiAutoConnector
    private var lastLux = -1f
    private var filteredLux = -1f
    private val luxThreshold = 5f
    private var lastAlsSentTime = 0L
    private lateinit var tvTime: TextView
    private lateinit var ivRegen: ImageView
    private lateinit var alertManager: ClusterAlertManager
    private lateinit var tvBatteryPercent: TextView
    private lateinit var ivHillHold: ImageView
    private lateinit var ivAbsState: ImageView
    private lateinit var ivBluetooth: ImageView
    private lateinit var ivWifi: ImageView
    private lateinit var ivLeftWarning2: ImageView
    private lateinit var ivNetwork: ImageView
    private lateinit var ivToolbarCenter: ImageView
    private lateinit var ivLeftIndicator: ImageView
    private lateinit var ivRightIndicator: ImageView
    private lateinit var ivTraction: ImageView
    private lateinit var ivRightWarning1: ImageView
    private lateinit var pbBattery: ProgressBar
    private lateinit var btnLeft: ImageView
    private lateinit var btnRight: ImageView
    private lateinit var btnTop: ImageView
    private lateinit var btnBottom: ImageView
    private lateinit var main: ConstraintLayout
    private lateinit var clToolBar: ConstraintLayout
    private lateinit var btnEnter: ImageView
    private lateinit var btnBack: ImageView
    private lateinit var ivRightWarning3: ImageView
    private lateinit var ivLeftWarning3: ImageView
    private lateinit var ivMotorStatus: ImageView
    private lateinit var ivRightWarning4: ImageView
    private lateinit var ivRightWarning5: ImageView
    private lateinit var ivRightWarning2: ImageView
    private lateinit var ivLeftWarning4: ImageView
    private lateinit var ivMode: ImageView
    private val tag = MainActivity::class.java.simpleName
    private var runtimePermission = ArrayList<String>()
    private val sensorManager: SensorManager by lazy {
        getSystemService(SENSOR_SERVICE) as SensorManager
    }
    private val lightSensor: Sensor?
        get() = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    private var navController: NavController? = null
    private var navHostFragment: NavHostFragment? = null
    private var hillHoldBlinkJob: Job? = null
    private var absWarningJob: Job? = null
    private var leftIndicatorBlinkJob: Job? = null
    private var rightIndicatorBlinkJob: Job? = null
    private var tractionBlinkJob: Job? = null
    //HeartBeat Implementation
    private var heartbeatJob: Job? = null
    private var heartbeatCounter: Long = 0L
    @Volatile private var isHeartbeatEnabled: Boolean = true
    private val MSG_ID_USB_HEARTBEAT_IMX_S32 = 0x2170030F
    //private val bluetoothViewModel by viewModels<BluetoothViewModel> { ViewModelFactory(context = this) }
    //private val wifiViewModel by viewModels<WifiViewModel> { ViewModelFactory(context = this) }
    private val dataViewModel by viewModels<DataViewModel> { ViewModelFactory(context = this) }
    private val carViewModel by viewModels<CarViewModel> { ViewModelFactory(context = this) }
    private val viewModel by viewModels<SharedViewModel> { ViewModelFactory(context = this) }
    private val performanceViewModel by viewModels<PerformanceViewModel> { ViewModelFactory(context = this) }
    /** New architecture ViewModel — reads from ClusterRepository + PreferenceManager only. */
    private val clusterViewModel: ClusterViewModel by viewModels {
        ClusterViewModel.Factory(application as ClusterApplication)
    }
    private lateinit var composeSplash: ComposeView
    private var haveDashcam = false
    private lateinit var fotaReceiver: FotaReceiver
    private val timeHandler = Handler(Looper.getMainLooper()) // rtc v1.4(rtc)
    private lateinit var timeRunnable: Runnable// rtc v1.4(rtc)
    private var isBallistic = false
    private lateinit var ivLeftWarning1: ImageView
    private var hazardAnimator: AnimatorSet? = null
    private lateinit var tvRegen: TextView

    private var hazardActive = false
    private var indicatorRaw = IndicatorMode.Off
    private var currentMode = IndicatorMode.Off
    private var isDashboard = false
    private var alsJob: Job? = null
    private var isClusterReady=false
    private var isCCOff = true

    /**
     * Register the permissions callback, which handles the user's response to the system permissions dialog.
     */
    private val runtimePermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach { entry ->
                val permission = entry.key
                val isGranted = entry.value
                if (isGranted) {
                    d(tag, "$permission granted")
                } else {
                    d(tag, "$permission denied")
                }
            }
        }

    /** Ambient sensor listener to get light sensor values */
    private val ambientSensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {

            if (event == null) return

            val lux = event.values[0]

            // Smooth
            if (filteredLux < 0) {
                filteredLux = lux
            } else {
                filteredLux = filteredLux * 0.8f + lux * 0.2f
            }

            // Ignore small fluctuations (UI optimization only)
            if (kotlin.math.abs(filteredLux - lastLux) < luxThreshold) return

            lastLux = filteredLux

            if (!viewModel.isAutoBrightnessEnabled) return

            val brightnessLevel = getBrightnessLevelFromLux(filteredLux)
            val brightnessPercentage = (brightnessLevel * 100).toInt()

            d("ALS", "Lux: $filteredLux  Brightness: $brightnessPercentage")

            viewModel.saveBrightness(brightnessPercentage)
            checkSettingAndBrightness(brightnessPercentage)

   /*         if (viewModel.mode == getString(R.string.auto)) {
                if (brightnessPercentage > 50) {
                    if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_NO) {
                        d("ALS", "Switching to DAY mode")
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }
                } else {
                    if (AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES) {
                        d("ALS", "Switching to NIGHT mode")
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    }
                }
            } */
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        hideSystemBars()
        viewModel.saveCustomModeAbs(false)
        //viewModel.saveBallisticPlus(false)


	initViews()

        // ── Permanent Compose host ────────────────────────────────────────────
        // composeSplash is the single ComposeView that hosts the entire Compose UI:
        //   Phase 1 (splashDone=false): shows SplashScreen
        //   Phase 2 (splashDone=true):  shows ClusterNavHost (routes by ScreenMode)
        //     - For Compose-migrated screens: full-screen Compose UI
        //     - For pending screens: returns Unit → Fragment nav shows underneath
        val splashMode = (application as ClusterApplication).preferenceManager.mode
        composeSplash.setContent {
            val uiState by clusterViewModel.uiState
                .collectAsStateWithLifecycle()
            val splashDone = (uiState as? com.ultraviolette.uvclusterhmi.domain.model.ClusterUiState.Active)
                ?.splashDone ?: false

            if (!splashDone) {
                SplashScreen(
                    mode = splashMode,
                    onComplete = { clusterViewModel.onSplashComplete() },
                )
            } else {
                val active = uiState as? com.ultraviolette.uvclusterhmi.domain.model.ClusterUiState.Active
                if (active != null) {
                    com.ultraviolette.uvclusterhmi.ui.composable.ClusterNavHost(
                        uiState       = active,
                        onMenuTileTap = { position ->
                            clusterViewModel.onMenuTileTapped(position)
                        },
                        onMenuDismiss = { clusterViewModel.dismissMenu() },
                    )
                }
            }
        }
        // ─────────────────────────────────────────────────────────────────────

        // ── Menu navigation event bridge ──────────────────────────────────────
        // When a Compose Menu tile is tapped (or Enter pressed on it via handlebar),
        // ClusterViewModel emits a MenuPosition. We collect it here and navigate the
        // Fragment nav controller to the appropriate sub-screen.
        lifecycleScope.launch {
            clusterViewModel.menuNavEvent.collect { position ->
                navigateToMenuTarget(position)
            }
        }
        // ─────────────────────────────────────────────────────────────────────

        requestPermission()
        hideSystemUI()
        initNavController()
        initClickListener()

        if (viewModel.isOtaComplete) {
            d("OTAUpdate", "File deleted")
        }
        d("MainActivityLifeCycle", "onCreate is called")
        if (SHOULD_INCLUDE_CAR_SERVICE) {
            d(tag, "All permissions granted")
            initCarViewModel()
            MyViewModelProvider.init(carViewModel)
        }
        frameworkStartTime = android.os.SystemClock.elapsedRealtime()
        fotaReceiver = FotaReceiver()

        val filter = IntentFilter().apply {
            addAction(FotaReceiver.ACTION_FOTA_EVENT)
//            addAction(FotaReceiver.ACTION_UDP_TIMEOUT)
        }

        registerReceiver(fotaReceiver, filter, RECEIVER_EXPORTED)
        FotaReceiver.listener = { action ->

            when (action) {

                FotaReceiver.ACTION_FOTA_EVENT -> {
                    alertManager.show(
                        titleText = "FOTA UPDATE",
                        messageText = "FOTA download complete"
                    )
                }

                /*FotaReceiver.ACTION_UDP_TIMEOUT -> {
                    alertManager.show(
                        titleText = "VCU UDP Disconnect",
                        messageText = "VCU Disconnection occured"
                    )
                }*/
            }
        }

        val imei = getSystemProperty("persist.sys.bike.imei", "000000000000000")
        d("IMEI", imei)
        //ClusterNotification.reset()
	window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        //initViews()
        //requestPermission()
        hideSystemUI()
        //initNavController()
        //initClickListener()
        //bluetoothViewModel.bluetoothStateChange()
        //wifiViewModel.wifiStateChange()
        wifiAutoConnector = WifiAutoConnector(this)
        wifiAutoConnector.startAutoConnectLoop()

        dataViewModel.stateChange()
        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        d("ScreenInfo", "Resolution: ${width}x${height}")
        //showTellTales()
        initObserver()
        //NotificationManager.init(this, lifecycleOwner = this, carViewModel, viewModel)
	NotificationManager.init(applicationContext, carViewModel, viewModel)
    }

    /**
     * Handlebar button dispatch — routes MENU/BACK to [clusterViewModel] first;
     * falls through to the Fragment-hosted navigation for all other keys.
     * Rule 6: both touch and handlebar paths converge on the same ViewModel action.
     */
    override fun dispatchKeyEvent(e: KeyEvent): Boolean {
        if (e.action == KeyEvent.ACTION_DOWN &&
            clusterViewModel.handleHandlebarButton(e.keyCode)) {
            return true
        }
        return super.dispatchKeyEvent(e)
    }

    override fun onStart() {
        super.onStart()
        //bluetoothViewModel.registerBluetoothActionReceiver()
        //wifiViewModel.registerWifiStateChangeReceiver()
        dataViewModel.registerReceiver()
	dataViewModel.getNetworkSignalLevel()
    }

    fun initObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // ── Brightness ──────────────────────────────────────────────────
                // Previously from carViewModel.displayBrightness; now flows through
                // VehicleSnapshot.brightnessLevel/brightnessAuto via ClusterViewModel.
                launch {
                    clusterViewModel.uiState.collect { uiState ->
                        val active = uiState as? ClusterUiState.Active ?: return@collect
                        val brightness = active.brightness
                        viewModel.saveBrightnessState(brightness.isAuto)
                        viewModel.saveBrightness(brightness.level)
                        if (brightness.level > 0) {
                            checkSettingAndBrightness(brightness.level)
                        }
                    }
                }

                // ── Toolbar icons ───────────────────────────────────────────────
                // Previously from carViewModel.tellTales; now flows through
                // ClusterViewModel.uiState.toolbar.
                launch {
                    clusterViewModel.uiState.collect { uiState ->
                        val active = uiState as? ClusterUiState.Active ?: return@collect
                        handleToolbarFromUiState(active.toolbar)
                    }
                }
            }
        }
    }

    /**
     * Drives every toolbar icon from [ToolbarUiState] — the single source of truth
     * built by [ClusterViewModel] from [VehicleSnapshot].
     *
     * Replaces the old carViewModel.tellTales → handleTellTales(TellTales) path.
     */
    private fun handleToolbarFromUiState(toolbar: ToolbarUiState) {
        // ── SOC ───────────────────────────────────────────────────────────────
        handleSoc(toolbar.batterySoc)

        // ── Motor arm/disarm ─────────────────────────────────────────────────
        motorState(toolbar.motorArmed)

        // ── Motor temperature ─────────────────────────────────────────────────
        when (toolbar.motorTempIcon) {
            2 -> {
                ivRightWarning4.setImageDrawable(getDrawable(this, R.drawable.ic_motor_temp_orange))
                ivRightWarning4.visibility = View.VISIBLE
            }
            3 -> {
                ivRightWarning4.setImageDrawable(getDrawable(this, R.drawable.ic_motor_temp_red))
                ivRightWarning4.visibility = View.VISIBLE
            }
            else -> ivRightWarning4.visibility = View.INVISIBLE
        }

        // ── Battery error / over-temp ─────────────────────────────────────────
        when {
            toolbar.batteryOverTemp -> {
                ivRightWarning3.setImageDrawable(
                    getDrawable(this, R.drawable.ic_toolbar_battery_temperature)
                )
                ivRightWarning3.visibility = View.VISIBLE
            }
            toolbar.batteryError -> {
                ivRightWarning3.setImageDrawable(getDrawable(this, R.drawable.ic_battery_error))
                ivRightWarning3.visibility = View.VISIBLE
            }
            else -> ivRightWarning3.visibility = View.INVISIBLE
        }

        // ── Critical malfunction ──────────────────────────────────────────────
        if (toolbar.criticalMalfunction) {
            ivRightWarning1.visibility = View.VISIBLE
            ivRightWarning1.setImageDrawable(getDrawable(this, R.drawable.ic_warning_toolbar))
        } else {
            ivRightWarning1.visibility = View.INVISIBLE
        }

        // ── MIL (malfunction indicator lamp) ─────────────────────────────────
        if (toolbar.milIcon == 1) {
            when (toolbar.milState) {
                1 -> {
                    ivRightWarning5.setImageDrawable(getDrawable(R.drawable.ic_international_mil))
                    ivRightWarning5.visibility = View.VISIBLE
                }
                2 -> {
                    ivRightWarning5.setImageDrawable(getDrawable(R.drawable.ic_domestic_mil))
                    ivRightWarning5.visibility = View.VISIBLE
                }
                else -> ivRightWarning5.visibility = View.INVISIBLE
            }
        } else {
            ivRightWarning5.visibility = View.INVISIBLE
        }

        // ── High beam ─────────────────────────────────────────────────────────
        if (toolbar.highBeam) {
            ivLeftWarning2.setImageDrawable(getDrawable(this, R.drawable.ic_high_beam_toolbar))
            ivLeftWarning2.visibility = View.VISIBLE
        } else {
            ivLeftWarning2.visibility = View.INVISIBLE
        }

        // ── Hill hold ─────────────────────────────────────────────────────────
        updateHillHold(toolbar.hillHoldState)

        // ── MTC / Traction control ────────────────────────────────────────────
        handleMtcControl(toolbar.mtcState, toolbar.mtcMode)

        // ── Ride mode icon ────────────────────────────────────────────────────
        val rideModeInt = when (toolbar.rideMode) {
            RideMode.Glide    -> 1
            RideMode.Combat   -> 2
            RideMode.Ballistic -> 3
        }
        isBallistic = toolbar.rideMode == RideMode.Ballistic
        handleRideMode(rideModeInt)

        // ── ABS ───────────────────────────────────────────────────────────────
        handleAbs(toolbar.absMode, toolbar.absWarning)

        // ── Regen ────────────────────────────────────────────────────────────
        if (toolbar.regenUnavailable) {
            ivRegen.visibility = View.VISIBLE
            tvRegen.visibility = View.INVISIBLE
        } else {
            handleRegen(toolbar.regenLevel)
        }

        // ── Radar ─────────────────────────────────────────────────────────────
        handleRadarTelltales(toolbar.radarState)

        // ── Indicators & hazard ───────────────────────────────────────────────
        onHazardUpdate(toolbar.hazardActive)
        val indicatorInt = when {
            toolbar.rightIndicatorOn -> 1
            toolbar.leftIndicatorOn  -> 2
            else                     -> 0
        }
        onIndicatorUpdate(indicatorInt)

        // ── Charging navigation ───────────────────────────────────────────────
        // Charging screen is now owned by ScreenMode.Charging → ClusterNavHost →
        // ChargingScreen (Compose). No Fragment nav needed here.
    }

    /**
     * Set the toolbar icons.
     */
    fun setToolbarIcons(isDashboard: Boolean) {
        val toolbarBg = if (isDashboard) resolveAttr(R.attr.bgMode) else R.color.bgToolBar
        val mainBg =
            if (isDashboard) resolveAttr(R.attr.appBackgroundColor) else R.color.bgToolBar
        val toolbarCenter =
            if (isDashboard) resolveAttr(R.attr.toolbarCenter) else R.drawable.toolbar_center_dark
        val textColor = if (isDashboard) resolveAttr(R.attr.appTextColor) else R.color.white
        main.setBackgroundResource(mainBg)
        clToolBar.setBackgroundResource(toolbarBg)
        ivToolbarCenter.setImageDrawable(getDrawable(this, toolbarCenter))
        tvTime.setTextColor(ContextCompat.getColor(this, textColor))
        tvRegen.setTextColor(ContextCompat.getColor(this, textColor))
        if (isDashboard) {
            // ivWifi.setImageDrawable(getDrawable(this, R.drawable.ic_toolbar_wifi))
            ivWifi.setColorFilter(ContextCompat.getColor(this, textColor))
            ivBluetooth.setImageDrawable(
                getDrawable(
                    this, R.drawable.ic_toolbar_bluetooth
                )
            )
            //ivLeftWarning1.setImageDrawable(getDrawable(this, R.drawable.ic_cruse_control))
           // ivNetwork.setImageDrawable(getDrawable(this, R.drawable.ic_toolbar_network))
	    ivNetwork.setColorFilter(ContextCompat.getColor(this, textColor))
            val currentResMtc = ivTraction.tag as? Int

            when (currentResMtc) {
                R.drawable.ic_mtc_1_white -> {
                    ivTraction.setImageDrawable(getDrawable(this, R.drawable.ic_mtc_1))
                    ivTraction.tag = R.drawable.ic_mtc_1
                }

                R.drawable.ic_mtc_2_white -> {
                    ivTraction.setImageDrawable(getDrawable(this, R.drawable.ic_mtc_2))
                    ivTraction.tag = R.drawable.ic_mtc_2
                }

                R.drawable.ic_mtc_3_white -> {
                    ivTraction.setImageDrawable(getDrawable(this, R.drawable.ic_mtc_3))
                    ivTraction.tag = R.drawable.ic_mtc_3
                }
            }

            val currentResHillHold = ivHillHold.tag as? Int
            if (currentResHillHold == R.drawable.ic_hill_hold_on_white) {
                ivHillHold.setImageDrawable(getDrawable(this, R.drawable.ic_hill_hold_on))
                ivHillHold.tag = R.drawable.ic_hill_hold_on
            }

            val currentResRadar = ivLeftWarning3.tag as? Int
            if(currentResRadar == R.drawable.ic_radar_white){
                ivLeftWarning3.setImageDrawable(getDrawable(this, R.drawable.ic_radar))
                 ivLeftWarning3.tag = R.drawable.ic_radar
            }

        } else {
            // ivWifi.setImageDrawable(getDrawable(this, R.drawable.ic_wifi_toolbar))
            ivWifi.setColorFilter(ContextCompat.getColor(this, textColor))
            ivBluetooth.setImageDrawable(
                getDrawable(
                    this, R.drawable.ic_bluetooth_toolbar
                )
            )
	    ivNetwork.setColorFilter(ContextCompat.getColor(this, textColor))
            //ivNetwork.setImageDrawable(
               // getDrawable(
               //     this, R.drawable.ic_network_toolbar
             //   )
           // )

            //ivLeftWarning1.setImageDrawable(getDrawable(this, R.drawable.ic_curse_control_white))
            val currentResMtc = ivTraction.tag as? Int
            d("CurrentState", "CurrentRes : $currentResMtc drawableId:${R.drawable.ic_mtc_1}")


            when (currentResMtc) {
                R.drawable.ic_mtc_1 -> {
                    ivTraction.setImageDrawable(
                        getDrawable(
                            this,
                            R.drawable.ic_mtc_1_white
                        )
                    )
                    ivTraction.tag = R.drawable.ic_mtc_1_white
                }

                R.drawable.ic_mtc_2 -> {
                    ivTraction.setImageDrawable(
                        getDrawable(
                            this,
                            R.drawable.ic_mtc_2_white
                        )
                    )
                    ivTraction.tag = R.drawable.ic_mtc_2_white
                }

                R.drawable.ic_mtc_3 -> {
                    ivTraction.setImageDrawable(
                        getDrawable(
                            this,
                            R.drawable.ic_mtc_3_white
                        )
                    )
                    ivTraction.tag = R.drawable.ic_mtc_3_white
                }
            }

            val currentResHillHold = ivHillHold.tag as? Int
            if (currentResHillHold == R.drawable.ic_hill_hold_on) {
                ivHillHold.setImageDrawable(getDrawable(this, R.drawable.ic_hill_hold_on_white))
                ivHillHold.tag = R.drawable.ic_hill_hold_on_white
            }
            val currentResRadar = ivLeftWarning3.tag as? Int
            if(currentResRadar == R.drawable.ic_radar){
                ivLeftWarning3.setImageDrawable(getDrawable(this, R.drawable.ic_radar_white))
                ivLeftWarning3.tag = R.drawable.ic_radar_white
            }
        }
    }


    /**
     * Initialize the navigation controller.
     */
    private fun initNavController() {
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        navController = navHostFragment?.navController
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navController.addOnDestinationChangedListener { _, destination, _ ->
            isDashboard = destination.id == R.id.dashboardFragment
            setToolbarIcons(isDashboard)
        }
    }

    /**
     * Resolve an attribute resource.
     */
    fun resolveAttr(attr: Int): Int {
        val typedValue = TypedValue()
        theme.resolveAttribute(attr, typedValue, false)
        return typedValue.data
    }

    /**
     * Initialize the CarViewModel.
     */
    private fun initCarViewModel() {
        d(tag, "initCarViewModel")
        //initObserver()
        carViewModel.connect()
        //NotificationManager.init(this, lifecycleOwner = this, carViewModel)
    }


    fun loadBinFile(context: Context, fileName: String): ByteArray {
        return context.assets.open(fileName).readBytes()
    }

    /**
     * Initialize the observer for the CarViewModel.
     */

    private fun handleRadarTelltales(radarTelltaleState: Int) {
        when (radarTelltaleState) {
            1 -> ivLeftWarning3.visibility = View.INVISIBLE
            2-> {
                ivLeftWarning3.visibility = View.VISIBLE
                if(isDashboard){
                    ivLeftWarning3.setImageDrawable(getDrawable(this, R.drawable.ic_radar))
                    ivLeftWarning3.tag = R.drawable.ic_radar
                }else{
                    ivLeftWarning3.setImageDrawable(getDrawable(this,R.drawable.ic_radar_white))
                    ivLeftWarning3.tag = R.drawable.ic_radar_white
                }
            }
            3 -> {
                ivLeftWarning3.visibility = View.VISIBLE
                ivLeftWarning3.setImageDrawable(getDrawable(this, R.drawable.ic_radar_malfunction))
                ivLeftWarning3.tag = R.drawable.ic_radar_malfunction
            }
        }
    }

    private fun handleScreenModes(value: Int?) {
        val isDayMode = value == 0
        if (isDayMode) {
            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                viewModel.hasThemeConfigChanged = true
                d("MainActivityLifeCycle", "Vhal data theme change day")
                viewModel.saveMode(getString(R.string.day))
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        } else {
            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO) {
                viewModel.hasThemeConfigChanged = true
                d("MainActivityLifeCycle", "Vhal data theme change night")
                viewModel.saveMode(getString(R.string.night))
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }

    private fun handleDisplayBrightness(value: IntArray) {
        val brightnessLevel = value[0]
        val isAuto = value[1] == 1
        viewModel.saveBrightnessState(isAuto)
        viewModel.saveBrightness(brightnessLevel)
        checkSettingAndBrightness(brightnessLevel)
    }

    private fun handleDateTime(value: IntArray) {
        val day = value[0].toUByte().toInt()
        val hour = value[1].toUByte().toInt()
        val minute = value[2].toUByte().toInt()
        val second = value[3].toUByte().toInt()
        val month = value[4].toUByte().toInt()
        val weekday = value[5].toUByte().toInt()
        val year = 2000 + value[6].toUByte().toInt()

        val time = LocalTime.of(hour, minute, second)
        val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
        val formattedTime = time.format(formatter)
        tvTime.text = formattedTime
    }


    /**
     * Binds UI components from the provided root view using their IDs.
     *
     * Initializes:
     * - tvTime, tvRegen, tvBatteryPercent (text views)
     * - ivToolbarCenter, ivWifi, ivBluetooth, ivDiagonal, ivNetwork (image view)
     * - pbBattery (progress bar)
     * - clToolBar, main (constraintLayout)
     */
    private fun initViews() {
        tvTime = findViewById(R.id.tvTime)
        ivRegen = findViewById(R.id.ivRegen)
        tvRegen = findViewById(R.id.tvRegen)
        tvBatteryPercent = findViewById(R.id.tvBatteryPercent)
        ivToolbarCenter = findViewById(R.id.ivToolbarCenter)
        ivWifi = findViewById(R.id.ivWifi)
        ivBluetooth = findViewById(R.id.ivBluetooth)
        ivNetwork = findViewById(R.id.ivNetwork)
        clToolBar = findViewById(R.id.clToolBar)
        pbBattery = findViewById(R.id.pbBattery)
        ivAbsState = findViewById(R.id.ivAbs)
        ivHillHold = findViewById(R.id.ivHillHold)

        main = findViewById(R.id.main)
        btnTop = findViewById(R.id.btnTop)
        btnRight = findViewById(R.id.btnRight)
        btnBottom = findViewById(R.id.btnBottom)
        btnLeft = findViewById(R.id.btnLeft)
        btnEnter = findViewById(R.id.btnEnter)
        btnBack = findViewById(R.id.btnBack)
        ivMotorStatus = findViewById(R.id.ivMotorStatus)

        ivLeftIndicator = findViewById(R.id.ivLeftIndicator)
        ivRightIndicator = findViewById(R.id.ivRightIndicator)

        ivRightWarning1 = findViewById(R.id.ivRightWarning1)
        ivRightWarning2 = findViewById(R.id.ivRightWarning2)
        ivRightWarning3 = findViewById(R.id.ivRightWarning3)
        ivRightWarning4 = findViewById(R.id.ivRightWarning4)
        ivRightWarning5 = findViewById(R.id.ivRightWarning5)

        ivLeftWarning1 = findViewById(R.id.ivLeftWarning1)
        ivLeftWarning2 = findViewById(R.id.ivLeftWarning2)
        ivLeftWarning3 = findViewById(R.id.ivLeftWarning3)
        ivLeftWarning4 = findViewById(R.id.ivLeftWarning4)


        ivTraction = findViewById(R.id.ivTraction)
        ivMode = findViewById(R.id.ivMode)
        composeSplash = findViewById(R.id.composeSplash)
        val alertCard = findViewById<ConstraintLayout>(R.id.alertCard)
        val tvHeading = findViewById<TextView>(R.id.tvAlertHeading)
        val tvSubtext = findViewById<TextView>(R.id.tvAlertSubtext)
        val btnClose = findViewById<ImageView>(R.id.btnAlertClose)

        alertManager = ClusterAlertManager(
            alertCard,
            tvHeading,
            tvSubtext,
            btnClose
        )
    }


    fun randomTellTales(): TellTales {
        return TellTales(
            hillHold = (0..7).random(),
            motorTempIcon = (0..7).random(),
            absWarningLamp = (0..3).random(),
            mtcMode = (0..7).random(),
            mtcState = (0..7).random(),
            charger = (0..3).random(),
            rideMode = (0..3).random(),
            modeHover = (0..1).random(),
            milState = (0..3).random(),
            absMode = (0..3).random(),
            batteryError = (0..1).random(),
            batteryOverTemp = (0..1).random(),
            highBeam = (0..1).random(),
            indicatorLeft = (0..1).random(),
            indicatorRight = (0..1).random(),
            milIcon = (0..1).random(),
            motorArmed = (0..1).random(),
            otaPending = (0..1).random(),
            regenUnavailable = (0..1).random(),
            vehicleSpeed = (0..511).random(),
            batterySoc = (0..127).random(),
            hazardLamps = (0..1).random(),
            regenLevel = (0..15).random(),
            criticalMalfunction = (0..1).random(),
            reserved = (0..1023).random()
        )
    }

    private fun initClickListener() {
        btnRight.setOnSoundClickListener(this) {
            handleButtonNavigation(ButtonNavigation.Right.ordinal)
        }
        btnLeft.setOnSoundClickListener(this) {
            handleButtonNavigation(ButtonNavigation.Left.ordinal)
        }
        btnTop.setOnSoundClickListener(this) {
            handleButtonNavigation(ButtonNavigation.Top.ordinal)
        }
        btnBottom.setOnSoundClickListener(this) {
            handleButtonNavigation(ButtonNavigation.Bottom.ordinal)
        }
        btnEnter.setOnSoundClickListener(this) {
            handleButtonNavigation(ButtonNavigation.Enter.ordinal)
        }
        btnBack.setOnSoundClickListener(this) {
            handleButtonNavigation(ButtonNavigation.Back.ordinal)

        }

    }

    private fun handleButtonNavigation(button: Int) {
        d("Button Navigation", "${Utilities.getButtonState(button).name}")

        // Intercept for Compose-managed screens before Fragment dispatch.
        val activeState = clusterViewModel.uiState.value as? ClusterUiState.Active
        when (activeState?.screenMode) {
            ScreenMode.HoverMode -> {
                when (button) {
                    ButtonNavigation.Left.ordinal  -> adjustRegen(-1)
                    ButtonNavigation.Right.ordinal -> adjustRegen(+1)
                }
                return
            }
            else -> { /* Fall through to Fragment dispatch. */ }
        }

        val primaryFragment = navHostFragment?.childFragmentManager?.primaryNavigationFragment
        when (primaryFragment) {
            is MenuFragment -> primaryFragment.handleButtonNavigation(button)
            is MyF77MenuFragment -> primaryFragment.handleButtonNavigation(button)
            is DocumentMenuFragment -> primaryFragment.handleButtonNavigation(button)
            is TutorialFragment -> primaryFragment.handleButtonNavigation(button)
            is BatteryFragment -> primaryFragment.handleButtonNavigation(button)
            is RideModesFragment -> primaryFragment.handleButtonNavigation(button)
            is PerformanceFragment -> primaryFragment.handleButtonNavigation(button)
            is TripsFragment -> primaryFragment.handleButtonNavigation(button)
            is ControlMenuFragment -> primaryFragment.handleButtonNavigation(button)
            is SettingsFragment -> primaryFragment.handleButtonNavigation(button)
            is AdvancedFeaturesFragment -> primaryFragment.handleButtonNavigation(button)
            is EmergencyFragment -> primaryFragment.handleButtonNavigation(button)
            is TpmsFragment -> primaryFragment.handleButtonNavigation(button)
            is VioletteFragment -> primaryFragment.handleButtonNavigation(button)
            is DocumentFragment -> primaryFragment.handleButtonNavigation(button)
            is InfoFragment -> primaryFragment.handleButtonNavigation(button)
            is TutorialPlayerFragment -> primaryFragment.handleButtonNavigation(button)
            is MusicFragment -> primaryFragment.handleButtonNavigation(button)
            is WinterModeFragment -> primaryFragment.handleButtonNavigation(button)
        }
    }

    /**
     * Navigate the Fragment nav controller to the sub-screen for a confirmed [MenuPosition].
     *
     * Called after ClusterViewModel emits [menuNavEvent] (tile tap or handlebar Enter).
     * The ViewModel has already set [_menuVisible] = false before emitting, so ScreenMode
     * transitions away from Menu before this navigate call resolves.
     *
     * Fragment destination IDs match the actions used in the old MenuFragment.
     */
    private fun navigateToMenuTarget(position: MenuPosition) {
        val destId = when (position) {
            MenuPosition.MyF77    -> R.id.myF77MenuFragment
            MenuPosition.Battery  -> R.id.batteryFragment
            MenuPosition.Setting  -> R.id.settingMenuFragment
            MenuPosition.Music    -> R.id.musicFragment
            MenuPosition.Controls -> R.id.controlFragment
            MenuPosition.Tpms     -> R.id.tpmsFragment
            MenuPosition.Navigate -> R.id.mapFragment
            MenuPosition.Enter    -> return   // sentinel — no navigation
        }
        try {
            navController?.navigate(destId)
        } catch (e: IllegalArgumentException) {
            d(tag, "navigateToMenuTarget: failed for $position — ${e.message}")
        }
    }

    /**
     * Adjust regen level by [delta] (+1 or -1 step).
     * Called from [handleButtonNavigation] when [ScreenMode.HoverMode] is active.
     * Write path intentionally goes through CarViewModel until ClusterRepository supports writes.
     */
    private fun adjustRegen(delta: Int) {
        val is10Levels = (clusterViewModel.uiState.value as? com.ultraviolette.uvclusterhmi.domain.model.ClusterUiState.Active)
            ?.dashboard?.regen?.is10Levels ?: true
        val step = if (is10Levels) 1 else 3
        var current = viewModel.regenValue
        if (!is10Levels) current = (current / 3) * 3
        val newLevel = (current + delta * step).coerceIn(0, 9)
        viewModel.saveRegenValue(newLevel)
        carViewModel.sendByteArrayProperty(0x2170039F, byteArrayOf(newLevel.toByte()))
        d("HoverRegen", "Regen adjusted to $newLevel (is10=$is10Levels)")
    }

    override fun onResume() {
         super.onResume()

    // Ensure WiFi ON
            /*if (!wifiViewModel.isWifiEnabled()) {
        wifiViewModel.enableWifi(true)
    }*/

   

            
           //wifiViewModel.scanResult()
           //wifiViewModel.startSignalMonitoring()
	   dataViewModel.getNetworkSignalLevel()

          d("MainActivityLifeCycle", "onResume is called")

    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)//error fix_1.4(screen dimming error)

    lightSensor?.let {
        sensorManager.registerListener(
            ambientSensorListener,
            it,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

         checkStorageAndTriggerCleanup()

      
    d("MainActivity","onResume: DefaultNightMode: BootCompleted Flag :: ${carViewModel.bootCompleted}")

    if (!carViewModel.bootCompleted){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            viewModel.saveMode(getString(R.string.night))
            carViewModel.onBootCompleted()
        }
      
       sendClusterReady()

       startToolbarClock()
    }

    override fun onPause() {
        super.onPause()
        d("MainActivityLifeCycle", "onPause is called")
        timeHandler.removeCallbacks(timeRunnable) // rtc v1.4(rtc)
        sensorManager.unregisterListener(ambientSensorListener)
        alsJob?.cancel()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)//error fix_1.4(screen dimming error)
    }
    private fun startAlsJob(){
        alsJob?.cancel()

        alsJob = lifecycleScope.launch {
            while (isActive) {
                val brightnessLevel = getBrightnessLevelFromLux(filteredLux)
                val brightnessPercentage = (brightnessLevel * 100).toInt()

                //val dayNight = if (brightnessPercentage > 50) 1 else 2
                val dayNight = if (
                    LocalTime.now(ZoneId.of("Asia/Kolkata")).let {
                        it.isAfter(LocalTime.of(6, 0)) && it.isBefore(LocalTime.of(18, 0))
                    }
                ) 1 else 2
                val payload = buildAlsPayload(filteredLux, dayNight)

                sendByteArray(Utilities.PROP_ID_ALS_INFO, payload)

                d("ALS_VCU", "Periodic send lux=${filteredLux.toInt()}  Brightness= ${brightnessPercentage}%  DayNight=${dayNight}")

                delay(2000)
            }
        }

    }


     private fun sendVehicleInfo() {
        val CLUSTER_TO_VCU_VEHICLE_INFO = 0x21700300
        val buffer = java.nio.ByteBuffer.allocate(51)
        buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN)

        d(tag, "Packing Vehicle Info -> IMEI: $viewModel.imeiNumber | VIN: $viewModel.vinTextValue")
        buffer.put(viewModel.imeiNumber.toByteArray(Charsets.US_ASCII).copyOf(16))
        buffer.put(viewModel.vinTextValue.toByteArray(Charsets.US_ASCII).copyOf(32))
    
        // Hardcoded firmware and build versions (0)
        buffer.put(0.toByte()) // fw_ver_major
        buffer.put(0.toByte()) // fw_ver_minor
        buffer.put(0.toByte()) // build_number
        val payload = buffer.array()

        sendByteArray(CLUSTER_TO_VCU_VEHICLE_INFO,payload)
        d(tag, "VehicleInfo sent")
    }


    private fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
        d(tag, "Heartbeat: job cancelled")
    }   

    private fun buildHeartbeatPayload(): ByteArray {
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

    fun handleHeartbeatControlFromVcu(byte0: Int) {
        when (byte0) {
            1 -> {
                isHeartbeatEnabled = true
                d(tag, "Heartbeat: VCU ENABLED heartbeat")
            }
            2 -> {
                isHeartbeatEnabled = false
                d(tag, "Heartbeat: VCU DISABLED heartbeat")
            }
            else -> d(tag, "Heartbeat: unknown control byte 0x${byte0.toString(16)}")
        }
    }

    private fun updateCurrentStates(context: Context) {
        val wifiManager = context.getSystemService(WIFI_SERVICE) as WifiManager
        val bluetoothManager = context.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
        val connectivityManager =
            context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        ivWifi.visibility = if (wifiManager.isWifiEnabled) View.VISIBLE else View.INVISIBLE
        ivBluetooth.visibility =
            if (bluetoothAdapter?.isEnabled == true) View.VISIBLE else View.INVISIBLE
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        ivNetwork.visibility =
            if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true) View.VISIBLE else View.INVISIBLE
    }

    /**
     * Handle the visibility of the toolbar.
     */
    fun handleToolbar(shouldShow: Boolean) {
        clToolBar.isVisible = shouldShow
    }

    /**
     *  Hides the system UI (i.e. status bar and navigation bar)
     *  for testing in emulator will be removed in production software
     */
    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    /** Request runtime permissions for the app.*/
    private fun requestPermission() {
        addRequiredRuntimePermissions()
        val permissionsToRequest = mutableListOf<String>()
        for (permission in runtimePermission) {
            when {
                ContextCompat.checkSelfPermission(
                    this, permission
                ) == PackageManager.PERMISSION_GRANTED -> {
                    d(tag, "$permission already granted")
                }

                ActivityCompat.shouldShowRequestPermissionRationale(this, permission) -> {
                    showPermissionExplanationDialog(permission) {
                        permissionsToRequest.add(permission)
                        runtimePermissionsLauncher.launch(permissionsToRequest.toTypedArray())
                    }
                    return
                }

                else -> {
                    permissionsToRequest.add(permission)
                }
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            d(
                "RunTimePermission",
                "${permissionsToRequest.size} ${permissionsToRequest.toTypedArray()}"
            )
            runtimePermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        } /*else {
            if (SHOULD_INCLUDE_CAR_SERVICE) {
                d(tag, "All permissions granted")
                initCarViewModel()
            }
        }*/
    }

    /**
     * Show a dialog explaining why the app needs a specific permission.
     */
    private fun showPermissionExplanationDialog(permission: String, onAccept: () -> Unit) {
        AlertDialog.Builder(this).setTitle(getString(R.string.permission_required))
            .setMessage(getString(R.string.rationale_message, permission))
            .setPositiveButton(getString(R.string.allow)) { _, _ -> onAccept() }
            .setNegativeButton(getString(R.string.no_thanks), null).show()
    }

    /**
     * Add the required runtime permissions for the app.
     */
    private fun addRequiredRuntimePermissions() {
        runtimePermission.addAll(permissionsForSDKR)
        runtimePermission.addAll(permissionsForSDKAboveR)
    }


    /**
     * Check if the app can write to system settings and adjust the brightness level.
     */
    fun checkSettingAndBrightness(brightnessLevel: Int) {
        if (!Settings.System.canWrite(this)) {
            d("UI Update", "No permission for write")
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(
                    this,
                    "Cannot open settings screen on this device. Please grant permission manually.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            val brightnessInt = ((brightnessLevel.toFloat() / 100) * 255).toInt().coerceIn(0, 255)
            Settings.System.putInt(
                contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightnessInt
            )
            window?.attributes = window?.attributes?.apply {
                screenBrightness = brightnessLevel.toFloat() / 100
            }
        }
    }

    fun sendByteArray(propertyId: Int, byteArray: ByteArray) {
        carViewModel.sendByteArrayProperty(propertyId, byteArray)
    }
     private fun buildAlsPayload(lux: Float, dayNight: Int): ByteArray {

        val luxInt = kotlin.math.round(lux).toInt()

        val buffer = java.nio.ByteBuffer.allocate(8)
        buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN)

        buffer.putInt(luxInt)
        buffer.putInt(dayNight)

        return buffer.array()
    }

    fun sendBoolean(propertyId: Int, value: Boolean) = carViewModel.sendBoolean(propertyId, value)

    override fun onDestroy() {
        super.onDestroy()
        d("MainActivityLifeCycle", "onDestroy is called")
	isClusterReady = false
        if (SHOULD_INCLUDE_CAR_SERVICE) carViewModel.disconnect()
        unregisterReceiver(fotaReceiver)
        hillHoldBlinkJob?.cancel()
        leftIndicatorBlinkJob?.cancel()
        rightIndicatorBlinkJob?.cancel()
        sensorManager.unregisterListener(ambientSensorListener)
        wifiAutoConnector.stopAutoConnectLoop()
        //bluetoothViewModel.unregisterBluetoothActionReceiver()
        //wifiViewModel.unregisterWifiStateChangeReceiver()
        dataViewModel.unregisterReceiver()
	dataViewModel.stopSignalUpdates()
	//stopHeartbeat()
    }

    private fun updateIndicators(value: Int) {
        when (value) {
            1 -> {
                d("Indicators", "Off")
                stopIndicatorBlinking()
                ivRightIndicator.visibility = View.INVISIBLE
                ivLeftIndicator.visibility = View.INVISIBLE
            }

            2 -> {
                d("Indicators", "Right")
                stopIndicatorBlinking()
                ivLeftIndicator.visibility = View.INVISIBLE
                ivRightIndicator.visibility = View.VISIBLE
                ivRightIndicator.setImageDrawable(
                    getDrawable(
                        this,
                        R.drawable.ic_toolbar_right_indicator
                    )
                )
                rightIndicatorBlinkJob = blinkImage(ivRightIndicator)
            }

            3 -> {
                d("Indicators", "Left")
                stopIndicatorBlinking()
                ivLeftIndicator.visibility = View.VISIBLE
                ivRightIndicator.visibility = View.INVISIBLE
                ivLeftIndicator.setImageDrawable(
                    getDrawable(
                        this,
                        R.drawable.ic_toolbar_right_indicator
                    )
                )
                leftIndicatorBlinkJob = blinkImage(ivLeftIndicator)
            }
        }
    }

    private fun updateIndicator(value: Int) {


        when (value) {
            0x00 -> {
                d("Indicators", "Off")
                stopIndicatorBlinking()
                ivRightIndicator.visibility = View.INVISIBLE
                ivLeftIndicator.visibility = View.INVISIBLE
            }

            0x01 -> {
                d("Indicators", "Right")
                stopIndicatorBlinking()
                ivLeftIndicator.visibility = View.INVISIBLE
                ivRightIndicator.visibility = View.VISIBLE
                ivRightIndicator.setImageDrawable(
                    getDrawable(
                        this,
                        R.drawable.ic_toolbar_right_indicator
                    )
                )
                rightIndicatorBlinkJob = blinkImage(ivRightIndicator)
            }

            0x02 -> {
                d("Indicators", "Left")
                stopIndicatorBlinking()
                ivLeftIndicator.visibility = View.VISIBLE
                ivRightIndicator.visibility = View.INVISIBLE
                ivLeftIndicator.setImageDrawable(
                    getDrawable(
                        this,
                        R.drawable.ic_toolbar_right_indicator
                    )
                )
                leftIndicatorBlinkJob = blinkImage(ivLeftIndicator)
            }
        }
    }


    private fun stopIndicatorBlinking() {
        rightIndicatorBlinkJob?.cancel()
        leftIndicatorBlinkJob?.cancel()
        rightIndicatorBlinkJob = null
        leftIndicatorBlinkJob = null
    }

    private fun stopHillHoldBlinking() {
        hillHoldBlinkJob?.cancel()
        hillHoldBlinkJob = null
    }


    private fun motorState(motorArmed: Boolean) {
        d("MotorArm: ", "$motorArmed")
        ivMotorStatus.visibility = View.VISIBLE
        if (motorArmed) {
            ivMotorStatus.setImageDrawable(getDrawable(this, R.drawable.ic_motor_on))
        } else {
            ivMotorStatus.setImageDrawable(getDrawable(this, R.drawable.ic_motor_off))
        }
    }

    fun handleTellTales(tellTales: TellTales) {
        val batteryError = tellTales.batteryError == 1
        val batteryOverTemperature = tellTales.batteryOverTemp == 1
        val criticalMalfunction = tellTales.criticalMalfunction == 1
        val otaPending = tellTales.otaPending == 1

       /* if (tellTales.milState == 1) {
            val milIcon = if (tellTales.milIcon == 1)
                R.drawable.ic_international_mil
            else
                R.drawable.ic_domestic_mil
            ivRightWarning5.visibility = View.VISIBLE
            ivRightWarning5.setImageDrawable(getDrawable(this, milIcon))
        } else {
            ivRightWarning5.visibility = View.INVISIBLE
        }*/
        
        if (tellTales.milIcon==1){
            if (tellTales.milState==1)
            {
                ivRightWarning5.setImageDrawable(getDrawable(R.drawable.ic_international_mil))
                ivRightWarning5.visibility = View.VISIBLE
            }else if (tellTales.milState==2)
            {
                ivRightWarning5.setImageDrawable(getDrawable(R.drawable.ic_domestic_mil))
                ivRightWarning5.visibility = View.VISIBLE
            }
            else{
                ivRightWarning5.visibility = View.INVISIBLE
            }
        }else{
            ivRightWarning5.visibility = View.INVISIBLE
        }
        
        d("UUVV", "tellTales.milState:${tellTales.milState} Mil icon:${tellTales.milIcon}")

        val motorArmed = tellTales.motorArmed == 1

        d("Faizuuuuu", "Telltales motorArmed:$motorArmed")

        motorState(motorArmed)
        val motorTemp = tellTales.motorTempIcon

        d("Faizuuuuu", "Telltales motorTemp:$motorTemp")

        if (motorTemp == 2) {
            ivRightWarning4.setImageDrawable(getDrawable(this, R.drawable.ic_motor_temp_orange))
            ivRightWarning4.visibility = View.VISIBLE

        } else if (motorTemp == 3) {
            ivRightWarning4.setImageDrawable(getDrawable(this, R.drawable.ic_motor_temp_red))
            ivRightWarning4.visibility = View.VISIBLE

        } else {
            ivRightWarning4.visibility = View.INVISIBLE
        }

        d(
            "Faizuuuuu",
            " Telltales BatteryError $batteryError batteryOverTemperature:$batteryOverTemperature"
        )

        if (batteryOverTemperature) {
            ivRightWarning3.setImageDrawable(
                getDrawable(
                    this,
                    R.drawable.ic_toolbar_battery_temperature
                )
            )
            ivRightWarning3.visibility = View.VISIBLE
        } else if (batteryError) {
            ivRightWarning3.setImageDrawable(
                getDrawable(
                    this,
                    R.drawable.ic_battery_error
                )
            )
           ivRightWarning3.visibility = View.VISIBLE
        } else {
            ivRightWarning3.visibility = View.INVISIBLE
        }

        d("Faizuuuuu", "Telltales critical malfunction:$criticalMalfunction")

        if (criticalMalfunction) {
            ivRightWarning1.visibility = View.VISIBLE
            ivRightWarning1.setImageDrawable(getDrawable(this, R.drawable.ic_warning_toolbar))
        } else {
            ivRightWarning1.visibility = View.INVISIBLE
        }
        d("Faizuuuuu", "Telltales otaPending:$otaPending")

        /* if (otaPending) {
             ivRightWarning1.setImageDrawable(getDrawable(this, R.drawable.ic_toolbar_settings))
             ivRightWarning1.visibility = View.VISIBLE
         } else {
             ivRightWarning1.visibility = View.INVISIBLE
         }*/


        val highBeam = tellTales.highBeam == 1
        d("Faizuuuuu", "Telltales High beam :$highBeam")

        if (highBeam) {
            ivLeftWarning2.setImageDrawable(getDrawable(this, R.drawable.ic_high_beam_toolbar))
            ivLeftWarning2.visibility = View.VISIBLE
        } else {
            ivLeftWarning2.visibility = View.INVISIBLE
        }
        val isHoverMode = tellTales.modeHover == 1
        //val isHoverMode=true
        isBallistic = tellTales.rideMode == 3
        d("UUVV", "Telltales modeHover :${tellTales.modeHover}  , isHoverMode:$isHoverMode ")
        // hoverModeFragment removed: ScreenMode.HoverMode now handled by ClusterViewModel → ClusterNavHost.


        val hillHold = tellTales.hillHold
        d("Faizuuuuu", "Telltales HillHold :$hillHold")

        updateHillHold(hillHold)

        val mtcState = tellTales.mtcState
        d("Faizuuuuu", "Telltales mtcState :$mtcState mtc mode:${tellTales.mtcMode}")

        handleMtcControl(mtcState, tellTales.mtcMode)


        val rideMode = tellTales.rideMode
        d("Faizuuuuu", "Telltales RideMode :$rideMode")
        handleRideMode(rideMode)

        val soc = tellTales.batterySoc
        d("Faizuuuuu", "Telltales RideMode :$soc")
        //handleSoc(soc)

        val regenLevel = if (tellTales.regenLevel >= 9) 9 else tellTales.regenLevel
        d("Faizuuuuu", "Telltales RegenLevel :${regenLevel}")

        d("Faizuuuuu", "Telltales Regen :${tellTales.regenUnavailable}")
        if (tellTales.regenUnavailable == 1) {
            ivRegen.visibility = View.VISIBLE
            tvRegen.visibility = View.INVISIBLE
        } else {
            handleRegen(regenLevel)
        }


        // chargeState / navigateChargingScreen removed: ChargingScreen is now driven
        // by ScreenMode.Charging via ClusterViewModel → ClusterNavHost.
        @Suppress("UNUSED_VARIABLE") val chargeState = tellTales.charger
        val absState = tellTales.absMode
        val absWarningLamp = tellTales.absWarningLamp

        d("Faizuuuuu", "Telltales absState :${absState} absWarning Lamp :$absWarningLamp")

        handleAbs(absMode = absState, absWarning = absWarningLamp)
        val hazardOn = tellTales.hazardLamps == 1
        d("Faizuuuuu", "Telltales hazard :$hazardOn")
        onHazardUpdate(hazardOn)
       val radarState = tellTales.radarIndicator
        d("RadarState", "Telltales radar :$radarState")
        handleRadarTelltales(radarState)
         // thermalRunaway now handled by ClusterViewModel → ScreenMode.ThermalRunaway → ClusterNavHost.
        // Fragment navigation for thermalRunawayFragment removed.

        val indicator=if(tellTales.indicatorRight==1)
        {
            1
        }else if(tellTales.indicatorLeft==1){
            2
        }else{
            0
        }
        onIndicatorUpdate(indicator)


    }

    private fun handleRegen(regenLevel: Int) {
        viewModel.saveRegenValue(regenLevel)
        ivRegen.visibility = View.INVISIBLE
        tvRegen.visibility = View.VISIBLE
        tvRegen.text = "R $regenLevel"
    }

    private fun handleSoc(soc: Int) {
        val finalSoc = soc.applyMinMax(viewModel.socLimit)
        tvBatteryPercent.text = "$finalSoc %"
        pbBattery.progress = finalSoc
    }

    private fun handleRideMode(rideMode: Int) {
        when (rideMode) {
            1 -> {
                ivMode.visibility = View.VISIBLE
                ivMode.setImageDrawable(getDrawable(this, R.drawable.ic_glide))
            }

            2 -> {
                ivMode.visibility = View.VISIBLE
                ivMode.setImageDrawable(getDrawable(this, R.drawable.ic_combat))

            }

            3 -> {
                ivMode.visibility = View.VISIBLE
                ivMode.setImageDrawable(getDrawable(this, R.drawable.ic_ballistic_mode_toolbar))
            }
        }
    }

    private fun handleAbs(absMode: Int, absWarning: Int) {
        ivAbsState.visibility = View.VISIBLE
        absWarningJob?.cancel()
        absWarningJob = null
	ivAbsState.alpha = 1f
        when (absWarning) {
            0 -> {
                showAbsMode(absMode)
            }

            1 -> {
                ivAbsState.setImageDrawable(getDrawable(this, R.drawable.ic_abs_malfunction))
            }

            2 -> {
                ivAbsState.setImageDrawable(getDrawable(this, R.drawable.ic_abs_malfunction))
                absWarningJob = blinkImage(ivAbsState)
		ivAbsState.alpha = 1f
            }
        }
    }


    private fun showAbsMode(absMode: Int) {
        when (absMode) {
            1 -> ivAbsState.setImageDrawable(getDrawable(this, R.drawable.abs_final))
            else -> ivAbsState.visibility = View.INVISIBLE
        }
    }

    // navigateChargingScreen removed: charging is now handled by
    // ScreenMode.Charging → ClusterNavHost → ChargingScreen (Compose).
    // ChargingFragment and R.id.chargingFragment have been removed from the nav graph.

    private fun handleMtcControl(mtcState: Int, mtcMode: Int) {
        ivTraction.visibility = View.VISIBLE
        when (mtcState) {
            0 -> {
                stopTractionBlinking()
                handleMtcMode(mtcMode)
            }

            1 -> {
                tractionBlinkJob?.cancel()
                ivTraction.setImageDrawable(getDrawable(this, R.drawable.ic_mtc))
                tractionBlinkJob = blinkImage(ivTraction)
                ivTraction.tag = R.drawable.ic_mtc
		ivTraction.alpha = 1f
            }
            4->{
                ivTraction.setImageDrawable(getDrawable(this, R.drawable.ic_mtc_malfunction))
                ivTraction.tag = R.drawable.ic_mtc_malfunction

            }

            2,3,5 -> {
                stopTractionBlinking()
                ivTraction.setImageDrawable(getDrawable(this, R.drawable.ic_mtc))
                ivTraction.tag = R.drawable.ic_mtc
            }

        }
    }

    private fun handleMtcMode(mtcMode: Int) {
        when (mtcMode) {
            1 -> {
                ivTraction.setImageDrawable(getDrawable(this, R.drawable.ic_mtc_malfunction))
                ivTraction.tag = R.drawable.ic_mtc_malfunction
            }

            2 -> {
                if (isDashboard) {
                    ivTraction.setImageDrawable(getDrawable(this, R.drawable.ic_mtc_1))
                    ivTraction.tag = R.drawable.ic_mtc_1
                } else {
                    ivTraction.setImageDrawable(getDrawable(this, R.drawable.ic_mtc_1_white))
                    ivTraction.tag = R.drawable.ic_mtc_1_white
                }
            }

            3 -> {
                if (isDashboard) {
                    ivTraction.setImageDrawable(getDrawable(this, R.drawable.ic_mtc_2))
                    ivTraction.tag = R.drawable.ic_mtc_2
                } else {
                    ivTraction.setImageDrawable(getDrawable(this, R.drawable.ic_mtc_2_white))
                    ivTraction.tag = R.drawable.ic_mtc_2_white
                }
            }

            4 -> {
                if (isDashboard) {
                    ivTraction.setImageDrawable(getDrawable(this, R.drawable.ic_mtc_3))
                    ivTraction.tag = R.drawable.ic_mtc_3
                } else {
                    ivTraction.setImageDrawable(getDrawable(this, R.drawable.ic_mtc_3_white))
                    ivTraction.tag = R.drawable.ic_mtc_3_white
                }
            }

        }
    }

    private fun stopTractionBlinking() {
        tractionBlinkJob?.cancel()
        tractionBlinkJob = null
	ivTraction.alpha = 1f
    }

    private fun handleHazardLamp(hazard: Boolean) {
        d("Faizuuuuuuu", "Hazard:$hazard")
        if (hazard) {
            ivRightIndicator.visibility = View.VISIBLE
            ivLeftIndicator.visibility = View.VISIBLE
            ivRightIndicator.setImageDrawable(
                getDrawable(
                    this,
                    R.drawable.ic_toolbar_right_indicator
                )
            )
            ivLeftIndicator.setImageDrawable(
                getDrawable(
                    this,
                    R.drawable.ic_toolbar_right_indicator
                )
            )
            blinkHazard(ivRightIndicator, ivLeftIndicator)
        } else {
            hazardAnimator?.cancel()
            hazardAnimator = null

            if (rightIndicatorBlinkJob?.isActive == true) {
                ivLeftIndicator.visibility = View.INVISIBLE
                return
            }
            if (leftIndicatorBlinkJob?.isActive == true) {
                ivLeftIndicator.visibility = View.INVISIBLE
                return
            }
        }
    }


    private fun updateHillHold(state: Int) {
        stopHillHoldBlinking()
        val isHillHoldOn=viewModel.isHillHold.value==true
        d("update_hillhold", "HillHold_state:$state")
        d("update_hillhold", "isHillHoldOn:$isHillHoldOn")
        when (state) {
            1 -> {
                
                ivHillHold.setImageDrawable(getDrawable(this, R.drawable.ic_hill_hold_malfunction))
                ivHillHold.tag = R.drawable.ic_hill_hold_malfunction
                 if (isHillHoldOn) {
                        ivHillHold.visibility = View.VISIBLE
                    }else{
                        ivHillHold.visibility = View.INVISIBLE
                    }
            }

            2 -> {
                
                if (isDashboard) {
                    ivHillHold.setImageDrawable(getDrawable(this, R.drawable.ic_hill_hold_on))
                    ivHillHold.tag = R.drawable.ic_hill_hold_on
                } else {
                    ivHillHold.setImageDrawable(getDrawable(this, R.drawable.ic_hill_hold_on_white))
                    ivHillHold.tag = R.drawable.ic_hill_hold_on_white
                }
                  if (isHillHoldOn) {
                        ivHillHold.visibility = View.VISIBLE
                    }else{
                        ivHillHold.visibility = View.INVISIBLE
                    }
            }

            3 -> {
               
                ivHillHold.setImageDrawable(getDrawable(this, R.drawable.ic_hill_hold_active))
                ivHillHold.tag = R.drawable.ic_hill_hold_active
                 if (isHillHoldOn) {
                        ivHillHold.visibility = View.VISIBLE
                    }else{
                        ivHillHold.visibility = View.INVISIBLE
                    }
            }

          4->{
                    ivHillHold.setImageDrawable(getDrawable(this, R.drawable.ic_hill_hold_active))
                    ivHillHold.tag = R.drawable.ic_hill_hold_active
                    if (isHillHoldOn) {
                        ivHillHold.visibility = View.VISIBLE
                    }else{
                        ivHillHold.visibility = View.INVISIBLE
                    }
                }

            else -> ivHillHold.visibility = View.INVISIBLE
        }
       
    }

    private fun startToolbarClock() {// rtc v1.4(rtc
        timeRunnable = object : Runnable {
            override fun run() {

                val is12Hour = viewModel.normalTimeFormat
                val pattern = if (is12Hour) "hh:mm a" else "HH:mm"

                val currentTime = SimpleDateFormat(pattern, Locale.getDefault())
                    .format(Date(System.currentTimeMillis()))

                tvTime.text = currentTime

                timeHandler.postDelayed(this, 1000)
            }
        }

        timeHandler.post(timeRunnable)
    }

    private fun is12HourFormat(): Boolean {//error rtc v1.4(rtc
        val prefs = getSharedPreferences("UV_CLUSTER_PREF", MODE_PRIVATE)
        return prefs.getBoolean("KEY_TIME_FORMAT_12H", true)
    }

    fun blinkImage(imageView: ImageView): Job {
        return this.lifecycleScope.launch {
            /*while (isActive) {
                imageView.alpha = 0f
                delay(400)
                imageView.alpha = 1f
                delay(400)
            }
            imageView.alpha = 1f*/
	    while (isActive) {
                imageView.alpha = 1f
                delay(400)
                imageView.alpha = 0f
                delay(400)
            }
            imageView.alpha = 1f
        }
    }

    // for bug no 29 changing delay 300 to 500 - pls check duration of blink for other telltales


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        d("MainActivityLifeCycle", "windowFocusChanged = $hasFocus")
    }


    fun loadVendorVehicleConfig(): String? {
        val file = File("/vendor/etc/vehicle_config.json")
        return try {
            if (file.exists() && file.canRead()) {
                file.readText(Charsets.UTF_8)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    fun isMobileDataEnabled(context: Context): Boolean {
        val tm = context.getSystemService(TelephonyManager::class.java)
        return try {
            val method = TelephonyManager::class.java.getDeclaredMethod("getDataEnabled")
            method.isAccessible = true
            method.invoke(tm) as Boolean
        } catch (e: Exception) {
            false
        }
    }

    fun runShellCommand(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec(command)
            val reader = process.inputStream.bufferedReader()
            val output = reader.readText()
            reader.close()
            output
        } catch (e: Exception) {
            e.message ?: "Error"
        }
    }

    private fun startVcuToggleLoop() {
        val CLUSTER_TO_VCU_GENERIC_ID = 0x21700FFF
        val INTERVAL_MS = 1 * 60 * 1000L // 2 Minutes

        lifecycleScope.launch {
            // Start with true to send 9 first
            var sendNineNext = true

            // loop while the activity is active
            while (isActive) {
                try {
                    // 1. Determine Data Value (9 or 4)
                    val dataVal: Byte = if (sendNineNext) 9 else 0

                    // 2. Create a simple 1-byte array containing the value
                    val packet = byteArrayOf(dataVal)

                    // 3. Send to VHAL
                    d(tag, "Sending VCU Packet (Value Only): $dataVal")
                    sendByteArray(CLUSTER_TO_VCU_GENERIC_ID, packet)

                    // 4. Toggle state for next run
                    sendNineNext = !sendNineNext

                } catch (e: Exception) {
                    d(tag, "Error sending VCU packet: ${e.message}")
                }

                // 5. Wait for 3 minutes before the next loop
                delay(INTERVAL_MS)
            }
        }
    }

    private fun sendClusterReady() {
    lifecycleScope.launch {
        d(tag, "Entering delay")
        delay(1000)

        // ✅ Send Cluster Ready only ONCE per key cycle
        if (!isClusterReady) {
            val CLUSTER_TO_VCU_CLUSTER_READY = 0x2170036F
            val dataVal: Byte = 1
            val packet = byteArrayOf(dataVal)
            d("Cluster_ready", "Sending CLUSTER READY to VCU")
            sendByteArray(CLUSTER_TO_VCU_CLUSTER_READY, packet)
	    sendVehicleInfo()
            isClusterReady = true
        } else {
            d("Cluster_ready", "Already sent, skipping")
        }

        // ✅ Safe to call every time — startHeartbeat guards itself
        carViewModel.startHeartbeat()
        startAlsJob()
    }
}

    fun setToolbarVisible(visible: Boolean) {
        findViewById<View>(R.id.clToolBar)?.visibility =
            if (visible) View.VISIBLE else View.GONE
    }

    fun getSystemProperty(key: String, defaultValue: String): String {
        return try {
            val systemProperties = Class.forName("android.os.SystemProperties")
            val getMethod =
                systemProperties.getMethod("get", String::class.java, String::class.java)
            getMethod.invoke(null, key, defaultValue) as String
        } catch (e: Exception) {
            e.printStackTrace()
            defaultValue
        }
    }

    private fun hideSystemBars() {
        window.setDecorFitsSystemWindows(false)

        window.insetsController?.apply {
            hide(
                WindowInsets.Type.statusBars() or
                        WindowInsets.Type.navigationBars()
            )

            systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun showTellTales() {
        if (viewModel.hasThemeConfigChanged) {
            d("ThemeChanging", "Theme is changed")
            initObserver()
            return
        }
        CoroutineScope(Dispatchers.Main).launch {
            tvRegen.visibility = View.INVISIBLE
            ivLeftIndicator.visibility = View.VISIBLE
            ivLeftWarning2.visibility = View.VISIBLE
            ivLeftWarning1.visibility = View.VISIBLE
            ivLeftWarning3.visibility = View.VISIBLE
            ivLeftWarning4.visibility = View.VISIBLE
            ivTraction.visibility = View.VISIBLE
            ivRegen.visibility = View.VISIBLE
            ivMode.visibility = View.VISIBLE
            ivMotorStatus.visibility = View.VISIBLE
            ivHillHold.visibility = View.VISIBLE
            ivAbsState.visibility = View.VISIBLE
            ivRightWarning1.visibility = View.VISIBLE
            ivRightWarning2.visibility = View.VISIBLE
            ivRightWarning3.visibility = View.VISIBLE
            ivRightWarning4.visibility = View.VISIBLE
            ivRightWarning5.visibility = View.VISIBLE
            ivRightIndicator.visibility = View.VISIBLE
            delay(2000)
            ivLeftIndicator.visibility = View.INVISIBLE
            ivRightIndicator.visibility = View.INVISIBLE
            ivLeftWarning4.visibility = View.INVISIBLE
            ivLeftWarning1.visibility = View.INVISIBLE
            ivRightWarning2.visibility = View.INVISIBLE
            initObserver()
            d("ThemeChanging", "Theme is not changed ")
        }
    }


    fun onHazardUpdate(value: Boolean) {
        hazardActive = value
        resolveAndRender()

    }

    fun onIndicatorUpdate(value: Int) {
        indicatorRaw = when (value) {
            1 -> IndicatorMode.Right
            2 -> IndicatorMode.Left
            else -> IndicatorMode.Off
        }
        resolveAndRender()
    }

    fun onhighBeamTellTaleUpdate(value: Int) {
        val highBeam = value == 1
        d("Faizuuuuu", "Telltales High beam :$highBeam")

        if (highBeam) {
            ivLeftWarning2.setImageDrawable(getDrawable(this, R.drawable.ic_high_beam_toolbar))
            ivLeftWarning2.visibility = View.VISIBLE
        } else {
            ivLeftWarning2.visibility = View.INVISIBLE
        }
    }

    fun onhazardLightTellTaleUpdate(value: Int ) {
        val hazardOn = value == 1
        d("Faizuuuuu", "Telltales hazard :$hazardOn")
        onHazardUpdate(hazardOn)
    }

    fun onmotorArmDisarmTellTaleUpdate(value: Int) {
        val motorArmed = value == 1
        d("Faizuuuuu", "Telltales motorArmed:$motorArmed")
        ivMotorStatus.visibility = View.VISIBLE
        if (motorArmed) {
            ivMotorStatus.setImageDrawable(getDrawable(this, R.drawable.ic_motor_on))
        } else {
            ivMotorStatus.setImageDrawable(getDrawable(this, R.drawable.ic_motor_off))
        }
    }

    private fun resolveAndRender() {
        val newMode = when {
            hazardActive -> IndicatorMode.Hazard
            indicatorRaw != IndicatorMode.Off -> indicatorRaw
            else -> IndicatorMode.Off
        }

        if (newMode != currentMode) {
            currentMode = newMode
            renderMode(newMode)
        }
    }

    private fun renderMode(mode: IndicatorMode) {
        d("Faizuuuu", "mode:$mode")

        stopIndicatorBlinking()
        hazardAnimator?.cancel()
        hazardAnimator = null

        ivLeftIndicator.alpha = 1f
        ivRightIndicator.alpha = 1f
        ivLeftIndicator.visibility = View.INVISIBLE
        ivRightIndicator.visibility = View.INVISIBLE

        when (mode) {

            IndicatorMode.Off -> {
            }

            IndicatorMode.Left -> {
                ivLeftIndicator.visibility = View.VISIBLE
                leftIndicatorBlinkJob = blinkImage(ivLeftIndicator)
            }

            IndicatorMode.Right -> {
                ivRightIndicator.visibility = View.VISIBLE
                rightIndicatorBlinkJob = blinkImage(ivRightIndicator)
            }

            IndicatorMode.Hazard -> {
                ivLeftIndicator.visibility = View.VISIBLE
                ivRightIndicator.visibility = View.VISIBLE
                blinkHazard(ivRightIndicator, ivLeftIndicator)
            }
        }
    }

    private fun checkStorageAndTriggerCleanup() {

    val stat = android.os.StatFs(android.os.Environment.getDataDirectory().path)

    val total = stat.totalBytes.toDouble()
    val used = total - stat.availableBytes
    val percent = (used / total) * 100

    android.util.Log.d("StorageCheck", "Used: $percent%")

    if (percent > 60) {

        val intent = android.content.Intent("com.example.database.ACTION_STORAGE_CLEANUP").apply {
            setPackage("com.example.database")
            putExtra("used_pct", percent)
        }

        try {
            //applicationContext.sendBroadcastAsUser(intent, UserHandle.ALL) // safer than sendBroadcastAsUser
            android.util.Log.d("StorageCheck", "✅ Broadcast sent")
        } catch (e: Exception) {
            android.util.Log.e("StorageCheck", "❌ Failed", e)
        }
    }
    }
 

    fun blinkHazard(right: ImageView, left: ImageView) {
        val rightAnim = ObjectAnimator.ofFloat(right, View.ALPHA, 1f, 0f).apply {
            duration = 300
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
        }

        val leftAnim = ObjectAnimator.ofFloat(left, View.ALPHA, 1f, 0f).apply {
            duration = 300
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
        }

        hazardAnimator = AnimatorSet().apply {
            playTogether(rightAnim, leftAnim)
            start()
        }
    }
}

enum class IndicatorMode {
    Off,
    Left,
    Right,
    Hazard
}



