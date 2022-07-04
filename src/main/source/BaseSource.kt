package main.source

import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.nio.channels.SocketChannel

abstract class BaseSource {

    abstract fun addSource(inputStream: InputStream, outputStream: OutputStream)

    abstract fun addSource(socket: Socket)

    abstract fun addSource(socketNonBlocking: SocketChannel)

    abstract var sourceMode: SourceMode


}