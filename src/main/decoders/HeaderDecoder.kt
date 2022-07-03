package main.decoders

import Event
import OBDMessage
import kotlinx.coroutines.flow.MutableStateFlow

class HeaderDecoder(private val socketEventFlow: MutableStateFlow<Event<OBDMessage>>): Decoder(socketEventFlow){


    override fun decode(bytes: ByteArray) {
        TODO("Not yet implemented")
    }
}