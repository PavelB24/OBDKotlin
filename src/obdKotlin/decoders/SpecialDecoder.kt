package obdKotlin.decoders

import kotlinx.coroutines.flow.MutableSharedFlow
import obdKotlin.WorkMode
import obdKotlin.messages.Message

class SpecialDecoder(val eventFlow: MutableSharedFlow<Message?>): Decoder(eventFlow) {


    override suspend fun decode(message: ByteArray, workMode: WorkMode): Boolean {
        TODO("Not yet implemented")
    }


}