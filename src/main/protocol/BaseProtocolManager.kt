package main.protocol

import AtCommands
import kotlinx.coroutines.flow.SharedFlow
import main.ProtocolManagerStrategy
import main.messages.Message

abstract class BaseProtocolManager {

    abstract val obdCommandFlow: SharedFlow<String>
    abstract suspend fun handleAnswer()
    abstract suspend fun askObdProto()
    abstract suspend fun askCurrentProto()

    abstract suspend fun reset()
    abstract fun isLastSettingSend(): Boolean
    abstract suspend fun sendNextSettings()
    abstract fun checkIfCanProto(proto: Message): Boolean
    abstract suspend fun setSetting(command: AtCommands)
    abstract suspend fun onRestart(strategy: ProtocolManagerStrategy, protocol: Protocol? = null)





}