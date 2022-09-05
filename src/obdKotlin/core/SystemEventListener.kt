package obdKotlin.core

import obdKotlin.WorkMode

interface SystemEventListener {

    fun onWorkModeChanged(workMode: WorkMode)

    fun onDecodeError(command: String?)

    fun onSwitchMode(canMode: Boolean)
}