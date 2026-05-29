package com.ultraviolette.uvclusterhmi.ui.features.menus.control

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import com.ultraviolette.uvclusterhmi.ClusterApplication
import com.ultraviolette.uvclusterhmi.ui.adapter.ControlMenuAdapter
import com.ultraviolette.uvclusterhmi.ui.viewModel.ClusterViewModel
import com.ultraviolette.uvclusterhmi.ui.viewModel.SharedViewModel
import com.ultraviolette.uvclusterhmi.utils.Utilities
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
import kotlinx.coroutines.launch

class ControlMenuFragment : Fragment() {
    private lateinit var ivBack: ImageView
    private lateinit var rvControls: RecyclerView
    private var controlMenuAdapter: ControlMenuAdapter? = null
    private val viewModel: ControlViewModel by viewModels()
    private val clusterViewModel: ClusterViewModel by activityViewModels {
        ClusterViewModel.Factory(requireActivity().application as ClusterApplication)
    }
    private val sharedViewModel by activityViewModels<SharedViewModel> { ViewModelFactory(context = requireContext()) }
    private var isEnterClicked = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_control, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initRecyclerView()
        initClickListener()
        observeAdapterPosition()
    }

    /**
     * Binds UI components from the provided root view using their IDs.
     *
     * @param view The root view containing the layout elements.
     *
     * Initializes:
     * - tvBack (TextView)
     * - rvControls (RecyclerView)
     *
     */
    private fun initViews(view: View) {
        ivBack = view.findViewById(R.id.ivBack)
        rvControls = view.findViewById(R.id.rvControls)
    }

    private fun observeAdapterPosition() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.adapterPosition.collect { position ->
                        controlMenuAdapter?.updateSelectedPosition(position)
                        rvControls.scrollToPosition(position)
                    }
                }
                launch {
                    // Handlebar events via ClusterDataBus (replaces carViewModel.swiftButton)
                    clusterViewModel.handlebarButton.collect { swiftButton ->
                        val button = Utilities.getButtonState(swiftButton)
                        if (button == ButtonNavigation.None) return@collect
                        handleButtonNavigation(button.ordinal)
                    }
                }
            }
        }
    }

    fun handleButtonNavigation(button: Int) {
        var adapterPosition = viewModel.adapterPosition.value
        when (button) {
            ButtonNavigation.Right.ordinal -> {
                if (adapterPosition >= controlMenuAdapter!!.itemCount - 1) return
                adapterPosition++
                viewModel.updatePosition(adapterPosition)
            }

            ButtonNavigation.Left.ordinal -> {
                if (adapterPosition <= 0) return
                adapterPosition--
                viewModel.updatePosition(adapterPosition)
            }

            ButtonNavigation.Enter.ordinal -> {
                isEnterClicked = true
                rvControls.findViewHolderForAdapterPosition(adapterPosition)?.itemView?.performClick()
            }

            ButtonNavigation.Back.ordinal -> {
                findNavController().navigateUp()
            }
        }
    }

    /**
     * Initializes click listeners for UI components.
     */
    private fun initClickListener() {
        ivBack.setOnSoundClickListener(requireContext()) {
            findNavController().navigate(R.id.menuFragment)
        }
    }

    /**
     * Initialize the RecyclerView with the control menu items.
     */
    private fun initRecyclerView() {
        controlMenuAdapter = ControlMenuAdapter { position ->
            viewModel.updatePosition(position)
            when (position) {
                MenuPosition.AdvancedFeatures.ordinal -> {
                    val childClick = if (isEnterClicked) {
                        isEnterClicked = false
                        false
                    } else {
                        true
                    }
                    sharedViewModel.handleAfChildClick(childClick)
                    findNavController().navigate(R.id.advancedFeaturesFragment)
                }
                MenuPosition.Performance.ordinal -> findNavController().navigate(R.id.performanceFragment)
                MenuPosition.Violette.ordinal -> findNavController().navigate(R.id.violetteFragment)
                MenuPosition.Trips.ordinal -> findNavController().navigate(R.id.tripsFragment)
            }
        }
        val controlList = listOf(
            getString(R.string.advanced_features),
            getString(R.string.performance),
            getString(R.string.violette),
            getString(R.string.trips)
        )
        rvControls.layoutAnimation =
            AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_slide_in)
        rvControls.adapter = controlMenuAdapter
        rvControls.scheduleLayoutAnimation()
        controlMenuAdapter?.submitList(controlList.toList())
    }

    /**
     * Enum class representing the menu positions.
     */
    enum class MenuPosition {
        AdvancedFeatures,
        Performance,
        Violette,
        Trips
    }
}

