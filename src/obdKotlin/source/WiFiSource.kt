package obdKotlin.source

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import kotlin.jvm.Throws

class WiFiSource(
    private val address: InetSocketAddress
) : Source() {

    companion object {
        private const val TIMEOUT = 500L
        private const val RECEIVE_BUFFER_SIZE = 4098
    }

    private val receiveBuffer: ByteBuffer = ByteBuffer.allocate(RECEIVE_BUFFER_SIZE)

    private val selector: Selector = Selector.open()

    private val socket: SocketChannel = SocketChannel.open()

    override suspend fun observeByteCommands(scope: CoroutineScope, error: (() -> Unit)?) {
        scope.launch {
            outputByteFlow.onEach {
                sendToSource(it)
            }.collect()
        }
        scope.launch {
            connect()
            init(this, error)
        }
    }

    private suspend fun init(scope: CoroutineScope, error: (() -> Unit)?) {
        while (scope.isActive) {
            try {
                selector.select(TIMEOUT)
                val selectedKeys = selector.selectedKeys()
                val keys = selectedKeys.iterator()
                while (keys.hasNext()) {
                    val selectionKey = keys.next()
                    when {
                        selectionKey.isReadable && selectionKey.isValid -> {
                            readFromSocket()
                            selectionKey.interestOps(SelectionKey.OP_READ)
                        }

                        selectionKey.isConnectable && selectionKey.isValid -> {
                            socket.finishConnect()
                            selectionKey.interestOps(SelectionKey.OP_READ)
                        }
                    }
                    keys.remove()
                }
            } catch (e: Exception) {
                error?.invoke()
                e.printStackTrace()
            }
        }
    }

    private suspend fun readFromSocket() {
        receiveBuffer.clear()
        val read = socket.read(receiveBuffer)
        if (read != -1) {
            val data = ByteArray(read)
            receiveBuffer.flip()
            System.arraycopy(receiveBuffer.array(), 0, data, 0, read)
            data.filter {
                it != SPACE_BYTE_VALUE && it != NULL_BYTE_VALUE && it != CR_BYTE_VALUE
            }.toByteArray()
            inputByteFlow.emit(data)
        }
    }

    private suspend fun sendToSource(command: ByteArray) {
        command.let {
            val dataBuffer: ByteBuffer = ByteBuffer.allocate(it.size)
            dataBuffer.put(it)
            dataBuffer.flip()
            var bytes = 1
            while (dataBuffer.hasRemaining() && bytes > 0) {
                bytes = socket.write(dataBuffer)
            }
        }
    }

    @Throws(IOException::class)
    private fun connect() {
        try {
            socket.configureBlocking(false)
            socket.register(selector, SelectionKey.OP_CONNECT or SelectionKey.OP_READ)
            socket.connect(address)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
