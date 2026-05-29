package com.ultraviolette.uvclusterhmi.ui.features.myF77.document

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
import com.ultraviolette.uvclusterhmi.ClusterApplication
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import com.ultraviolette.uvclusterhmi.ui.adapter.MyF77MenuAdapter
import com.ultraviolette.uvclusterhmi.ui.viewModel.ClusterViewModel
import com.ultraviolette.uvclusterhmi.utils.Utilities
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener
import kotlinx.coroutines.launch

class DocumentMenuFragment : Fragment() {
    private lateinit var ivBack: ImageView
    private lateinit var rvDocuments: RecyclerView
    private var documentAdapter: MyF77MenuAdapter? = null
    private val viewModel: DocumentMenuViewModel by viewModels()
    private val clusterViewModel: ClusterViewModel by activityViewModels {
        ClusterViewModel.Factory(requireActivity().application as ClusterApplication)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_document_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initClickListener()
        initRecyclerView()
        observeAdapterPosition()
    }


    fun handleButtonNavigation(button: Int) {
        var adapterPosition = viewModel.adapterPosition.value
        when (button) {
            ButtonNavigation.Right.ordinal -> {
                if (adapterPosition >= documentAdapter!!.itemCount - 1) return
                adapterPosition++
                viewModel.updateAdapterPosition(adapterPosition)
            }

            ButtonNavigation.Left.ordinal -> {
                if (adapterPosition <= 0) return
                adapterPosition--
                viewModel.updateAdapterPosition(adapterPosition)
            }

            ButtonNavigation.Enter.ordinal -> {
                rvDocuments.findViewHolderForAdapterPosition(adapterPosition)?.itemView?.performClick()
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
     * - tvBack,(TextView)
     * - rvDocuments (RecyclerView)
     *
     */
    private fun initViews(view: View) {
        ivBack = view.findViewById(R.id.ivBack)
        rvDocuments = view.findViewById(R.id.rvDocuments)
    }


    private fun observeAdapterPosition() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.adapterPosition.collect { position ->
                        documentAdapter?.updateSelectedPosition(position)
                        rvDocuments.scrollToPosition(position)
                    }
                }
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

    /**
     * Initializes click listeners for UI components.
     */
    private fun initClickListener() {
        ivBack.setOnSoundClickListener(requireContext()) {
            findNavController().navigateUp()
        }
    }

    /**
     * Initializes the horizontal RecyclerView for document selection.
     *
     * Sets up the [MyF77MenuAdapter] with a click listener that:
     * - Navigates to [DocumentFragment] with the selected document type.
     */
    private fun initRecyclerView() {
        val documentList = listOf(
            getString(R.string.registration),
            getString(R.string.license),
            getString(R.string.insurance)
        )
        documentAdapter = MyF77MenuAdapter { position ->
            viewModel.updateAdapterPosition(position)
            val bundle = Bundle()
            bundle.putInt(Utilities.ARG_DOCUMENT_TYPE, position)
            findNavController().navigate(
                R.id.documentFragment,
                bundle
            )
        }
        rvDocuments.layoutAnimation =
            AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_slide_in)
        rvDocuments.adapter = documentAdapter
        rvDocuments.scheduleLayoutAnimation()
        documentAdapter?.submitList(documentList.toList())
    }

}
