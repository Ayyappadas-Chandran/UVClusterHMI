package com.ultraviolette.uvclusterhmi.ui.features.myF77.document

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import com.ultraviolette.uvclusterhmi.ui.adapter.DocumentAdapter
import com.ultraviolette.uvclusterhmi.ui.customWidget.PinchImageView
import com.ultraviolette.uvclusterhmi.ui.viewModel.CarViewModel
import com.ultraviolette.uvclusterhmi.utils.Utilities
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
import kotlinx.coroutines.launch
import kotlin.math.abs

class DocumentFragment : Fragment() {
    private lateinit var ivBack: ImageView
    private lateinit var tvDocumentDetails: TextView
    private lateinit var ivZoomImage: PinchImageView
    private lateinit var vpDocuments: ViewPager2
    private lateinit var llImageSettingChange: LinearLayout
    private lateinit var mScaleGestureDetector: ScaleGestureDetector
    private var mScaleFactor = 1.0f
    private val carViewModel by activityViewModels<CarViewModel> { ViewModelFactory(context = requireContext()) }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_document, container, false)
    }

    fun handleButtonNavigation(button: Int) {
        when (button) {
            ButtonNavigation.Back.ordinal -> {
                findNavController().navigateUp()
            }
            ButtonNavigation.Top.ordinal->{

            }
            ButtonNavigation.Bottom.ordinal ->{

            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initObserver()
        initClickListener()
        val documentType = arguments?.getInt(Utilities.ARG_DOCUMENT_TYPE)
        selectedDocument(documentType)
        mScaleGestureDetector = ScaleGestureDetector(requireContext(), ScaleListener())
    }

    private fun initObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    carViewModel.swiftButton.collect { swiftButton ->
                        val button = Utilities.getButtonState(swiftButton)
                        if(button == ButtonNavigation.None) return@collect
                        handleButtonNavigation(button.ordinal)
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
     * - tvBack, tvDocumentDetails (TextViews)
     * - ivZoomImage (ImageView)
     * - llImageSettingChange (Linear layout)
     * - vpDocuments (ViewPager)
     */
    private fun initViews(view: View) {
        ivBack = view.findViewById(R.id.ivBack)
        tvDocumentDetails = view.findViewById(R.id.tvDocumentDetails)

        ivZoomImage = view.findViewById(R.id.ivZoomImage)

        vpDocuments = view.findViewById(R.id.vpDocuments)
        llImageSettingChange = view.findViewById(R.id.llImageSettingChange)
    }


    /**
     * Displays document images based on the selected document type.
     *
     * @param documentType The type of document:
     * - 0: Registration
     * - 1: License
     * - 2: Insurance
     *
     * Updates the document title and initializes the ViewPager with corresponding images.
     */
    private fun selectedDocument(documentType: Int?) {
        val documentList = when (documentType) {
            0 -> {
                tvDocumentDetails.text = getString(R.string.registration)
                listOf(R.drawable.image_rc_front, R.drawable.image_rc_back)
            }

            1 -> {
                tvDocumentDetails.text = getString(R.string.license)
                listOf(R.drawable.image_dl_front, R.drawable.image_dl_back)
            }

            else -> {
                tvDocumentDetails.text = getString(R.string.insurance)
                listOf(
                    R.drawable.image_insurance_1,
                    R.drawable.image_insurance_2,
                    R.drawable.image_insurance_3,
                    R.drawable.image_insurance_4,
                )
            }
        }
        initViewPager(documentList)
    }

    /**
     * Initializes click listeners for UI components.
     */
    private fun initClickListener() {
        ivBack.setOnSoundClickListener(requireContext()) {
            findNavController().navigateUp()
        }
        ivZoomImage.setOnSoundClickListener(requireContext()) {
            ivZoomImage.isVisible = false
            llImageSettingChange.isVisible = true
            vpDocuments.isVisible = true
        }
    }

    /**
     * Initializes the document ViewPager with a list of image resources.
     *
     * Sets up the [com.ultraviolette.uvclusterhmi.ui.adapter.DocumentAdapter], applies padding and a page transformer for spacing,
     * and handles zoom-in image preview when an item is long-clicked.
     *
     * @param documentList List of drawable resource IDs to display in the ViewPager.
     */
    private fun initViewPager(documentList: List<Int>) {
        val adapter = DocumentAdapter(requireContext(), documentList) {
            ivZoomImage.isVisible = true
            ivZoomImage.resetZoom()
            llImageSettingChange.isVisible = false
            vpDocuments.isVisible = false
            ivZoomImage.setImageResource(it)
        }
        vpDocuments.adapter = adapter
        val pageMarginPx = resources.getDimensionPixelOffset(R.dimen.pageMargin)
        val offsetPx = resources.getDimensionPixelOffset(R.dimen.offset)
        vpDocuments.apply {
            offscreenPageLimit = 2
            getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            setPadding(offsetPx, 0, offsetPx, 0)
            setPageTransformer { page, position ->
                val tag = page.tag
                val isPortrait = tag == Utilities.IMAGE_PORTRAIT
                val offSetDifference = if (isPortrait) 5 else 2
                val offset = position * -(offSetDifference * offsetPx + pageMarginPx)
                if (orientation == ViewPager2.ORIENTATION_HORIZONTAL) {
                    page.translationX = offset
                } else {
                    page.translationY = offset
                }
            }
        }
    }

    /**
     * Handles touch events for the ViewPager.
     */
    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
            val scale = scaleGestureDetector.scaleFactor
            if (abs(scale - 1f) > 0.01f) {
                mScaleFactor *= scale
                mScaleFactor = mScaleFactor.coerceIn(01f, 3f)
                ivZoomImage.scaleX = mScaleFactor
                ivZoomImage.scaleY = mScaleFactor
            }
            return true
        }
    }
}
