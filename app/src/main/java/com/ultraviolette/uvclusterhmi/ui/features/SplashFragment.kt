package com.ultraviolette.uvclusterhmi.ui.features

import android.net.Uri
import android.os.Bundle
import android.util.Log.d
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.VideoView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.ui.viewModel.SharedViewModel
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory

class SplashFragment : Fragment() {
    private lateinit var vVSplash: VideoView
    private val viewModel by viewModels<SharedViewModel> { ViewModelFactory(context = requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        d("SplashFragment","Splash on onCreateView")
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.handleToolbar(false)
        viewModel.saveUpdate(true)
        playVideo(getVideoUri())
    }

    private fun getVideoUri(isUpdate: Boolean = false): Uri {
        val mode = getMode()
        return if (isUpdate) {
            val videoStr =
                if (mode == getString(R.string.day)) R.raw.updated_intro_day else R.raw.updated_intro_night
            "android.resource://${requireContext().packageName}/$videoStr".toUri()
        } else {
            val videoStr =
                if (mode == getString(R.string.day)) R.raw.splash_day else R.raw.splash_night
            "android.resource://${requireContext().packageName}/$videoStr".toUri()
        }
    }

    private fun playVideo(videoUri: Uri, shouldNavigate: Boolean = false) {
        vVSplash.setVideoURI(videoUri)
        vVSplash.setOnPreparedListener { mp ->
            mp.isLooping = false
            vVSplash.start()
        }
        vVSplash.setOnCompletionListener {
            if (shouldNavigate) {
                val options = NavOptions.Builder()
                    .setPopUpTo(R.id.splashFragment, true)
                    .build()
                findNavController().navigate(R.id.dashboardFragment, null, options)
                return@setOnCompletionListener
            }
            playVideo(getVideoUri(true), true)
        }
    }

    fun getMode(): String {
        return when (viewModel.mode) {
            getString(R.string.day) -> getString(R.string.day)
            getString(R.string.night) -> getString(R.string.night)
            else -> {
                if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
                    getString(R.string.night)
                else getString(R.string.day)
            }
        }
    }

    private fun initView(view: View) {
        vVSplash = view.findViewById(R.id.vVSplash)
    }
}