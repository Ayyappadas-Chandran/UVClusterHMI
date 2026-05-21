package com.ultraviolette.uvclusterhmi.ui.features.myF77

import android.os.Bundle
import android.view.View
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.ui.features.MainActivity

class UIFragment : Fragment(R.layout.fragment_dummy_ui) {

    private lateinit var pager: ViewPager2
    private lateinit var images: IntArray


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        images = requireArguments().getIntArray("images")!!
        val pager: ViewPager2 = view.findViewById(R.id.pager)
        pager.adapter = ImagePagerAdapter(images.toList()) {
            (requireActivity() as MainActivity).setToolbarVisible(true)
            //requireActivity().onBackPressedDispatcher.onBackPressed()
            findNavController().popBackStack()
        }
        (requireActivity() as MainActivity).setToolbarVisible(false)
    }
}
