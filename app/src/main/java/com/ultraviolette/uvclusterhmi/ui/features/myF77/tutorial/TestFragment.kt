package com.ultraviolette.uvclusterhmi.ui.features.myF77.tutorial
import com.ultraviolette.uvclusterhmi.ui.adapter.TutorialPagerAdapter


import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.ultraviolette.uvclusterhmi.R

class TestFragment : Fragment(R.layout.fragment_new_tutorial_test) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val back = view.findViewById<View>(R.id.tvBack)
        val pager = view.findViewById<View>(R.id.viewPager)

        if (back == null) {
            throw RuntimeException("tvBack NOT FOUND in layout")
        }

        if (pager == null) {
            throw RuntimeException("viewPager NOT FOUND in layout")
        }

        // If both exist, then continue
        view.findViewById<View>(R.id.tvBack).setOnClickListener {
            findNavController().popBackStack()
        }

        val images = listOf(
            R.drawable.atest1,
            R.drawable.atest2,
            R.drawable.atest3,
            R.drawable.atest4,
            R.drawable.atest5,
            R.drawable.atest6,
            R.drawable.atest7
        )

        val viewPager = view.findViewById<ViewPager2>(R.id.viewPager)
        viewPager.adapter = TutorialPagerAdapter(images)
    }
}

