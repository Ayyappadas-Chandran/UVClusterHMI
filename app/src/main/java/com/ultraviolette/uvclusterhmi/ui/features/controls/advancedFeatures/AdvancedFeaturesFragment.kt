package com.ultraviolette.uvclusterhmi.ui.features.controls.advancedFeatures

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.dataModel.SettingMenuItem
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import com.ultraviolette.uvclusterhmi.ui.adapter.VerticalMenuAdapter
import com.ultraviolette.uvclusterhmi.ClusterApplication
import com.ultraviolette.uvclusterhmi.ui.features.controls.advanceFeatures.camera.CameraFragment
import com.ultraviolette.uvclusterhmi.ui.features.controls.advanceFeatures.radar.RadarFragment
import com.ultraviolette.uvclusterhmi.ui.viewModel.ClusterViewModel
import com.ultraviolette.uvclusterhmi.ui.viewModel.SharedViewModel
import com.ultraviolette.uvclusterhmi.utils.Utilities
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
import kotlinx.coroutines.launch

class AdvancedFeaturesFragment : Fragment() {
    private lateinit var ivBack: ImageView
    private lateinit var rvAdvancedFeatures: RecyclerView
    private val sharedViewModel by activityViewModels<SharedViewModel> { ViewModelFactory(context = requireContext()) }
    private lateinit var verticalMenuAdapter: VerticalMenuAdapter
    private var adapterPosition = 0
    private var hasChildSelected = false
    private val clusterViewModel: ClusterViewModel by activityViewModels {
        ClusterViewModel.Factory(requireActivity().application as ClusterApplication)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_advanced_features, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initObserver()
        initRecyclerView()
        initClickListener()
    }

    fun handleButtonNavigation(button: Int) {
        if (button != ButtonNavigation.Enter.ordinal) {
            consumeChildButtonNavigation(button)
        }
        when (button) {
            ButtonNavigation.Top.ordinal -> {
                consumeButtonScroll(adapterPosition <= 0, true)
            }

            ButtonNavigation.Bottom.ordinal -> {
                consumeButtonScroll(adapterPosition >= verticalMenuAdapter.itemCount - 1)
            }

            ButtonNavigation.Enter.ordinal -> {
                sharedViewModel.handleAfChildClick(true)
                if (!hasChildSelected) {
                    hasChildSelected = true
                    rvAdvancedFeatures.post {
                        rvAdvancedFeatures.findViewHolderForAdapterPosition(adapterPosition)
                            ?.itemView
                            ?.performClick()
                    }
                }
            }

            ButtonNavigation.Back.ordinal -> {
                sharedViewModel.handleAfChildClick(false)
                if (hasChildSelected) {
                    verticalMenuAdapter.updateSelectedPosition(adapterPosition)
                    hasChildSelected = false
                } else {
                    findNavController().navigateUp()
                }
            }
        }
    }

    private fun consumeChildButtonNavigation(button: Int) {
        if (!hasChildSelected) return
        val childNavHost = childFragmentManager.findFragmentById(R.id.nav_host_fragment)
        val currentChildFragment = childNavHost
            ?.childFragmentManager
            ?.primaryNavigationFragment
        when (currentChildFragment) {
            is CameraFragment -> {
                currentChildFragment.handleButtonNavigation(button)
            }

            is RadarFragment -> {
                currentChildFragment.handleButtonNavigation(button)
            }
        }
    }

    private fun consumeButtonScroll(shouldReturn: Boolean, isTop: Boolean = false) {
        if (hasChildSelected) {
            sharedViewModel.handleAfChildClick(true)
            return
        }
        sharedViewModel.handleAfChildClick(false)
        if (shouldReturn) return
        if (isTop) adapterPosition-- else adapterPosition++
        verticalMenuAdapter.updateSelectedPosition(adapterPosition)
        rvAdvancedFeatures.scrollToPosition(adapterPosition)
    }

    /**
     * Binds UI components from the provided root view using their IDs.
     *
     * @param view The root view containing the layout elements.
     *
     * Initializes:
     * - rvAdvancedFeatures(RecyclerViews)
     * - tvBack (ImageView)
     */
    private fun initViews(view: View) {
        rvAdvancedFeatures = view.findViewById(R.id.rvAdvanceFeatureMenu)
        ivBack = view.findViewById(R.id.ivBack)
    }

    /**
     * Sets up the RecyclerView with a list of advanced feature items.
     * Navigates to the corresponding destination when a menu item is clicked.
     */
    private fun initRecyclerView() {
        val menuList = listOf(
            SettingMenuItem(getString(R.string.camera), R.id.cameraFragment),
            SettingMenuItem(getString(R.string.radar), R.id.radarFragment),
            //SettingMenuItem("Haptic", R.id.hapticFragment)
        )
        verticalMenuAdapter = VerticalMenuAdapter(0, { destinationId, adapterPosition ->
            hasChildSelected = true
            this.adapterPosition = adapterPosition
            val navHostFragment =
                childFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
            val navController = navHostFragment?.navController
            navController?.navigate(destinationId)
        })
        rvAdvancedFeatures.adapter = verticalMenuAdapter
        verticalMenuAdapter.submitList(menuList)
    }

    /**
     * Initializes click listeners for UI elements.
     */
    private fun initClickListener() {
        ivBack.setOnSoundClickListener(requireContext()) {
            findNavController().navigate(R.id.controlFragment)
        }
    }

    /**
     * Observes the childClick LiveData from [SharedViewModel]
     * and passes its value to the adapter.
     */
    private fun initObserver() {
        sharedViewModel.afChildClick.observe(viewLifecycleOwner) { isClicked ->
            verticalMenuAdapter.handleChildClick(isClicked)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    // Handlebar events via ClusterDataBus (new path, replaces carViewModel.swiftButton)
                    clusterViewModel.handlebarButton.collect { swiftButton ->
                        val button = Utilities.getButtonState(swiftButton)
                        if (button == ButtonNavigation.None) return@collect
                        handleButtonNavigation(button.ordinal)
                    }
                }
            }
        }
    }
}


