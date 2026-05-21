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
import com.ultraviolette.uvclusterhmi.utils.Utilities.ARG_BALLISTIC_PLUS
import com.ultraviolette.uvclusterhmi.utils.Utilities.applyMinMax
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener
import com.ultraviolette.uvclusterhmi.utils.Utilities.toFloat
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
import kotlinx.coroutines.launch
import org.w3c.dom.Text
import kotlin.math.roundToInt


class MapFragment : Fragment() {
    private lateinit var tvOdo: TextView
    private lateinit var tvRange: TextView
    private lateinit var tvSpeed: TextView
    private lateinit var tvWhKm: TextView
    private lateinit var tvRangeBallistic: TextView
    private lateinit var tvSpeedBallistic: TextView
    private lateinit var tvRide: TextView
    private lateinit var tvODo: TextView
    private lateinit var ivRegenLevel1: ImageView
    private lateinit var ivRegenLevel2: ImageView
    private lateinit var ivRegenLevel3: ImageView
    private lateinit var ivRegenLevel4: ImageView
    private lateinit var ivRegenLevel5: ImageView
    private lateinit var ivRegenLevel6: ImageView
    private lateinit var ivRegenLevel7: ImageView
    private lateinit var ivRegenLevel8: ImageView
    private lateinit var ivRegenLevel9: ImageView
    private lateinit var clMapWithBallistic: ConstraintLayout
    private lateinit var clMapWithoutBallistic: ConstraintLayout
    private lateinit var ivManeuverBallistic: ImageView
    private lateinit var tvSpeedUnit: TextView
    private lateinit var tvRangeUnit: TextView
    private lateinit var tvODoUnit: TextView
    private lateinit var ivManeuver: ImageView
    private lateinit var tvDistance: TextView
    private var isBallistic = false
    private lateinit var tvWhKmLabel: TextView
    private lateinit var tvSpeedBallisticUnit: TextView
    private lateinit var tvRangeBallisticUnit: TextView
    private lateinit var tvDistanceBallistic: TextView
    private lateinit var ivBack: ImageView
    private val carViewModel: CarViewModel by activityViewModels { ViewModelFactory(requireContext()) }
    private val sharedViewModel: SharedViewModel by activityViewModels {
        ViewModelFactory(
            requireContext()
        )
    }

