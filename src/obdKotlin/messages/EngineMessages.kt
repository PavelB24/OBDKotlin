package obdKotlin.messages

abstract class EngineMessages(val messageType: MessageType): Message(messageType) {

    data class TemperatureMessage(
        val temperature: Int,
        override val type: MessageType
    ): EngineMessages(type)

    data class EngineLoadMessage(
        val load: Float,
        override val type: MessageType
    ): EngineMessages(type)

    data class VehicleSpeed(
        val speed: Int,
        override val type: MessageType
    ): EngineMessages(type)
}