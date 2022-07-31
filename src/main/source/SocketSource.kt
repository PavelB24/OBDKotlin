package main.source

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import java.io.*
import java.net.Socket
import java.nio.ByteBuffer

class SocketSource(private val socket: Socket) : Source() {


    override val inputByteFlow: MutableSharedFlow<ByteArray> = MutableSharedFlow()

    override val outputByteFlow: MutableSharedFlow<ByteArray> = MutableSharedFlow()


    private val input = BufferedInputStream(socket.getInputStream())
    private val output = BufferedOutputStream(socket.getOutputStream())


    override suspend fun observeByteCommands() {
        outputByteFlow.onEach { sendToSource(it) }.collect()
    }


    private suspend fun sendToSource(bytes: ByteArray) {
        output.write(bytes)
    }

    /*
        Run this func only in coroutine or new thread
     */
    private suspend fun readData(job: CoroutineScope) {
        val localBuffer = ByteBuffer.allocate(8)
        while (job.isActive) {
            try {
                var readByte: Byte
                do {
                    readByte = input.read().toByte()
                    if (readByte.toInt().toChar() == '>') {
                        break
                    } else {
                        localBuffer.put(readByte)
                    }
                } while (readByte > -1)
                sendToCommander(localBuffer)

            } catch (e: Exception){
                // todo
            }
        }


    }

    private suspend fun sendToCommander(buffer: ByteBuffer) {
        buffer.flip()
        inputByteFlow.emit(buffer.array())
        buffer.clear()
    }


}