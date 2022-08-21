package obdKotlin.messages

import obdKotlin.core.WorkMode
import obdKotlin.protocol.Protocol

sealed class Message() {

    /**
     * If encoder cant encode the data, it emits that message in the flow
     */
    data class UnknownAnswer(
        val hex: String,
        val bin: ByteArray
    ): Message()

    /**
     * To use in special encoder, put data in your class inherited from CustomData
     */
    data class SpecialObdAnswer(
        val answerData: CustomData
    ): Message(){
        abstract inner class CustomData
    }
    data class InitElmMessage(
        val elmVersion: String
    ): Message()

    data class CommonAtAnswer(
        val messages: String
        ): Message()

    data class WrongCommandMessage(
        val phase: WorkMode
    ): Message()

    data class SelectedProtocolMessage(
        val protocol: Protocol,
    ): Message()

    data class MonitorDTCsStatus(
        val isMilOn: Boolean,
        val numberOfErrors: UByte,
        val isSparkTestAvailable: Boolean,
    ): Message()

    data class CatalystTemperature(
        val temperature: Int,
        val bank: Int,
        val sensor: Int
    ): Message()

    data class SupportedCommands(
        val commands: Map<String, Boolean>,
        val fromHex: String,
        val toHex: String
    ): Message()

    data class Supported2140Commands(
        val commands: Map<String, Boolean>,
    ): Message()

    data class Supported4160Commands(
        val commands: Map<String, Boolean>,
    ): Message()

    data class Supported6180Commands(
        val commands: Map<String, Boolean>,
    ): Message()

    data class Supported81A0Commands(
        val commands: Map<String, Boolean>,
    ): Message()

    data class SupportedA1C0Commands(
        val commands: Map<String, Boolean>,
    ): Message()

    data class SupportedC1E0Commands(
        val commands: Map<String, Boolean>,
    ): Message()

    data class TemperatureMessage(
        val temperature: Int
    ): Message()

    data class EngineLoadMessage(
        val load: Float
    ): Message()

    data class VehicleSpeed(
        val speed: Int
    ): Message()
}