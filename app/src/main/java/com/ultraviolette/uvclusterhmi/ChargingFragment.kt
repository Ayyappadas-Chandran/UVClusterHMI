package com.ultraviolette.uvclusterhmi

import android.content.Context
import android.os.Bundle
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.getDrawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import com.ultraviolette.uvclusterhmi.ui.features.MainActivity
import com.ultraviolette.uvclusterhmi.ui.viewModel.CarViewModel
import com.ultraviolette.uvclusterhmi.ui.viewModel.SharedViewModel
import com.ultraviolette.uvclusterhmi.utils.Utilities
import com.ultraviolette.uvclusterhmi.utils.Utilities.ARG_CHARGING_STATUS
import com.ultraviolette.uvclusterhmi.utils.Utilities.applyMinMax
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class ChargingFragment : Fragment() {
    private lateinit var clChargingReview: ConstraintLayout
    private lateinit var clCharging: ConstraintLayout
    private lateinit var tvModeValue: TextView
    private lateinit var tvRangeValue: TextView
    private lateinit var tvCharge: TextView
    private lateinit var tvTime: TextView
    private val carViewModel by activityViewModels<CarViewModel> { ViewModelFactory(context = requireContext()) }
    private val sharedViewModel by activityViewModels<SharedViewModel> { ViewModelFactory(context = requireContext()) }
    private var unit: String = ""

    private lateinit var pbBattery: CircularGradientProgress
    private lateinit var ivRideModes: ImageView

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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_charging, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
	d("CharingScreen","onViewCreated")
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        initObserver()
//        val isCharging = arguments?.getBoolean(ARG_CHARGING_STATUS) ?: false
//        clCharging.visibility = if (isCharging) View.VISIBLE else View.INVISIBLE
//        clChargingReview.visibility = if (isCharging) View.INVISIBLE else View.VISIBLE
        (activity as MainActivity).handleToolbar(false)
        unit = sharedViewModel.distanceUnit
    }

    private fun initView(view: View) {
        clCharging = view.findViewById(R.id.clCharging)
        clChargingReview = view.findViewById(R.id.clChargingReview)
        tvModeValue = view.findViewById(R.id.tvModeValue)
        tvRangeValue = view.findViewById(R.id.tvRangeValue)
        tvCharge = view.findViewById(R.id.tvCharge)
        pbBattery = view.findViewById(R.id.pbBattery)
        ivRideModes=view.findViewById(R.id.ivRideModes)
        tvTime = view.findViewById(R.id.tvTime)

    }


    private fun initObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    carViewModel.rideModes.collect {

                    }
                }
                launch {
                    carViewModel.vcuInfoMsg.collect {
                        val rawRange = it.range.toInt()
                        val finalRange = rawRange.applyMinMax(sharedViewModel.rangeLimit)
                        val isMiles=sharedViewModel.distanceUnit.equals("miles", ignoreCase = true)
                        val displayRange =
                            if (isMiles)
                                (finalRange * 0.621371).roundToInt()
                            else
                                finalRange
                        val unitText = if (isMiles) "miles" else "km"
                        var range=displayRange.toString()
                        tvRangeValue.text = "$range $unitText"

                    }
                }
                launch {
                    carViewModel.tellTales.collect { tellTales ->

                        var rideModes=tellTales.rideMode
                        d("CharingScreen","RideMode: $rideModes")
                        handleRideMode(rideModes)
                        val chargingStatus = tellTales.charger
                        if (chargingStatus==0)
                        {
                            if(tellTales.modeHover==1)
                            findNavController().navigate(R.id.hoverModeFragment)
                            else
                                findNavController().navigate(R.id.dashboardFragment)
                        }
                    }
                }
                launch {
                    carViewModel.chargeCtx.collect { chargeCtx ->
                        var remainTime=chargeCtx.chargerRemainingTime
                        d("CharingScreen","remainTime: $remainTime")
                        //handleRemainingTime(remainTime.toInt())


                    }
                }
                launch {
                    carViewModel.keyOff.collect { keyOff ->
                        d("CharingScreen","keyOff: $keyOff")
                        if(keyOff){
                            clChargingReview.visibility = View.INVISIBLE
                            clCharging.visibility = View.VISIBLE

                        }else{
                            clChargingReview.visibility = View.VISIBLE
                            clCharging.visibility = View.INVISIBLE
                        }

                         }
                }
/*
                launch {
                    carViewModel.tellTales.collect {
                        val batteryValue = it.batterySoc
                        val finalSoc = batteryValue.applyMinMax(sharedViewModel.socLimit)
                        tvCharge.text = finalSoc.toString()
                        pbBattery.progress = finalSoc.toFloat()
                        val chargingStatus = it.charger
                        //  handleChargingStatus(chargingStatus)
                    }
                }
*/

                launch {
                    carViewModel.imxDbgMsg.collect { imxDbgMsg ->
                        val batterySoc = (imxDbgMsg.soc.toInt() and 0xFF)
                        tvCharge.text = "$batterySoc"
                        pbBattery.progress = batterySoc.toFloat()
                        var remainTime = imxDbgMsg.chargeTtf
                        handleRemainingTime(remainTime.toInt())

                    }
                }

		launch {
                    carViewModel.swiftButton.collect { swiftButton ->
                        d("CharingScreen","ButtonEvent came")
			val button = Utilities.getButtonState(swiftButton)
                        if (button == ButtonNavigation.None) return@collect
                        handleButtonNavigation(button.ordinal)
                    }
                }
            }
        }
    }
    private fun handleRideMode(rideMode: Int) {
        when (rideMode) {
            1 -> {
                ivRideModes.visibility = View.VISIBLE
                ivRideModes.setImageDrawable(getDrawable(requireContext() ,R.drawable.ic_glide))
                tvModeValue.text=getString(R.string.glide)
            }

            2 -> {
                ivRideModes.visibility = View.VISIBLE
                ivRideModes.setImageDrawable(getDrawable(requireContext(), R.drawable.ic_combat))
                tvModeValue.text=getString(R.string.combat)

            }

            3 -> {
                ivRideModes.visibility = View.VISIBLE
                ivRideModes.setImageDrawable(getDrawable(requireContext(), R.drawable.ic_ballistic_mode_toolbar))
                tvModeValue.text=getString(R.string.ballistic)
            }
        }
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
    }

    private fun handleChargingStatus(status: Int) {
        when (status) {
            0x00 -> findNavController().navigateUp()
            0x01 -> clChargingReview.visibility = View.VISIBLE
            0x02 -> clCharging.visibility = View.VISIBLE
        }
    }
     private fun handleRemainingTime(time: Int) {
        val hours = time / 3600
        val minutes = (time % 3600) / 60

        tvTime.text = if (hours > 0) {
            "$hours Hr $minutes Min"
        } else {
            "$minutes Min"
        }
    }
}





