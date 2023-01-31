package obdKotlin.core

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import obdKotlin.WorkMode
import obdKotlin.commandProcessors.BaseCommandHandler
import obdKotlin.commands.CommandRout
import obdKotlin.decoders.Decoder
import obdKotlin.decoders.EncodingState
import obdKotlin.decoders.SpecialEncoderHost
import obdKotlin.encoders.SpecialEncoder
import obdKotlin.exceptions.NoSourceProvidedException
import obdKotlin.exceptions.WrongInitCommandException
import obdKotlin.exceptions.WrongMessageTypeException
import obdKotlin.profiles.Profile
import obdKotlin.protocol.BaseProtocolManager
import obdKotlin.protocol.Protocol
import obdKotlin.protocol.ProtocolManagerStrategy
import obdKotlin.source.Source
import obdKotlin.utils.CommandUtil
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.jvm.Throws

internal class OBDCommander(
    protocolManager: BaseProtocolManager,
    private val warmStarts: Boolean,
    private val atDecoder: Decoder,
    private val pinDecoder: SpecialEncoderHost,
    private val commandHandler: BaseCommandHandler,
    private val eventListener: SystemEventListener?,
    enableRawData: Boolean
) : Commander(
    enableRawData,
    protocolManager
) {

    constructor(
        protoManager: BaseProtocolManager,
        warmStarts: Boolean,
        atDecoder: Decoder,
        pinDecoderEntity: SpecialEncoderHost,
        commandHandler: BaseCommandHandler,
        eventListener: SystemEventListener?,
        source: Source,
        enableRawData: Boolean
    ) : this(
        protoManager,
        warmStarts,
        atDecoder,
        pinDecoderEntity,
        commandHandler,
        eventListener,
        enableRawData
    ) {
        this.source = source
        observeInput()
        observeSource()
    }

    private val commanderScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    init {
        observeCommands()
        bindDecodersFlows()
    }

    private fun bindDecodersFlows() {
        commanderScope.apply {
            launch {
                atDecoder.eventFlow.onEach {
                    _encodedDataMessages.emit(it)
                }.collect()
            }
            launch {
                pinDecoder.eventFlow.onEach {
                    _encodedDataMessages.emit(it)
                }.collect()
            }
        }
    }

    private val extendedMode = AtomicBoolean(false)

    private var source: Source? = null

    private fun observeCommands() {
        commanderScope.launch {
            Log.d("@@@", "OBSERVE COMMANDS")
            protocolManager.obdCommandFlow
                .map {
                    it.toByteArray(Charsets.US_ASCII)
                }.onEach {
                    Log.d("@@@", it.size.toString())
                    println(it.size)
                    source?.outputByteFlow?.emit(it)
                }.collect()
        }
        commanderScope.launch {
            commandHandler.commandFlow.map {
                it.toByteArray(Charsets.US_ASCII)
            }.onEach {
                source?.outputByteFlow?.emit(it)
            }.collect()
        }
    }

    private fun observeInput() {
        source?.let {
            commanderScope.launch {
                Log.d("@@@", "OBS INPUT")
                it.inputByteFlow.onEach {
                    Log.d("@@@", "RECEIVE ->>>>> ${it.size}")
                    if (it.isNotEmpty()) {
                        if (enableRawData) {
                            _rawDataFlow.emit(it.decodeToString())
                        }
                        manageInputData(it)
                    }
                }.collect()
            }
        }
    }

    private suspend fun manageInputData(bytes: ByteArray) {
        when (workMode) {
            WorkMode.IDLE -> {
                doOnIdle(bytes)
            }

            WorkMode.PROTOCOL -> {
                doOnProtocol(bytes)
            }

            WorkMode.SETTINGS -> {
                doOnSettings(bytes)
            }

            WorkMode.COMMANDS -> {
                doOnCommands(bytes)
            }
        }
    }

    private suspend fun doOnCommands(bytes: ByteArray) {
        when (val answer = pinDecoder.decode(bytes, workMode)) {
            is EncodingState.Successful -> commandHandler.sendNextCommand(false)
            is EncodingState.Unsuccessful -> {
                eventListener?.onDecodeError(
                    FailOn(
                        workMode,
                        answer.onAnswer,
                        commandHandler.getCurrentCommand()
                    )
                )
                commandHandler.sendNextCommand(true)
            }

            is EncodingState.WaitNext -> {}
        }
        if (commandHandler.isQueueEmpty()) {
            commandHandler.commandAllowed.set(true)
        }
    }

    private suspend fun doOnSettings(bytes: ByteArray) {
        atDecoder.decode(bytes, workMode)
        if (protocolManager.isQueueEmpty()) {
            if (commandHandler.commandAllowed.get() && !commandHandler.isQueueEmpty()) {
                commandHandler.commandAllowed.set(false)
            }
            changeModeAndInvokeModeCallBack(WorkMode.COMMANDS)
            commandHandler.sendNextCommand()
        } else {
            protocolManager.sendNextSettings(true) {
                commandHandler.sendNextCommand()
            }
        }
    }

    private suspend fun doOnProtocol(bytes: ByteArray) {
        when (val result = atDecoder.decode(bytes, workMode)) {
            is EncodingState.Successful -> {
                onProtoSelected()
            }

            is EncodingState.Unsuccessful -> {
                handleNegativeAnswer(result.onAnswer)
            }

            else -> {
                throw WrongMessageTypeException()
            }
        }
    }

    private suspend fun doOnIdle(bytes: ByteArray) {
        when (val result = atDecoder.decode(bytes, workMode)) {
            is EncodingState.Successful -> {
                if (protocolManager.userProtocol != null) {
                    changeModeAndInvokeModeCallBack(WorkMode.PROTOCOL)
                    protocolManager.handleInitialAnswer()
                } else {
                    onProtoSelected()
                }
            }

            is EncodingState.Unsuccessful -> {
                handleNegativeAnswer(result.onAnswer)
            }

            else -> {
                throw WrongMessageTypeException()
            }
        }
    }

    private suspend fun onProtoSelected() {
        val nextMode = if (protocolManager.isQueueEmpty()) WorkMode.COMMANDS else WorkMode.SETTINGS
        changeModeAndInvokeModeCallBack(nextMode)
        if (nextMode == WorkMode.SETTINGS) {
            protocolManager.sendNextSettings()
        } else {
            commandHandler.sendNextCommand()
        }
    }

    private fun changeModeAndInvokeModeCallBack(mode: WorkMode) {
        workMode = mode
        eventListener?.onWorkModeChanged(workMode)
    }

    private fun observeSource() {
        source?.let { source ->
            commanderScope.launch {
                val onError = if (eventListener != null) eventListener::onSourceError else null
                val connect = if (eventListener != null) eventListener::onConnect else null
                source.observeByteCommands(commanderScope, onError, connect)
            }
        }
    }

    @Throws(WrongMessageTypeException::class)
    private fun handleNegativeAnswer(code: String) {
        when (workMode) {
            WorkMode.IDLE -> {
                eventListener?.onDecodeError(FailOn(workMode, code, null))
            }

            WorkMode.PROTOCOL -> {
                throw WrongInitCommandException()
            }

            else -> {}
        }
    }

    /**
     * Use carefully, only if you sure in your command
     * The function will skip initialization commands is they have not been applied
     * Command should be w/o prefix or postfix. Example for ATZ\r put just Z
     * CAUTION Do not send pin commands, they will not be handled here, use SetCommand
     */
    override fun setNewSetting(command: String) {
        checkSource()
        val transformedCommand = CommandUtil.formatAT(command.replace(" ", ""))
        commanderScope.launch {
            when (CommandUtil.checkAndRout(extendedMode.get(), workMode, transformedCommand)) {
                CommandRout.RESET -> {
                    onReset()
                    protocolManager.resetSession(warmStarts)
                }

                CommandRout.TO_CH -> {
                    sendCommand(transformedCommand)
                }

                CommandRout.PASS -> {
                    workMode = WorkMode.SETTINGS
                    protocolManager.setSetting(transformedCommand)
                }
            }
        }
    }

    override fun sendRawCommand(command: String) {
        if (enableRawData) {
            commanderScope.launch {
                source?.outputByteFlow?.emit(command.toByteArray(Charsets.US_ASCII))
            }
        }
    }

    override fun start(
        protocol: Protocol?,
        extra: List<String>?,
        specialEncoder: SpecialEncoder?,
        extendedMode: Boolean
    ) {
        checkSource()
        commanderScope.launch {
            onReset()
            switchExtended(
                extendedMode,
                specialEncoder == null
            )
            specialEncoder?.let {
                pinDecoder.setSpecialEncoder(specialEncoder)
            }
            val filteredExtra = extra?.run {
                return@run CommandUtil.filterExtraAndFormat(extra, extendedMode)
            }
            protocolManager.onStart(ProtocolManagerStrategy.TRY, warmStarts, protocol, filteredExtra)
        }
    }

    override suspend fun resetSettings() {
        checkSource()
        commanderScope.launch {
            onReset()
            protocolManager.resetSession(warmStarts)
        }
    }

    private suspend fun onReset() {
        commandHandler.clearCommandsQueue()
        protocolManager.resetStates()
        changeModeAndInvokeModeCallBack(WorkMode.IDLE)
    }

    @Throws(NoSourceProvidedException::class)
    override fun startWithAuto(
        extra: List<String>?,
        specialEncoder: SpecialEncoder?,
        extendedMode: Boolean
    ) {
        checkSource()
        commanderScope.launch {
            onReset()
            switchExtended(extendedMode, true)
            specialEncoder?.let {
                pinDecoder.setSpecialEncoder(specialEncoder)
            }
            val filteredExtra = extra?.run {
                return@run CommandUtil.filterExtraAndFormat(extra, extendedMode)
            }
            protocolManager.onStart(ProtocolManagerStrategy.AUTO, warmStarts, Protocol.AUTOMATIC, filteredExtra)
        }
    }

    @Throws(NoSourceProvidedException::class)
    override fun startAndRemember(
        protocol: Protocol?,
        extra: List<String>?,
        specialEncoder: SpecialEncoder?,
        extendedMode: Boolean
    ) {
        checkSource()
        commanderScope.launch {
            onReset()
            switchExtended(extendedMode, specialEncoder == null)
            specialEncoder?.let {
                pinDecoder.setSpecialEncoder(specialEncoder)
            }
            val filteredExtra = extra?.run {
                return@run CommandUtil.filterExtraAndFormat(extra, extendedMode)
            }
            protocolManager.onStart(ProtocolManagerStrategy.SET, warmStarts, protocol, filteredExtra)
        }
    }

    /**
     * If connection is not ready, command will be stored in queue and automatically send when connection will be ready
     * CAUTION Do not send AT commands, they will not be handled, use SetSetting(), except RV and I commands
     */
    override fun sendCommand(command: String, repeatTime: Long?) {
        checkSource()
        commanderScope.launch {
            val checkedCommand = CommandUtil.checkPid(command)
            commandHandler.receiveCommands(checkedCommand, repeatTime, workMode)
        }
    }

    override fun sendCommands(commands: List<String>, repeatTime: Long?) {
        checkSource()
        commanderScope.launch {
            commandHandler.receiveCommands(commands, repeatTime, workMode)
        }
    }

    override fun stop() {
        commanderScope.coroutineContext.cancelChildren()
    }

    override fun disconnect() {
        source?.disconnect()
    }

    @Throws(NoSourceProvidedException::class)
    private fun checkSource() {
        if (source == null) {
            throw NoSourceProvidedException()
        }
    }

    override fun bindSource(source: Source, resetStates: Boolean) {
        onNewSource(resetStates)
        this.source = source
        observeInput()
        observeSource()
        observeCommands()
    }

    private fun switchExtended(
        mode: Boolean,
        unbindSpecialEncoder: Boolean
    ) {
        extendedMode.set(mode)
        commandHandler.extended.set(mode)
        pinDecoder.extended.set(mode)
        eventListener?.onSwitchMode(mode)
        if (unbindSpecialEncoder) {
            pinDecoder.setSpecialEncoder(null)
        }
    }

    /**
     * Remove command from queue by pid
     */
    override fun removeRepeatedCommand(command: String) {
        commanderScope.launch {
            commandHandler.removeCommand(command)
        }
    }

    /**
     * Remove all commands from queue
     */
    override fun removeAllCommands() {
        commanderScope.launch {
            commandHandler.clearCommandsQueue()
        }
    }

    /**
     * Receive command and send it to queue if connection
     */

    override fun sendMultiCommand() {}

    private fun onNewSource(resetStates: Boolean) {
        commanderScope.coroutineContext.cancelChildren()
        if (resetStates) {
            commanderScope.launch {
                pinDecoder.setSpecialEncoder(null)
                onReset()
            }
        }
    }

    /**
     * The method configures the connection with the diagnostic device from selecting the readiness protocol
     * to receiving commands.
     * To pass parameters to the method, create a Profile class with the desired parameters
     */
    override fun startWithProfile(profile: Profile) {
        checkSource()
        commanderScope.launch {
            onReset()
            if (profile.encoder != null) {
                switchExtended(
                    mode = true,
                    unbindSpecialEncoder = false
                )
            }
            profile.commands?.let {
                commandHandler.receiveCommands(
                    it,
                    null,
                    workMode
                )
            }
            profile.encoder?.let {
                pinDecoder.setSpecialEncoder(it)
            }
            protocolManager.startWithProfile(profile)
        }
    }
}
