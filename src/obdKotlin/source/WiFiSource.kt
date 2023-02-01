package obdKotlin.source

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import obdKotlin.core.SystemEventListener
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
        private const val TIMEOUT = 2000L
        private const val RECEIVE_BUFFER_SIZE = 4098
    }

    private val receiveBuffer: ByteBuffer = ByteBuffer.allocate(RECEIVE_BUFFER_SIZE)

    private val selector: Selector = Selector.open()

    private val socket: SocketChannel = SocketChannel.open()

    override suspend fun observeByteCommands(
        scope: CoroutineScope,
        error: ((SystemEventListener.SourceType) -> Unit)?,
        connect: ((SystemEventListener.SourceType) -> Unit)?
    ) {
        scope.launch {
            connect(error, connect)
            init(this, error)
        }
        scope.launch {
            outputByteFlow.onEach {
                if (socket.isOpen) {
                    sendToSource(it)
                } else {
                    delay(TIMEOUT)
                    if (!socket.isOpen) {
                        error?.invoke(SystemEventListener.SourceType.WIFI)
                    } else sendToSource(it)
                }
            }.collect()
        }
    }

    override fun disconnect() {
        try {
            socket.close()
            selector.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun init(
        scope: CoroutineScope,
        error: ((SystemEventListener.SourceType) -> Unit)?
    ) {
        while (scope.isActive && socket.isConnected) {
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
                selector.close()
                socket.close()
                error?.invoke(SystemEventListener.SourceType.WIFI)
                e.printStackTrace()
            }
        }
        try {
            selector.close()
            socket.close()
        } catch (ignored: Exception) {}
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
                try {
                    Log.d("@@@", "WRITING ${dataBuffer.array().size}")
                    bytes = socket.write(dataBuffer)
                } catch (e: Exception) {
                    if (socket.isConnected) {
                        socket.close()
                    }
                    e.printStackTrace()
                    break
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun connect(
        error: ((SystemEventListener.SourceType) -> Unit)?,
        connect: ((SystemEventListener.SourceType) -> Unit)?
    ) {
        try {
            socket.configureBlocking(false)
            socket.register(selector, SelectionKey.OP_CONNECT or SelectionKey.OP_READ)
            socket.connect(address)
            connect?.invoke(SystemEventListener.SourceType.WIFI)
        } catch (e: IOException) {
            e.printStackTrace()
            error?.invoke(SystemEventListener.SourceType.WIFI)
        }
    }
}
