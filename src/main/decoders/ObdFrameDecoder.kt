package main.decoders

import Event
import OBDMessage
import kotlinx.coroutines.flow.MutableStateFlow

class ObdFrameDecoder(private val socketEventFlow: MutableStateFlow<Event<OBDMessage>>): Decoder(socketEventFlow) {


    override fun decode(bytes: ByteArray) {
        TODO("Not yet implemented")
    }

    fun isPositiveOBDAnswer(bytes: ByteArray): Boolean{
       return bytes.decodeToString() == "OK"

    }
}