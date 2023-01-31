package obdKotlin

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.test.runTest
import obdKotlin.core.Commander
import org.junit.Test

class MainTest {

    val obd = Commander.Builder().build()
    val mutex = Mutex()

    @Test
    fun test() {
        runTest {
//           obd.bindSource(BluetoothSource(BluetoothSocket()))
            while (true) {
                delay(5000)
            }
        }
    }
}
