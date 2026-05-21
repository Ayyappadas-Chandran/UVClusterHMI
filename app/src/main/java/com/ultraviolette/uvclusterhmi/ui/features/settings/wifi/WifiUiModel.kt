package com.ultraviolette.uvclusterhmi.ui.features.settings.wifi

data class WifiUiModel(
    val ssid: String,
    val level: Int,        // 0 to 5
    val isSecured: Boolean
)
