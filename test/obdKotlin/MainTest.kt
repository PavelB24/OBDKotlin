package obdKotlin

import kotlinx.coroutines.test.runTest
import obdKotlin.core.Commander
import obdKotlin.source.WiFiSource
import org.junit.Test
import java.net.InetSocketAddress

class MainTest {

    val obd = Commander.Builder().source(WiFiSource(InetSocketAddress("", 1))).build()

    @Test
    fun test() {
        runTest {
            obd.startWithAuto()
        }
    }
}
