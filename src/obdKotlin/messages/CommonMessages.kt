package obdKotlin.messages

import obdKotlin.WorkMode
import obdKotlin.commands.Commands
import obdKotlin.protocol.Protocol

abstract class CommonMessages( val messageType: MessageType): Message(messageType) {

    data class InitElmMessage(
        val elmVersion: String,
        override val type: MessageType
    ): CommonMessages(type)

    data class CommonAtAnswer(
        val messages: String,
        override val type: MessageType
        ): CommonMessages(type)

    data class WrongCommandMessage(
        val phase: WorkMode,
        override val type: MessageType
    ): CommonMessages(type)

    data class SelectedProtocolMessage(
        val protocol: Protocol,
        override val type: MessageType
    ): CommonMessages(type)

    data class MonitorDTCsStatus(
        val isMilOn: Boolean,
        val numberOfErrors: UByte,
        val isSparkTestAvailable: Boolean,
        override val type: MessageType
    ): CommonMessages(type)

    data class Supported0120Commands(
        val commands: Map<Commands.PidCommands, Boolean>,
        override val type: MessageType
    ): CommonMessages(type){

    }
}