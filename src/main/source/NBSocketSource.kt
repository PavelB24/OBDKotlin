package main.source

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.nio.channels.SocketChannel

class NBSocketSource(val socket: SocketChannel): Source() {

    override val inputByteFlow: SharedFlow<ByteArray>


    override val outputByteFlow: MutableSharedFlow<ByteArray>



    override var sourceMode: SourceMode
        get() = TODO("Not yet implemented")
        set(value) {}


}