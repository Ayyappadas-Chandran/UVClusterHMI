package com.ultraviolette.uvclusterhmi.domain.dataModel

import androidx.annotation.IdRes

/**
 * Represents an item in the settings menu.
 * @property title The display title of the menu item.
 * @property destination The navigation destination ID (from Navigation Graph).
 */
data class SettingMenuItem(
    val title: String,
    @IdRes val destination: Int
)
