package main.source

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.nio.channels.SocketChannel

class Source() : BaseSource() {

    val inputByteFlow = MutableSharedFlow<ByteArray>()

    val outputByteFlow = MutableSharedFlow<ByteArray>()


    private var scope: CoroutineScope? = null

    constructor(inputStream: InputStream, outputStream: OutputStream) : this() {
        this.inputStream = inputStream
        this.outputStream = outputStream
        sourceMode = SourceMode.STREAM
    }

    constructor(socket: Socket) : this() {
        this.socket = socket
        sourceMode = SourceMode.SOCKET
    }

    constructor(socketNonBlocking: SocketChannel) : this() {
        this.socketNonBlocking = socketNonBlocking
        sourceMode = SourceMode.NONE_BLOCKING_SOCKET
    }

    override var sourceMode = SourceMode.NONE

    private var socket: Socket? = null

    private var socketNonBlocking: SocketChannel? = null

    private var inputStream: InputStream? = null

    private var outputStream: OutputStream? = null

    private fun startObserve() {
        scope?.let {
            it.launch {
                inputByteFlow.onEach {
                    when (sourceMode) {
                        SourceMode.NONE_BLOCKING_SOCKET -> TODO()
                        SourceMode.STREAM -> TODO()
                        SourceMode.SOCKET -> {
                            socket?.let {
                                val buffer = DataInputStream(it.getInputStream())
                                buffer.readAllBytes()
                            }
                        }
                        SourceMode.NONE -> TODO()
                    }
                }.collect()
                it.launch {
                    outputByteFlow.onEach {
                        when (sourceMode) {
                            SourceMode.NONE_BLOCKING_SOCKET -> TODO()
                            SourceMode.STREAM -> TODO()
                            SourceMode.SOCKET -> {
                            }
                            SourceMode.NONE -> TODO()
                        }
                    }.collect()
                }
            }
        }
    }

    override fun addSource(inputStream: InputStream, outputStream: OutputStream) {
        this.inputStream = inputStream
        this.outputStream = outputStream
        resetOtherSources()
    }

    override fun addSource(socket: Socket) {
        this.socket = socket
        sourceMode = SourceMode.SOCKET
        resetOtherSources()
    }

    override fun addSource(socketNonBlocking: SocketChannel) {
        this.socketNonBlocking = socketNonBlocking
        sourceMode = SourceMode.NONE_BLOCKING_SOCKET
        resetOtherSources()
    }

    private fun resetOtherSources() {
        when (sourceMode) {
            SourceMode.SOCKET -> {
                inputStream = null
                outputStream = null
                socketNonBlocking = null
            }
            SourceMode.STREAM -> {
                sourceMode = SourceMode.STREAM
                socket = null
                socketNonBlocking = null
            }
            SourceMode.NONE_BLOCKING_SOCKET -> {
                inputStream = null
                outputStream = null
                socket = null
            }
        }
    }

    fun sendScope(commanderScope: CoroutineScope) {
        this.scope = commanderScope
    }
}