package main.source

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.*
import java.net.Socket
import java.nio.channels.SocketChannel

class Source() : BaseSource() {

    override val workScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _inputByteFlow = MutableSharedFlow<ByteArray>()
    override val inputByteFlow: SharedFlow<ByteArray> = _inputByteFlow

    override val outputByteFlow = MutableSharedFlow<ByteArray>()
    private val _outputByteFlow: SharedFlow<ByteArray> = outputByteFlow


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

    init {
        startObserve()
    }

    override var sourceMode = SourceMode.NONE

    private var socket: Socket? = null

    private var socketNonBlocking: SocketChannel? = null

    private var inputStream: InputStream? = null

    private var outputStream: OutputStream? = null

    private fun startObserve() {
        workScope.launch {
                outputByteFlow.onEach {
                    when (sourceMode) {
                        SourceMode.NONE_BLOCKING_SOCKET -> writeToNonBlockingSocket(it)
                        SourceMode.STREAM -> writeToStream(it)
                        SourceMode.SOCKET ->  writeToStream(it)
                        SourceMode.NONE -> Unit
                    }
                }.collect()
            }
        }

    override fun addSource(inputStream: InputStream, outputStream: OutputStream) {
        this.inputStream = inputStream
        this.outputStream = outputStream
        resetOtherSources()
        observeBlockingSorce()
    }

    override fun addSource(socket: Socket) {
        this.socket = socket
        sourceMode = SourceMode.SOCKET
        resetOtherSources()
        observeBlockingSorce()
    }

    override fun addSource(socketNonBlocking: SocketChannel) {
        this.socketNonBlocking = socketNonBlocking
        sourceMode = SourceMode.NONE_BLOCKING_SOCKET
        resetOtherSources()
        observeNonBlockingSource()
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

}