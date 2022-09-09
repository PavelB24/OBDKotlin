package obdKotlin.encoders

import obdKotlin.decoders.EncodingState
import obdKotlin.hexToInt
import obdKotlin.toHex


class Yanwar_7_2_Encoder(): SpecialEncoder() {

    var messageCount = 0


    override suspend fun handleBytes(message: ByteArray): EncodingState {
        return super.handleBytes(message)
    }

    override suspend fun decodeSingleMessage(message: ByteArray): EncodingState {
        val dataBytes = message[1].toHex().hexToInt() * 2
        val cleanData = message.decodeToString(2, dataBytes)
        return EncodingState.SUCCESSFUL
    }

    override suspend fun decodeBufferedMessage(): EncodingState {
        TODO("Not yet implemented")
    }


}