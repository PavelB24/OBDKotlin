package obdKotlin.decoders

import kotlinx.coroutines.flow.MutableSharedFlow
import obdKotlin.WorkMode
import obdKotlin.messages.Message
import obdKotlin.protocol.Protocol

class AtDecoder() : Decoder() {

    companion object {
        private const val POSITIVE_ANSWER = "ok"
        const val DEVICE_NAME = "elm"
        const val WARM_RES_ANSWER = "atws"
        const val HARD_RES_ANSWER = "atz"
    }

    override val eventFlow: MutableSharedFlow<Message?> = MutableSharedFlow()

    override suspend fun decode(message: ByteArray, workMode: WorkMode): EncodingState {
        val decodedString = message.decodeToString().replace(" ", "")
        when (workMode) {
            WorkMode.IDLE -> {
                return isPositiveIdleAnswer(decodedString)
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

    private fun isPositiveOBDAnswer(answer: String): EncodingState {
        return if (answer.contains(POSITIVE_ANSWER, true)) {
            EncodingState.Successful
        } else EncodingState.Unsuccessful(answer)
    }

    private suspend fun isPositiveIdleAnswer(answer: String): EncodingState {
        val deviceName: String? =
            if (answer.contains(HARD_RES_ANSWER, true)) answer.substring(3, answer.length)
            else if (answer.contains(WARM_RES_ANSWER, true)) answer.substring(4, answer.length)
            else null
        return if (deviceName != null) {
            eventFlow.emit(Message.ElmDeviceName(deviceName))
            EncodingState.Successful
        } else {
            EncodingState.Unsuccessful(answer)
        }
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
