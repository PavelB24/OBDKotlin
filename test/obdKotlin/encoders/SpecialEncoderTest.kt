package obdKotlin.encoders

import kotlinx.coroutines.test.runTest
import obdKotlin.hexToInt
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before

class SpecialEncoderTest {

    var dec = Yanwar_7_2_Encoder()

    @Before
    fun newObject(){
        dec = Yanwar_7_2_Encoder()
    }

    @Test
    fun handleBytes() {
    }

    @Test
    fun decodeSingleMessage() {
    }

    @Test
    fun decodeFirstMessage() = runTest {
        val message = "1020666666666666".toByteArray(Charsets.US_ASCII)
        print("Size: ${message.decodeToString(1,4)} : ${message.decodeToString(1,4).hexToInt()} \n")
        dec.decodeFirstMessage(message)
        print("Buffer len: ${dec.buffer?.capacity()} Expect: ${dec.expectedFrames}")
    }

    @Test
    fun decodeSecondaryMessage() {
    }

    @Test
    fun bindMessagesFlow() {
    }
}