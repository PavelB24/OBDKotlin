package obdKotlin.decoders

import obdKotlin.messages.Message
import kotlinx.coroutines.flow.MutableSharedFlow
import obdKotlin.WorkMode

abstract class Decoder( eventFlow: MutableSharedFlow<Message?>) {

    //Основная логическая и вычислительная нагрузка тут
    abstract suspend fun decode(message: ByteArray, workMode: WorkMode): Boolean


}