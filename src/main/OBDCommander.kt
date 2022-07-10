package main

import AtCommands
import BusCommander
import Event
import OBDMessage
import Protocol
import ProtocolManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import main.decoders.ObdFrameDecoder
import main.source.Source
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.jvm.Throws


class OBDCommander(
    private var protoManager: ProtocolManager,
) : BusCommander(protoManager) {

    constructor(protoManager: ProtocolManager, source: Source) : this(protoManager) {
        this.source = source
    }

    companion object {
        const val OBD_PREFIX = "AT"
    }

    class Builder(){
        companion object{
            @JvmStatic
            private val commander = OBDCommander(ProtocolManager())
            @JvmStatic
            fun addSource(source: Source): Companion {
                commander.switchSource(source)
                return this
            }
            @JvmStatic
            fun addProtocolManager(manager: ProtocolManager): Companion{
                commander.protoManager = manager
                return this
            }
            @JvmStatic
            fun build(): OBDCommander {
                return commander
            }
        }
    }

    /**
     * Проверяем на длинну строки и на релевантность "OK" или не ОК(либо ок, тогда смотрим прото, если не ок, то следующий)
     * Try Next
     */

    private val canMode = AtomicBoolean(false)
    private var source: Source? = null
    private var currentProto: Protocol? = null

    private val commanderScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var workMode = WorkMode.IDLE

    private var onPositiveAnswerStrategy = OnPositiveAnswerStrategy.IDLE

    override val socketEventFlow: MutableSharedFlow<Event<OBDMessage?>> = MutableSharedFlow()

    init {
        observeInput()
        observeCommands()
    }

    private val obdFrameDecoder = ObdFrameDecoder(socketEventFlow)

    private fun observeInput() {
        source?.let {
             commanderScope.launch {
                it.inputByteFlow.onEach {
                    manageInputData(it)
                }.collect()
            }
        }
    }

    private suspend fun manageInputData(bytes: ByteArray){
        when (workMode) {
            WorkMode.IDLE -> {
                if (obdFrameDecoder.isPositiveIdleAnswer(bytes)) {
                    handlePositiveAnswer()
                    workMode = WorkMode.PROTOCOL
                } else {
                    handleNegativeAnswer()
                }
            }
            WorkMode.PROTOCOL -> {
                if (obdFrameDecoder.isPositiveProtoOBDAnswer(bytes, onPositiveAnswerStrategy)) {
                    handlePositiveAnswer()
                    workMode = WorkMode.SETTINGS
                } else {
                    handleNegativeAnswer()
                }
            }
            WorkMode.SETTINGS -> {
                if (obdFrameDecoder.isPositiveOBDAnswer(bytes)) {
                    if (protoManager.isLastCommandSend()) {
                        workMode = WorkMode.COMMANDS
                        selectPinDecoder()
                    } else {
                        handlePositiveAnswer()
                    }
                } else {
                    handleNegativeAnswer()
                }
            }
            WorkMode.COMMANDS -> {
                //on command PIN answer should not be true. Only in case new setting it will be true
                if (obdFrameDecoder.isPositiveOBDAnswer(bytes)) {

                } else {
                    if(!pinAnswerDecoder.decode()){
                        //todo error
                    }
                }

            }
        }
    }

    private fun selectPinDecoder() {
        TODO("Not yet implemented")
    }

    fun setSetting(command: AtCommands) {
        //todo filter command depends on proto protocol
        protoManager.setSetting(command)
    }

    private suspend fun handleNegativeAnswer() {
        when (onPositiveAnswerStrategy) {
            OnPositiveAnswerStrategy.IDLE -> Unit
            OnPositiveAnswerStrategy.TRY -> {
                protoManager.skipProto()
            }
            OnPositiveAnswerStrategy.ASK_RECOMMENDED -> protoManager.askObdProto()
            OnPositiveAnswerStrategy.SET -> TODO()
            OnPositiveAnswerStrategy.CAN -> TODO()
        }
    }

    private suspend fun handlePositiveAnswer() {
        when (onPositiveAnswerStrategy) {
            OnPositiveAnswerStrategy.IDLE -> Unit //готов
            OnPositiveAnswerStrategy.TRY -> handleOnTry()//готов
            OnPositiveAnswerStrategy.ASK_RECOMMENDED -> handleOnAsk() //готов
            OnPositiveAnswerStrategy.SET -> handleSet() //готов
            OnPositiveAnswerStrategy.CAN -> TODO()
        }
    }

    private suspend fun handleSet() {
        when (workMode) {
            WorkMode.IDLE -> protoManager.setProto()
            WorkMode.PROTOCOL -> Unit
            WorkMode.SETTINGS -> protoManager.sendNextSettings()
            WorkMode.COMMANDS -> TODO()
        }
    }

    private suspend fun handleOnAsk() {
        when (workMode) {
            WorkMode.IDLE -> protoManager.askObdProto()
            WorkMode.PROTOCOL -> {
                currentProto = obdFrameDecoder.getProtoByCachedHex()
                protoManager.setRecommendedProto(obdFrameDecoder.getSavedAnswer())
            }
            WorkMode.SETTINGS -> protoManager.sendNextSettings()
            WorkMode.COMMANDS -> TODO()
        }
    }

    private suspend fun handleOnTry() {
        when (workMode) {
            WorkMode.IDLE -> protoManager.tryNextProto()
            WorkMode.PROTOCOL -> protoManager.setTriedProto()
            WorkMode.SETTINGS -> protoManager.sendNextSettings()
            WorkMode.COMMANDS -> TODO()
        }
    }

    private  fun observeCommands() {
        source?.let { source ->
             commanderScope.launch {
                protoManager.obdCommandFlow.map {
                    return@map it.toByteArray()
                }.onEach {
                    source.outputByteFlow.emit(it)
                }.collect()
            }
        }
    }

    override fun tryProtos() {
        checkSource()
        onPositiveAnswerStrategy = OnPositiveAnswerStrategy.TRY
        protoManager.onRestart()
    }

    override fun switchToCanMode() {
        checkSource()
        protoManager.onRestart(true)
        canMode.set(true)
    }

    override suspend fun resetSettings() {
        checkSource()
        protoManager.onRestart()
    }

    @Throws(NoSourceProvidedException::class)
    override fun obdAutoAll() {
        checkSource()
        onPositiveAnswerStrategy = OnPositiveAnswerStrategy.TRY
        protoManager.onRestart(false, auto = true)
    }

    @Throws(NoSourceProvidedException::class)
    override fun askAndSetRecommendedProto() {
        checkSource()
        onPositiveAnswerStrategy = OnPositiveAnswerStrategy.ASK_RECOMMENDED
        protoManager.onRestart()
    }

    @Throws(NoSourceProvidedException::class)
    override fun setProto() {
        checkSource()
        onPositiveAnswerStrategy = OnPositiveAnswerStrategy.SET
        protoManager.onRestart()
    }

    override fun setCustomOBDSettings(obdCommands: Set<String>) {
        TODO("Not yet implemented")
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

    private fun resetStates() {
        commanderScope.coroutineContext.cancelChildren()
        workMode = WorkMode.IDLE
        onPositiveAnswerStrategy = OnPositiveAnswerStrategy.IDLE
    }

}
