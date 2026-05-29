package com.ultraviolette.uvclusterhmi.ui.features.controls.performance

import android.os.Bundle
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.ultraviolette.uvclusterhmi.ClusterApplication
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import com.ultraviolette.uvclusterhmi.domain.model.ClusterUiState
import com.ultraviolette.uvclusterhmi.ui.viewModel.CarViewModel
import com.ultraviolette.uvclusterhmi.ui.viewModel.ClusterViewModel
import com.ultraviolette.uvclusterhmi.ui.viewModel.SharedViewModel
import com.ultraviolette.uvclusterhmi.utils.Utilities
import com.ultraviolette.uvclusterhmi.utils.Utilities.getRegenValueForLevel4
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class PerformanceFragment : Fragment() {
    private lateinit var ivBack: ImageView
    private lateinit var tvTenLevels: TextView
    private lateinit var tvFourLevels: TextView
    private lateinit var tvHillHoldOff: TextView
    private lateinit var tvHillHoldOn: TextView
    private lateinit var tvR0: TextView
    private lateinit var tvR3: TextView
    private lateinit var tvR6: TextView
    private lateinit var tvR9: TextView
    private lateinit var tvTcOff: TextView
    private lateinit var tvTc1: TextView
    private lateinit var tvTc2: TextView
    private lateinit var tvTc3: TextView
    private lateinit var ivTcOffSelect: ImageView
    private lateinit var ivTc1SelectLeft: ImageView
    private lateinit var ivTc1SelectRight: ImageView
    private lateinit var ivTc2SelectRight: ImageView
    private lateinit var ivTc2SelectLeft: ImageView
    private lateinit var ivTc3Select: ImageView
    private lateinit var tvRegenValue: TextView
    private lateinit var ivTenLevelsSelect: ImageView
    private lateinit var ivFourLevelsSelect: ImageView
    private lateinit var ivHillHoldOnSelect: ImageView
    private lateinit var ivHillHoldOffSelect: ImageView
    private lateinit var ivR0Select: ImageView
    private lateinit var ivR3SelectRight: ImageView
    private lateinit var ivR3SelectLeft: ImageView
    private lateinit var ivR6SelectLeft: ImageView
    private lateinit var ivR6SelectRight: ImageView
    private lateinit var ivR9Select: ImageView
    private lateinit var ivThumbView: ImageView
    private lateinit var sbRegenLevel: SeekBar
    private lateinit var clRegenLevelFour: ConstraintLayout
    private lateinit var clRegenLevelTen: ConstraintLayout
    private lateinit var tvSurgeModeOff: TextView
    private lateinit var tvSurgeModeOn: TextView
    private lateinit var ivSurgeModeOffSelect: ImageView
    private lateinit var tvDualAbsCustom: TextView
    private lateinit var tvMonoAbsCustom: TextView
    private lateinit var tvCruiseOff: TextView
    private lateinit var tvCruiseOn: TextView
    private lateinit var ivSurgeModeOnSelect: ImageView
    private lateinit var ivDualSelect: ImageView
    private lateinit var ivMonoSelect: ImageView
    private lateinit var tvCruiseSelectOff: ImageView
    private lateinit var tvCruiseSelectOn: ImageView
    private val viewModel by viewModels<PerformanceViewModel> { ViewModelFactory(context = requireContext()) }
    private val carViewModel by activityViewModels<CarViewModel> { ViewModelFactory(context = requireContext()) }
    private val sharedViewModel by activityViewModels<SharedViewModel> { ViewModelFactory(context = requireContext()) }
    private val clusterViewModel: ClusterViewModel by activityViewModels {
        ClusterViewModel.Factory(requireActivity().application as ClusterApplication)
    }
    private val segmentIds = listOf(
        R.id.vRegenLevel1,
        R.id.vRegenLevel2,
        R.id.vRegenLevel3,
        R.id.vRegenLevel4,
        R.id.vRegenLevel5,
        R.id.vRegenLevel6,
        R.id.vRegenLevel7,
        R.id.vRegenLevel8,
        R.id.vRegenLevel9
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_performance, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initClickListener()
        observeUiState()
    }

    private fun observeUiState() {
        viewModel.uiState.onEach { state ->
            render(state)
        }.flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .launchIn(viewLifecycleOwner.lifecycleScope)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    // Handlebar events via ClusterDataBus (replaces carViewModel.swiftButton)
                    clusterViewModel.handlebarButton.collect { swiftButton ->
                        val button = Utilities.getButtonState(swiftButton)
                        if (button == ButtonNavigation.None) return@collect
                        handleButtonNavigation(button.ordinal)
                    }
                }
                // Observe VCU-reported ballisticPlus state from ClusterViewModel.
                // All other vehicle reads (hillHold, tellTales, cruise) are loaded
                // from SharedPreferences via PerformanceViewModel.init and no longer
                // need live VCU updates here.
                launch {
                    clusterViewModel.uiState.collect { uiState ->
                        val active = uiState as? ClusterUiState.Active ?: return@collect
                        viewModel.saveBallisticPlus(active.dashboard.drive.isBallisticPlus)
                    }
                }
            }
        }
    }

    private fun render(state: PerformanceUiState) {
        tvRegenValue.text = getString(R.string.r, state.regenValue)
        handleRegenModesUi(state)
        handleRegenUi(state)
        handleHillHoldUi(state)
        handleSurgeModeUi(state)
        handleAbsModeUi(state)
        handleCruiseUi(state)
        handleTractionControl(state)
    }

    private fun handleRegenUi(state: PerformanceUiState) {
        val isRegenSelected = state.focusedState == FocusedState.Regen
        if (state.is10Levels) {
            moveThumb(state.regenValue, !isRegenSelected)
            updateRegenLevel(
                state.regenValue,
                if (isRegenSelected) R.color.activeSelectionRed else R.color.white
            )
        } else {
            val regenValue = getRegenValueForLevel4(state.regenValue)
            handleLevel4UiState(regenValue, isRegenSelected)
        }
    }

    private fun handleRegenModesUi(state: PerformanceUiState) {
        val isRegenModeSelected = state.focusedState == FocusedState.RegenModes
        val selectedTxtClr = if (isRegenModeSelected) R.color.white else R.color.black
        val selectedBgClr = if (isRegenModeSelected) R.color.activeSelectionRed else R.color.white
        val unselectedTxtClr = R.color.unSelected
        val unselectedBgClr = R.color.transparent
        val actualOnTxtClr = if (state.is10Levels) selectedTxtClr else unselectedTxtClr
        val actualOnBgClr = if (state.is10Levels) selectedBgClr else unselectedBgClr
        val actualOffTxtClr = if (state.is10Levels) unselectedTxtClr else selectedTxtClr
        val actualOffBgClr = if (state.is10Levels) unselectedBgClr else selectedBgClr
        tvTenLevels.apply {
            setTextColor(ContextCompat.getColor(requireContext(), actualOnTxtClr))
            setBackgroundColor(ContextCompat.getColor(requireContext(), actualOnBgClr))
        }
        tvFourLevels.apply {
            setTextColor(ContextCompat.getColor(requireContext(), actualOffTxtClr))
            setBackgroundColor(ContextCompat.getColor(requireContext(), actualOffBgClr))
        }
        ivTenLevelsSelect.isVisible = state.is10Levels && isRegenModeSelected
        ivFourLevelsSelect.isVisible = !state.is10Levels && isRegenModeSelected
        clRegenLevelTen.isVisible = state.is10Levels
        clRegenLevelFour.isVisible = !state.is10Levels
    }

    private fun handleTractionControl(state: PerformanceUiState) {
        val isTractionControl = state.focusedState == FocusedState.TractionControl
        val selectedTxtClr = if (isTractionControl) R.color.white else R.color.black
        val selectedBgClr = if (isTractionControl) R.color.activeSelectionRed else R.color.white
        val unselectedTxtClr = R.color.unSelected
        val unselectedBgClr = R.color.transparent
        when (state.tractionLevel) {
            getString(R.string.off) -> {
                tvTcOff.apply {
                    setTextColor(ContextCompat.getColor(requireContext(), selectedTxtClr))
                    setBackgroundColor(ContextCompat.getColor(requireContext(), selectedBgClr))
                }
                tvTc1.apply {
                    setTextColor(ContextCompat.getColor(requireContext(), unselectedTxtClr))
                    setBackgroundColor(ContextCompat.getColor(requireContext(), unselectedBgClr))
                }
                tvTc2.apply {
                    setTextColor(ContextCompat.getColor(requireContext(), unselectedTxtClr))
                    setBackgroundColor(ContextCompat.getColor(requireContext(), unselectedBgClr))
                }
                tvTc3.apply {
                    setTextColor(ContextCompat.getColor(requireContext(), unselectedTxtClr))
                    setBackgroundColor(ContextCompat.getColor(requireContext(), unselectedBgClr))
                }
                ivTcOffSelect.visibility = if(isTractionControl)View.VISIBLE else View.INVISIBLE
                ivTc3Select.visibility = View.INVISIBLE
                ivTc1SelectLeft.visibility = View.INVISIBLE
                ivTc1SelectRight.visibility = View.INVISIBLE
                ivTc2SelectLeft.visibility = View.INVISIBLE
                ivTc2SelectRight.visibility = View.INVISIBLE
            }

            getString(R.string.tc1) -> {
                tvTcOff.apply {
                    setTextColor(ContextCompat.getColor(requireContext(), unselectedTxtClr))
                    setBackgroundColor(ContextCompat.getColor(requireContext(), unselectedBgClr))
                }
                tvTc1.apply {
                    setTextColor(ContextCompat.getColor(requireContext(), selectedTxtClr))
                    setBackgroundColor(ContextCompat.getColor(requireContext(), selectedBgClr))
                }
                tvTc2.apply {
                    setTextColor(ContextCompat.getColor(requireContext(), unselectedTxtClr))
                    setBackgroundColor(ContextCompat.getColor(requireContext(), unselectedBgClr))
                }
                tvTc3.apply {
                    setTextColor(ContextCompat.getColor(requireContext(), unselectedTxtClr))
                    setBackgroundColor(ContextCompat.getColor(requireContext(), unselectedBgClr))
                }

                ivTcOffSelect.visibility = View.INVISIBLE
                ivTc3Select.visibility = View.INVISIBLE
                ivTc1SelectLeft.visibility = if(isTractionControl) View.VISIBLE else View.INVISIBLE
                ivTc1SelectRight.visibility = View.VISIBLE
                ivTc2SelectLeft.visibility = View.INVISIBLE
                ivTc2SelectRight.visibility = View.INVISIBLE
            }

            getString(R.string.tc2) -> {
                tvTcOff.apply {
                    setTextColor(ContextCompat.getColor(requireContext(), unselectedTxtClr))
                    setBackgroundColor(ContextCompat.getColor(requireContext(), unselectedBgClr))
                }
                tvTc2.apply {
                    setTextColor(ContextCompat.getColor(requireContext(), selectedTxtClr))
                    setBackgroundColor(ContextCompat.getColor(requireContext(), selectedBgClr))
                }
                tvTc1.apply {
                    setTextColor(ContextCompat.getColor(requireContext(), unselectedTxtClr))
                    setBackgroundColor(ContextCompat.getColor(requireContext(), unselectedBgClr))
                }
                tvTc3.apply {
                    setTextColor(ContextCompat.getColor(requireContext(), unselectedTxtClr))
                    setBackgroundColor(ContextCompat.getColor(requireContext(), unselectedBgClr))
                }

                ivTcOffSelect.visibility = View.INVISIBLE
                ivTc3Select.visibility = View.INVISIBLE
                ivTc1SelectLeft.visibility = View.INVISIBLE
                ivTc1SelectRight.visibility = View.INVISIBLE
                ivTc2SelectLeft.visibility = if(isTractionControl) View.VISIBLE else View.INVISIBLE
                ivTc2SelectRight.visibility = if(isTractionControl) View.VISIBLE else View.INVISIBLE
            }

            getString(R.string.tc3) -> {
                tvTcOff.apply {
                    setTextColor(ContextCompat.getColor(requireContext(), unselectedTxtClr))
                    setBackgroundColor(ContextCompat.getColor(requireContext(), unselectedBgClr))
                }
                tvTc3.apply {
                    setTextColor(ContextCompat.getColor(requireContext(), selectedTxtClr))
                    setBackgroundColor(ContextCompat.getColor(requireContext(), selectedBgClr))
                }
                tvTc1.apply {
                    setTextColor(ContextCompat.getColor(requireContext(), unselectedTxtClr))
                    setBackgroundColor(ContextCompat.getColor(requireContext(), unselectedBgClr))
                }
                tvTc2.apply {
                    setTextColor(ContextCompat.getColor(requireContext(), unselectedTxtClr))
                    setBackgroundColor(ContextCompat.getColor(requireContext(), unselectedBgClr))
                }

                ivTc3Select.visibility = if(isTractionControl) View.VISIBLE else View.INVISIBLE
                ivTc1SelectLeft.visibility = View.INVISIBLE
                ivTc1SelectRight.visibility = View.INVISIBLE
                ivTc2SelectLeft.visibility = View.INVISIBLE
                ivTc2SelectRight.visibility = View.INVISIBLE
            }
        }
    }

    private fun handleHillHoldUi(state: PerformanceUiState) {
        val isHillHoldSelected = state.focusedState == FocusedState.HillHold
        val selectedTxtClr = if (isHillHoldSelected) R.color.white else R.color.black
        val selectedBgClr = if (isHillHoldSelected) R.color.activeSelectionRed else R.color.white
        val unselectedTxtClr = R.color.unSelected
        val unselectedBgClr = R.color.transparent
        val actualOnTxtClr = if (state.isHillHold) selectedTxtClr else unselectedTxtClr
        val actualOnBgClr = if (state.isHillHold) selectedBgClr else unselectedBgClr
        val actualOffTxtClr = if (state.isHillHold) unselectedTxtClr else selectedTxtClr
        val actualOffBgClr = if (state.isHillHold) unselectedBgClr else selectedBgClr
        tvHillHoldOn.apply {
            setTextColor(ContextCompat.getColor(requireContext(), actualOnTxtClr))
            setBackgroundColor(ContextCompat.getColor(requireContext(), actualOnBgClr))
        }
        tvHillHoldOff.apply {
            setTextColor(ContextCompat.getColor(requireContext(), actualOffTxtClr))
            setBackgroundColor(ContextCompat.getColor(requireContext(), actualOffBgClr))
        }
        ivHillHoldOnSelect.isVisible = state.isHillHold && isHillHoldSelected
        ivHillHoldOffSelect.isVisible = !state.isHillHold && isHillHoldSelected
    }

    private fun handleSurgeModeUi(state: PerformanceUiState) {
        val isBallisticPlusSelected = state.focusedState == FocusedState.BallisticPlus
        d("BallisticPlus", "BallisticPlus: $isBallisticPlusSelected")
        val selectedTxtClr = if (isBallisticPlusSelected) R.color.white else R.color.black
        val selectedBgClr =
            if (isBallisticPlusSelected) R.color.activeSelectionRed else R.color.white
        val unselectedTxtClr = R.color.unSelected
        val unselectedBgClr = R.color.transparent
        val actualOnTxtClr = if (state.isBallisticPlus) selectedTxtClr else unselectedTxtClr
        val actualOnBgClr = if (state.isBallisticPlus) selectedBgClr else unselectedBgClr
        val actualOffTxtClr = if (state.isBallisticPlus) unselectedTxtClr else selectedTxtClr
        val actualOffBgClr = if (state.isBallisticPlus) unselectedBgClr else selectedBgClr
        tvSurgeModeOn.apply {
            setTextColor(ContextCompat.getColor(requireContext(), actualOnTxtClr))
            setBackgroundColor(ContextCompat.getColor(requireContext(), actualOnBgClr))
        }
        tvSurgeModeOff.apply {
            setTextColor(ContextCompat.getColor(requireContext(), actualOffTxtClr))
            setBackgroundColor(ContextCompat.getColor(requireContext(), actualOffBgClr))
        }
        ivSurgeModeOnSelect.isVisible = state.isBallisticPlus && isBallisticPlusSelected
        ivSurgeModeOffSelect.isVisible = !state.isBallisticPlus && isBallisticPlusSelected
    }

    private fun handleAbsModeUi(state: PerformanceUiState) {
        val isAbsMono = state.focusedState == FocusedState.Abs
        val selectedTxtClr = if (isAbsMono) R.color.white else R.color.black
        val selectedBgClr = if (isAbsMono) R.color.activeSelectionRed else R.color.white
        val unselectedTxtClr = R.color.unSelected
        val unselectedBgClr = R.color.transparent
        val actualOnTxtClr = if (state.isMonoAbs) selectedTxtClr else unselectedTxtClr
        val actualOnBgClr = if (state.isMonoAbs) selectedBgClr else unselectedBgClr
        val actualOffTxtClr = if (state.isMonoAbs) unselectedTxtClr else selectedTxtClr
        val actualOffBgClr = if (state.isMonoAbs) unselectedBgClr else selectedBgClr
        tvMonoAbsCustom.apply {
            setTextColor(ContextCompat.getColor(requireContext(), actualOnTxtClr))
            setBackgroundColor(ContextCompat.getColor(requireContext(), actualOnBgClr))
        }
        tvDualAbsCustom.apply {
            setTextColor(ContextCompat.getColor(requireContext(), actualOffTxtClr))
            setBackgroundColor(ContextCompat.getColor(requireContext(), actualOffBgClr))
        }
        ivMonoSelect.isVisible = state.isMonoAbs && isAbsMono
        ivDualSelect.isVisible = !state.isMonoAbs && isAbsMono
    }

    private fun handleCruiseUi(state: PerformanceUiState) {
        val isCruiseState = state.focusedState == FocusedState.CruiseControl
        val selectedTxtClr = if (isCruiseState) R.color.white else R.color.black
        val selectedBgClr = if (isCruiseState) R.color.activeSelectionRed else R.color.white
        val unselectedTxtClr = R.color.unSelected
        val unselectedBgClr = R.color.transparent
        val actualOnTxtClr = if (state.isCruise) selectedTxtClr else unselectedTxtClr
        val actualOnBgClr = if (state.isCruise) selectedBgClr else unselectedBgClr
        val actualOffTxtClr = if (state.isCruise) unselectedTxtClr else selectedTxtClr
        val actualOffBgClr = if (state.isCruise) unselectedBgClr else selectedBgClr
        tvCruiseOff.apply {
            setTextColor(ContextCompat.getColor(requireContext(), actualOffTxtClr))
            setBackgroundColor(ContextCompat.getColor(requireContext(), actualOffBgClr))
        }
        tvCruiseOn.apply {
            setTextColor(ContextCompat.getColor(requireContext(), actualOnTxtClr))
            setBackgroundColor(ContextCompat.getColor(requireContext(), actualOnBgClr))
        }
        tvCruiseSelectOff.isVisible = state.isCruise && isCruiseState
        tvCruiseSelectOn.isVisible = !state.isCruise && isCruiseState
    }

    private fun initViews(view: View) {
        ivBack = view.findViewById(R.id.ivBack)
        tvTenLevels = view.findViewById(R.id.tvTenLevels)
        tvFourLevels = view.findViewById(R.id.tvFourLevels)
        tvHillHoldOff = view.findViewById(R.id.tvHillHoldOff)
        tvHillHoldOn = view.findViewById(R.id.tvHillHoldOn)
        tvR0 = view.findViewById(R.id.tvR0)
        tvR3 = view.findViewById(R.id.tvR3)
        tvR6 = view.findViewById(R.id.tvR6)
        tvR9 = view.findViewById(R.id.tvR9)
        tvRegenValue = view.findViewById(R.id.tvRegenValue)

        tvTcOff = view.findViewById(R.id.tvTcOff)
        tvTc1 = view.findViewById(R.id.tvTc1)
        tvTc2 = view.findViewById(R.id.tvTc2)
        tvTc3 = view.findViewById(R.id.tvTc3)
        ivTcOffSelect = view.findViewById(R.id.ivTcOffSelect)
        ivTc1SelectLeft = view.findViewById(R.id.ivTc1SelectLeft)
        ivTc1SelectRight = view.findViewById(R.id.ivTc1SelectRight)
        ivTc2SelectRight = view.findViewById(R.id.ivTc2SelectRight)
        ivTc2SelectLeft = view.findViewById(R.id.ivTc2SelectLeft)
        ivTc3Select = view.findViewById(R.id.ivTc3Select)
        ivMonoSelect = view.findViewById(R.id.ivMonoSelect)
        ivDualSelect = view.findViewById(R.id.ivDualSelect)
        tvCruiseSelectOff = view.findViewById(R.id.ivCruiseSelectOff)
        tvCruiseSelectOn = view.findViewById(R.id.ivCruiseSelectOn)


        tvMonoAbsCustom = view.findViewById(R.id.tvMonoAbsCustom)
        tvDualAbsCustom = view.findViewById(R.id.tvDualAbsCustom)
        tvCruiseOff = view.findViewById(R.id.tvCruiseOff)
        tvCruiseOn = view.findViewById(R.id.tvCruiseOn)

        ivTenLevelsSelect = view.findViewById(R.id.ivTenLevelsSelect)
        ivFourLevelsSelect = view.findViewById(R.id.ivFourLevelsSelect)
        ivHillHoldOnSelect = view.findViewById(R.id.ivHillHoldOnSelect)
        ivHillHoldOffSelect = view.findViewById(R.id.ivHillHoldOffSelect)
        ivR0Select = view.findViewById(R.id.ivR0Select)
        ivR3SelectRight = view.findViewById(R.id.ivR3SelectRight)
        ivR3SelectLeft = view.findViewById(R.id.ivR3SelectLeft)
        ivR6SelectLeft = view.findViewById(R.id.ivR6SelectLeft)
        ivR6SelectRight = view.findViewById(R.id.ivR6SelectRight)
        ivR9Select = view.findViewById(R.id.ivR9Select)

        sbRegenLevel = view.findViewById(R.id.sbRegenLevel)
        clRegenLevelFour = view.findViewById(R.id.clRegenLevelFour)
        clRegenLevelTen = view.findViewById(R.id.clRegenLevelTen)
        ivThumbView = view.findViewById(R.id.ivThumbView)
        tvSurgeModeOff = view.findViewById(R.id.tvSurgeModeOff)
        tvSurgeModeOn = view.findViewById(R.id.tvSurgeModeOn)
        ivSurgeModeOffSelect = view.findViewById(R.id.ivSurgeModeOffSelect)
        ivSurgeModeOnSelect = view.findViewById(R.id.ivSurgeModeOnSelect)

    }

    fun handleButtonNavigation(button: Int) {
        val uiState = viewModel.uiState.value
        when (button) {
            ButtonNavigation.Right.ordinal -> {
                when (uiState.focusedState) {
                    FocusedState.RegenModes -> {
                        viewModel.saveRegenModes(!uiState.is10Levels)
                    }
                    FocusedState.Regen -> {
                        if (uiState.is10Levels) {
                            val progress = (uiState.regenValue + 1).coerceAtMost(9)
                            val dataVal: Byte = progress.toByte()
                            val packet = byteArrayOf(dataVal)
                            carViewModel.sendByteArrayProperty(0x2170039F, packet)
                            viewModel.saveRegenValue(progress)
                        } else {
                            val regenValue = getRegenValueForLevel4(uiState.regenValue)
                            when (regenValue) {
                                0 ->{
                                    val dataVal: Byte = 3.toByte()
                                    val packet = byteArrayOf(dataVal)
                                    carViewModel.sendByteArrayProperty(0x2170039F, packet)
                                    viewModel.saveRegenValue(3)

                                }
                                3 -> {
                                    val dataVal: Byte = 6.toByte()
                                    val packet = byteArrayOf(dataVal)
                                    carViewModel.sendByteArrayProperty(0x2170039F, packet)
                                    viewModel.saveRegenValue(6)
                                }
                                6 -> {
                                    val dataVal: Byte = 9.toByte()
                                    val packet = byteArrayOf(dataVal)
                                    carViewModel.sendByteArrayProperty(0x2170039F, packet)
                                    viewModel.saveRegenValue(9)
                                }
                                9 -> {
                                    val dataVal: Byte = 0.toByte()
                                    val packet = byteArrayOf(dataVal)
                                    carViewModel.sendByteArrayProperty(0x2170039F, packet)
                                    viewModel.saveRegenValue(0)
                                }
                            }
                        }
                    }

                    FocusedState.HillHold -> viewModel.saveHillHold(!uiState.isHillHold)
                    FocusedState.BallisticPlus -> viewModel.saveBallisticPlus(!uiState.isBallisticPlus)
                    FocusedState.Abs -> viewModel.saveAbs(!uiState.isMonoAbs)
                    FocusedState.CruiseControl -> viewModel.saveCruise(!uiState.isCruise)
                    FocusedState.TractionControl -> {
                        when (uiState.tractionLevel) {
                            getString(R.string.off) -> viewModel.saveTractionLevel(getString(R.string.tc1))
                            getString(R.string.tc1) -> viewModel.saveTractionLevel(getString(R.string.tc2))
                            getString(R.string.tc2) -> viewModel.saveTractionLevel(getString(R.string.tc3))
                            getString(R.string.tc3) -> viewModel.saveTractionLevel(getString(R.string.off))
                        }
                    }
                }
            }

            ButtonNavigation.Left.ordinal -> {
                when (uiState.focusedState) {
                    FocusedState.RegenModes ->{
                        viewModel.saveRegenModes(!uiState.is10Levels)
                    }
                    FocusedState.Regen -> {
                        if (uiState.is10Levels) {
                            val progress = (uiState.regenValue - 1).coerceAtLeast(0)
                            val dataVal: Byte = progress.toByte()
                            val packet = byteArrayOf(dataVal)
                            viewModel.saveRegenValue(progress)
                            carViewModel.sendByteArrayProperty(0x2170039F, packet)
                            viewModel.saveRegenValue(progress)
                        } else {
                            val regenValue = getRegenValueForLevel4(uiState.regenValue)
                            when (regenValue) {
                                0 -> {
                                    val dataVal: Byte = 9.toByte()
                                    val packet = byteArrayOf(dataVal)
                                    viewModel.saveRegenValue(9)
                                    carViewModel.sendByteArrayProperty(0x2170039F, packet)
                                    viewModel.saveRegenValue(9)
                                }
                                3 -> {
                                    val dataVal: Byte = 0.toByte()
                                    val packet = byteArrayOf(dataVal)
                                    carViewModel.sendByteArrayProperty(0x2170039F, packet)
                                    viewModel.saveRegenValue(0)
                                }
                                6 -> {
                                    val dataVal: Byte = 3.toByte()
                                    val packet = byteArrayOf(dataVal)
                                    carViewModel.sendByteArrayProperty(0x2170039F, packet)
                                    viewModel.saveRegenValue(3)
                                }
                                9 ->{
                                    val dataVal: Byte = 6.toByte()
                                    val packet = byteArrayOf(dataVal)
                                    carViewModel.sendByteArrayProperty(0x2170039F, packet)
                                    viewModel.saveRegenValue(6)

                                }
                            }
                        }
                    }

                    FocusedState.HillHold -> viewModel.saveHillHold(!uiState.isHillHold)
                    FocusedState.BallisticPlus -> viewModel.saveBallisticPlus(!uiState.isBallisticPlus)
                    FocusedState.Abs -> viewModel.saveAbs(!uiState.isMonoAbs)
                    FocusedState.CruiseControl -> viewModel.saveCruise(!uiState.isCruise)
                    FocusedState.TractionControl -> {
                        when (uiState.tractionLevel) {
                            getString(R.string.off) -> viewModel.saveTractionLevel(getString(R.string.tc3))
                            getString(R.string.tc1) -> viewModel.saveTractionLevel(getString(R.string.off))
                            getString(R.string.tc2) -> viewModel.saveTractionLevel(getString(R.string.tc1))
                            getString(R.string.tc3) -> viewModel.saveTractionLevel(getString(R.string.tc2))
                        }
                    }
                }
            }

            ButtonNavigation.Bottom.ordinal -> {
                val nextState = when (uiState.focusedState) {
                    FocusedState.BallisticPlus -> FocusedState.RegenModes
                    FocusedState.RegenModes -> FocusedState.Regen
                    FocusedState.Regen -> FocusedState.HillHold
                    FocusedState.HillHold -> FocusedState.Abs
                    FocusedState.Abs -> FocusedState.CruiseControl
                    FocusedState.CruiseControl -> FocusedState.TractionControl
                    FocusedState.TractionControl -> FocusedState.BallisticPlus

                }
                viewModel.setFocusedState(nextState)
            }

            ButtonNavigation.Top.ordinal -> {
                val previousState = when (uiState.focusedState) {
                    FocusedState.BallisticPlus -> FocusedState.TractionControl
                    FocusedState.RegenModes -> FocusedState.BallisticPlus
                    FocusedState.Regen -> FocusedState.RegenModes
                    FocusedState.HillHold -> FocusedState.Regen
                    FocusedState.Abs -> FocusedState.HillHold
                    FocusedState.CruiseControl -> FocusedState.Abs
                    FocusedState.TractionControl -> FocusedState.CruiseControl
                }
                viewModel.setFocusedState(previousState)
            }

            ButtonNavigation.Back.ordinal -> {
                findNavController().navigateUp()
            }
        }
    }

    private fun initClickListener() {
        ivBack.setOnSoundClickListener(requireContext()) {
            findNavController().navigateUp()
        }
        tvTenLevels.setOnSoundClickListener(requireContext()) {
            viewModel.setFocusedState(FocusedState.RegenModes)
            viewModel.saveRegenModes(true)
        }
        tvFourLevels.setOnSoundClickListener(requireContext()) {
            //return@setOnSoundClickListener
            viewModel.setFocusedState(FocusedState.RegenModes)
            viewModel.saveRegenModes(false)
        }
        tvHillHoldOn.setOnSoundClickListener(requireContext()) {
            viewModel.setFocusedState(FocusedState.HillHold)
            viewModel.saveHillHold(true)
            sharedViewModel.setHillHold(true)
            writeHillHoldToVcu(true)
        }
        tvHillHoldOff.setOnSoundClickListener(requireContext()) {
            viewModel.setFocusedState(FocusedState.HillHold)
            viewModel.saveHillHold(false)
            sharedViewModel.setHillHold(false)
            writeHillHoldToVcu(false)
        }
        tvSurgeModeOn.setOnSoundClickListener(requireContext()) {
            viewModel.setFocusedState(FocusedState.BallisticPlus)
            viewModel.saveBallisticPlus(true)
            writeSurgeModeToVcu(true)
        }
        tvSurgeModeOff.setOnSoundClickListener(requireContext()) {
            viewModel.setFocusedState(FocusedState.BallisticPlus)
            viewModel.saveBallisticPlus(false)
            writeSurgeModeToVcu(false)
        }
        tvR0.setOnSoundClickListener(requireContext()) {
            viewModel.setFocusedState(FocusedState.Regen)
            viewModel.saveRegenValue(0)
            val dataVal: Byte = 0.toByte()
            val packet = byteArrayOf(dataVal)
            carViewModel.sendByteArrayProperty(0x2170039F, packet)
        }
        tvR3.setOnSoundClickListener(requireContext()) {
            viewModel.setFocusedState(FocusedState.Regen)
            viewModel.saveRegenValue(3)
            val dataVal: Byte = 3.toByte()
            val packet = byteArrayOf(dataVal)
            carViewModel.sendByteArrayProperty(0x2170039F, packet)
        }
        tvR6.setOnSoundClickListener(requireContext()) {
            viewModel.setFocusedState(FocusedState.Regen)
            viewModel.saveRegenValue(6)
            val dataVal: Byte = 6.toByte()
            val packet = byteArrayOf(dataVal)
            carViewModel.sendByteArrayProperty(0x2170039F, packet)
        }
        tvR9.setOnSoundClickListener(requireContext()) {
            viewModel.setFocusedState(FocusedState.Regen)
            viewModel.saveRegenValue(9)
            val dataVal: Byte = 9.toByte()
            val packet = byteArrayOf(dataVal)
            carViewModel.sendByteArrayProperty(0x2170039F, packet)
        }
        tvMonoAbsCustom.setOnSoundClickListener(requireContext()){
            writeAbsToVcu(true)
            viewModel.setFocusedState(FocusedState.Abs)
            viewModel.saveAbs(true)
        }

        tvDualAbsCustom.setOnSoundClickListener(requireContext()){
            writeAbsToVcu(false)
            viewModel.setFocusedState(FocusedState.Abs)
            viewModel.saveAbs(false)
        }
        tvCruiseOff.setOnSoundClickListener(requireContext()){
            viewModel.setFocusedState(FocusedState.CruiseControl)
            viewModel.saveCruise(false)
            writeCruiseToVcu(false)
        }

        tvCruiseOn.setOnSoundClickListener(requireContext()){
            viewModel.setFocusedState(FocusedState.CruiseControl)
            viewModel.saveCruise(true)
            writeCruiseToVcu(true)
        }

        tvTcOff.setOnSoundClickListener(requireContext()){
            viewModel.setFocusedState(FocusedState.TractionControl)
            viewModel.saveTractionLevel(getString(R.string.off))
            writeTractionToVcu("off")

        }

        tvTc1.setOnSoundClickListener(requireContext()){
            viewModel.setFocusedState(FocusedState.TractionControl)
            viewModel.saveTractionLevel(getString(R.string.tc1))
            writeTractionToVcu("tc1")
        }

        tvTc2.setOnSoundClickListener(requireContext()){
            viewModel.setFocusedState(FocusedState.TractionControl)
            viewModel.saveTractionLevel(getString(R.string.tc2))
            writeTractionToVcu("tc2")
        }

        tvTc3.setOnSoundClickListener(requireContext()){
            viewModel.setFocusedState(FocusedState.TractionControl)
            viewModel.saveTractionLevel(getString(R.string.tc3))
            writeTractionToVcu("tc3")
        }

        sbRegenLevel.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.setFocusedState(FocusedState.Regen)
                    val dataVal: Byte = progress.toByte()
                    val packet = byteArrayOf(dataVal)
                    viewModel.saveRegenValue(progress)
                    viewModel.saveRegenValue(progress)
                    carViewModel.sendByteArrayProperty(0x2170039F, packet)
                }
            }

            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

    }

    private fun writeAbsToVcu(isMono: Boolean) {
        val value: Byte = if (isMono) 0x4d.toByte() else 0x44.toByte()
        d("APP TO_VCU", "ABS value:$value")
        val packet = byteArrayOf(value)
        carViewModel.sendByteArrayProperty(0x2170037F, packet)
    }

    private fun writeCruiseToVcu(isCruise: Boolean) {
        val value: Byte = if (isCruise) 0xEC.toByte() else 0xDC.toByte()
        d("APP TO_VCU", "Cruise value:$value")
        val packet = byteArrayOf(value)
        carViewModel.sendByteArrayProperty(0x217002D0, packet)
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

    private fun moveThumb(level: Int, hide: Boolean) {
        val segmentView = view?.findViewById<View>(
            segmentIds[level.coerceIn(0, segmentIds.size - 1)]
        ) ?: return

        val constraintLayout = view?.findViewById<ConstraintLayout>(R.id.clRegenLevelTen) ?: return
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)
        constraintSet.connect(
            R.id.ivThumbView,
            ConstraintSet.START,
            segmentView.id,
            ConstraintSet.START,
            -6
        )

        constraintSet.connect(
            R.id.ivThumbView,
            ConstraintSet.TOP,
            segmentView.id,
            ConstraintSet.TOP
        )

        constraintSet.applyTo(constraintLayout)
        val thumbView = view?.findViewById<View>(R.id.ivThumbView)
        thumbView?.visibility = if (hide) View.INVISIBLE else View.VISIBLE
    }

    private fun updateRegenLevel(currentLevel: Int, color: Int) {
        segmentIds.forEachIndexed { index, id ->
            val segmentView = view?.findViewById<View>(id) ?: return@forEachIndexed
            val bg = if (index < currentLevel) {
                color
            } else {
                R.drawable.bg_segment_inactive
            }
            segmentView.setBackgroundResource(bg)
        }
    }

    private fun handleLevel4UiState(
        levelFourRegenValue: Int, isClicked: Boolean
    ) {
        val selectedTxtClr = if (isClicked) R.color.white else R.color.black
        val selectedBgClr = if (isClicked) R.color.activeSelectionRed else R.color.white
        val unselectedTxtClr = R.color.unSelected
        val unselectedBgClr = R.color.transparent
        tvR0.apply {
            val txtClr = if (levelFourRegenValue == 0) selectedTxtClr else unselectedTxtClr
            val bgClr = if (levelFourRegenValue == 0) selectedBgClr else unselectedBgClr
            setTextColor(ContextCompat.getColor(requireContext(), txtClr))
            setBackgroundColor(ContextCompat.getColor(requireContext(), bgClr))
        }

        tvR3.apply {
            val txtClr = if (levelFourRegenValue == 3) selectedTxtClr else unselectedTxtClr
            val bgClr = if (levelFourRegenValue == 3) selectedBgClr else unselectedBgClr
            setTextColor(ContextCompat.getColor(requireContext(), txtClr))
            setBackgroundColor(ContextCompat.getColor(requireContext(), bgClr))
        }

        tvR6.apply {
            val txtClr = if (levelFourRegenValue == 6) selectedTxtClr else unselectedTxtClr
            val bgClr = if (levelFourRegenValue == 6) selectedBgClr else unselectedBgClr
            setTextColor(ContextCompat.getColor(requireContext(), txtClr))
            setBackgroundColor(ContextCompat.getColor(requireContext(), bgClr))
        }

        tvR9.apply {
            val txtClr = if (levelFourRegenValue == 9) selectedTxtClr else unselectedTxtClr
            val bgClr = if (levelFourRegenValue == 9) selectedBgClr else unselectedBgClr
            setTextColor(ContextCompat.getColor(requireContext(), txtClr))
            setBackgroundColor(ContextCompat.getColor(requireContext(), bgClr))
        }

        ivR0Select.isVisible = levelFourRegenValue == 0 && isClicked
        ivR3SelectRight.isVisible = levelFourRegenValue == 3 && isClicked
        ivR3SelectLeft.isVisible = levelFourRegenValue == 3 && isClicked
        ivR6SelectLeft.isVisible = levelFourRegenValue == 6 && isClicked
        ivR6SelectRight.isVisible = levelFourRegenValue == 6 && isClicked
        ivR9Select.isVisible = levelFourRegenValue == 9 && isClicked
    }

    private fun writeHillHoldToVcu(isOn: Boolean) {
        val value: Byte = if (isOn) (0xC1).toByte() else (0xC0).toByte()
        d("APP TO_VCU", "HillHold value:$value")
        val packet = byteArrayOf(value)
        carViewModel.sendByteArrayProperty(0x2170035F, packet)
    }

    private fun writeSurgeModeToVcu(isOn: Boolean) {
        val value: Byte = if (isOn) (0xB2).toByte() else (0xB1).toByte()
        d("APP TO_VCU", "SurgeMode value:$isOn")
        val packet = byteArrayOf(value)
        carViewModel.sendByteArrayProperty(0x2170032F, packet)
    }


}


