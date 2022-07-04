package main.decoders

import Event
import OBDMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

abstract class Decoder(socketEventFlow: MutableSharedFlow<Event<OBDMessage?>>) {

    //Основная логическая и вычислительная нагрузка тут
    abstract fun decode(bytes: ByteArray)


}