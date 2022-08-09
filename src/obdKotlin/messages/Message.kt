package obdKotlin.messages

abstract class Message(open val type: MessageType) {

    enum class MessageType{
        COMMON, ENGINE, SYSTEM
    }
}