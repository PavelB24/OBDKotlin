package obdKotlin.decoders

import kotlinx.coroutines.flow.MutableSharedFlow
<<<<<<< HEAD
import obdKotlin.core.WorkMode
=======
import obdKotlin.WorkMode
>>>>>>> 61257416ebc4218fbd9b3c63ea2dcb4f83c64b4a
import obdKotlin.encoders.SpecialEncoder
import obdKotlin.messages.Message
import java.util.concurrent.atomic.AtomicBoolean

interface SpecialEncoderHost {

    val eventFlow: MutableSharedFlow<Message?>

    val canMode: AtomicBoolean

<<<<<<< HEAD
    suspend fun decode(message: ByteArray, workMode: WorkMode): Boolean
=======
    suspend fun decode(message: ByteArray, workMode: WorkMode): EncodingState
>>>>>>> 61257416ebc4218fbd9b3c63ea2dcb4f83c64b4a

    fun setSpecialEncoder(encoder: SpecialEncoder)
}