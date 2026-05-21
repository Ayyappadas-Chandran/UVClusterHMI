package com.ultraviolette.uvclusterhmi.ui.features.settings.incognito

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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

class IncognitoFragment : Fragment() {
    private lateinit var tvIncognitoOn: TextView
    private lateinit var tvIncognitoOff: TextView
    private lateinit var ivIncognitoOnSelect: ImageView
    private lateinit var ivIncognitoOffSelect: ImageView
    private val sharedViewModel by activityViewModels<SharedViewModel> { ViewModelFactory(context = requireContext()) }
    private val viewModel by viewModels<IncognitoViewModel> { ViewModelFactory(context = requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_incognito, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initClickListener()
        observeIncognitoValue()
    }

    private fun observeIncognitoValue() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isIncognitoEnabled.collect {
                        handleIncognitoUi(it.isEnabled, it.isButton)
                    }
                }
            }
        }
    }

    /**
     * Binds UI components from the provided root view using their IDs.
     *
     * @param view The root view containing the layout elements.
     *
     * Initializes:
     * - tvIncognitoOn,tvIncognitoOff (TextViews)
     * - ivIncognitoOnSelect,ivIncognitoOffSelect (ImageViews)
     *
     */
    private fun initViews(view: View) {
        tvIncognitoOn = view.findViewById(R.id.tvIncognitoOn)
        tvIncognitoOff = view.findViewById(R.id.tvIncognitoOff)
        ivIncognitoOnSelect = view.findViewById(R.id.ivIncognitoOnSelect)
        ivIncognitoOffSelect = view.findViewById(R.id.ivIncognitoOffSelect)
    }

    /**
     * Initialize click listeners for UI components.
     */
    private fun initClickListener() {
        tvIncognitoOn.setOnSoundClickListener(requireContext()) {
            sharedViewModel.handleSettingsChildClick(true)
            viewModel.saveIncognito(true, isButton = false)
        }
        tvIncognitoOff.setOnSoundClickListener(requireContext()) {
            sharedViewModel.handleSettingsChildClick(true)
            viewModel.saveIncognito(false, isButton = false)
        }
    }

    fun handleButtonNavigation(button: Int) {
        if (button == ButtonNavigation.Back.ordinal) {
            viewModel.saveIncognito(viewModel.isIncognitoEnabled.value.isEnabled, true)
            return
        }
        viewModel.saveIncognito(!viewModel.isIncognitoEnabled.value.isEnabled, false)
    }

    /**
     * Handles the UI state for incognito mode.
     */
    private fun handleIncognitoUi(isEnabled: Boolean, isButtonNavigation: Boolean) {
        ivIncognitoOnSelect.isVisible = isEnabled && !isButtonNavigation
        ivIncognitoOffSelect.isVisible = !isEnabled && !isButtonNavigation
        val selectedBackgroundColor =
            ContextCompat.getColor(
                requireContext(),
                if (isButtonNavigation) R.color.white else R.color.activeSelectionRed
            )
        val selectedTextColor = ContextCompat.getColor(
            requireContext(),
            if (isButtonNavigation) R.color.black else R.color.white
        )
        val unselectedBackgroundColor =
            ContextCompat.getColor(requireContext(), R.color.transparent)
        val unselectedTextColor = ContextCompat.getColor(requireContext(), R.color.unSelected)
        tvIncognitoOn.setTextColor(if (isEnabled) selectedTextColor else unselectedTextColor)
        tvIncognitoOff.setTextColor(if (!isEnabled) selectedTextColor else unselectedTextColor)
        tvIncognitoOn.setBackgroundColor(if (isEnabled) selectedBackgroundColor else unselectedBackgroundColor)
        tvIncognitoOff.setBackgroundColor(if (!isEnabled) selectedBackgroundColor else unselectedBackgroundColor)
    }
}