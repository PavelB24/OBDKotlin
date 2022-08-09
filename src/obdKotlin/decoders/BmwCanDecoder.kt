package obdKotlin.decoders

import kotlinx.coroutines.flow.MutableSharedFlow
import obdKotlin.WorkMode
import obdKotlin.messages.Message

class BmwCanDecoder(private val eventFlow: MutableSharedFlow<Message?>): PinAnswerDecoder(eventFlow) {

    override suspend fun decode(message: ByteArray, workMode: WorkMode): Boolean {
        return super.decode(message, workMode)
    }
}