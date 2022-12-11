package obdKotlin.source

import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException
import java.nio.ByteBuffer

class BluetoothSource(private val socket: BluetoothSocket) : Source() {

    private val input = socket.inputStream
    private val output = socket.outputStream

    override suspend fun observeByteCommands(scope: CoroutineScope, error: (() -> Unit)?) {
        scope.launch {
            outputByteFlow.onEach {
                sendToSource(it)
            }.collect()
        }
        scope.launch {
            readData(this, error)
        }
    }

    private suspend fun sendToSource(bytes: ByteArray) {
        if (socket.isConnected) {
            try {
                output.write(bytes)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun readData(job: CoroutineScope, error: (() -> Unit)?) {
        while (job.isActive) {
            val localBuffer: ByteBuffer = ByteBuffer.allocate(64)
            try {
                while (socket.isConnected) {
                    val readUByte = input.read()
                    if (readUByte.toChar() == '>') {
                        localBuffer.put(readUByte.toByte())
                        break
                    } else if (readUByte.toByte() == END_VALUE) {
                        break
                    } else {
                        localBuffer.put(readUByte.toByte())
                    }
                }
                sendToCommander(localBuffer)
            } catch (e: IOException) {
                error?.invoke()
                e.printStackTrace()
            }
        }
    }

    private suspend fun sendToCommander(buffer: ByteBuffer) {
        buffer.flip()
        val array = buffer.array()
        val filteredArray = array.filter {
            it != SPACE_BYTE_VALUE && it != NULL_BYTE_VALUE && it != CR_BYTE_VALUE
        }.toByteArray()
        inputByteFlow.emit(filteredArray)
        buffer.clear()
    }
}
