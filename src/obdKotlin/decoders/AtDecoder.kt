package obdKotlin.decoders

import kotlinx.coroutines.flow.MutableSharedFlow
import obdKotlin.WorkMode
import obdKotlin.messages.Message
import obdKotlin.protocol.Protocol

class AtDecoder() : Decoder() {

    companion object {
        private const val POSITIVE_ANSWER = "ok"
        private const val DEVICE_NAME = "elm"
    }

    override val eventFlow: MutableSharedFlow<Message?> = MutableSharedFlow()

    override suspend fun decode(message: ByteArray, workMode: WorkMode): EncodingState {
        val decodedString = message.decodeToString()
        when (workMode) {
            WorkMode.IDLE -> {
                return if (decodedString.contains(DEVICE_NAME, true)) {
                    isPositiveIdleAnswer(decodedString)
                } else {
                    eventFlow.emit(Message.CommonAtAnswer(decodedString))
                    EncodingState.Unsuccessful(decodedString)
                }
            }

            WorkMode.PROTOCOL -> {
                return isPositiveOBDAnswer(decodedString)
            }

            else -> {
                return if (decodedString.contains(POSITIVE_ANSWER)) {
                    isPositiveIdleAnswer(decodedString)
                } else if (decodeProtocol(decodedString)) {
                    EncodingState.Successful
                } else {
                    eventFlow.emit(Message.CommonAtAnswer(decodedString))
                    EncodingState.Unsuccessful(decodedString)
                }
            }
        }
    }

    private suspend fun isPositiveOBDAnswer(answer: String): EncodingState {
        return if (answer.contains(POSITIVE_ANSWER, true)) {
            EncodingState.Successful
        } else EncodingState.Unsuccessful(answer)
    }

    private suspend fun isPositiveIdleAnswer(answer: String): EncodingState {
        // todo refact substring
        eventFlow.emit(Message.InitElmMessage(answer.substring(0, 3)))
        return EncodingState.Successful
    }

    private suspend fun decodeProtocol(answer: String): Boolean {
        val protocol = Protocol.values().find {
            it.hexOrdinal == answer || it.hexOrdinal == answer.take(2)
        }
        protocol?.let {
            eventFlow.emit(Message.SelectedProtocolMessage(it))
            return true
        }
        return false
    }
}
