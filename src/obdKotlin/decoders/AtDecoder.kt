package obdKotlin.decoders

import obdKotlin.protocol.Protocol
import kotlinx.coroutines.flow.MutableSharedFlow
import obdKotlin.WorkMode
import obdKotlin.messages.*


class AtDecoder() : Decoder() {

    companion object {
        private const val POSITIVE_ANSWER = "ok"
        private const val DEVICE_NAME = "elm327"
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
                    EncodingState.UNSUCCESSFUL
                }
            }

            WorkMode.PROTOCOL -> {
                return isPositiveOBDAnswer(decodedString)
            }

            else -> {
                return if (decodedString.contains(POSITIVE_ANSWER)) {
                    isPositiveIdleAnswer(decodedString)
                } else if(decodeProtocol(decodedString)){
                    EncodingState.SUCCESSFUL
                } else {
                    eventFlow.emit(Message.CommonAtAnswer(decodedString))
                    EncodingState.UNSUCCESSFUL
                }
            }
        }

    }

    private suspend fun isPositiveOBDAnswer(answer: String): EncodingState {
        return if (answer.contains(POSITIVE_ANSWER, true))
            EncodingState.SUCCESSFUL else EncodingState.UNSUCCESSFUL
    }

    private suspend fun isPositiveIdleAnswer(answer: String): EncodingState {
        //todo refact substring
        eventFlow.emit(Message.InitElmMessage(answer.substring(0, 3)))
        return EncodingState.SUCCESSFUL
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