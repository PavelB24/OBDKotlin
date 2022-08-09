package obdKotlin.decoders

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import obdKotlin.WorkMode
import org.junit.Test

import org.junit.Assert.*

class PinAnswerDecoderTest {

    @Test
    fun decode() {
        val decoder = PinAnswerDecoder(MutableSharedFlow())
        var result = false
        CoroutineScope(Dispatchers.IO).launch {
             result = decoder.decode(byteArrayOf(1,5,6,88,7), WorkMode.IDLE)
        }
        Thread.sleep(250)
        assertEquals(false, result)
        CoroutineScope(Dispatchers.IO).launch {
            result = decoder.decode(byteArrayOf(65,5, 120), WorkMode.IDLE)
        }
        Thread.sleep(250)
        assertEquals(true, result)

    }
}