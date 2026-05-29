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
import com.ultraviolette.uvclusterhmi.ClusterApplication
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import com.ultraviolette.uvclusterhmi.domain.model.ClusterUiState
import com.ultraviolette.uvclusterhmi.domain.model.RadarDisplayState
import com.ultraviolette.uvclusterhmi.domain.model.RadarUiState
import com.ultraviolette.uvclusterhmi.domain.model.RideMode
import com.ultraviolette.uvclusterhmi.ui.customWidget.AngleGaugeView
import com.ultraviolette.uvclusterhmi.ui.customWidget.DiagonalProgressView
import com.ultraviolette.uvclusterhmi.ui.features.MainActivity
import com.ultraviolette.uvclusterhmi.ui.viewModel.CarViewModel
import com.ultraviolette.uvclusterhmi.ui.viewModel.ClusterViewModel
import com.ultraviolette.uvclusterhmi.ui.viewModel.SharedViewModel
import com.ultraviolette.uvclusterhmi.utils.Utilities
import com.ultraviolette.uvclusterhmi.utils.Utilities.getRegenValueForLevel4
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
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

    // ── ViewModels ────────────────────────────────────────────────────────────
    /** Write-only path: sends VHAL property writes (regen, etc.) to the VCU. */
    private val carViewModel by activityViewModels<CarViewModel> { ViewModelFactory(context = requireContext()) }
    /** Dashboard-local UI state (theme, regen, efficiency, etc.). */
    private val viewModel by activityViewModels<DashboardViewModel> { ViewModelFactory(context = requireContext()) }
    private val sharedViewModel by activityViewModels<SharedViewModel> { ViewModelFactory(context = requireContext()) }
    /** New architecture: single source of truth for all vehicle state. */
    private val clusterViewModel: ClusterViewModel by activityViewModels {
        ClusterViewModel.Factory(requireActivity().application as ClusterApplication)
    }

    private lateinit var ivBgBottomRadarLeft: ImageView
    private lateinit var ivBgBottomRadarRight: ImageView
    private var unit = ""
    private lateinit var ivBallisticPlus: ImageView
    private var radarJob: Job? = null
    private var isMotorArmed = false
    private var isNegativePower = false
    var regenUnAvailable = false
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
        viewModel.getRegenValue()
    }

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
                            if (diffX > 0) onSwipeRight() else onSwipeLeft()
                            return true
                        }
                    } else {
                        if (abs(diffY) > 100 && abs(velocityY) > 100) {
                            if (diffY < 0) onSwipeUp() else onSwipeDown()
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
                        if (currentTime - lastDoubleTapTime < DOUBLE_TAP_WINDOW) doubleTapCount++
                        else doubleTapCount = 1
                        lastDoubleTapTime = currentTime
                        if (doubleTapCount >= 2) { onSecretTriggered(); doubleTapCount = 0 }
                    } else {
                        doubleTapCount = 0
                    }
                    return true
                }
            })

        rootView?.setOnTouchListener { v, event ->
            val handled = gestureDetector.onTouchEvent(event)
            if (!handled && event.action == MotionEvent.ACTION_UP) v.performClick()
            true
        }
    }

    private fun onSecretTriggered() { findNavController()?.navigate(R.id.debugFragment) }

    private fun onSwipeUp() {
        if (speed <= 0 && findNavController().currentDestination?.id == R.id.dashboardFragment) {
            findNavController().navigate(R.id.action_dashboardFragment_to_menuFragment)
        }
    }

    private fun onSwipeDown() = Unit

    private fun onSwipeLeft() = Unit

    private fun onSwipeRight() {
        if (speed <= 0 && findNavController().currentDestination?.id == R.id.dashboardFragment) {
            findNavController().navigate(R.id.action_dashboardFragment_to_musicFragment)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initObserver()
        ivRegenLevel10.isVisible = viewModel.is10Levels
        llRegenLevel4.isVisible = !viewModel.is10Levels
        (activity as? MainActivity)?.handleToolbar(true)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Observers — ClusterViewModel is the single source of truth for all
    // vehicle state; CarViewModel is kept only for fire-and-forget writes
    // (VHAL property sends) and the swift-button event flow.
    // ─────────────────────────────────────────────────────────────────────────

    private fun initObserver() {
        d("DashboardFragment", "initObserver called")
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // ── ClusterViewModel: all vehicle state ───────────────────────
                launch {
                    clusterViewModel.uiState.collect { uiState ->
                        val active = uiState as? ClusterUiState.Active ?: return@collect
                        applyClusterState(active)
                    }
                }

                // ── DashboardViewModel: local UI state (theme, regen, etc.) ──
                launch {
                    viewModel.uiState.collect { uiState ->
                        updateUi(uiState)
                    }
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Apply ClusterUiState.Active to all dashboard views.
    // ─────────────────────────────────────────────────────────────────────────

    private fun applyClusterState(active: ClusterUiState.Active) {
        val dash = active.dashboard

        // ── Units (from prefs) ────────────────────────────────────────────────
        unit = active.prefs.distanceUnit
        tvSpeedUnit.text = if (unit == "miles") "mph" else "km/h"
        tvPowerLabel.text = if (unit == "miles") "Wh/mile" else "Wh/km"

        // ── Speed ─────────────────────────────────────────────────────────────
        speed = dash.motor.speedKmh
        tvSpeed.text = dash.motor.speedDisplay
        if (dash.motor.isArmed) {
            val typedValue = TypedValue()
            requireContext().theme.resolveAttribute(R.attr.appTextColor, typedValue, true)
            tvSpeed.setTextColor(typedValue.data)
        } else {
            tvSpeed.setTextColor(ContextCompat.getColor(requireContext(), R.color.lightGreyMedium))
        }
        ivMtrArmed.visibility = if (dash.motor.showArmedIcon) View.VISIBLE else View.INVISIBLE

        // ── Motor power bars ──────────────────────────────────────────────────
        // motorPower is raw watts; powerBarProgress is already normalised 0–1.
        isNegativePower = dash.motor.motorPower < 0
        val powerBarProgress = dash.motor.powerBarProgress
        pbPowerBottomLeft.progress  = powerBarProgress
        pbPowerBottomRight.progress = powerBarProgress
        pbPowerTopLeft.progress     = powerBarProgress
        pbPowerTopRight.progress    = powerBarProgress
        viewModel.setPowerValue(powerBarProgress)
        updatePowerColor(viewModel.uiState.value.themeMode)

        // ── Wh/km ─────────────────────────────────────────────────────────────
        tvWhPerKm.text = String.format("%03d", dash.odo.whPerUnitDisplay)
        viewModel.setEfficiencyValue(dash.odo.efficiencyLevel)

        // ── Odometer / Range / Trip ───────────────────────────────────────────
        tvOdoValue.text   = dash.odo.odoDisplay.toString()
        tvOdoUnit.text    = unit
        tvRangeValue.text = dash.odo.rangeDisplay.toString()
        tvRangeUnit.text  = unit
        tvRideValue.text  = dash.odo.tripDisplay.toString()
        tvRideUnit.text   = unit

        // ── Roll angle (lean) ─────────────────────────────────────────────────
        tvIncline.text    = abs(dash.rollAngle).toInt().toString()
        angleGauge.progress = dash.rollAngle / 90f

        // ── Motor armed state ─────────────────────────────────────────────────
        isMotorArmed = dash.motor.isArmed
        viewModel.setMotorArmed(isMotorArmed)

        // ── Regen ─────────────────────────────────────────────────────────────
        regenUnAvailable = dash.regen.isUnavailable
        viewModel.setRegenUnAvailable(regenUnAvailable)
        if (regenUnAvailable) {
            viewModel.setRegenValue(0)
        } else {
            viewModel.setRegenValue(dash.regen.level.coerceIn(0, 9))
        }

        // ── Ride mode (theme) ─────────────────────────────────────────────────
        updateThemeMode(when (dash.drive.rideMode) {
            RideMode.Glide     -> 1
            RideMode.Combat    -> 2
            RideMode.Ballistic -> 3
        })

        // ── Ballistic Plus badge ──────────────────────────────────────────────
        val isBallistic = dash.drive.rideMode == RideMode.Ballistic
        ivBallisticPlus.visibility =
            if (dash.drive.isBallisticPlus && isBallistic) View.VISIBLE else View.INVISIBLE
        viewModel.ballisticPlus(dash.drive.isBallisticPlus)

        // ── Radar ─────────────────────────────────────────────────────────────
        if (!dash.alertsEnabled) {
            ivBgBottomRadarLeft.visibility  = View.INVISIBLE
            ivBgBottomRadarRight.visibility = View.INVISIBLE
        } else {
            applyRadarState(dash.radar)
        }

        // ── Cruise control ────────────────────────────────────────────────────
        val cruiseActive = active.toolbar.cruiseActive
        ivCruiseEnabled.visibility   = if (cruiseActive) View.VISIBLE else View.INVISIBLE
        ivCruiseHighlight.visibility = if (cruiseActive) View.VISIBLE else View.INVISIBLE
    }

    private fun applyRadarState(radar: RadarUiState) {
        // ── Left side ─────────────────────────────────────────────────────────
        when {
            radar.rcwActive || radar.leftState == RadarDisplayState.Alert -> {
                ivBgBottomRadarLeft.visibility = View.VISIBLE
                ivBgBottomRadarLeft.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.bg_radar_left_alert)
                )
            }
            radar.leftState == RadarDisplayState.Warn -> {
                ivBgBottomRadarLeft.visibility = View.VISIBLE
                ivBgBottomRadarLeft.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.bg_dashboard_radar_left)
                )
            }
            else -> ivBgBottomRadarLeft.visibility = View.INVISIBLE
        }
        // ── Right side ────────────────────────────────────────────────────────
        when {
            radar.rcwActive || radar.rightState == RadarDisplayState.Alert -> {
                ivBgBottomRadarRight.visibility = View.VISIBLE
                ivBgBottomRadarRight.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.bg_dashboard_radar_right_alert)
                )
            }
            radar.rightState == RadarDisplayState.Warn -> {
                ivBgBottomRadarRight.visibility = View.VISIBLE
                ivBgBottomRadarRight.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.bg_dashboard_radar_right)
                )
            }
            else -> ivBgBottomRadarRight.visibility = View.INVISIBLE
        }
    }

    fun getStep(is10Levels: Boolean) = if (is10Levels) 1 else 3

    fun normalize(value: Int, is10Levels: Boolean): Int =
        if (is10Levels) value else (value / 3) * 3

    fun handleButtonNavigation(button: Int) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > SEQUENCE_TIMEOUT) sequenceStep = 0

        if (button == debugSequence[sequenceStep]) {
            lastClickTime = currentTime
            sequenceStep++
            if (sequenceStep == debugSequence.size) {
                sequenceStep = 0
                findNavController().navigate(R.id.debugFragment)
                return
            }
            return
        } else {
            sequenceStep = if (button == debugSequence[0]) { lastClickTime = currentTime; 1 } else 0
            if (sequenceStep != 0) return
        }

        when (button) {
            ButtonNavigation.Top.ordinal -> { if (speed > 0) return }

            ButtonNavigation.Left.ordinal -> {
                val step    = getStep(viewModel.is10Levels)
                var current = sharedViewModel.regenValue
                current = normalize(current, viewModel.is10Levels)
                val regenValue = (current - step).coerceAtLeast(0)
                sharedViewModel.saveRegenValue(regenValue)
                carViewModel.sendByteArrayProperty(0x2170039F, byteArrayOf(regenValue.toByte()))
                d("REGEN_VALUE", "LEFT -> $regenValue")
            }

            ButtonNavigation.Right.ordinal -> {
                val step    = getStep(viewModel.is10Levels)
                var current = sharedViewModel.regenValue
                current = normalize(current, viewModel.is10Levels)
                val regenValue = (current + step).coerceAtMost(9)
                sharedViewModel.saveRegenValue(regenValue)
                carViewModel.sendByteArrayProperty(0x2170039F, byteArrayOf(regenValue.toByte()))
                d("REGEN_VALUE", "RIGHT -> $regenValue")
            }

            ButtonNavigation.Bottom.ordinal -> {
                if (speed > 0) return
                findNavController().navigate(R.id.action_dashboardFragment_to_menuFragment)
            }

            ButtonNavigation.Enter.ordinal -> Unit
        }
    }

    private fun updateThemeMode(rideModes: Int?) {
        val themeMode = when (rideModes) {
            1    -> R.style.Theme_Glide
            2    -> R.style.Theme_Combat
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

    private fun initViews(view: View) {
        tvOdoLabel       = view.findViewById(R.id.tvOdoLabel)
        tvPowerLabel     = view.findViewById(R.id.tvPowerLabel)
        viewPower        = view.findViewById(R.id.viewPower)
        ivEfficiency     = view.findViewById(R.id.ivEfficiency)
        ivRegenLevel10   = view.findViewById(R.id.ivRegenLevel10)
        ivBgBottom       = view.findViewById(R.id.ivBgBottom)
        pbPowerTopRight  = view.findViewById(R.id.pbPowerTopLeft)
        pbPowerTopLeft   = view.findViewById(R.id.pbPowerTopRight)
        pbPowerBottomRight = view.findViewById(R.id.pbPowerBottomRight)
        pbPowerBottomLeft  = view.findViewById(R.id.pbPowerBottomLeft)
        tvRideLabel      = view.findViewById(R.id.tvRideLabel)
        tvRangeLabel     = view.findViewById(R.id.tvRangeLabel)
        viewRange        = view.findViewById(R.id.viewRange)
        tvSpeed          = view.findViewById(R.id.tvSpeed)
        tvWhPerKm        = view.findViewById(R.id.tvWhPerKm)
        tvMode           = view.findViewById(R.id.tvMode)
        tvRec            = view.findViewById(R.id.tvRec)
        angleGauge       = view.findViewById(R.id.angleGauge)
        tvIncline        = view.findViewById(R.id.tvIncline)
        tvOdoValue       = view.findViewById(R.id.tvOdoValue)
        tvRegenValue     = view.findViewById(R.id.tvRegenValue)
        tvRideValue      = view.findViewById(R.id.tvRideValue)
        tvRangeValue     = view.findViewById(R.id.tvRangeValue)
        llRegenLevel4    = view.findViewById(R.id.llRegenLevel4)
        ivRegen4Level1   = view.findViewById(R.id.ivRegen4Level1)
        ivRegen4Level2   = view.findViewById(R.id.ivRegen4Level2)
        ivRegen4Level3   = view.findViewById(R.id.ivRegen4Level3)
        ivBgSides        = view.findViewById(R.id.ivBgSides)
        ivTemperature    = view.findViewById(R.id.ivTemperature)
        tvEfficiencyLabel = view.findViewById(R.id.tvEfficiencyLabel)
        tvInclineDegree  = view.findViewById(R.id.tvInclineDegree)
        tvTemperatureValue = view.findViewById(R.id.tvTemperatureValue)
        tvTemperatureUnit  = view.findViewById(R.id.tvTemperatureUnit)
        ivReset          = view.findViewById(R.id.ivReset)
        ivBgBottomRadarLeft  = view.findViewById(R.id.ivBgBottomLeftRadar)
        ivBgBottomRadarRight = view.findViewById(R.id.ivBgBottomRightRadar)
        tvOdoUnit        = view.findViewById(R.id.tvOdoUnit)
        tvRangeUnit      = view.findViewById(R.id.tvRangeUnit)
        tvRideUnit       = view.findViewById(R.id.tvRideUnit)
        tvSpeedUnit      = view.findViewById(R.id.tvSpeedUnit)
        ivBallisticPlus  = view.findViewById(R.id.ivBallisticPlus)
        ivMtrArmed       = view.findViewById(R.id.ivMtrArmed)
        ivCruiseEnabled  = view.findViewById(R.id.ivCruiseEnabled)
        ivCruiseHighlight = view.findViewById(R.id.ivCruiseHighlight)
        unit = sharedViewModel.distanceUnit
    }

    fun handleHoverMode(hoverMode: Boolean) {
        pbPowerBottomLeft.visibility  = if (hoverMode) View.INVISIBLE else View.VISIBLE
        pbPowerBottomRight.visibility = if (hoverMode) View.INVISIBLE else View.VISIBLE
        pbPowerTopLeft.visibility     = if (hoverMode) View.INVISIBLE else View.VISIBLE
        pbPowerTopRight.visibility    = if (hoverMode) View.INVISIBLE else View.VISIBLE
        ivTemperature.visibility      = if (hoverMode) View.INVISIBLE else View.VISIBLE
        llRegenLevel4.visibility = if (hoverMode) View.INVISIBLE else {
            if (!viewModel.is10Levels) View.VISIBLE else View.INVISIBLE
        }
        ivRegenLevel10.visibility = if (hoverMode) View.INVISIBLE else {
            if (viewModel.is10Levels) View.VISIBLE else View.INVISIBLE
        }
        ivEfficiency.visibility       = if (hoverMode) View.INVISIBLE else View.VISIBLE
        tvEfficiencyLabel.visibility  = if (hoverMode) View.INVISIBLE else View.VISIBLE
        angleGauge.visibility         = if (hoverMode) View.INVISIBLE else View.VISIBLE
        tvIncline.visibility          = if (hoverMode) View.INVISIBLE else View.VISIBLE
        tvTemperatureValue.visibility = if (hoverMode) View.INVISIBLE else View.VISIBLE
        tvInclineDegree.visibility    = if (hoverMode) View.INVISIBLE else View.VISIBLE
        tvTemperatureUnit.visibility  = if (hoverMode) View.INVISIBLE else View.VISIBLE
        tvRegenValue.visibility       = if (hoverMode) View.INVISIBLE else View.VISIBLE
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
        updatePowerColor(uiState.themeMode)

        val wrapper = ContextThemeWrapper(requireContext(), uiState.themeMode)
        if (uiState.isMotorArmed) {
            val bgBottomVector = AppCompatResources.getDrawable(wrapper, R.drawable.bg_bottom)
            ivBgBottom.setImageDrawable(bgBottomVector)
        } else {
            ivBgBottom.setImageDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_bottom_grey)
            )
        }
        val newBackground = AppCompatResources.getDrawable(wrapper, R.drawable.bg_rounded)
        viewPower.background = newBackground
        viewRange.background = newBackground
        val drawable = AppCompatResources.getDrawable(wrapper, R.drawable.bg_side)
        ivBgSides.setImageDrawable(drawable)
        when (uiState.themeMode) {
            R.style.Theme_Ballistic -> { tvMode.text = getString(R.string.ballistic); tvRec.text = getString(R.string.rec_60) }
            R.style.Theme_Combat    -> { tvMode.text = getString(R.string.combat);    tvRec.text = getString(R.string.rec_52) }
            R.style.Theme_Glide     -> { tvMode.text = getString(R.string.glide);     tvRec.text = getString(R.string.rec_40) }
        }
    }

    fun getColorAttr(attr: Int, @StyleRes themeRes: Int): Int {
        val wrapper    = ContextThemeWrapper(requireContext(), themeRes)
        val typedValue = TypedValue()
        return if (wrapper.theme.resolveAttribute(attr, typedValue, true)) {
            ContextCompat.getColor(requireContext(), typedValue.resourceId)
        } else Color.BLACK
    }

    @SuppressLint("ResourceAsColor")
    fun updateRegenLevel(uiState: UiState, modeColor: Int) {
        if (regenUnAvailable) {
            viewModel.setRegenValue(0)
            if (viewModel.is10Levels) {
                val regenDrawable = AppCompatResources.getDrawable(
                    ContextThemeWrapper(requireContext(), R.style.Theme_MotorArmed),
                    R.drawable.regen_level_list
                )?.mutate()
                if (regenDrawable is LevelListDrawable) regenDrawable.level = 0
                ivRegenLevel10.setImageDrawable(regenDrawable)
            } else {
                ivRegen4Level1.setImageResource(R.drawable.ic_regen_4)
                ivRegen4Level2.setImageResource(R.drawable.ic_regen_4)
                ivRegen4Level3.setImageResource(R.drawable.ic_regen_4)
            }
            tvRegenValue.text = "R0"
        } else {
            tvRegenValue.text = "R${uiState.regenValue}"
            val wrapper = ContextThemeWrapper(requireContext(), uiState.themeMode)
            if (viewModel.is10Levels) {
                val regenDrawable = if (uiState.isMotorArmed) {
                    AppCompatResources.getDrawable(
                        ContextThemeWrapper(requireContext(), uiState.themeMode),
                        R.drawable.regen_level_list
                    )?.mutate()
                } else {
                    AppCompatResources.getDrawable(
                        ContextThemeWrapper(requireContext(), R.style.Theme_MotorArmed),
                        R.drawable.regen_level_list
                    )?.mutate()
                }
                if (regenDrawable is LevelListDrawable) regenDrawable.level = uiState.regenValue
                ivRegenLevel10.setImageDrawable(regenDrawable)
            } else {
                val regenLevel4   = getRegenValueForLevel4(uiState.regenValue)
                val defaultIcon1  = AppCompatResources.getDrawable(wrapper, R.drawable.ic_regen_4)?.mutate()
                val defaultIcon2  = AppCompatResources.getDrawable(wrapper, R.drawable.ic_regen_4)?.mutate()
                val defaultIcon3  = AppCompatResources.getDrawable(wrapper, R.drawable.ic_regen_4)?.mutate()
                ivRegen4Level1.setImageDrawable(defaultIcon1)
                ivRegen4Level2.setImageDrawable(defaultIcon2)
                ivRegen4Level3.setImageDrawable(defaultIcon3)
                when (regenLevel4) {
                    3 -> { if (uiState.isMotorArmed) ivRegen4Level1.setImageDrawable(getTintedRegenIcon(wrapper, modeColor)) else ivRegen4Level1.setImageDrawable(getGreyRegenIcon(requireContext())) }
                    6 -> {
                        val icon = if (uiState.isMotorArmed) getTintedRegenIcon(wrapper, modeColor) else getGreyRegenIcon(requireContext())
                        ivRegen4Level1.setImageDrawable(icon); ivRegen4Level2.setImageDrawable(icon)
                    }
                    9 -> {
                        val icon = if (uiState.isMotorArmed) getTintedRegenIcon(wrapper, modeColor) else getGreyRegenIcon(requireContext())
                        ivRegen4Level1.setImageDrawable(icon); ivRegen4Level2.setImageDrawable(icon); ivRegen4Level3.setImageDrawable(icon)
                    }
                }
            }
        }
    }

    private fun updateEfficiencyLevel(uiState: UiState, modeColor: Int) {
        val regenDrawable = if (uiState.isMotorArmed) {
            AppCompatResources.getDrawable(
                ContextThemeWrapper(requireContext(), uiState.themeMode),
                R.drawable.efficiency_level_list
            )?.mutate()
        } else {
            AppCompatResources.getDrawable(
                ContextThemeWrapper(requireContext(), R.style.Theme_MotorArmed),
                R.drawable.efficiency_level_list
            )?.mutate()
        }
        if (regenDrawable is LevelListDrawable) regenDrawable.level = uiState.efficiency
        ivEfficiency.setImageDrawable(regenDrawable)
    }

    fun getGreyRegenIcon(context: Context): Drawable? {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(R.attr.regen4level, typedValue, true)
        val color = if (typedValue.resourceId != 0) {
            ContextCompat.getColor(context, typedValue.resourceId)
        } else typedValue.data
        return AppCompatResources.getDrawable(context, R.drawable.ic_regen_4)?.mutate()?.apply { setTint(color) }
    }

    private fun getTintedRegenIcon(wrapper: ContextThemeWrapper, color: Int) =
        AppCompatResources.getDrawable(wrapper, R.drawable.ic_regen_4)?.mutate()?.apply { setTint(color) }

    private fun stimulateRadarIndication() {
        if (radarJob?.isActive == true) return
        radarJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                ivBgBottomRadarRight.visibility = View.VISIBLE
                ivBgBottomRadarLeft.visibility  = View.INVISIBLE
                delay(2000)
                ivBgBottomRadarRight.visibility = View.INVISIBLE
                ivBgBottomRadarLeft.visibility  = View.VISIBLE
                delay(2000)
                ivBgBottomRadarRight.visibility = View.VISIBLE
                ivBgBottomRadarLeft.visibility  = View.VISIBLE
                delay(2000)
            }
        }
    }

    private fun stopRadarIndication() {
        radarJob?.cancel(); radarJob = null
        ivBgBottomRadarRight.visibility = View.INVISIBLE
        ivBgBottomRadarLeft.visibility  = View.INVISIBLE
    }
}
