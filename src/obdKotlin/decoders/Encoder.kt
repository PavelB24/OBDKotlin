package obdKotlin.decoders

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import obdKotlin.messages.Message

abstract class Encoder(eventFlow: MutableSharedFlow<Message?>) {

    abstract suspend fun handleBytes( bytesBody: ByteArray,  pid: String): Boolean

}