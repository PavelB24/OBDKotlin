package obdKotlin.decoders

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import obdKotlin.WorkMode

import org.junit.Assert.*
import org.junit.Test

class AtDecoderTest {

    @Test
    fun decode() {
        val dec = AtDecoder( MutableSharedFlow())
        var res = false
        CoroutineScope(Dispatchers.Unconfined).launch {
             res = dec.decode("elm327".toByteArray(), WorkMode.IDLE)
        }
        Thread.sleep(250)
        assertEquals(true, res)
        var res2 = false
        CoroutineScope(Dispatchers.Unconfined).launch {
            res2 = dec.decode("  ok".toByteArray(), WorkMode.SETTINGS)
        }
        Thread.sleep(250)
        assertEquals(true, res2)
        CoroutineScope(Dispatchers.Unconfined).launch {
            res2 = dec.decode("  ok".toByteArray(), WorkMode.IDLE)
        }
        Thread.sleep(250)
        assertNotEquals(true, res2)
        CoroutineScope(Dispatchers.Unconfined).launch {
            res2 = dec.decode("1".toByteArray(), WorkMode.SETTINGS)
        }
        Thread.sleep(250)
        assertEquals(true, res2)


    }
}