package com.ultraviolette.uvclusterhmi.ui.features.controls.trips

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.dataModel.vcuData.TripMeterDisp
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import com.ultraviolette.uvclusterhmi.ui.viewModel.CarViewModel
import com.ultraviolette.uvclusterhmi.ui.viewModel.SharedViewModel
import com.ultraviolette.uvclusterhmi.utils.Utilities
import com.ultraviolette.uvclusterhmi.utils.Utilities.PROP_ID_CUSTOM
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.roundToInt

class TripsFragment : Fragment() {
    private lateinit var ivBack: ImageView
    private lateinit var tvTrip1: TextView
    private lateinit var tvTrip2: TextView
    private lateinit var tvTrip3: TextView
    private lateinit var tvReset: TextView
    private lateinit var tvTripDistanceValue: TextView
    private lateinit var tvTripDurationValue: TextView
    private lateinit var tvAverageSpeedValue: TextView
    private lateinit var ivTrip1Select: ImageView
    private lateinit var ivTrip2SelectRight: ImageView
    private lateinit var ivTrip2SelectLeft: ImageView
    private lateinit var ivTrip3Select: ImageView
    private lateinit var clTrip: ConstraintLayout
    private var tripResetDialog: AlertDialog? = null
    private val viewModel by viewModels<TripsViewModel> { ViewModelFactory(context = requireContext()) }
    private val carViewModel by activityViewModels<CarViewModel> { ViewModelFactory(context = requireContext()) }
    private val sharedViewModel by activityViewModels<SharedViewModel> { ViewModelFactory(context = requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_trips, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initClickListener()
        observeUiState()
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { uiState ->
                        render(uiState)
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
                    carViewModel.tripMeter.collect { trips ->
                        d("Tripss", "Trip value :$trips")
                        handleTrips(trips)
                    }
                }
            }
        }
    }

    private fun handleTrips(tripMeterDisp: TripMeterDisp) {
        if (tripMeterDisp.trip.isEmpty()) {
            tvTripDistanceValue.text = "---"
            tvTripDurationValue.text = "---"
            tvAverageSpeedValue.text = "---"
                return
        }
        val trip = tripMeterDisp.trip[viewModel.getTrips() - 1]
        d("Ttripss", "currentTrip :${viewModel.getTrips()} value :$trip")
        val distanceInKm = trip.distance.toInt()

        val isMiles =
            sharedViewModel.distanceUnit.equals("miles", ignoreCase = true)

        val displayDistance =
            if (isMiles) "${(distanceInKm * 0.621371).roundToInt()} miles" else "$distanceInKm km"

        val avgSpeed = trip.averageSpeed.toInt()

        val displayAvgSpeed = if (isMiles)
            "${(avgSpeed * 0.6211371).roundToInt()} mph"
        else
            "$avgSpeed km/h"

        tvTripDistanceValue.text = displayDistance
        val durationInSecs = trip.tripDuration
        val hours = (durationInSecs / 3600).toInt()
        val minutes = ((durationInSecs % 3600) / 60).toInt()
        d("Ttripss", "Hours :$hours minutes :$minutes")

        tvTripDurationValue.text = buildString {
            append(hours)
            append(" Hrs ")
            append(minutes)
            append(" Mins")
        }
        tvAverageSpeedValue.text = displayAvgSpeed

    }

    private fun render(uiState: TripsUiState) {
        tvReset.apply {
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.transparent))
            setTextColor(ContextCompat.getColor(requireContext(), R.color.unSelected))
        }

        listOf(tvTrip1, tvTrip2, tvTrip3).forEach {
            it.setTextColor(ContextCompat.getColor(requireContext(), R.color.unSelected))
            it.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.transparent))
        }
        ivTrip1Select.isVisible = false
        ivTrip2SelectLeft.isVisible = false
        ivTrip2SelectRight.isVisible = false
        ivTrip3Select.isVisible = false

        val (selectedView, tripDetail) = when (uiState.selectedTrip) {
            1 -> {
                ivTrip1Select.isVisible = true
                Pair(tvTrip1, uiState.tripDetails1)
            }

            2 -> {
                ivTrip2SelectLeft.isVisible = true
                ivTrip2SelectRight.isVisible = true
                Pair(tvTrip2, uiState.tripDetails2)
            }

            else -> {
                ivTrip3Select.isVisible = true
                Pair(tvTrip3, uiState.tripDetails3)
            }
        }

        selectedView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        selectedView.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.activeSelectionRed
            )
        )

        if (uiState.isResetMode) {
            refreshTripUi()
            selectedView.apply {
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
        }
    }


    /**
     * Binds UI components from the provided root view using their IDs.
     *
     * @param view The root view containing the layout elements.
     *
     * Initializes:
     * - tvBack, tvTrip1, tvTrip2, tvTrip3, tvReset (TextViews)
     * - tvTripDistanceValue, tvTripDurationValue, tvAverageSpeedValue (TextViews)
     * - ivTrip1Select, ivTrip2SelectLeft, ivTrip2SelectRight, ivTrip3Select (ImageViews)
     * - clTrip (ConstraintLayout)
     *
     * */
    private fun initViews(view: View) {
        ivBack = view.findViewById(R.id.ivBack)
        tvTrip1 = view.findViewById(R.id.tvTrip1)
        tvTrip2 = view.findViewById(R.id.tvTrip2)
        tvTrip3 = view.findViewById(R.id.tvTrip3)
        tvReset = view.findViewById(R.id.tvReset)
        tvTripDistanceValue = view.findViewById(R.id.tvTripDistanceValue)
        tvTripDurationValue = view.findViewById(R.id.tvTripDurationValue)
        tvAverageSpeedValue = view.findViewById(R.id.tvAverageSpeedValue)

        ivTrip1Select = view.findViewById(R.id.ivSelectTrip1)
        ivTrip2SelectLeft = view.findViewById(R.id.ivTrip2SelectLeft)
        ivTrip2SelectRight = view.findViewById(R.id.ivTrip2SelectRight)
        ivTrip3Select = view.findViewById(R.id.ivTrip3Select)

        clTrip = view.findViewById(R.id.clTrip)
    }

    fun handleButtonNavigation(button: Int) {
        val uiState = viewModel.uiState.value
        when (button) {
            ButtonNavigation.Right.ordinal -> {
                if (!uiState.isResetMode) {
                    when (uiState.selectedTrip) {
                        1 -> viewModel.onTripSelected(2)
                        2 -> viewModel.onTripSelected(3)
                        3 -> viewModel.onTripSelected(1)
                    }
                }
            }

            ButtonNavigation.Left.ordinal -> {
                if (!uiState.isResetMode) {
                    when (uiState.selectedTrip) {
                        1 -> viewModel.onTripSelected(3)
                        2 -> viewModel.onTripSelected(1)
                        3 -> viewModel.onTripSelected(2)
                    }
                }
            }

            ButtonNavigation.Top.ordinal, ButtonNavigation.Bottom.ordinal -> {
                viewModel.onResetState(!uiState.isResetMode)
            }

            ButtonNavigation.Enter.ordinal -> {
                if (uiState.isResetMode) {
                    tvReset.performClick()
                }
            }

            ButtonNavigation.Back.ordinal -> {
                //for bug no 46 - pop up exit on button press
                if (tripResetDialog?.isShowing == true) {
                    tripResetDialog?.dismiss()
                } else {
                    findNavController().navigateUp()
                }
            }
        }
    }

    /**
     * Initializes click listeners for UI components.
     */
    private fun initClickListener() {
        tvTrip1.setOnSoundClickListener(requireContext()) {
            viewModel.onTripSelected(1)
        }
        tvTrip2.setOnSoundClickListener(requireContext()) {
            viewModel.onTripSelected(2)
        }
        tvTrip3.setOnSoundClickListener(requireContext()) {
            viewModel.onTripSelected(3)
        }
        tvReset.setOnSoundClickListener(requireContext()) {
            resetTrip()
            // showResetConfirmationDialog()
        }
        ivBack.setOnSoundClickListener(requireContext()) {
            findNavController().navigateUp()
        }
    }

    private fun showResetConfirmationDialog() {
        val uiState = viewModel.uiState.value
        val tripName = when (uiState.selectedTrip) {
            1 -> getString(R.string.trip_1)
            2 -> getString(R.string.trip_2)
            else -> getString(R.string.trip_3)
        }
        showTripReset(tripName)
        viewModel.resetTripDetails(uiState.selectedTrip)
    }

    private fun refreshTripUi() {
        ivTrip1Select.isVisible = false
        ivTrip2SelectLeft.isVisible = false
        ivTrip2SelectRight.isVisible = false
        ivTrip3Select.isVisible = false
        tvReset.apply {
            setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.activeSelectionRed))
        }
    }

    /**
     * Shows a dialog to confirm trip reset and updates the UI accordingly.
     */
    private fun showTripReset(trip: String) {
        val blur = RenderEffect.createBlurEffect(10f, 10f, Shader.TileMode.CLAMP)
        clTrip.setRenderEffect(blur)
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_complete_message, null)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvTitle)
        val tvMessage = dialogView.findViewById<TextView>(R.id.tvMessage)
        tvTitle.text = getString(R.string.dialog_title_reset, trip)
        tvMessage.text = getString(R.string.reset_done)
        tripResetDialog =
            AlertDialog.Builder(requireContext()).setCancelable(true).setView(dialogView)
                .setOnDismissListener {
                    viewModel.onResetState(false)
                    clTrip.setRenderEffect(null)
                    tripResetDialog = null
                }.create()
        tripResetDialog?.show()
    }

    fun resetTrip() {
        val resetByteArray = (viewModel.getTrips() - 1).toByte()
        d("ByteArray", "resetByteArray:$resetByteArray")
        val byteArray = byteArrayOf(resetByteArray)
        carViewModel.sendByteArrayProperty(PROP_ID_CUSTOM, byteArray)
    }
}


