package obdKotlin.encoders

import kotlinx.coroutines.flow.MutableSharedFlow
import obdKotlin.decoders.Encoder
import obdKotlin.messages.Message

abstract class SpecialEncoder {

    abstract suspend fun handleBytes(message: ByteArray): Boolean

    protected var eventFlow: MutableSharedFlow<Message?>? = null

    fun bindMessagesFlow(flow: MutableSharedFlow<Message?>){
        eventFlow = flow
    }

    companion object{

        fun getInstance(type: SpecialEncoders): Encoder {

        }
    }
}