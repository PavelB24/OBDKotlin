package main.decoders

import kotlinx.coroutines.flow.MutableSharedFlow
import main.commands.PidMod
import main.messages.Message
import main.messages.OBDDataMessage
import main.toHex
import main.commands.*
import java.util.concurrent.ConcurrentLinkedQueue

open class PinAnswerDecoder(private val eventFlow: MutableSharedFlow<Message?>) : Decoder(eventFlow) {

    companion object{
        const val SERVICE_BYTES_SIZE = 2
    }
    override val buffer: ConcurrentLinkedQueue<Message>
        get() = TODO("Not yet implemented")


    override suspend fun decode(message: OBDDataMessage): Boolean {
        val binaryData = message.binaryData
        val dataBytes = binaryData.copyInto(
            ByteArray(binaryData.size - SERVICE_BYTES_SIZE),
            0,
            SERVICE_BYTES_SIZE,
            binaryData.size
        )
        return if (checkAnswer(binaryData.first())) {
            when (binaryData[1].toHex()) {
                pid01 -> {}
                pid05 -> {
                    decodeTemperature(dataBytes)
                }
            }
            true
        } else false
    }

    private fun decodeTemperature(dataBytes: ByteArray) {
        val dataBytes = binaryData.
    }

    private fun checkAnswer(byte: Byte): Boolean {
        val hex = byte.toHex()
        PidMod.values().forEach {
            if (it.positiveCode == hex) {
                return true
            }
        }
        return false
    }
}