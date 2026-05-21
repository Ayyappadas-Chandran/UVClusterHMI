package com.ultraviolette.cluster.aidl

object SignalTypes {
    const val SIGNAL_SPEED          = 1L shl 0
    const val SIGNAL_SOC            = 1L shl 1
    const val SIGNAL_REGEN          = 1L shl 2
    const val SIGNAL_RIDE_MODE      = 1L shl 3
    const val SIGNAL_INDICATOR      = 1L shl 4
    const val SIGNAL_ABS_MODE       = 1L shl 5
    const val SIGNAL_HILL_HOLD      = 1L shl 6
    const val SIGNAL_LOCKDOWN       = 1L shl 7
    const val SIGNAL_SLEEP_WAKE     = 1L shl 8
    const val SIGNAL_MTC_MODE       = 1L shl 9
    const val SIGNAL_CHARGER        = 1L shl 10
    const val SIGNAL_CRUISE         = 1L shl 11
    const val SIGNAL_SNAPSHOT       = 1L shl 15
    const val SIGNAL_BT             = 1L shl 16
    const val SIGNAL_WIFI           = 1L shl 17
}
