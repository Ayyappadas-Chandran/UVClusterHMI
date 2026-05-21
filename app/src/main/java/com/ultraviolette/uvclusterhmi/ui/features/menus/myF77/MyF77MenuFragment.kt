package com.ultraviolette.uvclusterhmi.ui.features.menus.myF77

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
import com.ultraviolette.uvclusterhmi.ui.adapter.MyF77MenuAdapter
import com.ultraviolette.uvclusterhmi.ui.viewModel.CarViewModel
import com.ultraviolette.uvclusterhmi.utils.Utilities
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
import kotlinx.coroutines.launch

class MyF77MenuFragment : Fragment() {
    private lateinit var ivBack: ImageView
    private lateinit var rvMyF77: RecyclerView
    private var myF77MenuAdapter: MyF77MenuAdapter? = null
    private val viewModel: MyF77ViewModel by viewModels()
    private val carViewModel by activityViewModels<CarViewModel> { ViewModelFactory(context = requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_f77_menu, container, false)
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
     * @param view The root view containing the layout elements.
     * Initializes:
     * - tvBack (TextView)
     * - rvMyF77 (RecyclerView)
     */

    private fun initViews(view: View) {
        ivBack = view.findViewById(R.id.ivBack)
        rvMyF77 = view.findViewById(R.id.rvMyF77)
    }

    /**
     * Initializes click listeners for UI components.
     */
    private fun initClickListener() {
        ivBack.setOnSoundClickListener(requireContext()) {
            findNavController().navigate(R.id.menuFragment)
        }
    }

    fun handleButtonNavigation(button: Int) {
        var adapterPosition = viewModel.adapterPosition.value
        when (button) {
            ButtonNavigation.Right.ordinal -> {
                if (adapterPosition >= myF77MenuAdapter!!.itemCount - 1) return
                adapterPosition++
                viewModel.updateAdapterPosition(adapterPosition)
            }

            ButtonNavigation.Left.ordinal -> {
                if (adapterPosition <= 0) return
                adapterPosition--
                viewModel.updateAdapterPosition(adapterPosition)
            }

            ButtonNavigation.Enter.ordinal -> {
                rvMyF77.findViewHolderForAdapterPosition(adapterPosition)?.itemView?.performClick()
            }

            ButtonNavigation.Back.ordinal -> {
                findNavController().navigateUp()
            }
        }
    }


    private fun observeAdapterPosition() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.adapterPosition.collect { position ->
                        myF77MenuAdapter?.updateSelectedPosition(position)
                        rvMyF77.scrollToPosition(position)
                    }
                }
                launch {
                    carViewModel.swiftButton.collect { swiftButton ->
                        val button = Utilities.getButtonState(swiftButton)
                        if (button == ButtonNavigation.None) return@collect
                        handleButtonNavigation(button.ordinal)
                    }
                }
            }
        }
    }

    /**
     * Initializes the horizontal RecyclerView for the MyF77 menu.
     *
     * - Sets up a list of menu items (Docs, Emergency, Info, Tutorial).
     * - Binds a [MyF77MenuAdapter] with a click listener to handle item selection.
     * - Navigates to the corresponding fragment based on the selected position.
     * - Submits the menu list to the adapter.
     *
     * This function should be called during the view setup phase (e.g., in `onViewCreated`).
     */
    private fun initRecyclerView() {
        val myF77MenuList = listOf(
            getString(R.string.docs),
            getString(R.string.emergency),
            getString(R.string.info),
            getString(R.string.tutorial)
        )

        myF77MenuAdapter = MyF77MenuAdapter { position ->
            viewModel.updateAdapterPosition(position)
            when (position) {
                0 -> findNavController().navigate(R.id.action_myF77MenuFragment_to_documentFragment)
                1 -> findNavController().navigate(R.id.action_myF77MenuFragment_to_emergencyFragment)
                2 -> findNavController().navigate(R.id.action_myF77MenuFragment_to_infoFragment)
                3 -> findNavController().navigate(R.id.action_myF77MenuFragment_to_tutorialFragment)
            }
        }
        rvMyF77.layoutAnimation =
            AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_slide_in)
        rvMyF77.adapter = myF77MenuAdapter
        rvMyF77.scheduleLayoutAnimation()
        myF77MenuAdapter?.submitList(myF77MenuList.toList())
    }
}

