package obdKotlin.source

import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

@SuppressWarnings("Unresolved reference")
class BluetoothSource(private val socket: BluetoothSocket) : Source() {

    private val input = socket.inputStream
    private val output = socket.outputStream

    override suspend fun observeByteCommands(job: CoroutineScope) {
        outputByteFlow.onEach { sendToSource(it) }.collect()
        readData(job)
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

    private suspend fun readData(job: CoroutineScope) {
        while (job.isActive) {
            var localBuffer: ByteBuffer? = null
            try {
                val capacity = input.available()
                while (socket.isConnected && capacity > 0) {
                    localBuffer = ByteBuffer.allocate(capacity)
                    localBuffer.order(ByteOrder.BIG_ENDIAN)
                    val readUByte = input.read()
                    if (readUByte.toChar() == '>') {
                        break
                    } else {
                        localBuffer.put(readUByte.toByte())
                    }
                }
                localBuffer?.let {
                    sendToCommander(it)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun sendToCommander(buffer: ByteBuffer) {
        buffer.flip()
        val array = buffer.array()
        val cc: Byte = 13
        val filteredArray = array.filter {
            it != cc
        }.toByteArray()
        inputByteFlow.emit(filteredArray)
        buffer.clear()
    }
}
