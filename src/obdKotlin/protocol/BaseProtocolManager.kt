package obdKotlin.protocol

import kotlinx.coroutines.flow.SharedFlow
import obdKotlin.commands.Commands
import obdKotlin.messages.Message
import obdKotlin.profiles.CustomProfile
import obdKotlin.profiles.Profile

abstract class BaseProtocolManager {


    abstract val obdCommandFlow: SharedFlow<String>
    abstract val currentHeader: String?
    abstract suspend fun handleAnswer()
    abstract suspend fun setSettingWithParameter(command: Commands.AtCommands, parameter: String)
    abstract fun resetStates()
    abstract suspend fun askCurrentProto()
    abstract suspend fun resetSession()
    abstract fun isLastSettingSend(): Boolean
    abstract suspend fun sendNextSettings()
    abstract fun checkIfCanProto(message: Message): Boolean
    abstract suspend fun setSetting(command: Commands.AtCommands)
    abstract suspend fun switchProtocol(protocol: Protocol)
    abstract suspend fun onRestart(
        strategy: ProtocolManagerStrategy,
        protocol: Protocol? = null,
        extra: List<String>? = null
    )
    abstract suspend fun setHeaderAndReceiver(headerAddress: String, receiverAddress: String, isAlreadyCan: Boolean)
    abstract suspend fun startWithProfile(profile: Profile)
    abstract suspend fun startWithProfile(profile: CustomProfile)



}