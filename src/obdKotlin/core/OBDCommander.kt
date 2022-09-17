package obdKotlin.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import obdKotlin.WorkMode
import obdKotlin.commandProcessors.BaseCommandHandler
import obdKotlin.commands.CommandContainer
import obdKotlin.commands.CommandRout
import obdKotlin.decoders.Decoder
import obdKotlin.decoders.EncodingState
import obdKotlin.decoders.SpecialEncoderHost
import obdKotlin.encoders.SpecialEncoder
import obdKotlin.exceptions.ConnectionIsNotReadyException
import obdKotlin.exceptions.ModsConflictException
import obdKotlin.exceptions.NoSourceProvidedException
import obdKotlin.exceptions.WrongInitCommandException
import obdKotlin.messages.Message
import obdKotlin.mix
import obdKotlin.profiles.Profile
import obdKotlin.protocol.BaseProtocolManager
import obdKotlin.protocol.Protocol
import obdKotlin.protocol.ProtocolManagerStrategy
import obdKotlin.source.Source
import obdKotlin.utills.CommandUtil
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.jvm.Throws

/**
 * Warm Start в билдер done
 * Билдер энкодеров по енаму
 * Выкидывать комманду из протокол менеджера только когда приходит ответ done
 * Включить поддержку повторяемых информационных АТ комманд
 * Посыллать ли null в в соокет с обработкой?
 * Что если пользователь из кан режима начнёт слать не кан комманды?
 * Подготовить к приёму > символа, по получению которого шлю след комманду done
 * Впиши строки выхода из кан режима
 */
