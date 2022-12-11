package obdKotlin.core

import obdKotlin.WorkMode

interface SystemEventListener {

    fun onWorkModeChanged(workMode: WorkMode)

    fun onDecodeError(fail: FailOn?)

    fun onSourceError()

    fun onSwitchMode(extendedMode: Boolean)
}
