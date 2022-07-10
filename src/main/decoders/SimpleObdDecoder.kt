package main.decoders

import Event
import OBDMessage
import kotlinx.coroutines.flow.MutableStateFlow

class SimpleObdDecoder(private val socketEventFlow: MutableStateFlow<Event<OBDMessage>>): Decoder(socketEventFlow) {
}