package main

import AtCommands
import BusCommander
import main.protocol.Protocol
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import main.commandProcessors.CanCommandHandler
import main.commandProcessors.CommandHandler
import main.commandProcessors.CommonCommandHandler
import main.decoders.CanAnswerDecoder
import main.decoders.Decoder
import main.messages.OBDDataMessage
import main.decoders.ObdFrameDecoder
import main.decoders.PinAnswerDecoder
import main.exceptions.NoSourceProvidedException
import main.exceptions.WrongInitCommandException
import main.protocol.BaseProtocolManager
import main.messages.Message
import main.protocol.ProtocolManager
import main.source.Source
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.jvm.Throws


class OBDCommander(
    private val protocolManager: BaseProtocolManager
) : BusCommander(protocolManager) {

    constructor(protoManager: BaseProtocolManager, source: Source) : this(protoManager) {
        this.source = source
    }

    companion object {
        const val OBD_PREFIX = "AT"
    }

    class Builder() {
        companion object {

            private var source: Source? = null
            private var protocolManager: BaseProtocolManager = ProtocolManager()

            @JvmStatic
            fun addSource(source: Source): Companion {
                this.source = source
                return this
            }

            @JvmStatic
            fun addProtocolManager(manager: BaseProtocolManager): Companion {
                this.protocolManager = manager
                return this
            }

            @JvmStatic
            fun build(): OBDCommander {
                return if (source != null) {
                    OBDCommander(protocolManager, source!!)
                } else {
                    OBDCommander(protocolManager)
                }
            }
        }
    }

    /**
     * Проверяем на длинну строки и на релевантность "OK" или не ОК(либо ок, тогда смотрим прото, если не ок, то следующий)
     * Try Next
     */

    private val canMode = AtomicBoolean(false)
    private var source: Source? = null

    private val commanderScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var workMode = WorkMode.IDLE

    override val eventFlow: MutableSharedFlow<Message?> = MutableSharedFlow()

    private var commandHandler: CommandHandler? = null


    init {
        observeInput()
        observeCommands()
    }

    private val atFrameDecoder: Decoder = ObdFrameDecoder(eventFlow)
    private var pinFrameDecoder: PinAnswerDecoder? = null

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
        val message = OBDDataMessage(bytes, workMode)
        when (workMode) {
            WorkMode.IDLE -> {
                if (atFrameDecoder.decode(message)) {
                    workMode = WorkMode.PROTOCOL
                    protocolManager.handleAnswer()
                } else {
                    handleNegativeAnswer()
                }
            }

            WorkMode.PROTOCOL -> {
                if (atFrameDecoder.decode(message)) {
                    workMode = WorkMode.CLARIFICATION
                    protocolManager.askCurrentProto()
                } else {
                    handleNegativeAnswer()
                }
            }

            WorkMode.CLARIFICATION -> {
                if (atFrameDecoder.decode(message)) {
                    if (protocolManager.checkIfCanProto(atFrameDecoder.buffer.poll())) {
                        //TODO PASS FLOW FOR ANSWERS
                        pinFrameDecoder = CanAnswerDecoder()
                        commandHandler = CanCommandHandler()
                    } else {
                        pinFrameDecoder= PinAnswerDecoder()
                        commandHandler = CommonCommandHandler()
                    }
                    workMode = WorkMode.SETTINGS
                    protocolManager.sendNextSettings()
                } else {
                }
            }

            WorkMode.SETTINGS -> {
                if (atFrameDecoder.decode(message)) {
                    if (protocolManager.isLastSettingSend()) {
                        workMode = WorkMode.COMMANDS
                    } else {
                        protocolManager.sendNextSettings()
                    }
                } else {
                    handleNegativeAnswer()
                }
            }

            WorkMode.COMMANDS -> {
                //on command PIN answer should not be true. Only in case new setting it will be true
//                if (obdFrameDecoder.isPositiveOBDAnswer(bytes)) { obdFrameDecoder should not do that

            }

        }
    }

    @Throws(WrongInitCommandException::class)
    private suspend fun handleNegativeAnswer() {
        //TODO handle errors
        when (workMode) {
            WorkMode.COMMANDS -> {
                skipCommand()
            }
            WorkMode.SETTINGS -> {
                protocolManager.sendNextSettings()
            }
            else -> {
                throw WrongInitCommandException()
            }
        }
    }


    fun setNewSetting(command: AtCommands) {
        //todo filter command depends on proto protocol
        protocolManager.setSetting(command)
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
                commandHandler?.let {handler->
                    handler.commandFlow.map {command->
                        return@map command.toByteArray()
                    }.onEach {
                        source.outputByteFlow.emit(it)
                    }.collect()
                }
            }
        }
    }

    override fun tryProto(protocol: Protocol) {
        checkSource()
        protocolManager.onRestart(ProtocolManagerStrategy.TRY, protocol)
    }

    override suspend fun resetSettings() {
        checkSource()
        onReset()
        protocolManager.reset()
    }

    private fun onReset() {
        workMode = WorkMode.IDLE
        pinFrameDecoder = null
        atFrameDecoder.buffer.clear()
    }

    @Throws(NoSourceProvidedException::class)
    override fun obdAutoAll() {
        checkSource()
        protocolManager.onRestart(ProtocolManagerStrategy.AUTO)
    }


    @Throws(NoSourceProvidedException::class)
    override fun setProto(protocol: Protocol) {
        checkSource()
        protocolManager.onRestart(ProtocolManagerStrategy.SET, protocol)
    }


    override fun setCommand(command: String) { //OBDcommand
        TODO("Not yet implemented")
    }

    override fun setPinCommand(command: String) { //PINCommand
        TODO("Not yet implemented")
    }


    override fun stopJob() {
        commanderScope.cancel()
    }


    @Throws(NoSourceProvidedException::class)
    private fun checkSource() {
        if (source == null) {
            throw NoSourceProvidedException()
        }
    }

    fun switchSource(source: Source) {
        this.source = source
        resetStates()
        observeInput()
        observeCommands()
    }


    fun configureForCanCommands() {
        //ATSH ATCRA
    }

    private fun resetStates() {
        commanderScope.coroutineContext.cancelChildren()
        onReset()
    }

}
