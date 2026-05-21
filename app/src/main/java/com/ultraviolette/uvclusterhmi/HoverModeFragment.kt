package com.ultraviolette.uvclusterhmi

import android.os.Bundle
import android.util.Log.d
import android.util.TypedValue
import android.view.GestureDetector
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.ultraviolette.uvclusterhmi.domain.dataModel.RangeLimit
import com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData.VcuInfoMsg
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import com.ultraviolette.uvclusterhmi.ui.features.dashboard.DashboardViewModel
import com.ultraviolette.uvclusterhmi.ui.viewModel.CarViewModel
import com.ultraviolette.uvclusterhmi.ui.viewModel.SharedViewModel
import com.ultraviolette.uvclusterhmi.utils.Utilities
import com.ultraviolette.uvclusterhmi.utils.Utilities.applyMinMax
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt


class HoverModeFragment : Fragment() {
    private lateinit var gestureDetector: GestureDetector
    private lateinit var tvOdo: TextView
    private lateinit var tvSpeed: TextView
    private lateinit var tvSpeedUnit: TextView
    var unit = ""
    private var speed = 0
    private val carViewModel: CarViewModel by activityViewModels { ViewModelFactory(requireContext()) }
    private val sharedViewModel: SharedViewModel by activityViewModels { ViewModelFactory(requireContext()) }
    private val viewModel by activityViewModels<DashboardViewModel> { ViewModelFactory(context = requireContext()) }
    private var isMotorArmed=false
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
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_hover_mode, container, false)
        addSwipeGesture(rootView)
        return rootView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        initObserver()
    }

    private fun initView(view: View) {
        tvSpeed = view.findViewById(R.id.tvSpeed)
        tvOdo = view.findViewById(R.id.tvODo)
        tvSpeedUnit = view.findViewById(R.id.tvSpeedUnit)
        //tvSpeed.setOnClickListener { findNavController().navigateUp() }
        unit = sharedViewModel.distanceUnit
        tvSpeed.text = "- - -"
    }

    private fun initObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    carViewModel.vehicleValue.collect { vehicleValue ->
                        updateVehicleValue(vehicleValue)
                    }
                }
                launch {
                    carViewModel.vcuInfoMsg.collect { vcuInfoMsg ->
                        updateVcuMsg(vcuInfoMsg)
                    }
                }
                launch {
                    carViewModel.tellTales.collect { tellTales ->
                        val isHoverMode = tellTales.modeHover == 1
                        // isMotorArmed = tellTales.motorArmed == 1
                        //val isHoverMode = true
                        if (!isHoverMode) {
                            findNavController().navigate(R.id.dashboardFragment)

                        }
                       /* if (isMotorArmed) {
                            tvSpeed.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.white
                                )
                            )
                            if (tvSpeed.text.toString() == "- - -") {
                                tvSpeed.text = "000"
                                tvSpeed.visibility = View.VISIBLE
                            }
                         //   tvSpeed.setTextColor(typedValue.data)
                        } else {
                            tvSpeed.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.lightGreyMedium
                                )
                            )
                            if (speed == 0) {
                                tvSpeed.text = "- - -"
                            }
                        }*/


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
                    carViewModel.motorArmDisarmTellTale.collect { motorArmDisarmTellTale ->
                        d("Faizuuuuu", "motorArmDisarmTellTale: $motorArmDisarmTellTale")
                        isMotorArmed=motorArmDisarmTellTale==1
                        if (isMotorArmed) {
                            tvSpeed.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.white
                                )
                            )
                            if (tvSpeed.text.toString() == "- - -") {
                                tvSpeed.text = "000"
                                tvSpeed.visibility = View.VISIBLE
                            }
                            //   tvSpeed.setTextColor(typedValue.data)
                        } else {
                            tvSpeed.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.lightGreyMedium
                                )
                            )
                            if (speed == 0) {
                                tvSpeed.text = "- - -"
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateVehicleValue(value: FloatArray = floatArrayOf()) {
        d("update speed", "speed")
        val isMiles = unit == "miles"
        tvSpeedUnit.text = if (isMiles) "mph" else "km/h"


        if (value.size < 4) return
        if (::tvSpeed.isInitialized) {


            val rawSpeedKm = value.getOrNull(0)?.toInt()
            val finalSpeedKm =
                rawSpeedKm?.applyMinMax(sharedViewModel.speedLimit) ?: 0
            speed = finalSpeedKm

            val displaySpeed = if (isMiles) {
                (finalSpeedKm * 0.621371f).roundToInt()
            } else {
                finalSpeedKm
            }
            if (!isMotorArmed && speed==0){
                tvSpeed.text="- - -"

            }else{
                tvSpeed.text = String.format("%03d", displaySpeed)
            }

        }
    }


    fun updateVcuMsg(vcuInfoMsg: VcuInfoMsg) {

        val rawOdometerKm = vcuInfoMsg.odometer.toInt()
        val finalOdoKm = rawOdometerKm.applyMinMax(sharedViewModel.odoLimit)

        val isMiles = unit == "miles"

        val displayOdo = if (isMiles) {
            (finalOdoKm * 0.621371f).roundToInt()
        } else {
            finalOdoKm
        }

        val unitLabel = if (isMiles) "miles" else "km"

        tvOdo.text = "$displayOdo $unitLabel"

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
                    tvSpeed.text = "- - -"
                }
            }
        }

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
                            if (diffX > 0) {
                                //onSwipeRight()
                            } else {
                                //onSwipeLeft()
                            }
                            return true
                        }
                    } else {
                        if (abs(diffY) > 100 && abs(velocityY) > 100) {
                            if (diffY < 0) {
                                //onSwipeUp()
                            } else {
                                //onSwipeDown()
                            }
                            return true
                        }
                    }
                    return false
                }

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    findNavController()?.navigate(R.id.debugFragment)
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
                //findNavController().navigate(R.id.action_dashboardFragment_to_menuFragment)
            }
            ButtonNavigation.Enter.ordinal -> {
                NotificationManager.show(
                    ClusterNotification.Params(
                        "Cruise Control Unavailable",
                        ""
                    )
                )
            }

        }
    }


}






