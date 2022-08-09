package obdKotlin.protocol


import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import obdKotlin.commands.Commands
import obdKotlin.commands.POSTFIX
import obdKotlin.exceptions.ModsConflictException
import obdKotlin.exceptions.WrongMessageTypeException
import obdKotlin.messages.CommonMessages
import obdKotlin.messages.Message
import obdKotlin.profiles.CustomProfile
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

    private val additionalCommandsSet: Set<String> = setOf(
        "${Commands.AtCommands.FlowControlOff.command}$POSTFIX",
        "${Commands.AtCommands.AutoFormatCanFramesOff.command}$POSTFIX",
        "${Commands.AtCommands.SetWakeUpMessagesOff.command}$POSTFIX"
    )

    private var userProtocol: Protocol? = null
    private val settingsQueue = ConcurrentLinkedQueue<String>()


    @Throws(IllegalStateException::class)
    override suspend fun handleAnswer() {
        strategy?.let {
            when (strategy) {
                ProtocolManagerStrategy.TRY -> tryProto()
                ProtocolManagerStrategy.SET -> setProto()
                ProtocolManagerStrategy.AUTO -> setProto()
            }
        }

    }

    override suspend fun setSettingWithParameter(command: Commands.AtCommands, parameter: String) {
        _obdCommandFlow.emit("${command.command}$parameter$POSTFIX")
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

    override suspend fun onRestart(strategy: ProtocolManagerStrategy, protocol: Protocol?, extra: List<String>?) {
        this.strategy = strategy
        userProtocol = protocol
        prepare(extra)
        _obdCommandFlow.emit("${Commands.AtCommands.ResetAll.command}$POSTFIX")
    }

    override suspend fun setHeaderAndReceiver(headerAddress: String, receiverAddress: String, isAlreadyCan: Boolean) {
        if (!isAlreadyCan) {
            settingsQueue.addAll(additionalCommandsSet)
        }
        settingsQueue.add("${Commands.AtCommands.SetHeader.command}$headerAddress$POSTFIX")
        settingsQueue.add("${Commands.AtCommands.SetReceiverAdrFilter.command}$receiverAddress$POSTFIX")
        sendNextSettings()
    }

    override suspend fun startWithProfile(profile: Profile) {
        strategy = ProtocolManagerStrategy.SET
        userProtocol = profile.protocol
        profile.settings.forEach {
            if (it.second == null) {
                settingsQueue.add("${it.first}$POSTFIX")
            } else {
                settingsQueue.add("${it.first}${it.second}$POSTFIX")
            }
        }
        _obdCommandFlow.emit(Commands.AtCommands.ResetAll.command)
    }

    override suspend fun startWithProfile(profile: CustomProfile) {
        strategy = ProtocolManagerStrategy.SET
        settingsQueue.addAll(standardSettingsSet)
        profile.settingsAndParams.forEach {
            if (it.second == null) {
                settingsQueue.add("${it.first}")
            } else {
                settingsQueue.add("${it.first}${it.second}")
            }
        }
        _obdCommandFlow.emit(Commands.AtCommands.ResetAll.command)
    }

    override suspend fun resetSession() {
        strategy = null
        _obdCommandFlow.emit(Commands.AtCommands.ResetAll.command)
    }

    override fun isLastSettingSend(): Boolean = settingsQueue.isEmpty()

    override suspend fun sendNextSettings() {
        _obdCommandFlow.emit(settingsQueue.poll())
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

    override suspend fun setSetting(command: Commands.AtCommands) {
        _obdCommandFlow.emit("${command.command}$POSTFIX")
    }

    override suspend fun switchProtocol(protocol: Protocol) {
        _obdCommandFlow.emit("${Commands.AtCommands.SetProto}${protocol.hexOrdinal}$POSTFIX")
    }

    @Throws(WrongMessageTypeException::class)
    override fun checkIfCanProto(message: Message): Boolean {
        if (message is CommonMessages.SelectedProtocolMessage) {
            return when (message.protocol) {
                Protocol.ISO_15765_4_CAN_11_bit_ID_500kbaud -> {
                    true
                }

                Protocol.ISO_15765_4_CAN_29_bit_ID_500kbaud -> {
                    true
                }

                Protocol.ISO_15765_4_CAN_11_bit_ID_250kbaud -> {
                    true
                }

                Protocol.ISO_15765_4_CAN_29_bit_ID_250kbaud -> {
                    true
                }

                Protocol.SAE_J1939_CAN_29_bit_ID_250kbaud -> {
                    true
                }

                else -> false
            }
        } else {
            throw WrongMessageTypeException("SelectedProtocolMessage should be provided")
        }
    }


}