package obdKotlin.decoders

import kotlinx.coroutines.flow.MutableSharedFlow
import obdKotlin.messages.Message
import java.util.concurrent.ConcurrentLinkedQueue

class CanAnswerDecoder(private val eventFlow: MutableSharedFlow<Message?>) : PinAnswerDecoder(eventFlow) {

}