internal class OBDCommander(
    protocolManager: BaseProtocolManager,
    private val warmStarts: Boolean,
    private val atDecoder: Decoder,
    private val pinDecoder: SpecialEncoderHost,
    private val commandHandler: BaseCommandHandler
) : Commander(protocolManager) {

    companion object {
        private const val BUFFER_CAPACITY = 100
        private const val CONNECTION_STATE_TEXT = "Connection is not ready: "
    }

    constructor(
        protoManager: BaseProtocolManager,
        warmStarts: Boolean,
        atDecoder: Decoder,
        pinDecoderEntity: SpecialEncoderHost,
        commandHandler: BaseCommandHandler,
        source: Source
    ) : this(
        protoManager,
        warmStarts,
        atDecoder,
        pinDecoderEntity,
        commandHandler
    ) {
        this.source = source
    }

    private var systemEventListener: SystemEventListener? = null
    private val canMode = AtomicBoolean(false)
    private var source: Source? = null
    private val commanderScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    var workMode = WorkMode.IDLE
        private set

    override val encodedDataMessages: SharedFlow<Message?> = atDecoder.eventFlow
        .mix(pinDecoder.eventFlow)
        .buffer(BUFFER_CAPACITY)
        .shareIn(commanderScope, SharingStarted.Eagerly)

    init {
        observeInput()
        observeCommands()
    }

    private fun observeInput() {
        source?.let {
            commanderScope.launch {
                it.inputByteFlow.onEach {
                    manageInputData(it)
                }.collect()
            }
        }
    }

    private suspend fun manageInputData(bytes: ByteArray) {
        when (workMode) {
            WorkMode.IDLE -> {
                if (atDecoder.decode(bytes, workMode) is EncodingState.Successful) {
                    changeModeAndInvokeModeCallBack(WorkMode.PROTOCOL)
                    protocolManager.handleInitialAnswer()
                } else {
                    handleNegativeAnswer()
                }
            }

            WorkMode.PROTOCOL -> {
                if (atDecoder.decode(bytes, workMode) is EncodingState.Successful) {
                    val nextMode = if (protocolManager.isQueueEmpty()) WorkMode.COMMANDS else WorkMode.SETTINGS
                    changeModeAndInvokeModeCallBack(nextMode)
                    if (nextMode == WorkMode.SETTINGS) {
                        protocolManager.sendNextSettings()
                    } else {
                        commandHandler.sendNextCommand()
                    }
                } else {
                    handleNegativeAnswer()
                }
            }

            WorkMode.SETTINGS -> {
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

            WorkMode.COMMANDS -> {
                when (val answer = pinDecoder.decode(bytes, workMode)) {
                    is EncodingState.Successful -> commandHandler.sendNextCommand(false)
                    is EncodingState.Unsuccessful -> {
                        systemEventListener?.onDecodeError(FailOn(answer.onAnswer, commandHandler.getCurrentCommand()))
                        commandHandler.sendNextCommand(true)
                    }
                    is EncodingState.WaitNext -> {}
                }
                if (commandHandler.isQueueEmpty()) {
                    commandHandler.commandAllowed.set(true)
                }
            }
        }
    }

    private fun changeModeAndInvokeModeCallBack(mode: WorkMode) {
        workMode = mode
        systemEventListener?.onWorkModeChanged(workMode)
    }

    private fun observeCommands() {
        source?.let { source ->
            commanderScope.launch {
                protocolManager.obdCommandFlow.map {
                    return@map it.toByteArray()
                }.onEach {
                    source.outputByteFlow.emit(it)
                }.collect()
            }
            commanderScope.launch {
                commandHandler.commandFlow.map { command ->
                    return@map command.toByteArray()
                }.onEach {
                    source.outputByteFlow.emit(it)
                }.collect()
            }
        }
    }

    @Throws(WrongInitCommandException::class)
    private fun handleNegativeAnswer() {
        // TODO handle errors
        when (workMode) {
            WorkMode.IDLE -> {
                throw WrongInitCommandException()
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
        checkState()
        val transformedCommand = CommandUtil.formatAT(command.replace(" ", ""))
        commanderScope.launch {
            when (CommandUtil.checkAndRout(canMode.get(), workMode, transformedCommand)) {
                CommandRout.RESET -> {
                    workMode = WorkMode.SETTINGS
                    protocolManager.setSetting(transformedCommand)
                    delay(80)
                    onReset()
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

    private fun checkState() {
        if (workMode != WorkMode.SETTINGS || workMode != WorkMode.COMMANDS) {
            throw ConnectionIsNotReadyException("$CONNECTION_STATE_TEXT ${workMode.name}")
        }
    }

    override fun start(protocol: Protocol?, systemEventListener: SystemEventListener?, extra: List<String>?, specialEncoder: SpecialEncoder?) {
        checkSource()
        onReset()
        setListener(systemEventListener)
        specialEncoder?.let {
            pinDecoder.setSpecialEncoder(specialEncoder)
            switchCan(true)
        }
        val filteredExtra = extra?.run {
            return@run CommandUtil.filterExtra(extra, canMode.get())
        }

//        val extra = if (protocol == Protocol.ISO_14230_4_FASTINIT) {
//            listOf(Commands.AtCommands.FastInit.command)
//        } else null
        commanderScope.launch {
            protocolManager.onRestart(ProtocolManagerStrategy.TRY, warmStarts, protocol, filteredExtra)
        }
    }

    private fun setListener(systemEventListener: SystemEventListener?) {
        this.systemEventListener = systemEventListener
    }

    override fun resetSettings() {
        checkSource()
        onReset()
        commanderScope.launch {
            protocolManager.resetSession()
        }
    }

    private fun onReset() {
        protocolManager.resetStates()
        switchCan(false)
        systemEventListener = null
        commandHandler.removeCommand()
        changeModeAndInvokeModeCallBack(WorkMode.IDLE)
    }

    @Throws(NoSourceProvidedException::class)
    override fun startWithAuto(systemEventListener: SystemEventListener?, extra: List<String>?, specialEncoder: SpecialEncoder?) {
        checkSource()
        onReset()
        setListener(systemEventListener)
        specialEncoder?.let {
            pinDecoder.setSpecialEncoder(specialEncoder)
            switchCan(true)
        }
        val filteredExtra = extra?.run {
            return@run CommandUtil.filterExtra(extra, canMode.get())
        }
        commanderScope.launch {
            protocolManager.onRestart(ProtocolManagerStrategy.AUTO, warmStarts, Protocol.AUTOMATIC, filteredExtra)
        }
    }

    @Throws(NoSourceProvidedException::class)
    override fun startAndRemember(protocol: Protocol?, systemEventListener: SystemEventListener?, extra: List<String>?, specialEncoder: SpecialEncoder?) {
        checkSource()
        onReset()
        setListener(systemEventListener)
        specialEncoder?.let {
            pinDecoder.setSpecialEncoder(specialEncoder)
            switchCan(true)
        }
        val filteredExtra = extra?.run {
            return@run CommandUtil.filterExtra(extra, canMode.get())
        }
        commanderScope.launch {
            protocolManager.onRestart(ProtocolManagerStrategy.SET, warmStarts, protocol, filteredExtra)
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
            commandHandler.receiveCommand(checkedCommand, repeatTime, workMode)
        }
    }

    override fun sendCommands(commands: List<CommandContainer>) {
        checkSource()
        commanderScope.launch {
            val handledCommands = commands.map { CommandContainer(CommandUtil.formatPid(it.command), it.delay) }
            commandHandler.receiveCommand(handledCommands, workMode)
        }
    }

    @Throws(ModsConflictException::class)
    private fun checkSourceAndMode(mode: WorkMode) {
        checkSource()
        if (workMode != mode) {
            throw ModsConflictException("${mode.name} are not allowed in ${workMode.name}")
        }
    }

    override fun stop() {
        commanderScope.coroutineContext.cancelChildren()
    }

    @Throws(NoSourceProvidedException::class)
    private fun checkSource() {
        if (source == null) {
            throw NoSourceProvidedException()
        }
    }

    override fun bindSource(source: Source, resetStates: Boolean) {
        this.source = source
        resetStates(resetStates)
        observeInput()
        observeCommands()
    }

    /**
     *  SH: Header - address to whom we send command
     *  CRA: Receive address our listening device (7E8)
     *  Cation: The function will throw an exception if initial commands and protocol were not applied
     *  SpecialEncoder can not work properly if were applied wrong initial commands or chose wrong protocol
     */

//    override fun switchToCanMode(
//        headerAddress: String,
//        receiverAddress: String?,
//        specialEncoder: SpecialEncoder,
//        extra: List<String>?
//    ) {
//        checkSource()
//        if ((workMode != WorkMode.IDLE && workMode != WorkMode.PROTOCOL) || warmStarts) {
//            commanderScope.launch {
//                if (!commandHandler.commandAllowed.get()) {
//                    cancelRepeatJobs()
//                }
//                pinDecoder.setSpecialEncoder(specialEncoder)
//                changeModeAndInvokeModeCallBack(WorkMode.SETTINGS)
//                protocolManager.setHeaderAndReceiver(headerAddress, receiverAddress, canMode.get(), extra)
//                switchCan(true)
//            }
//        } else throw ConnectionIsNotReadyException("$CONNECTION_STATE_TEXT ${workMode.name}")
//    }

//    override fun switchToStandardMode(extra: List<String>?) {
//        checkSource()
//        if ((workMode != WorkMode.IDLE && workMode != WorkMode.PROTOCOL) || warmStarts) {
//            commanderScope.launch {
//                if (!commandHandler.commandAllowed.get()) {
//                    cancelRepeatJobs()
//                }
//                changeModeAndInvokeModeCallBack(WorkMode.SETTINGS)
//                protocolManager.switchToStandardMode(extra)
//                switchCan(false)
//            }
//        } else throw ConnectionIsNotReadyException("$CONNECTION_STATE_TEXT ${workMode.name}")
//    }

    private fun switchCan(mode: Boolean) {
        canMode.set(mode)
        commandHandler.canMode.set(mode)
        pinDecoder.canMode.set(mode)
        systemEventListener?.onSwitchMode(mode)
    }

    /**
     * Remove command from queue by pid
     */
    override fun removeRepeatedCommand(command: String) {
        commandHandler.removeCommand(command)
    }

    /**
     * Remove all commands from queue
     */
    override fun removeRepeatedCommands() {
        commandHandler.removeCommand()
    }

    /**
     * Receive command and send it to queue if connection
     */

    override fun sendMultiCommand() {}

    private suspend fun cancelRepeatJobs() {
        commandHandler.removeCommand()
        // do work and after delay to be sure all commands was delivered
        delay(50)
    }

    /**
     * Use carefully
     * Protocol manager will automatically send all initial settings via protocol witch was chosen
     * Settings can be configured manually by setSetting() or setSettingWithParameter()
     */
//    override fun switchProtocol(protocol: Protocol) {
//        checkSource()
//        commanderScope.launch {
//            cancelRepeatJobs()
//            workMode = WorkMode.PROTOCOL
//            protocolManager.switchProtocol(protocol)
//        }
//    }

    private fun resetStates(resetStates: Boolean) {
        commanderScope.coroutineContext.cancelChildren()
        if (resetStates) {
            onReset()
        }
    }

    /**
     * The method configures the connection with the diagnostic device from selecting the readiness protocol
     * to receiving commands.
     * To pass parameters to the method, create a Profile class with the desired parameters
     */
    override fun startWithProfile(profile: Profile, systemEventListener: SystemEventListener?) {
        checkSource()
        onReset()
        this.systemEventListener = systemEventListener
        if (profile.encoder != null) {
            switchCan(true)
        }
        profile.encoder?.let {
            pinDecoder.setSpecialEncoder(it)
        }

        commanderScope.launch {
            protocolManager.startWithProfile(profile)
        }
    }
}
