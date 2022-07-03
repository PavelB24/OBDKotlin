package main.decoders

import Event
import OBDMessage
import kotlinx.coroutines.flow.MutableStateFlow

abstract class Decoder(socketEventFlow: MutableStateFlow<Event<OBDMessage>>) {

    //Основная логическая и вычислительная нагрузка тут
    abstract fun decode(bytes: ByteArray)


}