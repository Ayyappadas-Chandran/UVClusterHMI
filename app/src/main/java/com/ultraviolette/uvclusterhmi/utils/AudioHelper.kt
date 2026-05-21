package com.ultraviolette.uvclusterhmi.utils

import android.content.Context
import android.media.AudioManager
import kotlin.math.roundToInt

class AudioHelper(context: Context) {
    private val audioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    /**
     * Returns the current volume as a percentage (0–100).
     */
    fun getCurrentVolume(): Int {
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        return ((current.toFloat() / max.toFloat()) * 100).roundToInt()
    }

    /**
     * Sets the volume for the music stream using a percentage (0–100).
     */
    fun setVolume(percent: Int) {
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val safePercent = percent.coerceIn(0, 100)
        val newVolume = ((safePercent / 100f) * max).roundToInt()
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            newVolume,
            AudioManager.FLAG_PLAY_SOUND
        )
    }

    /**
     * Returns the maximum system volume (raw units).
     */
    fun getMaxVolume(): Int {
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    }
}