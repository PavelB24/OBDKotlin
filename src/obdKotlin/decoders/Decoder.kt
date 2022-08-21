package obdKotlin.decoders

import obdKotlin.messages.Message
import kotlinx.coroutines.flow.MutableSharedFlow
import obdKotlin.core.WorkMode

abstract class Decoder() {

    //Основная логическая и вычислительная нагрузка тут

    abstract val eventFlow: MutableSharedFlow<Message?>
    abstract suspend fun decode(message: ByteArray, workMode: WorkMode): Boolean


}