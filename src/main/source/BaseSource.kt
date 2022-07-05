package main.source

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.nio.channels.SocketChannel

abstract class BaseSource {

    abstract val workScope: CoroutineScope

    abstract val inputByteFlow: SharedFlow<ByteArray>

    abstract val outputByteFlow: MutableSharedFlow<ByteArray>

    abstract fun addSource(inputStream: InputStream, outputStream: OutputStream)

    abstract fun addSource(socket: Socket)

    abstract fun addSource(socketNonBlocking: SocketChannel)

    abstract var sourceMode: SourceMode

    abstract fun cancelScope()


}