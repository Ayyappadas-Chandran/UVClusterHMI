package com.ultraviolette.uvclusterhmi.ui.features.controls.violette

import android.media.Image
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener
import kotlinx.coroutines.launch

class VioletteFragment : Fragment() {
    private lateinit var tvVioletteState: TextView
    private lateinit var tvOff: TextView
    private lateinit var ivBack: ImageView
    private lateinit var tvVioletteOn: TextView
    private lateinit var tvVioletteOff: TextView
    private lateinit var tvOn: TextView
    private lateinit var tvVioletteApp: TextView
    private lateinit var ivViolette: ImageView
    private lateinit var ivBgVioletteEnable: ImageView
    private lateinit var ivBgVioletteDisable: ImageView
    private lateinit var llVioletteDetail: LinearLayout
    private lateinit var clVioletteEnable: ConstraintLayout
    private val viewModel: VioletteViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_violette, container, false)
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
                viewModel.uiState.collect { uiState ->
                    tvVioletteState.text = getString(uiState.stateText)
                    tvVioletteState.setTextColor(ContextCompat.getColor(requireContext(), uiState.stateTextColor))
                    ivViolette.setImageResource(uiState.violetteIconRes)
                    llVioletteDetail.isVisible = uiState.isVioletteDetailVisible
                    clVioletteEnable.isVisible = uiState.isVioletteEnableLayoutVisible
                    ivBgVioletteEnable.isVisible = uiState.isBgVioletteEnableVisible
                    tvVioletteOn.isVisible = uiState.isVioletteOnVisible
                    tvOn.isVisible = uiState.isOnVisible
                    ivBgVioletteDisable.isVisible = uiState.isBgVioletteDisableVisible
                    tvVioletteOff.isVisible = uiState.isVioletteOffVisible
                    tvOff.isVisible = uiState.isOffVisible
                }
            }
        }
    }

    /**
     * Initializes click listeners for UI components.
     */
    private fun initClickListener() {
        ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
        tvVioletteApp.setOnSoundClickListener(requireContext()) {
            viewModel.simulateViolette()
        }
    }

    fun handleButtonNavigation(button: Int) {
        when (button) {
            ButtonNavigation.Enter.ordinal -> {
                if (tvVioletteApp.isVisible) {
                    viewModel.simulateViolette()
                }
            }

            ButtonNavigation.Back.ordinal -> {
                findNavController().navigateUp()
            }
        }
    }

    /**
     * Binds UI components from the provided root view using their IDs.
     *
     * @param view The root view containing the layout elements.
     *
     * Initializes:
     * - tvVioletteState, tvVioletteOff, tvOff, tvBack, tvVioletteApp, tvVioletteOn, tvOn (TextViews)
     * - ivViolette, bgVioletteEnable, bgVioletteDisable (ImageViews)
     * - llVioletteDetail(LinearLayout)
     * - clVioletteEnable (ConstraintLayout)
     */
    private fun initViews(view: View) {
        tvVioletteState = view.findViewById(R.id.tvVioletteState)
        tvVioletteOff = view.findViewById(R.id.tvVioletteOff)
        tvOff = view.findViewById(R.id.tvOff)
        ivBack = view.findViewById(R.id.ivBack)
        tvVioletteApp = view.findViewById(R.id.tvVioletteApp)
        tvVioletteOn = view.findViewById(R.id.tvVioletteOn)
        tvOn = view.findViewById(R.id.tvOn)

        ivViolette = view.findViewById(R.id.ivViolette)
        ivBgVioletteEnable = view.findViewById(R.id.ivBgVioletteEnable)
        ivBgVioletteDisable = view.findViewById(R.id.ivBgVioletteDisable)

        llVioletteDetail = view.findViewById(R.id.llVioletteDetail)
        clVioletteEnable = view.findViewById(R.id.clVioletteEnable)
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopSimulation()
    }
}
