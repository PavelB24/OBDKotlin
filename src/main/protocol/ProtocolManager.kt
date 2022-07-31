package main.protocol

import main.commands.AtCommands
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import main.exceptions.ModsConflictException
import main.exceptions.WrongMessageTypeException
import main.messages.Message
import main.messages.SelectedProtocolMessage
import java.util.concurrent.ConcurrentLinkedQueue

class ProtocolManager: BaseProtocolManager() {

    companion object {
        const val ATTENTION_PREFIX = "AT"
    }


    private var strategy: ProtocolManagerStrategy = ProtocolManagerStrategy.IDLE

    private val _obdCommandFlow = MutableSharedFlow<String>()
    override val obdCommandFlow: SharedFlow<String> = _obdCommandFlow

    private val standardSettingsSet: Set<AtCommands> = setOf(
        AtCommands.ResetAll, AtCommands.EchoOff, AtCommands.AllowLongMessages,
        AtCommands.PrintingSpacesOff
    )

    private val additionalCommandsSet: Set<AtCommands> = setOf(
        AtCommands.AllowLongMessages, AtCommands.FlowControlOff,
        AtCommands.AutoFormatCanFramesOff, AtCommands.SetWakeUpMessagesOff
    )


    private var userProtocol: Protocol? = null

    private val settingsQueue = ConcurrentLinkedQueue<AtCommands>()
    private val protocolQueue = ConcurrentLinkedQueue<Protocol>()

    //invoke this first time with WM idle when switch into proto


    override suspend fun handleAnswer() {
        when (strategy) {
            ProtocolManagerStrategy.IDLE -> {}
            ProtocolManagerStrategy.TRY -> tryProto()
            ProtocolManagerStrategy.SET -> setProto()
            ProtocolManagerStrategy.AUTO -> setProto()
        }
    }

    override suspend fun setSettingWithParameter(command: AtCommands, parameter: String) {
        _obdCommandFlow.emit("${ATTENTION_PREFIX}${command.command}$parameter")
    }

    private suspend fun setProto() {
        if (protocolQueue.isNotEmpty()) {
            _obdCommandFlow.emit("${ATTENTION_PREFIX}${AtCommands.SetProto}${protocolQueue.poll().hexOrdinal}")
        } else {
            _obdCommandFlow.emit("${ATTENTION_PREFIX}${AtCommands.SetProto}${Protocol.AUTOMATIC.hexOrdinal}")
        }
    }


    private suspend fun tryProto() {
        if (protocolQueue.isNotEmpty()) {
            _obdCommandFlow.emit("${ATTENTION_PREFIX}${AtCommands.TryProto}${protocolQueue.peek().hexOrdinal}")
        }
    }

    override suspend fun askObdProto() {  //Use only when we want set recommended proto
        _obdCommandFlow.emit("${ATTENTION_PREFIX}${AtCommands.GetVehicleProtoAsNumber.command}")
    }

    override suspend fun resetStates() {
        strategy = ProtocolManagerStrategy.IDLE
    }


    //Set proto without try or checking results only after obd answer with hexNum of proto
    override suspend fun askCurrentProto() {
        _obdCommandFlow.emit("${ATTENTION_PREFIX}${AtCommands.GetVehicleProtoAsNumber.command}")
    }

    override suspend fun onRestart(strategy: ProtocolManagerStrategy, protocol: Protocol?) {
        if (strategy == ProtocolManagerStrategy.IDLE) {
            throw java.lang.IllegalArgumentException("Idle ProtocolManagerStrategy should not be provided")
        }
        this.strategy = strategy
        userProtocol = protocol
        prepare()
        _obdCommandFlow.emit("${ATTENTION_PREFIX}${settingsQueue.poll().command}")
    }

    override suspend fun reset(){
        strategy = ProtocolManagerStrategy.IDLE
        _obdCommandFlow.emit("${ATTENTION_PREFIX}${AtCommands.ResetAll.command}")
    }

    override fun isLastSettingSend(): Boolean = settingsQueue.isEmpty()

    override suspend fun sendNextSettings() {
        _obdCommandFlow.emit("${ATTENTION_PREFIX}${settingsQueue.poll().command}")
    }

    private suspend fun prepare() {
        if (userProtocol != null && strategy != ProtocolManagerStrategy.AUTO) {
            protocolQueue.add(userProtocol)
            settingsQueue.addAll(standardSettingsSet)
        } else if (strategy == ProtocolManagerStrategy.AUTO) {
            protocolQueue.add(Protocol.AUTOMATIC)
            settingsQueue.addAll(standardSettingsSet)
        } else {
            userProtocol = null
            strategy = ProtocolManagerStrategy.IDLE
            throw ModsConflictException("For non-auto strategy, protocol should be provided")
        }
    }

    override suspend fun setSetting(command: AtCommands) {
        _obdCommandFlow.emit("${ATTENTION_PREFIX}${command.command}")
    }

    override suspend fun switchProtocol(protocol: Protocol) {
        _obdCommandFlow.emit("${ATTENTION_PREFIX}${protocol.hexOrdinal}")
    }

    @Throws(WrongMessageTypeException::class)
    override fun checkIfCanProto(message: Message): Boolean {
        if (message is SelectedProtocolMessage) {
            val specificOptions = mutableSetOf<AtCommands>()
            val isCan = when (message.protocol) {
                Protocol.ISO_15765_4_CAN_11_bit_ID_500kbaud -> { true }
                Protocol.ISO_15765_4_CAN_29_bit_ID_500kbaud -> { true }
                Protocol.ISO_15765_4_CAN_11_bit_ID_250kbaud -> { true }
                Protocol.ISO_15765_4_CAN_29_bit_ID_250kbaud -> { true }
                Protocol.SAE_J1939_CAN_29_bit_ID_250kbaud -> { true }
                Protocol.ISO_14230_4_FASTINIT -> {
                    specificOptions.add(AtCommands.FastInit)
                    false
                }
                else -> false
            }
            if (isCan){
                settingsQueue.addAll(additionalCommandsSet)
            }
            if(specificOptions.isNotEmpty()){
                settingsQueue.addAll(specificOptions)
            }
            return isCan
        } else{
            throw WrongMessageTypeException("SelectedProtocolMessage should be provided")
        }
    }


}