package obdKotlin.core

import obdKotlin.WorkMode

interface SystemEventListener {

    fun onWorkModeChanged(workMode: WorkMode)

    fun onDecodeError(fail: FailOn?)

    fun onSourceError(source: SourceType)

    fun onSwitchMode(extendedMode: Boolean)

    enum class SourceType {
        BLUETOOTH, WIFI
    }
}
