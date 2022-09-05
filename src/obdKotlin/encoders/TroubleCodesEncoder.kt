package obdKotlin.encoders

import kotlinx.coroutines.flow.MutableSharedFlow
import obdKotlin.decoders.EncodingState
import obdKotlin.decoders.PinAnswerDecoder
import obdKotlin.hexToBinary
import obdKotlin.hexToBinaryList
import obdKotlin.messages.Message

class TroubleCodesEncoder(private val eventFlow: MutableSharedFlow<Message?>): Encoder(eventFlow) {


    override suspend fun handleBytes(bytesBody: ByteArray, pid: String?): EncodingState {
        val isLast = bytesBody.last() == PinAnswerDecoder.END_BYTE
        val limit = if (isLast) bytesBody.size - 1 else bytesBody.size
        val answer = bytesBody.decodeToString(0, limit).chunked(2)
        answer.forEach {
            if(it != "00"){
                val bin = it.hexToBinary()

            }
        }

    }
}