package com.ultraviolette.uvclusterhmi.utils

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout

class ClusterAlertManager(
    private val card: ConstraintLayout,
    private val title: TextView,
    private val message: TextView,
    closeButton: ImageView
) {

    init {
        closeButton.setOnClickListener { dismiss() }
    }

    fun show(titleText: String, messageText: String) {
        title.text = titleText
        message.text = messageText

        if (card.visibility != View.VISIBLE) {
            card.alpha = 0f
            card.translationY = -40f
            card.visibility = View.VISIBLE

            card.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(250)
                .start()
        }
    }

    fun dismiss() {
        card.animate()
            .alpha(0f)
            .translationY(-40f)
            .setDuration(200)
            .withEndAction { card.visibility = View.GONE }
            .start()
    }
}

