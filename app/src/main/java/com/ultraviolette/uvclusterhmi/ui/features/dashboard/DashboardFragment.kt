package com.ultraviolette.uvclusterhmi.ui.features.dashboard

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.LevelListDrawable
import android.os.Bundle
import android.util.Log.d
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StyleRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.ultraviolette.uvclusterhmi.ClusterNotification
import com.ultraviolette.uvclusterhmi.NotificationManager
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.dataModel.RangeLimit
import com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData.TripMeterDisp
import com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData.VcuInfoMsg
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import com.ultraviolette.uvclusterhmi.ui.customWidget.AngleGaugeView
import com.ultraviolette.uvclusterhmi.ui.customWidget.DiagonalProgressView
import com.ultraviolette.uvclusterhmi.ui.features.MainActivity
import com.ultraviolette.uvclusterhmi.ui.viewModel.CarViewModel
import com.ultraviolette.uvclusterhmi.ui.viewModel.RadarState
import com.ultraviolette.uvclusterhmi.ui.viewModel.SharedViewModel
import com.ultraviolette.uvclusterhmi.utils.Utilities
import com.ultraviolette.uvclusterhmi.utils.Utilities.ARG_CHARGING_STATUS
import com.ultraviolette.uvclusterhmi.utils.Utilities.applyMinMax
import com.ultraviolette.uvclusterhmi.utils.Utilities.getRegenValueForLevel4
import com.ultraviolette.uvclusterhmi.utils.Utilities.toFloat
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.floor
import kotlin.math.roundToInt

class DashboardFragment : Fragment() {
    private lateinit var gestureDetector: GestureDetector
    private lateinit var tvOdoLabel: TextView
    private lateinit var tvPowerLabel: TextView
    private lateinit var tvRegenValue: TextView
    private lateinit var tvRideValue: TextView
    private lateinit var viewPower: View
    private lateinit var ivEfficiency: ImageView
    private lateinit var ivRegenLevel10: ImageView
    private lateinit var ivBgBottom: ImageView
    private lateinit var ivBgSides: ImageView
    private lateinit var pbPowerTopRight: DiagonalProgressView
    private lateinit var pbPowerTopLeft: DiagonalProgressView
    private lateinit var pbPowerBottomRight: DiagonalProgressView
    private lateinit var pbPowerBottomLeft: DiagonalProgressView
    private lateinit var tvRideLabel: TextView
    private lateinit var tvRangeLabel: TextView
    private lateinit var viewRange: View
    private lateinit var tvRangeUnit: TextView
    private lateinit var tvSpeed: TextView
    private lateinit var tvSpeedUnit: TextView
    private var speed = 0
    private lateinit var tvWhPerKm: TextView
    private lateinit var tvOdoValue: TextView
    private lateinit var tvOdoUnit: TextView
    private var simulationModeJob: Job? = null
    private lateinit var tvMode: TextView
    private lateinit var tvRec: TextView
    private lateinit var angleGauge: AngleGaugeView
    private lateinit var tvIncline: TextView
    private lateinit var tvRangeValue: TextView
    private lateinit var llRegenLevel4: LinearLayout
    private lateinit var ivRegen4Level1: ImageView
    private lateinit var ivRegen4Level2: ImageView
    private lateinit var ivRegen4Level3: ImageView
    private lateinit var tvInclineDegree: TextView
    private lateinit var tvEfficiencyLabel: TextView
    private lateinit var ivTemperature: ImageView
    private lateinit var tvTemperatureValue: TextView
    private lateinit var tvTemperatureUnit: TextView
    private lateinit var ivReset: ImageView
    private lateinit var tvRideUnit: TextView
    private lateinit var ivMtrArmed: ImageView
    private lateinit var ivCruiseEnabled: ImageView
    private lateinit var ivCruiseHighlight: ImageView

