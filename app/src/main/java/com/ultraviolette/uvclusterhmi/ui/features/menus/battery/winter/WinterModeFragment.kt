package com.ultraviolette.uvclusterhmi.ui.features.menus.battery.winter

import android.os.Bundle
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
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.ultraviolette.uvclusterhmi.ClusterApplication
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import com.ultraviolette.uvclusterhmi.ui.viewModel.ClusterViewModel
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class WinterModeFragment : Fragment() {
    private lateinit var tvBack: TextView
    private lateinit var tvStep1: TextView
    private lateinit var tvPlugCharger: TextView
    private lateinit var ivPlug: ImageView
    private lateinit var clChargerStatus: ConstraintLayout
    private lateinit var clWinterMode: ConstraintLayout
    private val clusterViewModel: ClusterViewModel by activityViewModels {
        ClusterViewModel.Factory(requireActivity().application as ClusterApplication)
    }
    private val viewModel by viewModels<WinterModeViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_winter_mode, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initClickListener()
        observeUiState()
        viewModel.simulateWinterMode()
    }

    private fun observeUiState() {
        viewModel.uiState.onEach { state ->
            handleUiState(state)
        }.flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    // Handlebar events via ClusterDataBus (replaces carViewModel.swiftButton)
                    clusterViewModel.handlebarButton.collect {
                        if (it == 0xE1) {
                            handleButtonNavigation(ButtonNavigation.Back.ordinal)
                        }
                    }
                }
            }
        }
    }

    private fun handleUiState(state: WinterModeUiState){
        ivPlug.drawable.setTint(ContextCompat.getColor(requireContext(), state.plugColor))
        tvStep1.setTextColor(ContextCompat.getColor(requireContext(), state.stepColor))
        tvPlugCharger.setTextColor(ContextCompat.getColor(requireContext(), state.plugChargerColor))
        clChargerStatus.isVisible = state.chargerStatus
        clWinterMode.isVisible = state.winterModelStatus
    }

    /**
     * Binds UI components from the provided root view using their IDs.
     *
     * @param view The root view containing the layout elements.
     *
     * Initializes:
     * - tvBack, tvStep1, tvPlugCharger (TextViews)
     * - ivPlug (ImageView)
     * - clChargerStatus, clWinterMode (ConstraintLayouts)
     */
    private fun initViews(view: View) {
        tvBack = view.findViewById(R.id.tvBack)
        tvStep1 = view.findViewById(R.id.tvStep1)

        ivPlug = view.findViewById(R.id.ivPlug)
        tvPlugCharger = view.findViewById(R.id.tvPlugCharger)

        clChargerStatus = view.findViewById(R.id.clChargerStatus)
        clWinterMode = view.findViewById(R.id.clWinterMode)
    }

    fun handleButtonNavigation(button: Int) {
        if (button == ButtonNavigation.Back.ordinal) {
            findNavController().navigateUp()
        }
    }

    /**
     * Initializes click listeners for UI elements.
     */
    private fun initClickListener() {
        tvBack.setOnSoundClickListener(requireContext()) {
            findNavController().navigateUp()
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.cancelSimulation()
    }
}