package com.ultraviolette.uvclusterhmi.ui.features.settings.sound

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.ultraviolette.uvclusterhmi.R
import com.ultraviolette.uvclusterhmi.domain.ennumerate.ButtonNavigation
import com.ultraviolette.uvclusterhmi.ui.viewModel.SharedViewModel
import com.ultraviolette.uvclusterhmi.utils.AudioHelper
import com.ultraviolette.uvclusterhmi.utils.ViewModelFactory

class SoundFragment : Fragment() {
    private lateinit var tvVolumeLevel: TextView
    private lateinit var sbVolume: SeekBar
    private val viewModel by activityViewModels<SharedViewModel> { ViewModelFactory(context = requireContext()) }
    private lateinit var audioHelper: AudioHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_sound, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        initAudioManager()
        initSeekbar()
    }


    fun handleButtonNavigation(button: Int) {
        when (button) {
            ButtonNavigation.Left.ordinal -> {
                sbVolume.progress = (sbVolume.progress - 1).coerceAtLeast(0)
                handleSeek(R.color.activeSelectionRed, R.drawable.ic_seekbar_thumb_1)
                viewModel.handleSettingsChildClick(true)
                audioHelper.setVolume(sbVolume.progress)
            }
            ButtonNavigation.Right.ordinal -> {
                sbVolume.progress = (sbVolume.progress + 1).coerceAtMost(sbVolume.max)
                handleSeek(R.color.activeSelectionRed, R.drawable.ic_seekbar_thumb_1)
                viewModel.handleSettingsChildClick(true)
                audioHelper.setVolume(sbVolume.progress)
            }

            ButtonNavigation.Back.ordinal ->{
                handleSeek(R.color.white, R.drawable.thumb_white)
            }
        }

    }

    /**
     * Initializes the AudioManagerWrapper and sets the current volume value.
     */
    private fun initAudioManager() {
        audioHelper = AudioHelper(requireContext())
        sbVolume.max = audioHelper.getMaxVolume()
        sbVolume.progress = audioHelper.getCurrentVolume()
    }

    /**
     * Initializes the SeekBar with the current volume value and sets up its listeners.
     */
    private fun initSeekbar() {
        handleSeek(R.color.activeSelectionRed, R.drawable.ic_seekbar_thumb_1)
        sbVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                handleSeek(R.color.activeSelectionRed, R.drawable.ic_seekbar_thumb_1)
                viewModel.handleSettingsChildClick(true)
                audioHelper.setVolume(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                handleSeek(R.color.white, R.drawable.thumb_white)
            }
        })
    }


    /**
     * Binds UI components from the provided root view using their IDs.
     *
     * @param view The root view containing the layout elements.
     *
     * Initializes:
     * - tvVolumeLevel (TextView)
     * - sbVolume (ScrollBar)
     */
    private fun initViews(view: View) {
        sbVolume = view.findViewById(R.id.sbVolume)
        tvVolumeLevel = view.findViewById(R.id.tvVolumeLevel)
    }

    /**
     * Updates the SeekBar's progress drawable and thumb drawable tint.
     *
     * @param drawableTint Resource ID of the color to apply to the progress drawable.
     * @param thumbTint Resource ID of the drawable to set as the SeekBar thumb.
     */
    private fun handleSeek(drawableTint: Int, thumbTint: Int) {
        DrawableCompat.setTint(
            DrawableCompat.wrap(sbVolume.progressDrawable),
            ContextCompat.getColor(requireContext(), drawableTint)
        )
        sbVolume.thumb = ContextCompat.getDrawable(requireContext(), thumbTint)
    }
}