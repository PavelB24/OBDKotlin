package obdKotlin.encoders

import kotlinx.coroutines.flow.MutableSharedFlow
import obdKotlin.decoders.EncodingState
import obdKotlin.decoders.PinAnswerDecoder
import obdKotlin.hexToBinary
import obdKotlin.hexToBinaryList
import obdKotlin.messages.Message
import obdKotlin.toOneCharHex
import java.lang.StringBuilder
import java.nio.charset.Charset

class TroubleCodesEncoder(private val eventFlow: MutableSharedFlow<Message?>) : Encoder(eventFlow) {

    private val strBuilder = StringBuilder()
    private val codesHash = mutableListOf<String>()


    override suspend fun handleBytes(bytesBody: ByteArray, pid: String?): EncodingState {
        val isLast = bytesBody.last() == PinAnswerDecoder.END_BYTE
        val limit = if (isLast) bytesBody.size - 1 else bytesBody.size
        val answer = bytesBody.decodeToString(0, limit).chunked(4)
        if (answer.size % 2 > 0) {
            return EncodingState.UNSUCCESSFUL
        }
        answer.forEach{
            if (it != "0000") {
                when(it.first().uppercaseChar()){
                    '0' ->{strBuilder.append("P0")}
                    '1' ->{strBuilder.append("P1")}
                    '2' ->{strBuilder.append("P2")}
                    '3' ->{strBuilder.append("P3")}
                    '4' ->{strBuilder.append("C0")}
                    '5' ->{strBuilder.append("C1")}
                    '6' ->{strBuilder.append("C2")}
                    '7' ->{strBuilder.append("C3")}
                    '8' ->{strBuilder.append("B0")}
                    '9' ->{strBuilder.append("B1")}
                    'A' ->{strBuilder.append("B2")}
                    'B' ->{strBuilder.append("B3")}
                    'C' ->{strBuilder.append("U0")}
                    'D' ->{strBuilder.append("U1")}
                    'E' ->{strBuilder.append("U2")}
                    'F' ->{strBuilder.append("U3")}
                }
                strBuilder.append(it.subSequence(1, it.length))
                codesHash.add(strBuilder.toString())
                strBuilder.clear()
            }
        }
        return if (isLast){
            eventFlow.emit(Message.TroubleCodes(codesHash))
            codesHash.clear()
            EncodingState.SUCCESSFUL
        } else EncodingState.WAIT_NEXT
    }
}