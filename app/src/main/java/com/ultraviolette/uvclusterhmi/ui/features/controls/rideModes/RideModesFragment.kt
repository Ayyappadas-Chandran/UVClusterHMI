package com.ultraviolette.uvclusterhmi.ui.features.controls.rideModes

import android.os.Bundle
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import com.ultraviolette.uvclusterhmi.ui.viewModel.CarViewModel
import com.ultraviolette.uvclusterhmi.utils.Utilities
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
import kotlinx.coroutines.launch

class RideModesFragment : Fragment() {
    private lateinit var ivBack: ImageView
    private lateinit var tvRain: TextView
    private lateinit var tvStreet: TextView
    private lateinit var tvSport: TextView
    private lateinit var tvCustom: TextView
    private lateinit var tvDualAbsCustom: TextView
    private lateinit var tvMonoAbsCustom: TextView
    private lateinit var tvTcOff: TextView
    private lateinit var tvTc1: TextView
    private lateinit var tvTc2: TextView
    private lateinit var tvTc3: TextView
    private lateinit var tvTcValue: TextView
    private lateinit var ivBikeMode: ImageView
    private lateinit var ivRainSelect: ImageView
    private lateinit var ivStreetSelectRight: ImageView
    private lateinit var ivStreetSelectLeft: ImageView
    private lateinit var ivSportSelectLeft: ImageView
    private lateinit var ivSportSelectRight: ImageView
    private lateinit var ivCustomSelect: ImageView
    private lateinit var ivMonoSelect: ImageView
    private lateinit var ivDualSelect: ImageView
    private lateinit var ivTcOffSelect: ImageView
    private lateinit var ivTc1SelectLeft: ImageView
    private lateinit var ivTc1SelectRight: ImageView
    private lateinit var ivTc2SelectLeft: ImageView
    private lateinit var ivTc2SelectRight: ImageView
    private lateinit var ivTc3Select: ImageView
    private lateinit var clManualMode: ConstraintLayout
    private lateinit var clCustomMode: ConstraintLayout
    private val viewModel: RideModesViewModel by viewModels { ViewModelFactory(context = requireContext()) }
    private val carViewModel by activityViewModels<CarViewModel> { ViewModelFactory(context = requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ride_modes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initClickListener()
        observeRideModeUi()
    }

    private fun observeRideModeUi() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect {
                        render(it)
                    }
                }
                launch {
                    carViewModel.swiftButton.collect { swiftButton ->
                        val button = Utilities.getButtonState(swiftButton)
                        if (button == ButtonNavigation.None) return@collect
                        handleButtonNavigation(button.ordinal)
                    }
                }
                // VCU absMode / mtcMode / tellTales are no longer consumed here —
                // ABS and traction state are written to SharedPreferences by the user
                // action handlers (writeAbsToVcu / writeTractionToVcu) and loaded back
                // via RideModesViewModel.init. If live VCU feedback is required in the
                // future, read active.toolbar.absMode / active.toolbar.mtcMode from
                // ClusterViewModel instead of from CarViewModel.
            }
        }
    }


    private fun render(uiState: RideModesUiState) {
        resetRideModeViews()
        resetCustomModeViews()

        clCustomMode.isVisible = uiState.selectedRideMode == RideMode.Custom
        clManualMode.isVisible = uiState.selectedRideMode != RideMode.Custom

        when (uiState.selectedRideMode) {
            RideMode.Rain -> {
                ivRainSelect.isVisible = true
                ivBikeMode.setImageResource(R.drawable.ic_rain_bike)
                tvRain.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                tvRain.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.activeSelectionRed
                    )
                )
                tvTcValue.text = "t3"
            }

            RideMode.Street -> {
                ivStreetSelectLeft.isVisible = true
                ivStreetSelectRight.isVisible = true
                ivBikeMode.setImageResource(R.drawable.ic_street_bike)
                tvStreet.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                tvStreet.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.activeSelectionRed
                    )
                )
                tvTcValue.text = "t2"
            }

            RideMode.Sport -> {
                ivSportSelectLeft.isVisible = true
                ivSportSelectRight.isVisible = true
                ivBikeMode.setImageResource(R.drawable.ic_sport_bike)
                tvSport.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                tvSport.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.activeSelectionRed
                    )
                )
                tvTcValue.text = "t1"
            }

            RideMode.Custom -> {
                renderCustomMode(uiState)
            }
        }
    }

    private fun renderCustomMode(uiState: RideModesUiState) {
        if (uiState.customModeAbsIsMono) {
            ivMonoSelect.isVisible = false
            tvMonoAbsCustom.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            tvMonoAbsCustom.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
        } else {
            ivDualSelect.isVisible = false
            tvDualAbsCustom.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            tvDualAbsCustom.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
        }

        when (uiState.tractionControlLevel.lowercase()) {
            "off" -> {
                tvTcOff.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                tvTcOff.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            }

            "tc1" -> {

                tvTc1.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                tvTc1.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            }

            "tc2" -> {

                tvTc2.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                tvTc2.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            }

            "tc3" -> {
                tvTc3.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                tvTc3.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
        }

        when (uiState.customModeSubScreen) {
            CustomModeSubScreen.RideModes -> {
                ivCustomSelect.isVisible = true
                tvCustom.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                tvCustom.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.activeSelectionRed
                    )
                )
            }

            CustomModeSubScreen.ABS -> {
                tvCustom.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                tvCustom.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                if (uiState.customModeAbsIsMono) {
                    ivMonoSelect.isVisible = true
                    tvMonoAbsCustom.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                    tvMonoAbsCustom.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.activeSelectionRed
                        )
                    )
                } else {
                    ivDualSelect.isVisible = true
                    tvDualAbsCustom.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                    tvDualAbsCustom.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.activeSelectionRed
                        )
                    )
                }
            }

            CustomModeSubScreen.TractionControl -> {
                tvCustom.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                tvCustom.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                ivCustomSelect.isVisible = false

                when (uiState.tractionControlLevel.lowercase()) {
                    "off" -> {
                        ivTcOffSelect.isVisible = true
                        tvTcOff.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.white
                            )
                        )
                        tvTcOff.setBackgroundColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.activeSelectionRed
                            )
                        )
                    }

                    "tc1" -> {
                        ivTc1SelectLeft.isVisible = true
                        ivTc1SelectRight.isVisible = true
                        tvTc1.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                        tvTc1.setBackgroundColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.activeSelectionRed
                            )
                        )
                    }

                    "tc2" -> {
                        ivTc2SelectLeft.isVisible = true
                        ivTc2SelectRight.isVisible = true
                        tvTc2.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                        tvTc2.setBackgroundColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.activeSelectionRed
                            )
                        )
                    }

                    "tc3" -> {
                        ivTc3Select.isVisible = true
                        tvTc3.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                        tvTc3.setBackgroundColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.activeSelectionRed
                            )
                        )
                    }
                }
            }
        }
    }

    fun handleButtonNavigation(button: Int) {
        when (button) {
            ButtonNavigation.Right.ordinal -> viewModel.onNextMode()
            ButtonNavigation.Left.ordinal -> viewModel.onPreviousMode()
            ButtonNavigation.Bottom.ordinal -> viewModel.onCustomSubScreenCycleUp()
            ButtonNavigation.Top.ordinal -> viewModel.onCustomSubScreenCycleDown()
            ButtonNavigation.Back.ordinal -> findNavController().navigateUp()
        }
    }


    private fun initViews(view: View) {
        ivBack = view.findViewById(R.id.ivBack)
        tvRain = view.findViewById(R.id.tvRain)
        tvStreet = view.findViewById(R.id.tvStreet)
        tvSport = view.findViewById(R.id.tvSport)
        tvCustom = view.findViewById(R.id.tvCustom)
        tvDualAbsCustom = view.findViewById(R.id.tvDualAbsCustom)
        tvMonoAbsCustom = view.findViewById(R.id.tvMonoAbsCustom)
        tvTcOff = view.findViewById(R.id.tvTcOff)
        tvTc1 = view.findViewById(R.id.tvTc1)
        tvTc2 = view.findViewById(R.id.tvTc2)
        tvTc3 = view.findViewById(R.id.tvTc3)
        tvTcValue = view.findViewById(R.id.tvTcValue)

        ivMonoSelect = view.findViewById(R.id.ivMonoSelect)
        ivDualSelect = view.findViewById(R.id.ivDualSelect)
        ivTcOffSelect = view.findViewById(R.id.ivTcOffSelect)
        ivTc1SelectLeft = view.findViewById(R.id.ivTc1SelectLeft)
        ivTc1SelectRight = view.findViewById(R.id.ivTc1SelectRight)
        ivTc2SelectRight = view.findViewById(R.id.ivTc2SelectRight)
        ivTc2SelectLeft = view.findViewById(R.id.ivTc2SelectLeft)
        ivTc3Select = view.findViewById(R.id.ivTc3Select)
        ivBikeMode = view.findViewById(R.id.ivBikeMode)
        ivRainSelect = view.findViewById(R.id.ivRainSelect)
        ivStreetSelectRight = view.findViewById(R.id.ivStreetSelectRight)
        ivStreetSelectLeft = view.findViewById(R.id.ivStreetSelectLeft)
        ivSportSelectLeft = view.findViewById(R.id.ivSportSelectLeft)
        ivSportSelectRight = view.findViewById(R.id.ivSportSelectRight)
        ivCustomSelect = view.findViewById(R.id.ivCustomSelect)

        clManualMode = view.findViewById(R.id.clManualMode)
        clCustomMode = view.findViewById(R.id.clCustomMode)

    }

    private fun initClickListener() {
        tvRain.setOnSoundClickListener(requireContext()) {
            viewModel.onRideModeSelected(RideMode.Rain)
            viewModel.onTractionControlSelected(getString(R.string.tc3))
            viewModel.onCustomModeAbsSelected(true)
        }
        tvStreet.setOnSoundClickListener(requireContext()) {
            viewModel.onRideModeSelected(RideMode.Street)
            viewModel.onCustomModeAbsSelected(true)
            viewModel.onTractionControlSelected(getString(R.string.tc2))
        }
        tvSport.setOnSoundClickListener(requireContext()) {
            viewModel.onRideModeSelected(RideMode.Sport)
            viewModel.onCustomModeAbsSelected(true)
            viewModel.onTractionControlSelected(getString(R.string.tc1))
        }
        tvCustom.setOnSoundClickListener(requireContext()) {
            viewModel.onRideModeSelected(RideMode.Custom)
        }
        ivBack.setOnSoundClickListener(requireContext()) { findNavController().navigateUp() }

        tvDualAbsCustom.setOnSoundClickListener(requireContext()) {
            writeAbsToVcu(false)
            viewModel.onCustomSubScreenChanged(CustomModeSubScreen.ABS)
            viewModel.onCustomModeAbsSelected(false)
        }
        tvMonoAbsCustom.setOnSoundClickListener(requireContext()) {
            writeAbsToVcu(true)
            viewModel.onCustomSubScreenChanged(CustomModeSubScreen.ABS)
            viewModel.onCustomModeAbsSelected(true)

        }
        tvTcOff.setOnSoundClickListener(requireContext()) {
            viewModel.onCustomSubScreenChanged(CustomModeSubScreen.TractionControl)
            viewModel.onTractionControlSelected(getString(R.string.off))
            writeTractionToVcu("off")
        }
        tvTc1.setOnSoundClickListener(requireContext()) {
            viewModel.onCustomSubScreenChanged(CustomModeSubScreen.TractionControl)
            viewModel.onTractionControlSelected(getString(R.string.tc1))
            writeTractionToVcu("tc1")
        }
        tvTc2.setOnSoundClickListener(requireContext()) {
            viewModel.onCustomSubScreenChanged(CustomModeSubScreen.TractionControl)
            viewModel.onTractionControlSelected(getString(R.string.tc2))
            writeTractionToVcu("tc2")
        }
        tvTc3.setOnSoundClickListener(requireContext()) {
            viewModel.onCustomSubScreenChanged(CustomModeSubScreen.TractionControl)
            viewModel.onTractionControlSelected(getString(R.string.tc3))
            writeTractionToVcu("tc3")
        }
    }

    private fun resetRideModeViews() {
        ivRainSelect.isVisible = false
        ivStreetSelectLeft.isVisible = false
        ivStreetSelectRight.isVisible = false
        ivSportSelectLeft.isVisible = false
        ivSportSelectRight.isVisible = false
        ivCustomSelect.isVisible = false
        tvRain.setTextColor(ContextCompat.getColor(requireContext(), R.color.unSelected))
        tvRain.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.transparent))
        tvStreet.setTextColor(ContextCompat.getColor(requireContext(), R.color.unSelected))
        tvStreet.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.transparent))
        tvSport.setTextColor(ContextCompat.getColor(requireContext(), R.color.unSelected))
        tvSport.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.transparent))
        tvCustom.setTextColor(ContextCompat.getColor(requireContext(), R.color.unSelected))
        tvCustom.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.transparent))
    }

    private fun resetCustomModeViews() {
        ivMonoSelect.isVisible = false
        ivDualSelect.isVisible = false
        ivTcOffSelect.isVisible = false
        ivTc1SelectLeft.isVisible = false
        ivTc1SelectRight.isVisible = false
        ivTc2SelectLeft.isVisible = false
        ivTc2SelectRight.isVisible = false
        ivTc3Select.isVisible = false

        tvMonoAbsCustom.setTextColor(ContextCompat.getColor(requireContext(), R.color.unSelected))
        tvMonoAbsCustom.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.transparent
            )
        )

        tvDualAbsCustom.setTextColor(ContextCompat.getColor(requireContext(), R.color.unSelected))
        tvDualAbsCustom.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.transparent
            )
        )

        tvTcOff.setTextColor(ContextCompat.getColor(requireContext(), R.color.unSelected))
        tvTcOff.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.transparent))

        tvTc1.setTextColor(ContextCompat.getColor(requireContext(), R.color.unSelected))
        tvTc1.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.transparent))

        tvTc2.setTextColor(ContextCompat.getColor(requireContext(), R.color.unSelected))
        tvTc2.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.transparent))

        tvTc3.setTextColor(ContextCompat.getColor(requireContext(), R.color.unSelected))
        tvTc3.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.transparent))
    }

    private fun writeAbsToVcu(isMono: Boolean) {
        val value: Byte = if (isMono) 0x4d.toByte() else 0x44.toByte()
        d("APP TO_VCU", "ABS value:$value")
        val packet = byteArrayOf(value)
        carViewModel.sendByteArrayProperty(0x2170037F, packet)
    }

    private fun writeTractionToVcu(tc: String) {
        val value = when (tc.lowercase()) {
            "off" -> 0x01.toByte()
            "tc1" -> 0x02.toByte()
            "tc2" -> 0x03.toByte()
            "tc3" -> 0x04.toByte()
            else -> 0x01.toByte()
        }
        d("APP TO_VCU", "TC value:$value")
        val packet = byteArrayOf(value)
        carViewModel.sendByteArrayProperty(0x2170038F, packet)

    }
}




