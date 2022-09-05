package obdKotlin.encoders

<<<<<<< HEAD
class Yanwar_7_2_Encoder(): SpecialEncoder() {


    override suspend fun handleBytes(message: ByteArray): Boolean {
        TODO("Not yet implemented")
=======
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
>>>>>>> 61257416ebc4218fbd9b3c63ea2dcb4f83c64b4a
    }



<<<<<<< HEAD
=======
    override fun decodeBufferedMessage(): EncodingState {
        TODO("Not yet implemented")
    }


>>>>>>> 61257416ebc4218fbd9b3c63ea2dcb4f83c64b4a
}