    private val carViewModel by activityViewModels<CarViewModel> { ViewModelFactory(context = requireContext()) }
    private val viewModel by activityViewModels<DashboardViewModel> { ViewModelFactory(context = requireContext()) }
    private val sharedViewModel by activityViewModels<SharedViewModel> { ViewModelFactory(context = requireContext()) }
    private lateinit var ivBgBottomRadarLeft: ImageView
    private lateinit var ivBgBottomRadarRight: ImageView
    private var unit = ""
    private lateinit var ivBallisticPlus: ImageView
    private var radarJob: Job? = null
    private var isMotorArmed = false
    private var isNegativePower=false
    var regenUnAvailable=false
    private var doubleTapCount = 0
    private var lastDoubleTapTime: Long = 0
    private val DOUBLE_TAP_WINDOW = 1000
    private val debugSequence = listOf(
        ButtonNavigation.Back.ordinal,
        ButtonNavigation.Right.ordinal,
        ButtonNavigation.Left.ordinal,
        ButtonNavigation.Bottom.ordinal,
        ButtonNavigation.Left.ordinal
    )
    private var sequenceStep = 0
    private var lastClickTime = 0L
    private val SEQUENCE_TIMEOUT = 2000L
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.fragment_dashboard, container, false)
        addSwipeGesture(rootView)
        return rootView
    }

    override fun onResume() {
        super.onResume()
        //initialUi()
        viewModel.getRegenValue()
    }

    /**
     * A GestureDetector that listens for fling gestures on the fragment's root view.
     *
     * It overrides the onFling method to detect an **upward swipe gesture**.
     * When the user performs a fast upward fling (with sufficient distance and velocity),
     * the custom [onSwipeUp] function is triggered.
     *
     * This helps to respond to upward swipe gestures in a more natural and efficient way
     * without manually calculating touch events every time.
     */
    private fun addSwipeGesture(rootView: View?) {
        gestureDetector = GestureDetector(
            requireContext(), object : GestureDetector.SimpleOnGestureListener() {
                override fun onFling(
                    e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float
                ): Boolean {
                    if (e1 == null) return false
                    val diffY = e2.y - e1.y
                    val diffX = e2.x - e1.x

                    if (abs(diffX) > abs(diffY)) {
                        if (abs(diffX) > 100 && abs(velocityX) > 100) {
                            if (diffX > 0) {
                                onSwipeRight()
                            } else {
                                onSwipeLeft()
                            }
                            return true
                        }
                    } else {
                        if (abs(diffY) > 100 && abs(velocityY) > 100) {
                            if (diffY < 0) {
                                onSwipeUp()
                            } else {
                                onSwipeDown()
                            }
                            return true
                        }
                    }
                    return false
                }

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    val w = (rootView?.width ?: resources.displayMetrics.widthPixels).toFloat()
                    val h = (rootView?.height ?: resources.displayMetrics.heightPixels).toFloat()
                    val currentTime = System.currentTimeMillis()

                    val isInBottomRight = e.x > w * 0.85f && e.y > h * 0.85f
                    d("Secret", "DoubleTap x=${e.x} y=${e.y} w=$w h=$h inCorner=$isInBottomRight")

                    if (isInBottomRight) {
                        if (currentTime - lastDoubleTapTime < DOUBLE_TAP_WINDOW) {
                            doubleTapCount++
                        } else {
                            doubleTapCount = 1
                        }
                        lastDoubleTapTime = currentTime

                        d("Secret", "Count=$doubleTapCount")

                        if (doubleTapCount >= 2) {
                            onSecretTriggered()
                            doubleTapCount = 0
                        }
                    } else {
                        doubleTapCount = 0
                    }

                    return true
                }
            })

        rootView?.setOnTouchListener { v, event ->
            val handled = gestureDetector.onTouchEvent(event)
            if (!handled && event.action == MotionEvent.ACTION_UP) {
                v.performClick()
            }
            true
        }
    }

    private fun onSecretTriggered() {
         findNavController()?.navigate(R.id.debugFragment)
    }

    private fun onSwipeUp() {


        if (speed <= 0 &&
            findNavController().currentDestination?.id == R.id.dashboardFragment
        ) {
            findNavController().navigate(
                R.id.action_dashboardFragment_to_menuFragment
            )
        }
    }

    private fun onSwipeDown() {
        return

        /* if (speed <= 0 &&
             findNavController().currentDestination?.id == R.id.dashboardFragment
         ) {
             findNavController().navigate(
                 R.id.action_dashboardFragment_to_controlSectionFragment
             )
         }*/

    }

    private fun onSwipeLeft() {
        //for map navigation
    }


    private fun onSwipeRight() {

        if (speed <= 0 &&
            findNavController().currentDestination?.id == R.id.dashboardFragment
        ) {
            findNavController().navigate(R.id.action_dashboardFragment_to_musicFragment)

        }


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initObserver()
        ivRegenLevel10.isVisible = viewModel.is10Levels
        llRegenLevel4.isVisible = !viewModel.is10Levels
        //ivBallisticPlus.isVisible = viewModel.isSurgeMode
        (activity as? MainActivity)?.handleToolbar(true)
        //initClickListener()
    }

    private fun initObserver() {
        d("DashboardFragment", "init observer called")
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    carViewModel.vehicleValue.collect { vehicleValue ->
                        d("L_VehicleValue", "speed: ${vehicleValue.joinToString()}")
                        updateVehicleValue(vehicleValue)
                    }
                }
                 launch {
                     carViewModel.rideModes.collect { rideModes ->
                         d("ridemode_evt", "rideModes:$rideModes")
                         /*carViewModel.tellTales.collect { tellTales ->
                             d("DashboardFragment", "rideModes:$rideModes")
                             *//*val isMotorArmed = tellTales.motorArmed == 1
                             viewModel.setMotorArmed(isMotorArmed)as
                             updateThemeMode(rideModes)*//*
                         }*/
                     }
                 }
                launch {
                    carViewModel.vcuInfoMsg.collect { vcuInfo ->
                        d("DashboardFragment", "vcuInfo:$vcuInfo")
                        updateVcuMsg(vcuInfo)
                    }
                }
                launch {
                    carViewModel.tripMeter.collect { tripDetails ->
                        d("DashboardFragment", "trip:$tripDetails")
                        updateTrip(tripDetails)
                    }
                }


                launch {
                    carViewModel.regen.collect { value ->
                        /*  if (value.size < 3) {
                              return@collect
                          }
                          val regenValue = value[1]
                          viewModel.setRegenValue(regenValue)*/
                    }
                }
                launch {
                    carViewModel.swiftButton.collect { swiftButton ->
                        val button = Utilities.getButtonState(swiftButton)
                        if (button == ButtonNavigation.None) return@collect
                        handleButtonNavigation(button.ordinal)
                    }
                }

                launch {
                    carViewModel.leftRadarState.collect { state ->
                        if (!sharedViewModel.isConsoleAlertsOn) return@collect
                        when (state) {
                            RadarState.Alert -> {
                                ivBgBottomRadarLeft.visibility = View.VISIBLE
                                ivBgBottomRadarLeft.setImageDrawable(
                                    ContextCompat.getDrawable(
                                        requireContext(),
                                        R.drawable.bg_radar_left_alert
                                    )
                                )
                            }

                            RadarState.Warn -> {
                                ivBgBottomRadarLeft.visibility = View.VISIBLE
                                ivBgBottomRadarLeft.setImageDrawable(
                                    ContextCompat.getDrawable(
                                        requireContext(),
                                        R.drawable.bg_dashboard_radar_left
                                    )
                                )
                            }

                            RadarState.Off -> {
                                ivBgBottomRadarLeft.visibility = View.INVISIBLE
                            }
                        }
                    }
                }

                launch {
                    carViewModel.rightRadarState.collect { state ->
                        if (!sharedViewModel.isConsoleAlertsOn) return@collect
                        when (state) {
                            RadarState.Alert -> {
                                ivBgBottomRadarRight.visibility = View.VISIBLE
                                ivBgBottomRadarRight.setImageDrawable(
                                    ContextCompat.getDrawable(
                                        requireContext(),
                                        R.drawable.bg_dashboard_radar_right_alert
                                    )
                                )
                            }

                            RadarState.Warn -> {
                                ivBgBottomRadarRight.visibility = View.VISIBLE
                                ivBgBottomRadarRight.setImageDrawable(
                                    ContextCompat.getDrawable(
                                        requireContext(),
                                        R.drawable.bg_dashboard_radar_right
                                    )
                                )
                            }

                            RadarState.Off -> {
                                ivBgBottomRadarRight.visibility = View.INVISIBLE
                            }
                        }

                    }
                }

               launch {
                    carViewModel.rcwRadarState.collect {rcwState->
                        if (!sharedViewModel.isConsoleAlertsOn) return@collect
                        if(rcwState) {
                            ivBgBottomRadarRight.setImageDrawable(
                                ContextCompat.getDrawable(
                                    requireContext(),
                                    R.drawable.bg_dashboard_radar_right_alert
                                )
                            )
                            
                            ivBgBottomRadarLeft.setImageDrawable(
                                ContextCompat.getDrawable(
                                    requireContext(),
                                    R.drawable.bg_radar_left_alert
                                )
                            )
                             ivBgBottomRadarRight.visibility = View.VISIBLE
                             ivBgBottomRadarLeft.visibility = View.VISIBLE
                        }
                        else{
                            ivBgBottomRadarRight.visibility = View.INVISIBLE
                            ivBgBottomRadarLeft.visibility = View.INVISIBLE
                        }
                    }
                }

                launch {
                    carViewModel.ballisticPlus.collect { isSurgeMode ->
                        //ivBallisticPlus.visibility = if (isSurgeMode && viewModel.isBallistic) View.VISIBLE else View.INVISIBLE

                        viewModel.ballisticPlus(isSurgeMode)
                        d("DashboardFragment", "surgeMode:$isSurgeMode")
                    }
                }
                launch {
                    carViewModel.motorArmDisarmTellTale.collect { motorArmDisarmTellTale ->
                        d("DashboardFragment", "motorArmDisarmTellTale: $motorArmDisarmTellTale")
                         isMotorArmed=motorArmDisarmTellTale==1
                        viewModel.setMotorArmed(isMotorArmed)
                        if (isMotorArmed) {
                            if (speed == 0) {
                                ivMtrArmed.visibility = View.VISIBLE
                            } else {
                                ivMtrArmed.visibility = View.INVISIBLE
                            }

                            val typedValue = TypedValue()
                            requireContext().theme.resolveAttribute(
                                R.attr.appTextColor,
                                typedValue,
                                true
                            )
                            if (tvSpeed.text == "---") tvSpeed.text = "000"
                            tvSpeed.setTextColor(typedValue.data)
                        } else {
                            ivMtrArmed.visibility = View.INVISIBLE
                            tvSpeed.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.lightGreyMedium
                                )
                            )
                            if (speed == 0) {
                                tvSpeed.text = "---"
                            }
                        }

                    }
                }
                launch {
                    carViewModel.tellTales.collect {
                        val hoverMode = it.modeHover == 1
                        d("DashboardFragment", "hoverMode:$hoverMode")
                        d("L_telltales_speed", "speed telltales:${it.vehicleSpeed}")
                        d("DashboardFragment", "armed:$it.motorArmed")
                        isMotorArmed = it.motorArmed == 1
                        if (isMotorArmed) {
                            if (speed == 0) {
                                ivMtrArmed.visibility = View.VISIBLE
                            } else {
                                ivMtrArmed.visibility = View.INVISIBLE
                            }

                            val typedValue = TypedValue()
                            requireContext().theme.resolveAttribute(
                                R.attr.appTextColor,
                                typedValue,
                                true
                            )
                            if (tvSpeed.text == "---") tvSpeed.text = "000"
                            tvSpeed.setTextColor(typedValue.data)
                        } else {
                            ivMtrArmed.visibility = View.INVISIBLE
                            tvSpeed.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.lightGreyMedium
                                )
                            )
                            if (it.vehicleSpeed == 0) {
                                tvSpeed.text = "---"
                            }
                        }
                        viewModel.setMotorArmed(isMotorArmed)
                        val rideModes = it.rideMode
                        val isBallistic = it.rideMode == 3
                        ivBallisticPlus.visibility = if (viewModel.isSurgeMode && isBallistic) View.VISIBLE else View.INVISIBLE
                        d("DashboardFragment", "isBallisticPlus: ${viewModel.isSurgeMode && isBallistic}")
                        updateThemeMode(rideModes)
                        val regenValue = it.regenLevel.applyMinMax(RangeLimit(0, 9))
                        viewModel.setRegenValue(regenValue)
                        d("DashboardFragment", "regenValue:$regenValue")
                        regenUnAvailable = if (it.regenUnavailable==1) {
                            true
                        } else{
                            false
                        }
                        viewModel.setRegenUnAvailable(regenUnAvailable)
                        if (regenUnAvailable)
                        {
                            viewModel.setRegenValue(0)
                        }else{
                            viewModel.setRegenValue(regenValue)
                         }
                    }
                }
               launch {
                    viewModel.uiState.collect { uiState ->
                        updateUi(uiState)
                    }
                }
               launch {
                   carViewModel.ccActive.collect { value ->
                       d("DashboardFragment", "Cruise cc active: $value")
                       if (value) {
                           ivCruiseEnabled.visibility = View.VISIBLE
                           ivCruiseHighlight.visibility = View.VISIBLE
                       }
                       else
                       {
                           ivCruiseEnabled.visibility = View.INVISIBLE
                           ivCruiseHighlight.visibility = View.INVISIBLE
                       }

                   }
               }
            }
        }
    }
    fun getStep(is10Levels: Boolean) = if (is10Levels) 1 else 3

    fun normalize(value: Int, is10Levels: Boolean): Int {
        return if (is10Levels) value else (value / 3) * 3
    }
    private fun handleButtonNavigation(button: Int) {

	val currentTime = System.currentTimeMillis()

        // 1. Check for Timeout: If too much time passed, reset the sequence progress
        if (currentTime - lastClickTime > SEQUENCE_TIMEOUT) {
            sequenceStep = 0
        }

        // 2. Sequence Logic
        if (button == debugSequence[sequenceStep]) {
            lastClickTime = currentTime
            sequenceStep++

            if (sequenceStep == debugSequence.size) {
                sequenceStep = 0
                findNavController().navigate(R.id.debugFragment)
                return
            }

            // While correctly entering the sequence, we block normal button behavior
            return
        } else {
            // Button didn't match the sequence: Reset sequence and continue to normal behavior
            // Check if this "wrong" button is actually the start of a new sequence attempt
            sequenceStep = if (button == debugSequence[0]) {
                lastClickTime = currentTime
                1
            } else {
                0
            }

            // If we reset to 0, we do NOT return, so the 'when' block below executes
            if (sequenceStep == 0) {
                // Fall through to normal behavior
            } else {
                // It was the start of a new sequence, block normal behavior
                return
            }
        }

        when (button) {
            ButtonNavigation.Top.ordinal -> {
                if (speed > 0) return
                //findNavController().navigate(R.id.action_dashboardFragment_to_controlSectionFragment)
            }


            ButtonNavigation.Left.ordinal -> {
                val step = getStep(viewModel.is10Levels)

                var current = sharedViewModel.regenValue
                current = normalize(current, viewModel.is10Levels)

                val regenValue = (current - step).coerceAtLeast(0)

                sharedViewModel.saveRegenValue(regenValue)

                val packet = byteArrayOf(regenValue.toByte())
                d("REGEN_VALUE", "LEFT -> $packet")

                carViewModel.sendByteArrayProperty(0x2170039F, packet)
            }

            ButtonNavigation.Right.ordinal -> {
                val step = getStep(viewModel.is10Levels)

                var current = sharedViewModel.regenValue
                current = normalize(current, viewModel.is10Levels)

                val regenValue = (current + step).coerceAtMost(9)

                sharedViewModel.saveRegenValue(regenValue)

                val packet = byteArrayOf(regenValue.toByte())
                d("REGEN_VALUE", "RIGHT -> $packet")

                carViewModel.sendByteArrayProperty(0x2170039F, packet)
            }

            ButtonNavigation.Bottom.ordinal -> {
                if (speed > 0) return
                findNavController().navigate(R.id.action_dashboardFragment_to_menuFragment)
            }
           ButtonNavigation.Enter.ordinal -> {
                /*NotificationManager.show(
                    ClusterNotification.Params(
                        "Cruise Control Unavailable",
                        ""
                    )
                )*/
            }

        }
    }

    private fun updateThemeMode(rideModes: Int?) {
        val themeMode = when (rideModes) {
            1 -> R.style.Theme_Glide
            2 -> R.style.Theme_Combat
            else -> R.style.Theme_Ballistic
        }
        viewModel.setThemeMode(themeMode)
    }

    private fun updatePowerColor(themeMode: Int) {
        val color = if (isNegativePower) {
            getColorAttr(R.attr.appTextColor, themeMode)
        } else {
            getColorAttr(R.attr.modeColor, themeMode)
        }

        pbPowerTopLeft.setModeColor(color)
        pbPowerTopRight.setModeColor(color)
        pbPowerBottomLeft.setModeColor(color)
        pbPowerBottomRight.setModeColor(color)
    }

    private fun updateVehicleValue(value: FloatArray = floatArrayOf()) {
        d("update speed", "speed")
        tvSpeedUnit.text = if (unit == "miles") "mph" else "km/h"
        tvPowerLabel.text = if (unit == "miles") "Wh/mile" else "Wh/km"
        d("VehicleValue", "value:$value")
	d("VehicleValue", "speed:$value[0]")
        if (value.isEmpty()) return

        val rawPower = value.getOrNull(1) ?: 0f
        val scaledPower = rawPower / 1000f
        val currentTheme = viewModel.uiState.value.themeMode
        isNegativePower = scaledPower < 0
        val powerMagnitude = abs(scaledPower)
        val powerLevel = (powerMagnitude / 10f).coerceIn(0f, 1f)
        pbPowerBottomLeft.progress = powerLevel
        pbPowerBottomRight.progress = powerLevel
        pbPowerTopLeft.progress = powerLevel
        pbPowerTopRight.progress = powerLevel

        updatePowerColor(currentTheme)
        val rawSpeedKm = value.getOrNull(0)?.toInt()
        val finalSpeedKm =
            rawSpeedKm?.applyMinMax(sharedViewModel.speedLimit) ?: 0
        speed = finalSpeedKm
        d("update speed", "speed: $speed")
        val displaySpeed =
            if (unit == "miles")
                (finalSpeedKm * 0.621371).roundToInt()
            else
                finalSpeedKm
        if(!isMotorArmed && speed==0 )
            tvSpeed.text = "---"
        else
            tvSpeed.text = String.format("%03d", displaySpeed)
        if (isMotorArmed && speed==0)
        {
            ivMtrArmed.visibility = View.VISIBLE
        }
        else{
            ivMtrArmed.visibility = View.INVISIBLE
        }


        val power = value.getOrNull(1) ?: 0f
        val progress = minOf(1.0f, abs(power) / 10f)
        viewModel.setPowerValue(progress)

        val rawWhPerKm = value.getOrNull(2)?.toInt() ?: 0
        d("whPerKm", "whPerKm: $rawWhPerKm")
        val finalWhPerKm =
            rawWhPerKm.applyMinMax(sharedViewModel.whPerKmLimit)
        val effTemp = when {
            rawWhPerKm >= 90 -> 1
            rawWhPerKm <= 35 -> 9
            else -> 8 - floor(((rawWhPerKm - 36.0) / 54.0) * 7.0).toInt()
        }

        viewModel.setEfficiencyValue(effTemp)

        val displayWh =
            if (unit == "miles") {

                (finalWhPerKm / 0.621371).roundToInt()
            } else {
                finalWhPerKm
            }
        tvWhPerKm.text =
            displayWh.let { String.format("%03d", it) } ?: "-"
    }


    /**
     * Binds UI components from the provided root view using their IDs.
     *
     * @param view The root view containing the layout elements.
     *
     * Initializes:
     * -
     * -
     *
     */
    private fun initViews(view: View) {
        tvOdoLabel = view.findViewById(R.id.tvOdoLabel)
        tvPowerLabel = view.findViewById(R.id.tvPowerLabel)
        viewPower = view.findViewById(R.id.viewPower)
        ivEfficiency = view.findViewById(R.id.ivEfficiency)
        ivRegenLevel10 = view.findViewById(R.id.ivRegenLevel10)
        ivBgBottom = view.findViewById(R.id.ivBgBottom)
        pbPowerTopRight = view.findViewById(R.id.pbPowerTopLeft)
        pbPowerTopLeft = view.findViewById(R.id.pbPowerTopRight)
        pbPowerBottomRight = view.findViewById(R.id.pbPowerBottomRight)
        pbPowerBottomLeft = view.findViewById(R.id.pbPowerBottomLeft)
        tvRideLabel = view.findViewById(R.id.tvRideLabel)
        tvRangeLabel = view.findViewById(R.id.tvRangeLabel)
        viewRange = view.findViewById(R.id.viewRange)
        tvSpeed = view.findViewById(R.id.tvSpeed)
        tvWhPerKm = view.findViewById(R.id.tvWhPerKm)
        tvMode = view.findViewById(R.id.tvMode)
        tvRec = view.findViewById(R.id.tvRec)
        angleGauge = view.findViewById(R.id.angleGauge)
        tvIncline = view.findViewById(R.id.tvIncline)
        tvOdoValue = view.findViewById(R.id.tvOdoValue)
        tvRegenValue = view.findViewById(R.id.tvRegenValue)
        tvRideValue = view.findViewById(R.id.tvRideValue)
        tvRangeValue = view.findViewById(R.id.tvRangeValue)
        llRegenLevel4 = view.findViewById(R.id.llRegenLevel4)
        ivRegen4Level1 = view.findViewById(R.id.ivRegen4Level1)
        ivRegen4Level2 = view.findViewById(R.id.ivRegen4Level2)
        ivRegen4Level3 = view.findViewById(R.id.ivRegen4Level3)
        ivBgSides = view.findViewById(R.id.ivBgSides)
        ivTemperature = view.findViewById(R.id.ivTemperature)
        tvEfficiencyLabel = view.findViewById(R.id.tvEfficiencyLabel)
        tvInclineDegree = view.findViewById(R.id.tvInclineDegree)
        tvTemperatureValue = view.findViewById(R.id.tvTemperatureValue)
        tvTemperatureUnit = view.findViewById(R.id.tvTemperatureUnit)
        ivReset = view.findViewById(R.id.ivReset)
        ivBgBottomRadarLeft = view.findViewById(R.id.ivBgBottomLeftRadar)
        ivBgBottomRadarRight = view.findViewById(R.id.ivBgBottomRightRadar)
        tvOdoUnit = view.findViewById(R.id.tvOdoUnit)
        tvRangeUnit = view.findViewById(R.id.tvRangeUnit)
        tvRideUnit = view.findViewById(R.id.tvRideUnit)
        tvSpeedUnit = view.findViewById(R.id.tvSpeedUnit)
        ivBallisticPlus = view.findViewById(R.id.ivBallisticPlus)
        ivMtrArmed = view.findViewById(R.id.ivMtrArmed)
        ivCruiseEnabled = view.findViewById(R.id.ivCruiseEnabled)
        ivCruiseHighlight = view.findViewById(R.id.ivCruiseHighlight)
        unit = sharedViewModel.distanceUnit
        tvWhPerKm.setOnClickListener {
            findNavController().navigate(R.id.hoverModeFragment)
        }
    }

    private fun initClickListener() {
        tvWhPerKm.setOnClickListener {
            stimulateRadarIndication()
        }

        tvOdoLabel.setOnClickListener {
            stopRadarIndication()
        }

        ivReset.setOnClickListener {
            findNavController().navigate(R.id.thermalRunawayFragment)
        }

        tvOdoValue.setOnClickListener {
            val bundle = Bundle()
            bundle.putBoolean(ARG_CHARGING_STATUS, false)
            findNavController().navigate(R.id.hoverModeFragment, bundle)

        }
        tvSpeedUnit.setOnClickListener {
            findNavController().navigate(R.id.hoverModeFragment)
        }


        ivTemperature.setOnClickListener {
            val bundle = Bundle()
            bundle.putBoolean(ARG_CHARGING_STATUS, true)
            findNavController().navigate(R.id.hoverModeFragment, bundle)
        }
    }


    fun updateVcuMsg(vcuInfoMsg: VcuInfoMsg) {
        // Read unit synchronously
        // "Km" or "Miles"

        // Raw values from VCU (always KM)
        val rawOdometerKm = vcuInfoMsg.odometer.toInt()
        val rawRangeKm = vcuInfoMsg.range.toInt()
        val efficiency = vcuInfoMsg.whPerKm
        val effTemp = when {
            efficiency >= 90f -> 1
            efficiency <= 35f -> 9
            else -> {
                val ratio = (efficiency - 36f) / 54f
                val scaled = ratio * 7f
                8 - floor(scaled).toInt()
            }
        }
        d("Value", "Efficiency value :$effTemp originalValue :${efficiency}")
        viewModel.setEfficiencyValue(effTemp)

        // Apply limits in KM
        val finalOdoKm = rawOdometerKm.applyMinMax(sharedViewModel.odoLimit)
        val finalRangeKm = rawRangeKm.applyMinMax(sharedViewModel.rangeLimit)

        // Convert ONLY for display
        val displayOdo =
            if (unit == "miles") {
                (finalOdoKm * 0.621371).roundToInt()
            } else
                finalOdoKm

        val displayRange =
            if (unit == "miles")
                (finalRangeKm * 0.621371).roundToInt()
            else
                finalRangeKm

        // Update UI
        tvOdoValue.text = displayOdo.toString()
        tvRangeValue.text = displayRange.toString()
        tvOdoUnit.text = unit
        tvRangeUnit.text = unit
        tvIncline.text = vcuInfoMsg.roll.absoluteValue.toInt().toString()
        angleGauge.progress = vcuInfoMsg.roll / 90f
        val distance = vcuInfoMsg.distance
        if (distance.isNotEmpty()) {
            val ride = distance.toFloat().toInt()
            val finalRide = ride.applyMinMax(sharedViewModel.rideLimit)
            d("Rideeeeeee", "Original ride :${vcuInfoMsg.distance}")
            d("Rideeeeeee", "ride:$ride")
            if (unit == "miles") {
                tvRideValue.text = (finalRide * 0.621371).roundToInt().toString()
            } else {
                tvRideValue.text = finalRide.toString()
            }
        }
	if (vcuInfoMsg.speed.isNotEmpty()) {
		if (vcuInfoMsg.speed[0].toInt() == 0)
        	{
	    		speed = 0
	    		if (isMotorArmed)
	    	{
			tvSpeed.text = "000"
            	}
            	else
            	{
                	tvSpeed.text = "---"
            	}
		}
	}
    }


    fun updateTrip(tripMeter: TripMeterDisp) {
        // val unit = sharedViewModel.DistanceUnit
        tvRideUnit.text = unit
        if (tripMeter.trip.size <= 2) return
        val rawRide = tripMeter.trip[0].distance.toInt()
        d("DashboardFragment", "updateTrip: $tripMeter")
        val finalRide = rawRide.applyMinMax(sharedViewModel.rideLimit)
        if (unit == "miles") {
            //  tvRideValue.text = (finalRide * 0.621371).roundToInt().toString()
        } else {

            //tvRideValue.text = finalRide.toString()
        }

    }

    fun handleHoverMode(hoverMode: Boolean) {
        pbPowerBottomLeft.visibility = if (hoverMode) View.INVISIBLE else View.VISIBLE
        pbPowerBottomRight.visibility = if (hoverMode) View.INVISIBLE else View.VISIBLE
        pbPowerTopLeft.visibility = if (hoverMode) View.INVISIBLE else View.VISIBLE
        pbPowerTopRight.visibility = if (hoverMode) View.INVISIBLE else View.VISIBLE
        ivTemperature.visibility = if (hoverMode) View.INVISIBLE else View.VISIBLE
        llRegenLevel4.visibility = if (hoverMode) View.INVISIBLE else {
            if (!viewModel.is10Levels) View.VISIBLE else View.INVISIBLE
        }
        ivRegenLevel10.visibility == if (hoverMode) View.INVISIBLE else {
            if (viewModel.is10Levels) View.VISIBLE else View.INVISIBLE
        }
        ivEfficiency.visibility = if (hoverMode) View.INVISIBLE else View.VISIBLE
        tvEfficiencyLabel.visibility = if (hoverMode) View.INVISIBLE else View.VISIBLE
        angleGauge.visibility = if (hoverMode) View.INVISIBLE else View.VISIBLE
        tvIncline.visibility = if (hoverMode) View.INVISIBLE else View.VISIBLE
        tvTemperatureValue.visibility = if (hoverMode) View.INVISIBLE else View.VISIBLE
        tvInclineDegree.visibility = if (hoverMode) View.INVISIBLE else View.VISIBLE
        tvTemperatureUnit.visibility = if (hoverMode) View.INVISIBLE else View.VISIBLE
        tvRegenValue.visibility = if (hoverMode) View.INVISIBLE else View.VISIBLE
    }


    override fun onDestroyView() {
        super.onDestroyView()
        simulationModeJob?.cancel()
    }

    fun updateUi(uiState: UiState) {
        val modeColor = getColorAttr(R.attr.modeColor, uiState.themeMode)
        updateRegenLevel(uiState, modeColor)
        updateEfficiencyLevel(uiState, modeColor)
        tvPowerLabel.setTextColor(modeColor)
        tvOdoLabel.setTextColor(modeColor)
        tvRideLabel.setTextColor(modeColor)
        tvRangeLabel.setTextColor(modeColor)
      /*  pbPowerTopLeft.setModeColor(modeColor)
        pbPowerTopRight.setModeColor(modeColor)
        pbPowerBottomLeft.setModeColor(modeColor)
        pbPowerBottomRight.setModeColor(modeColor)*/
/*

        pbPowerTopLeft.progress = uiState.power
        pbPowerTopRight.progress = uiState.power
        pbPowerBottomLeft.progress = uiState.power
        pbPowerBottomRight.progress = uiState.power

*/
        updatePowerColor(uiState.themeMode)


        /* val efficiencyDrawable = if (uiState.isMotorArmed) {
             AppCompatResources.getDrawable(
                 ContextThemeWrapper(
                     requireContext(),
                     uiState.themeMode
                 ), R.drawable.regen_level_list
             )?.mutate()
         } else {
             AppCompatResources.getDrawable(
                 ContextThemeWrapper(
                     requireContext(),
                     R.style.Theme_MotorArmed
                 ), R.drawable.regen_level_list
             )?.mutate()
         }

         ivEfficiency.setImageDrawable(efficiencyDrawable)*/

        val wrapper = ContextThemeWrapper(requireContext(), uiState.themeMode)
        if (uiState.isMotorArmed) {
            val bgBottomVector = AppCompatResources.getDrawable(wrapper, R.drawable.bg_bottom)
            ivBgBottom.setImageDrawable(bgBottomVector)
        } else {
            ivBgBottom.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.bg_bottom_grey
                )
            )
        }
        val newBackground = AppCompatResources.getDrawable(wrapper, R.drawable.bg_rounded)
        viewPower.background = newBackground
        viewRange.background = newBackground
        val drawable = AppCompatResources.getDrawable(wrapper, R.drawable.bg_side)
        ivBgSides.setImageDrawable(drawable)
        when (uiState.themeMode) {
            R.style.Theme_Ballistic -> {
                tvMode.text = getString(R.string.ballistic)
                tvRec.text = getString(R.string.rec_60)
            }

            R.style.Theme_Combat -> {
                tvMode.text = getString(R.string.combat)
                tvRec.text = getString(R.string.rec_52)
            }

            R.style.Theme_Glide -> {
                tvMode.text = getString(R.string.glide)
                tvRec.text = getString(R.string.rec_40)
            }
        }
    }

    /**
     * Retrieves a color value defined in a given theme attribute.
     *
     *
     * @param attr The attribute ID (e.g., R.attr.modeTextColor) to resolve.
     * @param themeRes The style resource to apply as a theme.
     * @return The resolved color as an [Int], or [android.graphics.Color.BLACK] if the attribute is not found.
     */
    fun getColorAttr(attr: Int, @StyleRes themeRes: Int): Int {
        val wrapper = ContextThemeWrapper(requireContext(), themeRes)
        val typedValue = TypedValue()
        return if (wrapper.theme.resolveAttribute(attr, typedValue, true)) {
            ContextCompat.getColor(requireContext(), typedValue.resourceId)
        } else Color.BLACK
    }

    @SuppressLint("ResourceAsColor")
    fun updateRegenLevel(uiState: UiState, modeColor: Int) {
        if (regenUnAvailable){
            viewModel.setRegenValue(0)
            if (viewModel.is10Levels){
              val regenDrawable= AppCompatResources.getDrawable(
                    ContextThemeWrapper(
                        requireContext(),
                        R.style.Theme_MotorArmed
                    ), R.drawable.regen_level_list
                )?.mutate()
              if (regenDrawable is LevelListDrawable) {
                    regenDrawable.level = 0
                }
                ivRegenLevel10.setImageDrawable(regenDrawable)
            }else{

                ivRegen4Level1.setImageResource(R.drawable.ic_regen_4)
                ivRegen4Level2.setImageResource(R.drawable.ic_regen_4)
                ivRegen4Level3.setImageResource(R.drawable.ic_regen_4)

            }
            tvRegenValue.text="R0"

        }else {

            tvRegenValue.text = buildString {
                append("R")
                append(uiState.regenValue)
            }
            val wrapper = ContextThemeWrapper(
                requireContext(),
                uiState.themeMode
            )
            if (viewModel.is10Levels) {
                val regenDrawable = if (uiState.isMotorArmed) {
                    AppCompatResources.getDrawable(
                        ContextThemeWrapper(
                            requireContext(),
                            uiState.themeMode
                        ), R.drawable.regen_level_list
                    )?.mutate()
                } else {
                    AppCompatResources.getDrawable(
                        ContextThemeWrapper(
                            requireContext(),
                            R.style.Theme_MotorArmed
                        ), R.drawable.regen_level_list
                    )?.mutate()
                }

                if (regenDrawable is LevelListDrawable) {
                    regenDrawable.level = uiState.regenValue
                }
                ivRegenLevel10.setImageDrawable(regenDrawable)
            } else {
                val regenLevel4 = getRegenValueForLevel4(uiState.regenValue)
                val defaultIcon1 =
                    AppCompatResources.getDrawable(wrapper, R.drawable.ic_regen_4)?.mutate()
                val defaultIcon2 =
                    AppCompatResources.getDrawable(wrapper, R.drawable.ic_regen_4)?.mutate()
                val defaultIcon3 =
                    AppCompatResources.getDrawable(wrapper, R.drawable.ic_regen_4)?.mutate()
                ivRegen4Level1.setImageDrawable(defaultIcon1)
                ivRegen4Level2.setImageDrawable(defaultIcon2)
                ivRegen4Level3.setImageDrawable(defaultIcon3)
                when (regenLevel4) {
                    3 -> {
                        if (uiState.isMotorArmed) {
                            ivRegen4Level1.setImageDrawable(getTintedRegenIcon(wrapper, modeColor))
                        } else {
                            ivRegen4Level1.setImageDrawable(
                                getGreyRegenIcon(requireContext())
                            )
                        }
                    }

                    6 -> {
                        if (uiState.isMotorArmed) {
                            val tinted = getTintedRegenIcon(wrapper, modeColor)
                            ivRegen4Level1.setImageDrawable(tinted)
                            ivRegen4Level2.setImageDrawable(tinted)
                        } else {
                            val grey = getGreyRegenIcon(requireContext())
                            ivRegen4Level1.setImageDrawable(grey)
                            ivRegen4Level2.setImageDrawable(grey)
                        }
                    }

                    9 -> {
                        if (uiState.isMotorArmed) {
                            val tinted = getTintedRegenIcon(wrapper, modeColor)
                            ivRegen4Level1.setImageDrawable(tinted)
                            ivRegen4Level2.setImageDrawable(tinted)
                            ivRegen4Level3.setImageDrawable(tinted)
                        } else {
                            val grey = getGreyRegenIcon(requireContext())
                            ivRegen4Level1.setImageDrawable(grey)
                            ivRegen4Level2.setImageDrawable(grey)
                            ivRegen4Level3.setImageDrawable(grey)
                        }
                    }
                }
            }
        }
    }

    private fun updateEfficiencyLevel(uiState: UiState, modeColor: Int) {
        val wrapper = ContextThemeWrapper(
            requireContext(),
            uiState.themeMode
        )
        val regenDrawable = if (uiState.isMotorArmed) {
            AppCompatResources.getDrawable(
                ContextThemeWrapper(
                    requireContext(),
                    uiState.themeMode
                ), R.drawable.efficiency_level_list
            )?.mutate()
        } else {
            AppCompatResources.getDrawable(
                ContextThemeWrapper(
                    requireContext(),
                    R.style.Theme_MotorArmed
                ), R.drawable.efficiency_level_list
            )?.mutate()
        }

        if (regenDrawable is LevelListDrawable) {
            regenDrawable.level = uiState.efficiency
        }
        ivEfficiency.setImageDrawable(regenDrawable)
    }

   /* fun getGreyRegenIcon(context: Context): Drawable? {
        return AppCompatResources
            .getDrawable(context, R.drawable.ic_regen_4)
            ?.mutate()
            ?.apply {
                setTint(
                    ContextCompat.getColor(context, R.color.levelGreyLight)
                )
            }
    }*/


   fun getGreyRegenIcon(context: Context): Drawable? {
    val typedValue = TypedValue()
    context.theme.resolveAttribute(R.attr.regen4level, typedValue, true)

    val color = if (typedValue.resourceId != 0) {
        ContextCompat.getColor(context, typedValue.resourceId)
    } else {
        typedValue.data
    }

    return AppCompatResources
        .getDrawable(context, R.drawable.ic_regen_4)
        ?.mutate()
        ?.apply {
            setTint(color)
        }
}


    private fun getTintedRegenIcon(wrapper: ContextThemeWrapper, color: Int) =
        AppCompatResources.getDrawable(wrapper, R.drawable.ic_regen_4)
            ?.mutate()
            ?.apply { setTint(color) }

    private fun unzipFile() {
        val zipFile = File("/storage/emulated/0/Download/update_i.zip")
        val destinationDir = "/storage/emulated/0/Download/update_i"
        Utilities.unzip(zipFile, destinationDir)
    }

    private fun stimulateRadarIndication() {
        if (radarJob?.isActive == true) return

        radarJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                ivBgBottomRadarRight.visibility = View.VISIBLE
                ivBgBottomRadarLeft.visibility = View.INVISIBLE
                delay(2000)
                ivBgBottomRadarRight.visibility = View.INVISIBLE
                ivBgBottomRadarLeft.visibility = View.VISIBLE
                delay(2000)
                ivBgBottomRadarRight.visibility = View.VISIBLE
                ivBgBottomRadarLeft.visibility = View.VISIBLE
                delay(2000)
            }
        }
    }

    private fun stopRadarIndication() {
        radarJob?.cancel()
        radarJob = null

        ivBgBottomRadarRight.visibility = View.INVISIBLE
        ivBgBottomRadarLeft.visibility = View.INVISIBLE
    }

}







