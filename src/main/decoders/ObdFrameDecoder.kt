package main.decoders

import main.messages.Message
import main.protocol.Protocol
import kotlinx.coroutines.flow.MutableSharedFlow
import main.WorkMode
import main.messages.InitElmMessage
import main.messages.OBDDataMessage
import main.messages.SelectedProtocolMessage
import java.util.concurrent.ConcurrentLinkedQueue

class ObdFrameDecoder(private val socketEventFlow: MutableSharedFlow<Message?>) : Decoder(socketEventFlow) {

    override val buffer = ConcurrentLinkedQueue<Message>()


    override suspend fun decode(message: OBDDataMessage): Boolean {
        return when (message.workMode) {
            WorkMode.IDLE -> {
                isPositiveIdleAnswer(message.binaryData)
            }
            WorkMode.PROTOCOL -> {
                isPositiveOBDAnswer(message.binaryData)
            }
            WorkMode.SETTINGS -> {
                isPositiveOBDAnswer(message.binaryData)
            }
            WorkMode.CLARIFICATION -> decodeAnsweredProtocolNumber(message.binaryData)
            else -> { false }
        }
    }

    private suspend fun isPositiveOBDAnswer(bytes: ByteArray): Boolean {
        val decodedString = bytes.decodeToString()
        return decodedString.contains("ok", true)
    }

    private suspend fun isPositiveIdleAnswer(bytes: ByteArray): Boolean {

        val decodedString = bytes.decodeToString()
        return if (decodedString.contains("elm327", true)) {
            //TODO REMAKE OFFSET AND LIMIT FOR SUBSTRING
            socketEventFlow.emit(InitElmMessage(decodedString.substring(7, 18)))
            true
        } else false

    }

    private fun decodeAnsweredProtocolNumber(bytes: ByteArray): Boolean {
        Protocol.values().forEach {
            val decodedMessage = bytes.decodeToString()
            if (it.hexOrdinal == decodedMessage || it.hexOrdinal == decodedMessage.take(2)) {
                buffer.add(SelectedProtocolMessage(it))
                return true
            }
        }
        return false
    }


}