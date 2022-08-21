package obdKotlin.protocol

import kotlinx.coroutines.flow.SharedFlow
import obdKotlin.core.WorkMode
import obdKotlin.profiles.Profile

abstract class BaseProtocolManager {


    abstract val obdCommandFlow: SharedFlow<String>
    abstract val currentHeader: String?
    abstract suspend fun switchToStandardMode(extra: List<String>? = null)
    abstract suspend fun handleAnswer()
    abstract fun resetStates()
    abstract suspend fun askCurrentProto()
    abstract suspend fun resetSession()
    abstract fun isQueueEmpty(): Boolean
    abstract suspend fun sendNextSettings()
    abstract suspend fun setSetting(command: String, workMode: WorkMode)
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