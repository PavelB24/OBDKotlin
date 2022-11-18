package obdKotlin.encoders

import kotlinx.coroutines.flow.MutableSharedFlow
import obdKotlin.decoders.EncodingState
import obdKotlin.messages.Message

abstract class Encoder(val eventFlow: MutableSharedFlow<Message?>) {

    abstract suspend fun handleBytes(bytesBody: ByteArray, pid: Int?): EncodingState
}
