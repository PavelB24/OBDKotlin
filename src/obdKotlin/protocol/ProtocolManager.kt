package obdKotlin.protocol

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import obdKotlin.commands.Commands
import obdKotlin.exceptions.ModsConflictException
import obdKotlin.profiles.Profile
import java.lang.IllegalStateException
import java.util.concurrent.ConcurrentLinkedQueue

internal class ProtocolManager : BaseProtocolManager() {

    private var strategy: ProtocolManagerStrategy? = null
    private val _obdCommandFlow = MutableSharedFlow<String>()
    override val obdCommandFlow: SharedFlow<String> = _obdCommandFlow
    override val currentHeader: String? = null

    /**
     * should not edit in runtime
     */
    private val standardSettingsSet = mutableSetOf(
        Commands.AtCommands.EchoOff.command,
        Commands.AtCommands.PrintingSpacesOff.command,
        Commands.AtCommands.AllowLongMessages.command

    )

    private var userProtocol: Protocol? = null
    private val settingsQueue = ConcurrentLinkedQueue<String>()

//    override suspend fun switchToStandardMode(extra: List<String>?) {
//        val isQueueEmpty = settingsQueue.isEmpty()
//        settingsQueue.addAll(canOffCommandsSet)
//        extra?.let {
//            it.forEach { command ->
//                settingsQueue.add(CommandUtil.formatAT(command.replace(" ", "")))
//            }
//        }
//        if (isQueueEmpty) {
//            _obdCommandFlow.emit(settingsQueue.poll())
//        }
//    }

    @Throws(IllegalStateException::class)
    override suspend fun handleInitialAnswer() {
        strategy?.let {
            when (it) {
                ProtocolManagerStrategy.TRY -> tryProto()
                ProtocolManagerStrategy.SET -> setProto()
                ProtocolManagerStrategy.AUTO -> setProto()
            }
        }
    }

    private suspend fun setProto() {
        if (userProtocol != null) {
            _obdCommandFlow.emit("${Commands.AtCommands.SetProto}${userProtocol!!.hexOrdinal}\r")
        } else {
            _obdCommandFlow.emit("${Commands.AtCommands.SetProto}${Protocol.AUTOMATIC.hexOrdinal}\r")
        }
    }

    private suspend fun tryProto() {
        if (userProtocol != null) {
            _obdCommandFlow.emit("${Commands.AtCommands.TryProto}${userProtocol!!.hexOrdinal}\r")
        } else {
            _obdCommandFlow.emit("${Commands.AtCommands.TryProto}${Protocol.AUTOMATIC.hexOrdinal}\r")
        }
    }

    override fun resetStates() {
        strategy = null
        userProtocol = null
        settingsQueue.clear()
    }

    override suspend fun askCurrentProto() {
        _obdCommandFlow.emit(Commands.AtCommands.GetVehicleProtoAsNumber.command)
    }

    override suspend fun onRestart(
        strategy: ProtocolManagerStrategy,
        warmStart: Boolean,
        protocol: Protocol?,
        extra: List<String>?
    ) {
        this.strategy = strategy
        userProtocol = protocol
        prepare(extra)
        val command = if (warmStart) Commands.AtCommands.WarmStart.command else Commands.AtCommands.ResetAll.command
        _obdCommandFlow.emit(command)
    }

//    override suspend fun setHeaderAndReceiver(
//        headerAddress: String,
//        receiverAddress: String?,
//        isAlreadyCan: Boolean,
//        extra: List<String>?
//    ) {
//        if (!isAlreadyCan) {
//            settingsQueue.addAll(canOnCommandsSet)
//        }
//        val isQueueEmpty = settingsQueue.isEmpty()
//        settingsQueue.add("${Commands.AtCommands.SetHeader.command}$headerAddress\r")
//        receiverAddress?.let {
//            settingsQueue.add("${Commands.AtCommands.SetReceiverAdrFilter.command}$it\r")
//        }
//        extra?.let {
//            it.forEach { command ->
//                settingsQueue.add(CommandUtil.formatAT(command.replace(" ", "")))
//            }
//        }
//        if (isQueueEmpty) {
//            sendNextSettings()
//        }
//    }

    override suspend fun startWithProfile(profile: Profile) {
        strategy = ProtocolManagerStrategy.SET
        settingsQueue.addAll(standardSettingsSet)
        profile.settingsAndParams.forEach {
            settingsQueue.add(it)
        }
        _obdCommandFlow.emit("${Commands.AtCommands.ResetAll.command}\r")
    }

    override suspend fun resetSession() {
        strategy = null
        _obdCommandFlow.emit("${Commands.AtCommands.ResetAll.command}\r")
    }

    override fun isQueueEmpty(): Boolean = settingsQueue.isEmpty()

    override suspend fun sendNextSettings(removeLast: Boolean, onEmptyQueue: (suspend () -> Unit)?) {
        if (removeLast) {
            settingsQueue.poll()
        }
        if (!isQueueEmpty()) {
            _obdCommandFlow.emit(settingsQueue.peek())
        } else onEmptyQueue?.invoke()
    }

    /**
     * Only when we start with strategy
     */
    private fun prepare(extra: List<String>?) {
        if (userProtocol != Protocol.AUTOMATIC && strategy != ProtocolManagerStrategy.AUTO) {
            settingsQueue.addAll(standardSettingsSet)
            extra?.let {
                standardSettingsSet.addAll(it)
            }
        } else if (userProtocol == Protocol.AUTOMATIC && strategy == ProtocolManagerStrategy.AUTO) {
            settingsQueue.addAll(standardSettingsSet)
            extra?.let {
                standardSettingsSet.addAll(it)
            }
        } else {
            resetStates()
            throw ModsConflictException("Strategy: $strategy on UserProtocol: $userProtocol")
        }
    }

    override suspend fun setSetting(command: String) {
        settingsQueue.add(command)
        if (isQueueEmpty()) {
            sendNextSettings()
        }
    }

    override suspend fun switchProtocol(protocol: Protocol) {
        _obdCommandFlow.emit("${Commands.AtCommands.SetProto}${protocol.hexOrdinal}\r")
    }
}
