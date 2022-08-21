package obdKotlin.protocol

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.junit.Test

import org.junit.Assert.*
import java.lang.IllegalStateException

class ProtocolManagerTest {

    private val manager = ProtocolManager()

    @Test
    fun getObdCommandFlow() {
        assertEquals(manager.obdCommandFlow, manager.obdCommandFlow )
    }

    @Test
    fun handleAnswer() {
        var exc: IllegalStateException? = null
        CoroutineScope(Dispatchers.Unconfined).launch {
           try{ manager.handleAnswer()
           } catch (e: IllegalStateException){
               exc = e
           }
        }
        Thread.sleep(200)
        assertSame(IllegalStateException::class.java, exc!!::class.java)

    }

    @Test
    fun setSettingWithParameter() {
    }

    @Test
    fun askObdProto() {
    }

    @Test
    fun resetStates() {
        assertEquals(Unit, manager.resetStates())
    }

    @Test
    fun askCurrentProto() {
    }

    @Test
    fun onRestart() {
        CoroutineScope(Dispatchers.IO).launch {
            manager.onRestart(ProtocolManagerStrategy.AUTO,)
        }

    }

    @Test
    fun setHeaderAndReceiver() {
    }

    @Test
    fun startWithProfile() {
    }

    @Test
    fun testStartWithProfile() {
    }

    @Test
    fun reset() {
    }

    @Test
    fun isLastSettingSend() {
    }

    @Test
    fun sendNextSettings() {
    }

    @Test
    fun setSetting() {
    }

    @Test
    fun switchProtocol() {
    }

    @Test
    fun checkIfCanProto() {
    }
}