package main.decoders

import main.messages.Message
import kotlinx.coroutines.flow.MutableSharedFlow
import main.messages.OBDDataMessage
import java.util.concurrent.ConcurrentLinkedQueue

abstract class Decoder(eventFlow: MutableSharedFlow<Message?>) {

    abstract val buffer: ConcurrentLinkedQueue<Message>
    //Основная логическая и вычислительная нагрузка тут
    abstract suspend fun decode(message: OBDDataMessage): Boolean


}