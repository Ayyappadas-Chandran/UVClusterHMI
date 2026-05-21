package com.ultraviolette.uvclusterhmi

import android.os.Bundle
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData.VcuInfoMsg
import com.ultraviolette.uvclusterhmi.ui.viewModel.CarViewModel
import com.ultraviolette.uvclusterhmi.ui.viewModel.SharedViewModel
import com.ultraviolette.uvclusterhmi.utils.Utilities.ARG_DASH_CAM
import com.ultraviolette.uvclusterhmi.utils.Utilities.applyMinMax
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


class ParkAssistantFragment : Fragment() {
    private lateinit var tvSpeed: TextView
    private lateinit var tvSwitchDirection: TextView
    private lateinit var ivParkAssistantRight: ImageView
    private lateinit var ivReverse: ImageView
    private lateinit var ivParkAssistantLeft: ImageView
    private lateinit var tvReverse: TextView
    private lateinit var tvSpeedReverse: TextView
    private lateinit var clWithoutDashCam: ConstraintLayout
    private lateinit var clWithDashCam: ConstraintLayout
    private lateinit var tvSwitchDirectionDashCam: TextView
    private var isReverse = true
    private var haveDashcam = false

    private val carViewModel by activityViewModels <CarViewModel> { ViewModelFactory(context = requireContext()) }
    private val sharedViewModel by activityViewModels<SharedViewModel> { ViewModelFactory(context = requireContext()) }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_park_assistant, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        //initClickListener()
        initObserver()
        haveDashcam = arguments?.getBoolean(ARG_DASH_CAM) ?: false
        clWithoutDashCam.visibility = if (haveDashcam) View.GONE else View.VISIBLE
        clWithDashCam.visibility = if (haveDashcam) View.VISIBLE else View.GONE
    }

    private fun initView(view: View) {
        tvSpeed = view.findViewById(R.id.tvSpeed)
        tvSwitchDirection = view.findViewById(R.id.tvSwitchDirection)
        ivParkAssistantRight = view.findViewById(R.id.ivParkAssistantRight)
        ivReverse = view.findViewById(R.id.ivReverse)
        ivParkAssistantLeft = view.findViewById(R.id.ivParkAssistantLeft)
        tvReverse = view.findViewById(R.id.tvReverse)
        clWithoutDashCam = view.findViewById(R.id.clWithoutDashCam)
        tvSwitchDirectionDashCam = view.findViewById(R.id.tvSwitchDirectionDashCam)
        clWithDashCam = view.findViewById(R.id.clWithDashCam)
        tvSpeedReverse = view.findViewById(R.id.tvSpeedReverse)
    }

    private fun initClickListener() {


        tvSwitchDirection.setOnSoundClickListener(requireContext()) {
            isReverse = !isReverse
            if (haveDashcam) {
                clWithDashCam.visibility = View.VISIBLE
                return@setOnSoundClickListener
            } else {
                clWithoutDashCam.visibility = View.VISIBLE
            }
            tvReverse.text = if (isReverse) "Reverse" else "Forward"
            ivReverse.rotationX = if (isReverse) 0f else 180f
            ivParkAssistantRight.rotationX = if (isReverse) 0f else 180f
            ivParkAssistantLeft.rotationX = if (isReverse) 0f else 180f
        }

        tvSwitchDirectionDashCam.setOnSoundClickListener(requireContext()) {
            isReverse = !isReverse
            clWithoutDashCam.visibility = View.VISIBLE
            clWithDashCam.visibility = View.INVISIBLE
            tvReverse.text = "Forward"
            ivReverse.rotationX = 180f
            ivParkAssistantRight.rotationX = 180f
            ivParkAssistantLeft.rotationX = 180f
        }
    }

    private fun initObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    carViewModel.paFwd.collect { value ->
                        d("UI Update", "paFwd: $value")
                        if (value) {
                            tvReverse.text = "Forward"
                            ivReverse.rotationX = 180f
                            ivParkAssistantRight.rotationX = 180f
                            ivParkAssistantLeft.rotationX = 180f
                        }
                    }
                }
                launch {
                    carViewModel.paRev.collect { value ->
                        d("UI Update", "paRev: $value")
                        if (value) {
                            tvReverse.text = "Reverse"
                            ivReverse.rotationX = 0f
                            ivParkAssistantRight.rotationX = 0f
                            ivParkAssistantLeft.rotationX = 0f
                        }
                    }
                }
                launch {
                    carViewModel.vehicleValue.collect { vehicleValue ->
                        d("ParkAssistFragment", "speed: ${vehicleValue.joinToString()}")
                        updateVehicleValue(vehicleValue)
                    }
                }
		launch {
                    carViewModel.vcuInfoMsg.collect { vcuInfo ->
                        d("ParkAssistFragment", "vcuInfo:$vcuInfo")
                        updateVcuMsg(vcuInfo)
                    }
                }
            }
        }
    }

    private fun updateVehicleValue(value: FloatArray = floatArrayOf()) {



        if (value.size < 4) return
        if (!::tvSpeed.isInitialized || !::tvSpeedReverse.isInitialized) return

        val rawSpeedKm = value[0].toInt()
        val finalSpeedKm =
            rawSpeedKm.applyMinMax(sharedViewModel.speedLimit)

        val isMiles =
            sharedViewModel.distanceUnit.equals("miles", ignoreCase = true)

        val displaySpeed =
            if (isMiles)
                (finalSpeedKm * 0.621371).roundToInt()
            else
                finalSpeedKm

        val unitText = if (isMiles) "mph" else "km/h"

        tvSpeed.text = "$displaySpeed $unitText"
        tvSpeedReverse.text = "$displaySpeed $unitText"

    }
    private fun updateVcuMsg(vcuInfoMsg: VcuInfoMsg) {
        if (vcuInfoMsg.speed.isNotEmpty()) {
            val isMiles =
                sharedViewModel.distanceUnit.equals("miles", ignoreCase = true)
            val unitText = if (isMiles) "mph" else "km/h"

            if (vcuInfoMsg.speed[0].toInt() == 0)
            {
                tvSpeed.text = "000 $unitText"
                tvSpeedReverse.text = "000 $unitText"
            }
        }
    }

}


