package obdKotlin

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.test.runTest
import obdKotlin.core.Commander
import obdKotlin.source.WiFiSource
import org.junit.Test
import java.net.InetSocketAddress

class MainTest {

    val obd = Commander.Builder().source(WiFiSource(InetSocketAddress("192.168.1.200", 9999))).build()
    val mutex = Mutex()

    @Test
    fun test() {
        runTest {
            obd.startWithAuto()
            while (true) {
                delay(5000)
            }
        }
    }
}
