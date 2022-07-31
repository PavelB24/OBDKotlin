package main.protocol

import AtCommands
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import main.exceptions.ModsConflictException
import main.OBDCommander
import main.ProtocolManagerStrategy
import main.exceptions.WrongMessageTypeException
import main.messages.Message
import main.messages.SelectedProtocolMessage
import java.util.concurrent.ConcurrentLinkedQueue

class ProtocolManager: BaseProtocolManager() {


    private var strategy: ProtocolManagerStrategy = ProtocolManagerStrategy.IDLE

    private val _obdCommandFlow = MutableSharedFlow<String>()
    override val obdCommandFlow: SharedFlow<String> = _obdCommandFlow

    private val standardSettingsSet: Set<AtCommands> = setOf(
        AtCommands.ResetAll, AtCommands.EchoOff, AtCommands.AllowLongMessages,
        AtCommands.PrintingSpacesOff
    )

    private val additionalCommandsSet: Set<AtCommands> = setOf(
        AtCommands.AllowLongMessages, AtCommands.AutoFormatCanFramesOff,
        AtCommands.FlowControlOff,
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

    private suspend fun setProto() {
        if (protocolQueue.isNotEmpty()) {
            _obdCommandFlow.emit("${OBDCommander.OBD_PREFIX}${AtCommands.SetProto}${protocolQueue.poll().hexOrdinal}")
        } else {
            _obdCommandFlow.emit("${OBDCommander.OBD_PREFIX}${AtCommands.SetProto}${Protocol.AUTOMATIC.hexOrdinal}")
        }
    }

    private suspend fun skipProto() {
        protocolQueue.poll()
        tryProto()
    }

    private suspend fun tryProto() {
        if (protocolQueue.isNotEmpty()) {
            _obdCommandFlow.emit("${OBDCommander.OBD_PREFIX}${AtCommands.TryProto}${protocolQueue.peek().hexOrdinal}")
        }
    }

    override suspend fun askObdProto() {  //Use only when we want set recommended proto
        _obdCommandFlow.emit("${OBDCommander.OBD_PREFIX}${AtCommands.GetVehicleProtoAsNumber.command}")
    }


    //Set proto without try or checking results only after obd answer with hexNum of proto
    override suspend fun askCurrentProto() {
        _obdCommandFlow.emit("${OBDCommander.OBD_PREFIX}${AtCommands.GetVehicleProtoAsNumber.command}")
    }

    override suspend fun onRestart(strategy: ProtocolManagerStrategy, protocol: Protocol?) {
        if (strategy == ProtocolManagerStrategy.IDLE) {
            throw java.lang.IllegalArgumentException("Idle ProtocolManagerStrategy should not be provided")
        }
        this.strategy = strategy
        userProtocol = protocol
        prepare()
        _obdCommandFlow.emit("${OBDCommander.OBD_PREFIX}${settingsQueue.poll().command}")
    }

    override suspend fun reset(){
        strategy = ProtocolManagerStrategy.IDLE
        _obdCommandFlow.emit("${OBDCommander.OBD_PREFIX}${AtCommands.ResetAll.command}")
    }

    override fun isLastSettingSend(): Boolean = settingsQueue.isEmpty()

    override suspend fun sendNextSettings() {
        _obdCommandFlow.emit("${OBDCommander.OBD_PREFIX}${settingsQueue.poll().command}")
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
        _obdCommandFlow.emit("${OBDCommander.OBD_PREFIX}${command.command}")
    }

    @Throws(WrongMessageTypeException::class)
    override fun checkIfCanProto(proto: Message): Boolean {
        if (proto is SelectedProtocolMessage) {
            val isCan = when (proto.protocol) {
                Protocol.ISO_15765_4_CAN_11_bit_ID_500kbaud -> { true }
                Protocol.ISO_15765_4_CAN_29_bit_ID_500kbaud -> { true }
                Protocol.ISO_15765_4_CAN_11_bit_ID_250kbaud -> { true }
                Protocol.ISO_15765_4_CAN_29_bit_ID_250kbaud -> { true }
                Protocol.SAE_J1939_CAN_29_bit_ID_250kbaud -> { true }
                else -> false
            }
            if (isCan){
                settingsQueue.addAll(additionalCommandsSet)
            }
            return isCan
        } else{
            throw WrongMessageTypeException("SelectedProtocolMessage should be provided")
        }
    }


}