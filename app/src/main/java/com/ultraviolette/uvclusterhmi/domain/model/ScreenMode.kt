package com.ultraviolette.uvclusterhmi.domain.model

sealed interface ScreenMode {
    data object Riding : ScreenMode
    data object SideStandAlert : ScreenMode
    data object TpmsAlert : ScreenMode
    data object IncomingCall : ScreenMode
    data object RearCamera : ScreenMode
    data object NavigationActive : ScreenMode
    data object Menu : ScreenMode
}
