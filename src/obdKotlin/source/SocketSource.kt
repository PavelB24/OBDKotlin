package obdKotlin.source

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import java.io.IOException
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.ByteOrder

class SocketSource(private val socket: Socket) : Source() {

    private val input = socket.getInputStream()
    private val output = socket.getOutputStream()

    override suspend fun observeByteCommands() {
        outputByteFlow.onEach { sendToSource(it) }.collect()
    }

    private suspend fun sendToSource(bytes: ByteArray) {
        if (socket.isConnected) {
            try {
                output.write(bytes)
            } catch (e: IOException) {
                // todo
            }
        }
    }

    /*
        Run this func only in coroutine or new thread
     */
    private suspend fun readData(job: CoroutineScope) {
        while (job.isActive) {
            var localBuffer: ByteBuffer? = null
            try {
                var capacity = input.available()
                while (socket.isConnected && capacity > 0) {
                    localBuffer = ByteBuffer.allocate(capacity)
                    localBuffer.order(ByteOrder.BIG_ENDIAN)
                    val readUByte = input.read()
                    if (readUByte.toChar() == '>') {
                        capacity = 0
                        break
                    } else {
                        localBuffer.put(readUByte.toByte())
                    }
                }
                localBuffer?.let {
                    sendToCommander(it)
                }
            } catch (e: IOException) {
                // todo
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