    private var unit: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        isBallistic = arguments?.getBoolean(ARG_BALLISTIC_PLUS) ?: false
        clMapWithBallistic.visibility = if (isBallistic) View.VISIBLE else View.INVISIBLE
        clMapWithoutBallistic.visibility = if (isBallistic) View.INVISIBLE else View.VISIBLE
        initObserver()
        initClickListener()
        unit = sharedViewModel.distanceUnit
    }

    fun initClickListener() {
//        ivManeuver.setOnSoundClickListener(requireContext()) {
//            d("Tag", "Back")
//            findNavController().navigate(R.id.menuFragment)
//        }
//        ivManeuverBallistic.setOnSoundClickListener(requireContext()) {
//            findNavController().navigateUp()
//        }
        ivBack.setOnSoundClickListener(requireContext()) {
            findNavController().navigateUp()
       }
    }

    private fun initView(view: View) {
        tvOdo = view.findViewById(R.id.tvOdo)
        tvRange = view.findViewById(R.id.tvRange)
        tvSpeed = view.findViewById(R.id.tvSpeed)
        tvWhKm = view.findViewById(R.id.tvWhKm)
        tvRangeBallistic = view.findViewById(R.id.tvRangeBallistic)
        tvSpeedBallistic = view.findViewById(R.id.tvSpeedBallistic)
        tvRide = view.findViewById(R.id.tvRide)
        tvODo = view.findViewById(R.id.tvODo)
        ivManeuverBallistic = view.findViewById(R.id.ivManeuverBallistic)
        ivManeuver = view.findViewById(R.id.ivManeuver)
        ivRegenLevel1 = view.findViewById(R.id.ivRegenLevel1)
        ivRegenLevel2 = view.findViewById(R.id.ivRegenLevel2)
        ivRegenLevel3 = view.findViewById(R.id.ivRegenLevel3)
        ivRegenLevel4 = view.findViewById(R.id.ivRegenLevel4)
        ivRegenLevel5 = view.findViewById(R.id.ivRegenLevel5)
        ivRegenLevel6 = view.findViewById(R.id.ivRegenLevel6)
        ivRegenLevel7 = view.findViewById(R.id.ivRegenLevel7)
        ivRegenLevel8 = view.findViewById(R.id.ivRegenLevel8)
        ivRegenLevel9 = view.findViewById(R.id.ivRegenLevel9)
        clMapWithBallistic = view.findViewById(R.id.clMapWithBallistic)
        clMapWithoutBallistic = view.findViewById(R.id.clMapWithoutBallistic)
        tvSpeedUnit = view.findViewById(R.id.tvSpeedUnit)
        tvODoUnit = view.findViewById(R.id.tvOdoUnit)
        tvRangeUnit = view.findViewById(R.id.tvRangeUnit)
        tvSpeedBallisticUnit = view.findViewById(R.id.tvSpeedBallisticUnit)
        tvRangeBallisticUnit = view.findViewById(R.id.tvRangeBallisticUnit)
        tvWhKmLabel = view.findViewById(R.id.tvWhKmLabel)
        tvDistance = view.findViewById(R.id.tvDistance)
        tvDistanceBallistic = view.findViewById(R.id.tvDistanceBallistic)
        ivBack=view.findViewById(R.id.ivBack)
    }

    private fun initObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    carViewModel.vcuInfoMsg.collect { vcuInfoMsg ->
                        updateVcuMsg(vcuInfoMsg)
                    }
                }
                launch {
                    carViewModel.vehicleValue.collect { vehicleValue ->
                        updateVehicleValue(vehicleValue)
                    }
                }
                launch {
                    carViewModel.tellTales.collect { tellTales ->
                        val regenLevel = tellTales.regenLevel
                        regenValue(regenLevel)
                    }
                }
            }
        }
    }

    private fun regenValue(level: Int) {
        val views = listOf(
            ivRegenLevel1,
            ivRegenLevel2,
            ivRegenLevel3,
            ivRegenLevel4,
            ivRegenLevel5,
            ivRegenLevel6,
            ivRegenLevel7,
            ivRegenLevel8,
            ivRegenLevel9
        )
        views.forEachIndexed { index, view ->
            view.visibility = if (index < level) View.VISIBLE else View.INVISIBLE
        }
    }


    private fun updateVcuMsg(vcuInfoMsg: VcuInfoMsg) {
        val rawOdometer = vcuInfoMsg.odometer.toInt()
        val rawRange = vcuInfoMsg.range.toInt()
        val finalOdo = rawOdometer.applyMinMax(sharedViewModel.odoLimit)
        val finalRange = rawRange.applyMinMax(sharedViewModel.rangeLimit)


        val displayRange = if (unit == "miles") (finalRange * 0.621371).roundToInt() else finalRange
        val displayOdo = if (unit == "miles") (finalOdo * 0.621371).roundToInt() else finalOdo

        if (isBallistic) {
            val displayOdo =
                if (unit == "miles") (displayOdo * 0.621371).roundToInt() else displayOdo
            tvODo.text =
                if (unit == "miles") "${(displayOdo * 0.621371).roundToInt()} miles" else "$displayOdo km"
            tvRangeBallistic.text = finalRange.toString()
            tvRangeBallisticUnit.text = if (unit == "miles") "miles" else "km"

            val distance = vcuInfoMsg.distance
            if (distance.isNotEmpty()) {
                val ride = distance.toFloat().toInt()
                val finalRide = ride.applyMinMax(sharedViewModel.rideLimit)
                d("Rideeeeeee", "ride:$ride")
                if (unit == "miles") {
                    tvRide.text = (finalRide * 0.621371).roundToInt().toString()
                } else {
                    tvRide.text = finalRide.toString()
                }
            }
            val distanceManuver = if (unit == "miles") "2.3 miles" else "2.3 km"
            tvDistanceBallistic.text = distanceManuver

        } else {
            tvOdo.text = displayOdo.toString()
            tvRange.text = displayRange.toString()
            tvODoUnit.text = if (unit == "miles") "miles" else "km"
            tvRangeUnit.text = if (unit == "miles") "miles" else "km"
            tvDistance.text = if (unit == "miles") "2.3 miles" else "2.3 km"
        }
	
	if (vcuInfoMsg.speed.isNotEmpty()) {
            val isMiles =
                sharedViewModel.distanceUnit.equals("miles", ignoreCase = true)
            val unitText = if (isMiles) "mph" else "km/h"

            if (vcuInfoMsg.speed[0].toInt() == 0)
            {
                tvSpeed.text = "0"
                tvSpeedUnit.text = unitText
            }
        }
    }

    private fun updateVehicleValue(value: FloatArray = floatArrayOf()) {
        if (::tvSpeed.isInitialized) {
            val rawSpeed = value.getOrNull(0)?.toInt()
            val finalSpeed = rawSpeed?.applyMinMax(sharedViewModel.speedLimit) ?: 0

            val rawWhPerKm = value.getOrNull(2)?.toInt() ?: 0
            val finalWhPerKm =
                rawWhPerKm.applyMinMax(sharedViewModel.whPerKmLimit)

            val displaySpeed =
                if (unit == "miles") (finalSpeed * 0.621371).roundToInt() else finalSpeed
            if (isBallistic) {
                tvSpeedBallistic.text = displaySpeed.toString()
                tvSpeedBallisticUnit.text = if (unit == "miles") "mph" else "km/h"
                val displayWh =
                    if (unit == "miles") {

                        (finalWhPerKm / 0.621371).roundToInt()
                    } else {
                        finalWhPerKm
                    }
                tvWhKm.text = displayWh.toString()
                tvWhKmLabel.text = if (unit == "miles") "Wh/mile" else "Wh/km"

            } else {
                tvSpeed.text = displaySpeed.toString()
                tvSpeedUnit.text = if (unit == "miles") "mph" else "km/h"

            }
        }
    }
}





