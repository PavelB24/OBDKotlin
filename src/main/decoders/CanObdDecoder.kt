package main.decoders

import Event
import OBDMessage
import kotlinx.coroutines.flow.MutableStateFlow
import java.nio.channels.SocketChannel

class CanObdDecoder(private val socketEventFlow: MutableStateFlow<Event<OBDMessage?>>): Decoder(socketEventFlow){


    override fun decode(bytes: ByteArray) {
        TODO("Not yet implemented")
    }
}