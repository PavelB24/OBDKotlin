package obdKotlin.protocol


import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import obdKotlin.core.WorkMode
import obdKotlin.commands.AT_PREFIX
import obdKotlin.commands.Commands
import obdKotlin.commands.POSTFIX
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
        "${Commands.AtCommands.EchoOff.command}$POSTFIX",
        "${Commands.AtCommands.PrintingSpacesOff.command}$POSTFIX",
        "${Commands.AtCommands.AllowLongMessages.command}$POSTFIX",

        )

    private val canOnCommandsSet: Set<String> = setOf(
        "${Commands.AtCommands.FlowControlOff.command}$POSTFIX",
//        "${Commands.AtCommands.AutoFormatCanFramesOff.command}$POSTFIX",
//        "${Commands.AtCommands.SetWakeUpMessagesOff.command}$POSTFIX"
    )

    private val canOffCommandsSet = setOf<String>(
        "${Commands.AtCommands.FlowControlOn}$POSTFIX",
    )

    private var userProtocol: Protocol? = null
    private val settingsQueue = ConcurrentLinkedQueue<String>()


    override suspend fun switchToStandardMode(extra: List<String>?){
        val isQueueEmpty = settingsQueue.isEmpty()
        settingsQueue.addAll(canOffCommandsSet)
        extra?.let{
            it.forEach {command->
                settingsQueue.add("$AT_PREFIX${command.replace(" ", "")}$POSTFIX")
            }
        }
        if(isQueueEmpty){
            _obdCommandFlow.emit(settingsQueue.poll())
        }
    }


    @Throws(IllegalStateException::class)
    override suspend fun handleAnswer() {
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
            _obdCommandFlow.emit("${Commands.AtCommands.SetProto}${userProtocol!!.hexOrdinal}$POSTFIX")
        } else {
            _obdCommandFlow.emit("${Commands.AtCommands.SetProto}${Protocol.AUTOMATIC.hexOrdinal}$POSTFIX")
        }
    }


    private suspend fun tryProto() {
        if (userProtocol != null) {
            _obdCommandFlow.emit("${Commands.AtCommands.TryProto}${userProtocol!!.hexOrdinal}$POSTFIX")
        } else {
            _obdCommandFlow.emit("${Commands.AtCommands.TryProto}${Protocol.AUTOMATIC.hexOrdinal}$POSTFIX")
        }
    }


    override fun resetStates() {
        strategy = null
        userProtocol = null
        settingsQueue.clear()
    }


    override suspend fun askCurrentProto() {
        _obdCommandFlow.emit("${Commands.AtCommands.GetVehicleProtoAsNumber.command}$POSTFIX")
    }

    override suspend fun onRestart(
        strategy: ProtocolManagerStrategy,
        warmStart: Boolean,
        protocol: Protocol?,
        extra: List<String>?
    ) {
        this.strategy = strategy
        userProtocol = protocol
        val isQueue = settingsQueue.isEmpty()
        prepare(extra)
        if (isQueue) {
            val command = if (warmStart) Commands.AtCommands.WarmStart else Commands.AtCommands.ResetAll.command
            _obdCommandFlow.emit("$command$POSTFIX")
        }
    }

    override suspend fun setHeaderAndReceiver(
        headerAddress: String,
        receiverAddress: String?,
        isAlreadyCan: Boolean,
        extra: List<String>?
    ) {
        if (!isAlreadyCan) {
            settingsQueue.addAll(canOnCommandsSet)
        }
        val isQueueEmpty = settingsQueue.isEmpty()
        settingsQueue.add("${Commands.AtCommands.SetHeader.command}$headerAddress$POSTFIX")
        receiverAddress?.let {
            settingsQueue.add("${Commands.AtCommands.SetReceiverAdrFilter.command}$it$POSTFIX")
        }
        extra?.let{
            it.forEach {command->
                settingsQueue.add("$AT_PREFIX${command.replace(" ", "")}$POSTFIX")
            }
        }
        if (isQueueEmpty) {
            sendNextSettings()
        }
    }


    override suspend fun startWithProfile(profile: Profile) {
        strategy = ProtocolManagerStrategy.SET
        settingsQueue.addAll(standardSettingsSet)
        profile.settingsAndParams.forEach {
           settingsQueue.add(it)
        }
        _obdCommandFlow.emit("${Commands.AtCommands.ResetAll.command}$POSTFIX")
    }

    override suspend fun resetSession() {
        strategy = null
        _obdCommandFlow.emit("${Commands.AtCommands.ResetAll.command}$POSTFIX")
    }

    override fun isQueueEmpty(): Boolean = settingsQueue.isEmpty()

    override suspend fun sendNextSettings() {
        settingsQueue.poll()
        if (!isQueueEmpty()) {
            _obdCommandFlow.emit(settingsQueue.peek())
        }
    }

    /**
     * Only when we start with strategy
     */
    private fun prepare(extra: List<String>?) {
        if (userProtocol != null && strategy != ProtocolManagerStrategy.AUTO) {
            settingsQueue.addAll(standardSettingsSet)
            extra?.let {
                standardSettingsSet.addAll(it)
            }
        } else if (userProtocol == null && strategy == ProtocolManagerStrategy.AUTO) {
            userProtocol = Protocol.AUTOMATIC
            settingsQueue.addAll(standardSettingsSet)
            extra?.let {
                standardSettingsSet.addAll(it)
            }
        } else {
            resetStates()
            throw ModsConflictException("For non-auto strategy, protocol should be provided")

        }
    }

    override suspend fun setSetting(command: String, workMode: WorkMode) {
        val handledCommand = "$AT_PREFIX$command$POSTFIX"
        if (workMode == WorkMode.SETTINGS && !isQueueEmpty()){
            settingsQueue.add(handledCommand)
        }
        _obdCommandFlow.emit(handledCommand)
    }

    override suspend fun switchProtocol(protocol: Protocol) {
        _obdCommandFlow.emit("${Commands.AtCommands.SetProto}${protocol.hexOrdinal}$POSTFIX")
    }



}