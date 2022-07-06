package main.source

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.nio.channels.SocketChannel

abstract class Source {


    abstract val inputByteFlow: SharedFlow<ByteArray>

    abstract val outputByteFlow: MutableSharedFlow<ByteArray>

    abstract fun observeByteCommands()

}