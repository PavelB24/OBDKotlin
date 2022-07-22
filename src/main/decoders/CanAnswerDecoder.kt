package main.decoders

import kotlinx.coroutines.flow.MutableSharedFlow
import main.messages.Message
import main.messages.OBDDataMessage
import java.util.concurrent.ConcurrentLinkedQueue

class CanAnswerDecoder(private val eventFlow: MutableSharedFlow<Message?>) : PinAnswerDecoder(eventFlow) {


    override val buffer: ConcurrentLinkedQueue<Message>
        get() = TODO("Not yet implemented")


    override suspend fun decode(message: OBDDataMessage): Boolean {
        TODO("Not yet implemented")
    }
}