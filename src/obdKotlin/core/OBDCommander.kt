package obdKotlin.core

import obdKotlin.WorkMode
import obdKotlin.protocol.Protocol
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import obdKotlin.commandProcessors.CommandHandler
import obdKotlin.commands.Commands
import obdKotlin.decoders.Decoder
import obdKotlin.decoders.AtDecoder
import obdKotlin.decoders.PinAnswerDecoder
import obdKotlin.exceptions.ModsConflictException
import obdKotlin.exceptions.NoSourceProvidedException
import obdKotlin.exceptions.WrongInitCommandException
import obdKotlin.protocol.BaseProtocolManager
import obdKotlin.messages.Message
import obdKotlin.profiles.CustomProfile
import obdKotlin.profiles.Profile
import obdKotlin.protocol.ProtocolManager
import obdKotlin.protocol.ProtocolManagerStrategy
import obdKotlin.source.Source
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.jvm.Throws


internal class OBDCommander(
    private val protocolManager: BaseProtocolManager,
    private val atDecoder: AtDecoder
) : Commander(protocolManager) {

    constructor(
        protoManager: BaseProtocolManager,
        atDecoder: AtDecoder,
        source: Source
    ) :this(protoManager, atDecoder) {
        this.source = source
    }

    init {
        observeInput()
        observeCommands()
    }

    private val canMode = AtomicBoolean(false)
    private var source: Source? = null
    private val commanderScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    var workMode = WorkMode.IDLE
//        private set

    override val eventFlow: MutableSharedFlow<Message?> = MutableSharedFlow()
    private var commandHandler: CommandHandler? = null
    private val atFrameDecoder: Decoder = AtDecoder(eventFlow)
    private var pinFrameDecoder: Decoder? = null

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
                if (atFrameDecoder.decode(bytes, workMode)) {
                    workMode = WorkMode.PROTOCOL
                    protocolManager.handleAnswer()
                } else {
                    handleNegativeAnswer()
                }
            }

            WorkMode.PROTOCOL -> {
                if (atFrameDecoder.decode(bytes, workMode)) {
                    workMode = WorkMode.SETTINGS
                    protocolManager.askCurrentProto()
                } else {
                    handleNegativeAnswer()
                }
            }

            WorkMode.SETTINGS -> {
                atFrameDecoder.decode(bytes, workMode)
                if (protocolManager.isLastSettingSend()) {
                    if (pinFrameDecoder == null) {
                        pinFrameDecoder = PinAnswerDecoder(eventFlow)
                    }
                    workMode = WorkMode.COMMANDS
                } else {
                    protocolManager.sendNextSettings()
                }
            }

            WorkMode.COMMANDS -> {
                pinFrameDecoder?.let {
                    if (it.decode(bytes, workMode)) {

                    }
                }
            }

        }
    }

    @Throws(WrongInitCommandException::class)
    private suspend fun handleNegativeAnswer() {
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
     */
    override fun setNewSetting(command: Commands.AtCommands) {
        //todo filter command depends on proto protocol
        checkSource()
        commanderScope.launch {
            if (command == Commands.AtCommands.ResetAll) {
                onReset()
            }
            cancelRepeatJobs()
            workMode = WorkMode.SETTINGS
            protocolManager.setSetting(command)
        }
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
                commandHandler?.let { handler ->
                    handler.commandFlow.map { command ->
                        return@map command.toByteArray()
                    }.onEach {
                        source.outputByteFlow.emit(it)
                    }.collect()
                }
            }
        }
    }

    override fun startWithProto(protocol: Protocol) {
        checkSource()
        onReset()
        val extra = if (protocol == Protocol.ISO_14230_4_FASTINIT) {
            listOf(Commands.AtCommands.FastInit.command)
        } else null
        commanderScope.launch {
            protocolManager.onRestart(ProtocolManagerStrategy.TRY, protocol, extra)
        }
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
        canMode.set(false)
        workMode = WorkMode.IDLE
        pinFrameDecoder = null
    }

    @Throws(NoSourceProvidedException::class)
    override fun startWithAuto() {
        checkSource()
        commanderScope.launch {
            protocolManager.onRestart(ProtocolManagerStrategy.AUTO)
        }
    }


    @Throws(NoSourceProvidedException::class)
    override fun startWithProtoAndRemember(protocol: Protocol) {
        checkSource()
        onReset()
        val extra = if (protocol == Protocol.ISO_14230_4_FASTINIT) {
            listOf(Commands.AtCommands.FastInit.command)
        } else null
        commanderScope.launch {
            protocolManager.onRestart(ProtocolManagerStrategy.SET, protocol, extra)
        }
    }


    /**
     * Send command if elm obd2 is configurated
     */
    override fun setCommand(command: Commands.PidCommands, repeat: Boolean, repeatTime: Long) {
        checkSourceAndMode(WorkMode.COMMANDS)
        commandHandler?.let {
            it.receiveCommand(command)
        }
    }

    /**
     * Send command if elm obd2 is configurated
     */
    override fun setCustomCommand(customCommand: String, repeat: Boolean, repeatTime: Long) {
        checkSourceAndMode(WorkMode.COMMANDS)
        commandHandler?.let {
            it.receiveCommand(customCommand)
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

    override fun switchSource(source: Source, resetStates: Boolean) {
        this.source = source
        if (resetStates) {
            resetStates()
        }
        observeInput()
        observeCommands()
    }


    /**
     *  SH: Header - address to whom we send command
     *  CRA: Receive address our listening device (7E8)
     *  The function will skip initialization commands is they have not been applied
     *
     */
    fun configureForCanCommands(
        headerAddress: String,
        receiverAddress: String,
        commandHandler: Class<out CommandHandler>? = null,
        decoder: Class<out Decoder>? = null
    ) {
        checkSource()
        commanderScope.launch {
            if (commandHandler != null && decoder != null) {
                cancelRepeatJobs()
            }
            setSpecificHandlers(commandHandler, decoder)
            workMode = WorkMode.SETTINGS

            protocolManager.setHeaderAndReceiver(headerAddress, receiverAddress, canMode.get())
            canMode.set(true)
        }


        //ATSH ATCRA
    }

    private fun setSpecificHandlers(
        commandHandler: Class<out CommandHandler>?,
        decoder: Class<out Decoder>?
    ) {
        decoder?.let {
            pinFrameDecoder = it.getConstructor(MutableSharedFlow::class.java).newInstance(eventFlow)
        }
        commandHandler?.let {
            //todo
        }
    }


    private suspend fun cancelRepeatJobs() {
        //do work and after delay to be sure all commands was delivered
        delay(500)
    }

    /**
     * Use carefully, only if you sure in your command
     * The function will skip initialization commands is they have not been applied
     */
    override fun setSettingWithParameter(command: Commands.AtCommands, parameter: String) {
        checkSource()
        commanderScope.launch {
            cancelRepeatJobs()
            workMode = WorkMode.SETTINGS
            protocolManager.setSettingWithParameter(command, parameter)
        }
    }

    /**
     * Use carefully
     * Protocol manager will automatically send all initial settings via protocol witch was chosen
     * Settings can be configured manually by setSetting() or setSettingWithParameter()
     */
//    override fun switchProtocol(protocol: Protocol){
//        checkSource()
//        cancelRepeteJobs()
//        workMode = WorkMode.PROTOCOL
//        protocolManager.switchProtocol(protocol)
//    }

    private fun resetStates() {
        commanderScope.coroutineContext.cancelChildren()
        onReset()
    }

    override fun startWorkWithProfile(profile: CustomProfile) {
        checkSource()
        commanderScope.launch {
            protocolManager.startWithProfile(profile)
        }
    }

    override fun startWorkWithProfile(profile: Profile) {
        checkSource()
        commanderScope.launch {
            protocolManager.startWithProfile(profile)
        }
    }


    fun checkIfCanProto() {}


}

class a {
    val a = OBDCommander(ProtocolManager()) as Commander

    fun foo() {

        a.setCommand(Commands.PidCommands.ABSOLUTE_LOAD, true, 600L)
    }


}
