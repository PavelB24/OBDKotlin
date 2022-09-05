package obdKotlin.decoders

import obdKotlin.messages.Message
import kotlinx.coroutines.flow.MutableSharedFlow
import obdKotlin.core.WorkMode

abstract class Decoder() {

    //Основная логическая и вычислительная нагрузка тут

    abstract val eventFlow: MutableSharedFlow<Message?>
<<<<<<< HEAD
    abstract suspend fun decode(message: ByteArray, workMode: WorkMode): Boolean
=======
    abstract suspend fun decode(message: ByteArray, workMode: WorkMode): EncodingState
>>>>>>> 61257416ebc4218fbd9b3c63ea2dcb4f83c64b4a


}