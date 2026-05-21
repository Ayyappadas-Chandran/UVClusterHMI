package com.ultraviolette.uvclusterhmi.domain.dataModel

/**
 * Represents an item in the settings menu.
 *
 * @property videoName The display Video title of the menu item.
 * @property videoDuration The display Video time of the menu item.
 */
data class TutorialVideoInfo(
    val videoName: String,
    val videoDuration: String
)
