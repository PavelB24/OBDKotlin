package main.protocol

import main.commands.AtCommands
import kotlinx.coroutines.flow.SharedFlow
import main.messages.Message

abstract class BaseProtocolManager {

    abstract val obdCommandFlow: SharedFlow<String>
    abstract suspend fun handleAnswer()
    abstract suspend fun setSettingWithParameter(command: AtCommands, parameter: String)
    abstract suspend fun askObdProto()
    abstract suspend fun resetStates()
    abstract suspend fun askCurrentProto()

    abstract suspend fun reset()
    abstract fun isLastSettingSend(): Boolean
    abstract suspend fun sendNextSettings()
    abstract fun checkIfCanProto(proto: Message): Boolean
    abstract suspend fun setSetting(command: AtCommands)
    abstract suspend fun switchProtocol(protocol: Protocol)
    abstract suspend fun onRestart(strategy: ProtocolManagerStrategy, protocol: Protocol? = null)





}