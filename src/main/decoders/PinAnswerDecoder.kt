package main.decoders

import kotlinx.coroutines.flow.MutableSharedFlow
import main.OBDCommander
import main.messages.Message
import main.messages.OBDDataMessage
import java.util.concurrent.ConcurrentLinkedQueue

open class PinAnswerDecoder(private val eventFlow: MutableSharedFlow<Message?>): Decoder() {

    override val buffer: ConcurrentLinkedQueue<Message>
        get() = TODO("Not yet implemented")

    override suspend fun decode(message: OBDDataMessage): Boolean {
        TODO("Not yet implemented")
    }
}