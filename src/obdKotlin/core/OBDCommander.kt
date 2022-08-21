package obdKotlin.core

import obdKotlin.protocol.Protocol
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import obdKotlin.commandProcessors.BaseCommandHandler
import obdKotlin.commands.Commands
import obdKotlin.decoders.Decoder
import obdKotlin.encoders.SpecialEncoder
import obdKotlin.decoders.SpecialEncoderHost
import obdKotlin.exceptions.ModsConflictException
import obdKotlin.exceptions.NoSourceProvidedException
import obdKotlin.exceptions.WrongInitCommandException
import obdKotlin.protocol.BaseProtocolManager
import obdKotlin.messages.Message
import obdKotlin.mix
import obdKotlin.profiles.Profile
import obdKotlin.protocol.ProtocolManagerStrategy
import obdKotlin.source.Source
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.jvm.Throws

/**
 * Warm Start в билдер done
 * Билдер энкодеров по енаму
 * Выкидывать комманду из протокол менеджера только когда приходит ответ done
 * Включить поддержку повторяемых информационных АТ комманд
 * Посыллать ли null в в соокет с обработкой?
 */
internal class OBDCommander(
    private val protocolManager: BaseProtocolManager,
    private val warmStarts: Boolean,
    private val atDecoder: Decoder,
    private val pinDecoder: SpecialEncoderHost,
    private val commandHandler: BaseCommandHandler
) : Commander(protocolManager) {

    companion object{
        private const val BUFFER_CAPACITY = 100
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
                if (atDecoder.decode(bytes, workMode)) {
                    changeModeAndInvokeModeCallBack(WorkMode.PROTOCOL)
                    protocolManager.handleAnswer()
                } else {
                    handleNegativeAnswer()
                }
            }

            WorkMode.PROTOCOL -> {
                if (atDecoder.decode(bytes, workMode)) {
                    val nextMode = if(protocolManager.isQueueEmpty()) WorkMode.COMMANDS else WorkMode.SETTINGS
                    changeModeAndInvokeModeCallBack(nextMode)
                    protocolManager.askCurrentProto()
                } else {
                    handleNegativeAnswer()
                }
            }

            WorkMode.SETTINGS -> {
                atDecoder.decode(bytes, workMode)
                if (protocolManager.isQueueEmpty()) {
                    changeModeAndInvokeModeCallBack(WorkMode.COMMANDS)
                    commandHandler.sendNextCommand()
                } else {
                    protocolManager.sendNextSettings()
                }

            }

            WorkMode.COMMANDS -> {
                if (pinDecoder.decode(bytes, workMode)) {
                    commandHandler.sendNextCommand(false)
                } else {
                    systemEventListener?.onDecodeError(commandHandler.getLastCommand())
                    commandHandler.sendNextCommand(true)
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
        //TODO handle errors
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
     */
    override fun setNewSetting(command: String) {
        //todo filter command depends on proto protocol
        val transformedCommand = command.replace(" ", "")
        checkSource()
        commanderScope.launch {
            if (transformedCommand == "Z" || transformedCommand == "z") {
                onReset()
            }
            cancelRepeatJobs()
            workMode = WorkMode.SETTINGS
            protocolManager.setSetting(transformedCommand, workMode)
        }
    }


    override fun startWithProto(protocol: Protocol, systemEventListener: SystemEventListener?) {
        checkSource()
        onReset()
        setListener(systemEventListener)
        val extra = if (protocol == Protocol.ISO_14230_4_FASTINIT) {
            listOf(Commands.AtCommands.FastInit.command)
        } else null
        commanderScope.launch {
            protocolManager.onRestart(ProtocolManagerStrategy.TRY, warmStarts, protocol, extra)
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
    override fun startWithAuto(systemEventListener: SystemEventListener?) {
        checkSource()
        onReset()
        setListener(systemEventListener)
        commanderScope.launch {
            protocolManager.onRestart(ProtocolManagerStrategy.AUTO, warmStarts)
        }
    }


    @Throws(NoSourceProvidedException::class)
    override fun startWithProtoAndRemember(protocol: Protocol, systemEventListener: SystemEventListener?) {
        checkSource()
        onReset()
        setListener(systemEventListener)
        val extra = if (protocol == Protocol.ISO_14230_4_FASTINIT) {
            listOf(Commands.AtCommands.FastInit.command)
        } else null
        commanderScope.launch {
            protocolManager.onRestart(ProtocolManagerStrategy.SET, warmStarts, protocol, extra)
        }
    }


    /**
     * Send command if elm obd2 is configurated
     */
    override fun setCommand(command: String, repeatTime: Long?) {
        checkSource()
        commanderScope.launch {
            commandHandler.receiveCommand(command, repeatTime, workMode)
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
        commanderScope.cancel()
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

    override fun switchToCanMode(
        headerAddress: String,
        receiverAddress: String?,
        specialEncoder: SpecialEncoder,
        extra: List<String>
    ) {
        checkSource()
        if (workMode != WorkMode.IDLE && workMode != WorkMode.PROTOCOL) {
            commanderScope.launch {
                if (!commandHandler.commandAllowed.get()) {
                    cancelRepeatJobs()
                }
                pinDecoder.setSpecialEncoder(specialEncoder)
                changeModeAndInvokeModeCallBack(WorkMode.SETTINGS)
                protocolManager.setHeaderAndReceiver(headerAddress, receiverAddress, canMode.get(), extra)
                switchCan(true)
            }
        }
    }

    override fun switchToStandardMode(extra: List<String>) {
        checkSource()
        if (workMode != WorkMode.IDLE && workMode != WorkMode.PROTOCOL) {
            commanderScope.launch {
                if (!commandHandler.commandAllowed.get()) {
                    cancelRepeatJobs()
                }
                changeModeAndInvokeModeCallBack(WorkMode.SETTINGS)
                protocolManager.switchToStandardMode(extra)
                switchCan(false)
            }
        }
    }

    private fun switchCan(mode: Boolean) {
        canMode.set(mode)
        commandHandler.canMode.set(mode)
        pinDecoder.canMode.set(mode)
        systemEventListener?.onSwitchMode(mode)
    }


    private suspend fun cancelRepeatJobs() {
        commandHandler.removeCommand()
        //do work and after delay to be sure all commands was delivered
        delay(50)
    }


    /**
     * Use carefully
     * Protocol manager will automatically send all initial settings via protocol witch was chosen
     * Settings can be configured manually by setSetting() or setSettingWithParameter()
     */
    override fun switchProtocol(protocol: Protocol){
        checkSource()
        commanderScope.launch {
            cancelRepeatJobs()
            workMode = WorkMode.PROTOCOL
            protocolManager.switchProtocol(protocol)
        }
    }

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
        switchCan(profile.canMode)
        profile.encoder?.let {
            pinDecoder.setSpecialEncoder(it)
        }

        commanderScope.launch {
            protocolManager.startWithProfile(profile)
        }
    }

}


