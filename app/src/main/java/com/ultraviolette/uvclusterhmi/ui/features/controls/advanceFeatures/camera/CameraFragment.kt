package com.ultraviolette.uvclusterhmi.ui.features.controls.advanceFeatures.camera

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
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import com.ultraviolette.uvclusterhmi.ui.viewModel.SharedViewModel
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
import kotlinx.coroutines.launch

class CameraFragment : Fragment() {
    private lateinit var tvCameraOn: TextView
    private lateinit var tvCameraOff: TextView
    private lateinit var ivCameraOnSelected: ImageView
    private lateinit var ivCameraOffSelected: ImageView
    private lateinit var clCamera : ConstraintLayout
    private val sharedViewModel by activityViewModels<SharedViewModel> { ViewModelFactory(context = requireContext()) }
    private val viewModel by viewModels<CameraViewModel> { ViewModelFactory(context = requireContext()) }
    private lateinit var tvEnter : TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        observeCamera()
        initClickListener()
    }

    private fun observeCamera() {
        sharedViewModel.afChildClick.observe(viewLifecycleOwner) { isClicked ->
            viewModel.initCamera(!isClicked)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect {
                        handleCameraUi(it.isOn, it.isButton)
                    }
                }
            }
        }
    }

    fun handleButtonNavigation(button: Int) {
        if (button == ButtonNavigation.Back.ordinal) {
            handleCameraState(viewModel.uiState.value.isOn, true)
            sharedViewModel.handleAfChildClick(false)
            return
        }
        viewModel.saveCamera(!viewModel.uiState.value.isOn, false)
    }

    /**
     * Binds UI components from the provided root view using their IDs.
     *
     * @param view The root view containing the layout elements.
     *
     * Initializes:
     * - tvCameraOn, tvCameraOff (TextViews)
     * - ivCameraOffSelected,ivCameraOnSelected (ImageView)
     */
    private fun initViews(view: View) {
        tvCameraOn = view.findViewById(R.id.tvCameraOn)
        tvCameraOff = view.findViewById(R.id.tvCameraOff)

        ivCameraOffSelected = view.findViewById(R.id.ivCameraOffSelected)
        ivCameraOnSelected = view.findViewById(R.id.ivCameraOnSelected)

        clCamera = view.findViewById(R.id.clCamera)
        tvEnter = view.findViewById(R.id.tvEnter)
    }

    /**
     * Initializes click listeners for UI elements.
     */
    private fun initClickListener() {
        tvCameraOn.setOnSoundClickListener(requireContext()) {
            handleCameraState(true, isButton = false)
        }
        tvCameraOff.setOnSoundClickListener(requireContext()) {
            handleCameraState(false, isButton = false)
        }
        tvEnter.setOnClickListener {
            val mainNavController = requireActivity().findNavController(R.id.nav_host_fragment)
            //mainNavController.navigate(R.id.cameraPreviewFragment)
            mainNavController.navigate(R.id.dashCamFragment)
        }
    }

    /**
     * Saves the camera state and updates UI accordingly.
     *
     * @param isOn Boolean indicating whether the camera should be ON (true) or OFF (false).
     */
    private fun handleCameraState(isOn: Boolean, isButton: Boolean) {
        sharedViewModel.handleAfChildClick(true)
        viewModel.saveCamera(isOn, isButton)
    }

    /**
     * Updates the UI elements (text color, background, selected icons)
     * based on the current camera state.
     */
    private fun handleCameraUi(isOn: Boolean, isButton: Boolean) {
        ivCameraOnSelected.isVisible = isOn && !isButton
        ivCameraOffSelected.isVisible = !isOn && !isButton
        clCamera.isVisible = isOn

        val selectedBackgroundColor =
            ContextCompat.getColor(
                requireContext(),
                if (isButton) R.color.white else R.color.activeSelectionRed
            )
        val selectedTextColor = ContextCompat.getColor(
            requireContext(),
            if (isButton) R.color.black else R.color.white
        )
        val unselectedBackgroundColor =
            ContextCompat.getColor(requireContext(), R.color.transparent)
        val unselectedTextColor = ContextCompat.getColor(requireContext(), R.color.unSelected)

        tvCameraOn.setTextColor(if (isOn) selectedTextColor else unselectedTextColor)
        tvCameraOff.setTextColor(if (!isOn) selectedTextColor else unselectedTextColor)
        tvCameraOn.setBackgroundColor(if (isOn) selectedBackgroundColor else unselectedBackgroundColor)
        tvCameraOff.setBackgroundColor(if (!isOn) selectedBackgroundColor else unselectedBackgroundColor)
    }
}

