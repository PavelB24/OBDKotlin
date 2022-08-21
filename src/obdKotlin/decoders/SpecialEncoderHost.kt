package obdKotlin.decoders

import kotlinx.coroutines.flow.MutableSharedFlow
import obdKotlin.core.WorkMode
import obdKotlin.encoders.SpecialEncoder
import obdKotlin.messages.Message
import java.util.concurrent.atomic.AtomicBoolean

interface SpecialEncoderHost {

    val eventFlow: MutableSharedFlow<Message?>

    val canMode: AtomicBoolean

    suspend fun decode(message: ByteArray, workMode: WorkMode): Boolean

    fun setSpecialEncoder(encoder: SpecialEncoder)
}