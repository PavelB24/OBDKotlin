package obdKotlin.encoders

import kotlinx.coroutines.flow.MutableSharedFlow
import obdKotlin.decoders.EncodingState
import obdKotlin.hexToInt
import obdKotlin.messages.Message
import obdKotlin.toHex
import java.lang.IllegalStateException
import java.nio.ByteBuffer

abstract class SpecialEncoder {

    var buffer: ByteBuffer? = null

    var cc = 0
    var expectedFrames = 0
    var eventFlow: MutableSharedFlow<Message?>? = null

    open suspend fun handleBytes(message: ByteArray): EncodingState {
        return when (message.first().toHex()) {
            ByteMessageType.SINGLE.hex -> {
                decodeSingleMessage(message)
            }
            ByteMessageType.FIRST.hex -> {
                decodeFirstMessage(message)
            }
            ByteMessageType.SECONDARY.hex -> {
                decodeSecondaryMessage(message)
            }

            else -> { EncodingState.Unsuccessful(message.decodeToString()) }
        }
    }

    abstract suspend fun decodeSingleMessage(message: ByteArray): EncodingState

    private suspend fun decodeFirstMessage(message: ByteArray): EncodingState {
        if (message.size > 16) return EncodingState.Unsuccessful(message.decodeToString())
        val expect = message.decodeToString(1, 4).hexToInt()
        expectedFrames = if ((expect - 6) % 7 == 0) (expect - 6) / 7 else (expect - 6) / 7 + 1
        buffer = ByteBuffer.allocate(expect * 2)
        val cleanData = message.copyOfRange(5, message.size)
        buffer?.put(cleanData)
        cc += cleanData.size
        return EncodingState.WaitNext
    }

    private suspend fun decodeSecondaryMessage(message: ByteArray): EncodingState {
        if (buffer == null) {
            throw IllegalStateException()
        } else {
            buffer?.put(message.copyOfRange(3, message.size))
            buffer?.let {
                return if (it.hasRemaining()) {
                    EncodingState.WaitNext
                } else {
                    decodeBufferedMessage()
                }
            }
            throw IllegalStateException("Message Buffer is null")
        }
    }

    abstract suspend fun decodeBufferedMessage(): EncodingState

    fun bindMessagesFlow(flow: MutableSharedFlow<Message?>) {
        eventFlow = flow
    }

//    companion object{
//
//        fun getInstance(type: SpecialEncoders): Encoder {
//            return when(type){
//
//            }
//        }
//    }
}
