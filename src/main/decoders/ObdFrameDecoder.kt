package main.decoders

import main.messages.Message
import Protocol
import kotlinx.coroutines.flow.MutableSharedFlow
import main.WorkMode
import main.messages.InitElmMessage
import main.messages.OBDDataMessage
import main.messages.SelectedProtocolMessage
import java.lang.IllegalArgumentException
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
            socketEventFlow.emit(InitElmMessage(decodedString.substring(7, 18)))
            true
        } else false

    }

    private fun decodeAnsweredProtocolNumber(bytes: ByteArray): Boolean {
        Protocol.values().forEach {
            if (it.hexOrdinal == bytes.decodeToString() || it.hexOrdinal == bytes.decodeToString().take(2)) {
                buffer.add(SelectedProtocolMessage(it))
                return true
            }
        }
        return false
    }

    fun isReadyForNewCommand(bytes: ByteArray): Boolean {
        return bytes.decodeToString() == AtCommands.Repeat.command
    }

    fun handleDataAnswer(bytes: ByteArray) {
        if (bytes.decodeToString() != "?") {
            buffer.add(bytes)
        } else {
            //TODO
        }

    }

    fun getProtoByCachedHex(): Protocol {
        Protocol.values().forEach {
            if (it.hexOrdinal == buffer.peek()) {
                return it
            }
        }
        throw IllegalArgumentException()
    }

    fun getSavedAnswer(): String = buffer.poll()
}