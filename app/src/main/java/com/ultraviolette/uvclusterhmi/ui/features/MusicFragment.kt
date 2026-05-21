package com.ultraviolette.uvclusterhmi.ui.features

import android.media.MediaPlayer
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import com.ultraviolette.uvclusterhmi.ui.viewModel.CarViewModel
import com.ultraviolette.uvclusterhmi.utils.AudioHelper
import com.ultraviolette.uvclusterhmi.utils.Utilities
import com.ultraviolette.uvclusterhmi.utils.Utilities.setOnSoundClickListener
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class MusicFragment : Fragment() {
    private lateinit var tvTotalTime: TextView
    private lateinit var tvSongName: TextView
    private lateinit var tvAlbumName: TextView
    private lateinit var tvCurrentTime: TextView
    private lateinit var ivPlayPause: ImageView
    private lateinit var ivForward: ImageView
    private lateinit var ivBackward: ImageView
    private lateinit var sbVolume: SeekBar
    private lateinit var sbSong: SeekBar
    private lateinit var gestureDetector: GestureDetector
    private lateinit var tvBack: TextView
    private var mediaPlayer: MediaPlayer? = null
    private val songs = listOf(R.raw.blinding_lights, R.raw.infected)
    private var currentIndex = 0
    private val carViewModel by activityViewModels<CarViewModel> { ViewModelFactory(context = requireContext()) }
    private val songName by lazy {
        listOf(
            getString(R.string.blinding_lights),
            getString(R.string.infected)
        )
    }
    private val albumName by lazy {
        listOf(
            getString(R.string.the_weekend),
            getString(R.string.sick_sick)
        )
    }

    private lateinit var audioHelper: AudioHelper
    private var timeJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_music, container, false)
        addSwipeGesture(rootView)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        initUi()
        initClickListener()
        initObserver()
        playSong(currentIndex)
    }

    private fun initObserver(){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
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
     * Initializes the UI components with their respective states.
     */
    private fun initUi() {
        (activity as? MainActivity)?.handleToolbar(true)
    }

    fun handleButtonNavigation(button: Int) {
        when (button) {
            ButtonNavigation.Right.ordinal -> {
                pulseAnimation(ivForward)
            }

            ButtonNavigation.Left.ordinal -> {
                pulseAnimation(ivBackward)
            }

            ButtonNavigation.Enter.ordinal -> {
                handlePlayPause()
            }

            ButtonNavigation.Back.ordinal -> {
                findNavController().navigateUp()
            }
        }
    }

    private fun handlePlayPause() {
        mediaPlayer?.let {
            ivPlayPause.setImageResource(if (it.isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
            pulseAnimation(ivPlayPause)
        }
    }

    /**
     * Adds swipe gesture detection to navigate back on swipe up.
     */
    private fun addSwipeGesture(rootView: View) {
        gestureDetector = GestureDetector(
            requireContext(),
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    handlePlayPause()
                    return true
                }

                override fun onFling(
                    e1: MotionEvent?, e2: MotionEvent,
                    velocityX: Float, velocityY: Float
                ): Boolean {
                    if (e1 == null) return false
                    val diffY = e2.y - e1.y
                    val diffX = e2.x - e1.x
                    if (abs(diffX) > abs(diffY) && abs(diffX) > 100 && abs(velocityX) > 100) {
                        if (diffX > 0) {
                            pulseAnimation(ivForward)
                        } else {
                            pulseAnimation(ivBackward)
                        }
                        return true
                    }
                    return false
                }
            })
        rootView.setOnTouchListener { v, event ->
            val handled = gestureDetector.onTouchEvent(event)
            if (!handled && event.action == MotionEvent.ACTION_UP) {
                v.performClick()
            }
            true
        }
    }

    /**
     * Binds UI components from the provided root view using their IDs.
     * @param view The root view containing the layout elements.
     * Initializes:
     * - tvTotalTime, tvCurrentTime, tvSongName, tvAlbumName(TextView)
     * - ivPlayPause, ivForward, ivBackward(ImageView)
     * - tvSongName, tvAlbumName(TextView)
     */
    private fun initView(view: View) {
        tvTotalTime = view.findViewById(R.id.tvTotalTime)
        tvCurrentTime = view.findViewById(R.id.tvCurrentTime)
        tvSongName = view.findViewById(R.id.tvSongName)
        tvAlbumName = view.findViewById(R.id.tvAlbumName)
        tvBack = view.findViewById(R.id.tvBack)

        ivPlayPause = view.findViewById(R.id.ivPlayPause)
        ivForward = view.findViewById(R.id.ivForward)
        ivBackward = view.findViewById(R.id.ivBackward)

        sbVolume = view.findViewById(R.id.sbVolume)
        sbSong = view.findViewById(R.id.sbSong)

        tvSongName.text = songName[currentIndex]
        tvAlbumName.text = albumName[currentIndex]
    }

    /**
     * Initializes click listeners for UI components.
     */
    private fun initClickListener() {
        audioHelper = AudioHelper(requireContext())
        audioHelper.setVolume(audioHelper.getCurrentVolume())
        sbVolume.max = audioHelper.getMaxVolume()
        sbVolume.progress = audioHelper.getCurrentVolume()
        sbVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    audioHelper.setVolume(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        sbSong.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val newPosition = (progress / 100f) * (mediaPlayer?.duration ?: 1)
                    mediaPlayer?.seekTo(newPosition.toInt())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        tvBack.setOnSoundClickListener(requireContext()) {
            findNavController().navigateUp()
        }
    }

    /**
     * Performs a pulse animation on the provided view.
     */
    private fun pulseAnimation(view: View) {
        val pulse = AnimationUtils.loadAnimation(requireContext(), R.anim.pulse)
        view.startAnimation(pulse)
        pulse.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                view.isVisible = true
            }

            override fun onAnimationEnd(animation: Animation?) {
                view.isVisible = false
                when (view) {
                    ivForward -> nextSong()
                    ivBackward -> prevSong()
                    ivPlayPause -> if (mediaPlayer?.isPlaying == true) mediaPlayer?.pause() else mediaPlayer?.start()
                }
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
    }

    /**
     * Plays the song at the given index.
     */
    private fun playSong(index: Int) {
        mediaPlayer?.let {
            it.stop()
            it.release()
        }
        mediaPlayer = MediaPlayer.create(requireContext(), songs[index]).apply {
            start()
            setOnCompletionListener { nextSong() }
        }

        tvSongName.text = songName[index]
        tvAlbumName.text = albumName[index]
        tvTotalTime.text = formatTime(mediaPlayer?.duration ?: 1)
        ivPlayPause.setImageResource(R.drawable.ic_pause)

        sbSong.progress = 0
        tvCurrentTime.text = getString(R.string._00_00)
        startTimer()
    }

    /**
     * Formats the given milliseconds into a formatted time string.
     */
    private fun formatTime(milliseconds: Int): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds.toLong())
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds.toLong()) % 60
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    /**
     * Plays the previous song.
     */
    private fun prevSong() {
        currentIndex = if (currentIndex - 1 < 0) songs.size - 1 else currentIndex - 1
        playSong(currentIndex)
    }

    /**
     * Plays the next song.
     */
    private fun nextSong() {
        currentIndex = (currentIndex + 1) % songs.size
        playSong(currentIndex)
    }

    /**
     * Starts a timer to update the current playback position.
     */
    private fun startTimer() {
        timeJob?.cancel()
        timeJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                mediaPlayer?.let {
                    val currentPos = it.currentPosition
                    val duration: Int = it.duration
                    val minutes = (currentPos / 1000) / 60
                    val seconds = (currentPos / 1000) % 60
                    tvCurrentTime.text = String.format(Locale.US, "%02d:%02d", minutes, seconds)
                    val progress = if (duration > 0) currentPos * 100 / duration else 0
                    sbSong.progress = progress
                }
                delay(1000)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer?.release()
        timeJob?.cancel()
    }
}
