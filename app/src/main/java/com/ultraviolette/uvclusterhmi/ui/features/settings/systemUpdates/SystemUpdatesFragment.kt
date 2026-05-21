package com.ultraviolette.uvclusterhmi.ui.features.settings.systemUpdates

import android.os.Bundle
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import com.ultraviolette.uvclusterhmi.ui.viewModel.SharedViewModel
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
import com.ultraviolette.uvclusterhmi.utils.Utilities
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class SystemUpdatesFragment : Fragment() {
    private lateinit var tvCheckForUpdate: TextView
    private lateinit var tvUpdateAvailable: TextView
    private lateinit var clSoftwareUpdate: ConstraintLayout
    private val viewModel by activityViewModels<SharedViewModel> { ViewModelFactory(context = requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_system_updates, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initClickListener()
    }


    fun handleButtonNavigation(button: Int) {
        when (button) {
            ButtonNavigation.Enter.ordinal -> {
                tvCheckForUpdate.performClick()
            }

            ButtonNavigation.Back.ordinal -> {
                tvCheckForUpdate.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.transparent
                    )
                )
                tvCheckForUpdate.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.unSelected
                    )
                )
            }
        }
    }

    /**
     * Binds UI components from the provided root view using their IDs.
     *
     * @param view The root view containing the layout elements.
     *
     * Initializes:
     * - tvCheckForUpdate,tvUpdateAvailable (TextView)
     * - clSoftwareUpdate (ConstraintLayout)
     */
    private fun initViews(view: View) {
        tvCheckForUpdate = view.findViewById(R.id.tvCheckForUpdate)
        tvUpdateAvailable = view.findViewById(R.id.tvUpdateAvailable)
        clSoftwareUpdate = view.findViewById(R.id.clSoftwareUpdate)
    }

    /**
     * Initialize click listeners for UI components.
     */
    private fun initClickListener() {
        tvCheckForUpdate.setOnSoundClickListener(requireContext()) {
            viewModel.handleSettingsChildClick(true)
            if (!Utilities.isOtaAvailable()) {
                d("OTAUpdate", "The folder is not available")
                Toast.makeText(requireContext(), "OTA is unavailable", Toast.LENGTH_LONG).show()
                return@setOnSoundClickListener
            }
            d("OTAUpdate", "The folder is available")
            tvCheckForUpdate.apply {
                setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.activeSelectionRed
                    )
                )
                setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
            if (tvCheckForUpdate.text == getString(R.string.update)) {
                navigateToUpdateFragment()
            } else {
                tvCheckForUpdate.text = getString(R.string.update)
                tvUpdateAvailable.isVisible = true
            }
        }
    }

    private fun navigateToUpdateFragment() {
        val mainNavController = requireActivity().findNavController(R.id.nav_host_fragment)
        mainNavController.navigate(R.id.updateFragment)
    }
}
