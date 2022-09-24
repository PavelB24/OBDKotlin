package obdKotlin.core

import obdKotlin.WorkMode

interface SystemEventListener {

    fun onWorkModeChanged(workMode: WorkMode)

    fun onDecodeError(fail: FailOn?)

    fun onConnectionLost()

    fun onSwitchMode(extendedMode: Boolean)
}
