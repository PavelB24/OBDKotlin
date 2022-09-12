package obdKotlin.decoders

import kotlinx.coroutines.flow.MutableSharedFlow
import obdKotlin.WorkMode
import obdKotlin.messages.Message

abstract class Decoder() {

    // Основная логическая и вычислительная нагрузка тут

    abstract val eventFlow: MutableSharedFlow<Message?>
    abstract suspend fun decode(message: ByteArray, workMode: WorkMode): EncodingState
}
