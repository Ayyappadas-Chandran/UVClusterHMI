package com.ultraviolette.uvclusterhmi.ui.features.myF77.tutorial

import android.os.Bundle
import android.util.Log
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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.ultraviolette.uvclusterhmi.ClusterApplication
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.dataModel.TutorialVideoInfo
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import com.ultraviolette.uvclusterhmi.ui.adapter.TutorialAdapter
import com.ultraviolette.uvclusterhmi.ui.viewModel.ClusterViewModel
import com.ultraviolette.uvclusterhmi.utils.Utilities
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
import kotlinx.coroutines.launch

class TutorialFragment : Fragment() {
    private lateinit var ivBack: ImageView
    private lateinit var rvTutorial: RecyclerView
    private val viewModel by activityViewModels<TutorialViewModel>{ ViewModelFactory(context = requireContext()) }
    private var tutorialAdapter: TutorialAdapter? = null
    private var adapterPosition = 0
    private val clusterViewModel: ClusterViewModel by activityViewModels {
        ClusterViewModel.Factory(requireActivity().application as ClusterApplication)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tutorial, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initClickListener()
        initRecyclerView()
        initObserver()
    }

    /**
     * Observes the selected adapter position from the [viewModel]
     */
    private fun initObserver() {
        viewModel.selectedVideoPosition.observe(viewLifecycleOwner) { position ->
            tutorialAdapter?.updateSelectedPosition(position)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    clusterViewModel.handlebarButton.collect { swiftButton ->
                        val button = Utilities.getButtonState(swiftButton)
                        if(button == ButtonNavigation.None) return@collect
                        handleButtonNavigation(button.ordinal)
                    }
                }
            }
        }
    }

    fun handleButtonNavigation(button: Int) {
        when (button) {
            ButtonNavigation.Right.ordinal -> {
                if (adapterPosition >= tutorialAdapter!!.itemCount - 1) return
                adapterPosition++
                viewModel.handleSelectedVideoPosition(adapterPosition)
            }

            ButtonNavigation.Left.ordinal -> {
                if (adapterPosition <= 0) return
                adapterPosition--
                viewModel.handleSelectedVideoPosition(adapterPosition)
            }

            ButtonNavigation.Top.ordinal -> {
                if (adapterPosition - 3 < 0) return
                adapterPosition -= 3
                viewModel.handleSelectedVideoPosition(adapterPosition)
            }

            ButtonNavigation.Bottom.ordinal -> {
                if (adapterPosition + 3 >= tutorialAdapter!!.itemCount) return
                adapterPosition += 3
                viewModel.handleSelectedVideoPosition(adapterPosition)
            }

            ButtonNavigation.Enter.ordinal -> {
                rvTutorial.findViewHolderForAdapterPosition(adapterPosition)?.itemView?.performClick()
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
     * - tvBack,tvVideoPlayBack (TextViews)
     * - rvTutorial (RecyclerView)
     */
    private fun initViews(view: View) {
        ivBack = view.findViewById(R.id.ivBack)
        rvTutorial = view.findViewById(R.id.rvTutorial)
    }

    /**
     * Initializes click listeners for UI components.
     */
    private fun initClickListener() {
        ivBack.setOnSoundClickListener(requireContext()) {
            findNavController().navigateUp()
        }
    }


    /**
     * Sets up the tutorial RecyclerView with a list of sample videos.
     *
     * Initializes [TutorialAdapter] and populates it with predefined [com.ultraviolette.uvclusterhmi.domain.dataModel.TutorialVideoInfo]s.
     */
    private fun initRecyclerView() {
        tutorialAdapter = TutorialAdapter { position ->
            adapterPosition = position
            Log.d("RecyclerViewPosition", "Position:$position")
            viewModel.handleSelectedVideoPosition(position)
            findNavController().navigate(R.id.action_tutorialFragment_to_tutorialPlayerFragment)
        }
        val videoList = listOf(
            TutorialVideoInfo("Video 01", "05:04"),
            TutorialVideoInfo("Video 02", "05:04"),
            TutorialVideoInfo("Video 03", "05:04"),
            TutorialVideoInfo("Video 04", "05:04"),
            TutorialVideoInfo("Video 05", "05:04")
        )
        rvTutorial.adapter = tutorialAdapter
        tutorialAdapter?.submitList(videoList)
    }
}
