package main.decoders

import Event
import OBDMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.ConcurrentLinkedQueue

abstract class Decoder(socketEventFlow: MutableSharedFlow<Event<OBDMessage?>>) {

    abstract val buffer: ConcurrentLinkedQueue<ByteArray>()
    //Основная логическая и вычислительная нагрузка тут
    abstract fun decode(bytes: ByteArray)


}