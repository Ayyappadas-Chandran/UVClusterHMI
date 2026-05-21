package com.ultraviolette.uvclusterhmi.ui.features.controls.advanceFeatures.radar

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
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import com.ultraviolette.uvclusterhmi.ui.viewModel.CarViewModel
import com.ultraviolette.uvclusterhmi.ui.viewModel.SharedViewModel
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
import kotlinx.coroutines.launch

class RadarFragment : Fragment() {
    private lateinit var tvRadarOn: TextView
    private lateinit var tvRadarOff: TextView
    private lateinit var tvConsoleAlertsOn: TextView
    private lateinit var tvConsoleAlertsOff: TextView
    private lateinit var tvMirrorAlertsOn: TextView
    private lateinit var tvMirrorAlertsOff: TextView
    private lateinit var tvAudioAlertsOn: TextView
    private lateinit var tvAudioAlertsOff: TextView
    private lateinit var ivRadarOnSelected: ImageView
    private lateinit var ivRadarOffSelected: ImageView
    private lateinit var ivConsoleAlertsOnSelected: ImageView
    private lateinit var ivConsoleAlertsOffSelected: ImageView
    private lateinit var ivMirrorAlertsOnSelected: ImageView
    private lateinit var ivMirrorAlertsOffSelected: ImageView
    private lateinit var ivAudioAlertsOnSelected: ImageView
    private lateinit var ivAudioAlertsOffSelected: ImageView
    private lateinit var clRadarOn: ConstraintLayout
    private val sharedViewModel by activityViewModels<SharedViewModel> { ViewModelFactory(context = requireContext()) }
    private val carViewModel by activityViewModels<CarViewModel> { ViewModelFactory(requireContext()) }
    private val viewModel by viewModels<RadarViewModel> { ViewModelFactory(context = requireContext()) }
    private var isBackClicked = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_radar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initClickListener()
        observeUiState()
    }

    private fun initViews(view: View) {
        tvRadarOn = view.findViewById(R.id.tvRadarOn)
        tvRadarOff = view.findViewById(R.id.tvRadarOff)
        tvConsoleAlertsOn = view.findViewById(R.id.tvConsoleAlertsOn)
        tvConsoleAlertsOff = view.findViewById(R.id.tvConsoleAlertsOff)
        tvMirrorAlertsOn = view.findViewById(R.id.tvMirrorAlertsOn)
        tvMirrorAlertsOff = view.findViewById(R.id.tvMirrorAlertsOff)
        tvAudioAlertsOn = view.findViewById(R.id.tvAudioAlertsOn)
        tvAudioAlertsOff = view.findViewById(R.id.tvAudioAlertsOff)

        ivRadarOnSelected = view.findViewById(R.id.ivRadarOnSelected)
        ivRadarOffSelected = view.findViewById(R.id.ivRadarOffSelected)
        ivConsoleAlertsOffSelected = view.findViewById(R.id.ivConsoleAlertsOffSelected)
        ivConsoleAlertsOnSelected = view.findViewById(R.id.ivConsoleAlertsOnSelected)
        ivMirrorAlertsOffSelected = view.findViewById(R.id.ivMirrorAlertsOffSelected)
        ivMirrorAlertsOnSelected = view.findViewById(R.id.ivMirrorAlertsOnSelected)
        ivAudioAlertsOffSelected = view.findViewById(R.id.ivAudioAlertsOffSelected)
        ivAudioAlertsOnSelected = view.findViewById(R.id.ivAudioAlertsOnSelected)

        clRadarOn = view.findViewById(R.id.clRadarOn)
    }

    fun handleButtonNavigation(button: Int) {
        when (button) {
            ButtonNavigation.Left.ordinal, ButtonNavigation.Right.ordinal -> {
                viewModel.toggleCurrentSelection()
            }

            ButtonNavigation.Bottom.ordinal -> {
                viewModel.selectNextOption()
            }

            ButtonNavigation.Top.ordinal -> {
                viewModel.selectPreviousOption()
            }

            ButtonNavigation.Back.ordinal -> {
                isBackClicked = true
                updateUi(viewModel.uiState.value)
            }
        }
    }

    private fun initClickListener() {

        tvRadarOn.setOnSoundClickListener(requireContext()) {
            viewModel.setRadar(true)
            sharedViewModel.saveRadar(true)
            viewModel.setSelectedOption(RadarSelectedOption.RADAR)
            sharedViewModel.handleAfChildClick(true)
            writeRadarToVcu(true)
        }
        tvRadarOff.setOnSoundClickListener(requireContext()) {
            viewModel.setRadar(false)
            sharedViewModel.saveRadar(false)
            viewModel.setSelectedOption(RadarSelectedOption.RADAR)
            sharedViewModel.handleAfChildClick(true)
            writeRadarToVcu(false)
        }
        tvConsoleAlertsOn.setOnSoundClickListener(requireContext()) {
            viewModel.setConsoleAlerts(true)
            viewModel.setSelectedOption(RadarSelectedOption.CONSOLE_ALERTS)
            sharedViewModel.handleAfChildClick(true)
        }
        tvConsoleAlertsOff.setOnSoundClickListener(requireContext()) {
            viewModel.setConsoleAlerts(false)
            viewModel.setSelectedOption(RadarSelectedOption.CONSOLE_ALERTS)
            sharedViewModel.handleAfChildClick(true)
        }
        tvMirrorAlertsOn.setOnSoundClickListener(requireContext()) {
            viewModel.setMirrorAlerts(true)
            viewModel.setSelectedOption(RadarSelectedOption.MIRROR_ALERTS)
            sharedViewModel.handleAfChildClick(true)
        }
        tvMirrorAlertsOff.setOnSoundClickListener(requireContext()) {
            viewModel.setMirrorAlerts(false)
            viewModel.setSelectedOption(RadarSelectedOption.MIRROR_ALERTS)
            sharedViewModel.handleAfChildClick(true)
        }
        tvAudioAlertsOn.setOnSoundClickListener(requireContext()) {
            viewModel.setAudioAlerts(true)
            viewModel.setSelectedOption(RadarSelectedOption.AUDIO_ALERTS)
            sharedViewModel.handleAfChildClick(true)
        }
        tvAudioAlertsOff.setOnSoundClickListener(requireContext()) {
            viewModel.setAudioAlerts(false)
            viewModel.setSelectedOption(RadarSelectedOption.AUDIO_ALERTS)
            sharedViewModel.handleAfChildClick(true)
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { uiState ->
                        updateUi(uiState)
                    }
                }
            }

        }
    }

    private fun updateUi(uiState: RadarUiState) {
        clRadarOn.isVisible = uiState.isRadarOn
        //clRadarOn.isVisible=false

        updateSelectionState(
            tvRadarOn,
            tvRadarOff,
            ivRadarOnSelected,
            ivRadarOffSelected,
            uiState.isRadarOn,
            uiState.selectedOption == RadarSelectedOption.RADAR && !isBackClicked
        )

        updateSelectionState(
            tvConsoleAlertsOn,
            tvConsoleAlertsOff,
            ivConsoleAlertsOnSelected,
            ivConsoleAlertsOffSelected,
            uiState.isConsoleAlertsOn,
            uiState.selectedOption == RadarSelectedOption.CONSOLE_ALERTS && !isBackClicked
        )

        updateSelectionState(
            tvMirrorAlertsOn,
            tvMirrorAlertsOff,
            ivMirrorAlertsOnSelected,
            ivMirrorAlertsOffSelected,
            uiState.isMirrorAlertsOn,
            uiState.selectedOption == RadarSelectedOption.MIRROR_ALERTS && !isBackClicked
        )

        updateSelectionState(
            tvAudioAlertsOn,
            tvAudioAlertsOff,
            ivAudioAlertsOnSelected,
            ivAudioAlertsOffSelected,
            uiState.isAudioAlertsOn,
            uiState.selectedOption == RadarSelectedOption.AUDIO_ALERTS && !isBackClicked
        )
        isBackClicked = false
    }

    private fun updateSelectionState(
        onView: TextView, offView: TextView,
        ivOn: ImageView, ivOff: ImageView,
        isOn: Boolean, isSelected: Boolean
    ) {
        val selectedTxtClr = if (isSelected) R.color.white else R.color.black
        val selectedBgClr = if (isSelected) R.color.activeSelectionRed else R.color.white
        val unselectedTxtClr = R.color.unSelected
        val unselectedBgClr = R.color.transparent

        val actualOnTxtClr = if (isOn) selectedTxtClr else unselectedTxtClr
        val actualOnBgClr = if (isOn) selectedBgClr else unselectedBgClr

        val actualOffTxtClr = if (isOn) unselectedTxtClr else selectedTxtClr
        val actualOffBgClr = if (isOn) unselectedBgClr else selectedBgClr

        ivOn.isVisible = isSelected && isOn
        ivOff.isVisible = isSelected && !isOn

        onView.apply {
            setTextColor(ContextCompat.getColor(requireContext(), actualOnTxtClr))
            setBackgroundColor(ContextCompat.getColor(requireContext(), actualOnBgClr))
        }
        offView.apply {
            setTextColor(ContextCompat.getColor(requireContext(), actualOffTxtClr))
            setBackgroundColor(ContextCompat.getColor(requireContext(), actualOffBgClr))
        }
    }

    private fun writeRadarToVcu(isOn: Boolean) {
        val value: Byte = if (isOn) 1 else 0
        d("APP TO_VCU", "Radar value:$value")
        val packet = byteArrayOf(value)
        carViewModel.sendByteArrayProperty(0x2170033F, packet)
    }
}



