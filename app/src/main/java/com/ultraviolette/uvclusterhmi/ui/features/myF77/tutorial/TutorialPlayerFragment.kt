package com.ultraviolette.uvclusterhmi.ui.features.myF77.tutorial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.fragment.findNavController
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import com.ultraviolette.uvclusterhmi.ui.viewModel.CarViewModel
import com.ultraviolette.uvclusterhmi.utils.Utilities
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
import kotlinx.coroutines.launch

class TutorialPlayerFragment : Fragment() {
    private lateinit var tvVideoPlayBack: TextView
    private lateinit var playerView: PlayerView
    private var player: ExoPlayer? = null
    private val carViewModel by activityViewModels<CarViewModel> { ViewModelFactory(context = requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tutorial_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initObserver()
        initClickListeners()
    }

    override fun onResume() {
        super.onResume()
        playVideo("android.resource://${requireContext().packageName}/${R.raw.sample_vedio}")
    }

    /**
     * Binds UI components from the provided root view using their IDs.
     *
     * @param view The root view containing the layout elements.
     *
     * Initializes:
     * - tvVideoPlayBack (TextView)
     * - playerView (PlayerView)
     */
    private fun initViews(view: View) {
        playerView = view.findViewById(R.id.playerView)
        tvVideoPlayBack = view.findViewById(R.id.tv_video_play_back)
    }

    /**
     * Initializes click listeners for UI components.
     */
    private fun initClickListeners() {
        tvVideoPlayBack.setOnSoundClickListener(requireContext()) {
            findNavController().navigateUp()
        }
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

    fun handleButtonNavigation(button: Int) {
        when (button) {
            ButtonNavigation.Back.ordinal -> {
                findNavController().navigateUp()
            }
        }
    }

    /**
     * Plays a video from the given URI string using ExoPlayer.
     *
     * @param videoUriString The URI of the video as a string.
     */
    @OptIn(UnstableApi::class)
    private fun playVideo(videoUriString: String) {
        player?.release()
        val videoUri = videoUriString.toUri()
        player = ExoPlayer.Builder(requireContext()).build()
        playerView.player = player
        playerView.showController()
        playerView.controllerShowTimeoutMs = 0
        val mediaItem = MediaItem.fromUri(videoUri)
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.playWhenReady = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player?.release()
        player = null
    }
}