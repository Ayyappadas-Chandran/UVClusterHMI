package com.ultraviolette.uvclusterhmi.ui.composable.screen

import android.net.Uri
import android.widget.VideoView
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import com.ultraviolette.uvclusterhmi.R

/**
 * Splash screen — plays the intro video then the update video, then calls [onComplete].
 *
 * Rule 10 exception: uses [AndroidView] wrapping [VideoView] because
 * ExoPlayer / VideoView require a Surface that has no Compose-native equivalent.
 *
 * @param mode        Day/night mode string from [ClusterPrefsState.mode]:
 *                    "day", "night", or "Auto" (resolved via system night mode).
 * @param onComplete  Called once both videos finish; triggers navigation to Riding.
 */
@Composable
fun SplashScreen(
    mode: String,
    onComplete: () -> Unit,
) {
    val context = LocalContext.current

    /** True = playing intro; false = playing update video. */
    var playingIntro by remember { mutableStateOf(true) }

    val isNight = remember(mode) {
        when (mode.lowercase()) {
            "day"  -> false
            "night" -> true
            else -> AppCompatDelegate.getDefaultNightMode() ==
                    AppCompatDelegate.MODE_NIGHT_YES
        }
    }

    fun videoUri(isUpdate: Boolean): Uri {
        val resId = when {
            isUpdate && !isNight -> R.raw.updated_intro_day
            isUpdate && isNight  -> R.raw.updated_intro_night
            !isUpdate && !isNight -> R.raw.splash_day
            else                  -> R.raw.splash_night
        }
        return "android.resource://${context.packageName}/$resId".toUri()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory  = { ctx ->
                VideoView(ctx).apply {
                    setOnPreparedListener { mp -> mp.isLooping = false; start() }
                    setOnCompletionListener {
                        if (playingIntro) {
                            // First video done → play update video
                            playingIntro = false
                            setVideoURI(videoUri(isUpdate = true))
                        } else {
                            // Both videos done → proceed to cluster UI
                            onComplete()
                        }
                    }
                    setVideoURI(videoUri(isUpdate = false))
                }
            },
            // No update needed: transitions are driven by the completion listener above.

        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(widthDp = 480,  heightDp = 320, name = "Compact — night")
@Preview(widthDp = 800,  heightDp = 480, name = "Standard — night")
@Preview(widthDp = 1024, heightDp = 600, name = "Wide — night")
@Composable
private fun SplashScreenPreview() {
    // VideoView won't render in preview — shows the black background only.
    SplashScreen(mode = "night", onComplete = {})
}