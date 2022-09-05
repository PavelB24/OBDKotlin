package obdKotlin.protocol

import kotlinx.coroutines.flow.SharedFlow
<<<<<<< HEAD
import obdKotlin.core.WorkMode
=======
>>>>>>> 61257416ebc4218fbd9b3c63ea2dcb4f83c64b4a
import obdKotlin.profiles.Profile

abstract class BaseProtocolManager {


    abstract val obdCommandFlow: SharedFlow<String>
    abstract val currentHeader: String?
    abstract suspend fun switchToStandardMode(extra: List<String>? = null)
<<<<<<< HEAD
    abstract suspend fun handleAnswer()
=======
    abstract suspend fun handleInitialAnswer()
>>>>>>> 61257416ebc4218fbd9b3c63ea2dcb4f83c64b4a
    abstract fun resetStates()
    abstract suspend fun askCurrentProto()
    abstract suspend fun resetSession()
    abstract fun isQueueEmpty(): Boolean
<<<<<<< HEAD
    abstract suspend fun sendNextSettings()
    abstract suspend fun setSetting(command: String, workMode: WorkMode)
=======
    abstract suspend fun sendNextSettings(removeLast: Boolean = false, onEmptyQueue: (suspend() -> Unit)? = null)
    abstract suspend fun setSetting(command: String)
>>>>>>> 61257416ebc4218fbd9b3c63ea2dcb4f83c64b4a
    abstract suspend fun switchProtocol(protocol: Protocol)
    abstract suspend fun onRestart(
        strategy: ProtocolManagerStrategy,
        warmStart: Boolean,
        protocol: Protocol? = null,
        extra: List<String>? = null
    )
    abstract suspend fun setHeaderAndReceiver(
        headerAddress: String,
        receiverAddress: String?,
        isAlreadyCan: Boolean,
        extra: List<String>? = null)
    abstract suspend fun startWithProfile(profile: Profile)



}