package obdKotlin.source

import android.bluetooth.BluetoothSocket
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import obdKotlin.core.SystemEventListener
import java.io.IOException
import java.nio.ByteBuffer

class BluetoothSource(private val socket: BluetoothSocket) : Source() {

    private val input = socket.inputStream
    private val output = socket.outputStream

    override suspend fun observeByteCommands(
        scope: CoroutineScope,
        error: ((SystemEventListener.SourceType) -> Unit)?,
        connect: ((SystemEventListener.SourceType) -> Unit)?
    ) {
        if (connect(error, connect)) {
            scope.launch {
                outputByteFlow.onEach {
                    Log.d("@@@", "SENDING--->>>  " + it.size.toString())
                    sendToSource(it)
                }.collect()
            }
            scope.launch {
                readData(this, error)
            }
        }
    }

    private fun connect(
        error: ((SystemEventListener.SourceType) -> Unit)?,
        connect: ((SystemEventListener.SourceType) -> Unit)?
    ): Boolean {
        return try {
            socket.connect()
            connect?.invoke(SystemEventListener.SourceType.BLUETOOTH)
            true
        } catch (e: Exception) {
            Log.d("@@@", e.stackTraceToString())
            e.printStackTrace()
            error?.invoke(SystemEventListener.SourceType.BLUETOOTH)
            false
        }
    }

    override fun disconnect() {
        try {
            socket.close()
        } catch (e: Exception) {
            e.printStackTrace()
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

    private suspend fun readData(
        job: CoroutineScope,
        error: ((SystemEventListener.SourceType) -> Unit)?
    ) {
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
                error?.invoke(SystemEventListener.SourceType.BLUETOOTH)
                socket.use { }
                Log.d("@@@", e.stackTraceToString())
                e.printStackTrace()
            }
        }
    }

    private suspend fun sendToCommander(buffer: ByteBuffer) {
        buffer.flip()
        val filteredArray = buffer.array().filter {
            it != SPACE_BYTE_VALUE && it != NULL_BYTE_VALUE && it != CR_BYTE_VALUE
        }.toByteArray()
        inputByteFlow.emit(filteredArray)
        buffer.clear()
    }
}
