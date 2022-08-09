package obdKotlin.decoders

import obdKotlin.protocol.Protocol
import kotlinx.coroutines.flow.MutableSharedFlow
import obdKotlin.WorkMode
import obdKotlin.messages.*


class AtDecoder(
    private val eventFlow: MutableSharedFlow<Message?>,
) : Decoder(eventFlow) {

    companion object {
        private const val POSITIVE_ANSWER = "ok"
        private const val DEVICE_NAME = "elm327"
    }


    override suspend fun decode(message: ByteArray, workMode: WorkMode): Boolean {
        val decodedString = message.decodeToString()
        when (workMode) {
            WorkMode.IDLE -> {
                return if (decodedString.contains(DEVICE_NAME, true)) {
                    isPositiveIdleAnswer(decodedString)
                } else {
                    eventFlow.emit(CommonMessages.CommonAtAnswer(decodedString, Message.MessageType.COMMON))
                    false
                }
            }

            WorkMode.PROTOCOL -> {
                return isPositiveOBDAnswer(decodedString)
            }

            else -> {
                return if (decodedString.contains(POSITIVE_ANSWER)) {
                    isPositiveIdleAnswer(decodedString)
                } else if(decodeProtocol(decodedString)){
                    true
                } else {
                    eventFlow.emit(CommonMessages.CommonAtAnswer(decodedString, Message.MessageType.COMMON))
                    false
                }
            }
        }

    }

    private suspend fun isPositiveOBDAnswer(answer: String): Boolean {
        return answer.contains(POSITIVE_ANSWER, true)
    }

    private suspend fun isPositiveIdleAnswer(answer: String): Boolean {
        //todo refact substring
        eventFlow.emit(CommonMessages.InitElmMessage(answer.substring(0, 3), Message.MessageType.COMMON))
        return true
    }


    private suspend fun decodeProtocol(answer: String): Boolean {
        val protocol = Protocol.values().find {
            it.hexOrdinal == answer || it.hexOrdinal == answer.take(2)
        }
        protocol?.let {
            eventFlow.emit(CommonMessages.SelectedProtocolMessage(it, Message.MessageType.COMMON))
            return true
        }
        return false
    }


}