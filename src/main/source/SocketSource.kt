package main.source

import kotlinx.coroutines.flow.*
import java.io.*
import java.net.Socket
import java.nio.channels.SocketChannel

class SocketSource(val socket: Socket) : Source() {



    override val inputByteFlow: SharedFlow<ByteArray>

    override val outputByteFlow: MutableSharedFlow<ByteArray>
        get() = TODO("Not yet implemented")



    override var sourceMode: SourceMode
        get() = TODO("Not yet implemented")
        set(value) {}



}