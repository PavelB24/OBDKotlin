package obdKotlin.core

<<<<<<< HEAD
=======
import obdKotlin.WorkMode

>>>>>>> 61257416ebc4218fbd9b3c63ea2dcb4f83c64b4a
interface SystemEventListener {

    fun onWorkModeChanged(workMode: WorkMode)

    fun onDecodeError(command: String?)

    fun onSwitchMode(canMode: Boolean)
}