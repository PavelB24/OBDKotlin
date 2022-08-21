package obdKotlin.decoders

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import obdKotlin.commands.*
import obdKotlin.core.WorkMode
import obdKotlin.encoders.SpecialEncoder
import obdKotlin.messages.Message
import java.util.concurrent.atomic.AtomicBoolean


internal class PinAnswerDecoder() : Decoder(), SpecialEncoderHost {

    companion object{
        private const val REPLAY = 1
        private const val BUFFER_CAPACITY = 100
    }

    override val eventFlow: MutableSharedFlow<Message?> = MutableSharedFlow(
        REPLAY,
        BUFFER_CAPACITY,
        BufferOverflow.SUSPEND
    )

    override val canMode = AtomicBoolean(false)

    private val currentDataEncoder by lazy {CurrentDataEncoder(eventFlow)}

    private var specialEncoder: SpecialEncoder? = null





    /**
     * Empty spaces and cariege return filtration should be in source class
     */

    override suspend fun decode(message: ByteArray, workMode: WorkMode): Boolean {

        if (canMode.get()){
            specialEncoder?.let {
               return it.handleBytes(message)
            }
            return false
        } else {

            val pid = message.decodeToString(2, 4)
            val bytesBody = message.copyOfRange(4, message.size)

            return when (checkAnswer(message)) {
                Commands.PidMod.SHOW_CURRENT -> {
                    currentDataEncoder.handleBytes(bytesBody, pid)
                }

                Commands.PidMod.SHOW_FREEZE_FRAME -> TODO()
                Commands.PidMod.SHOW_DIAGNOSTIC_TROUBLES_CODES -> TODO()
                Commands.PidMod.CLEAR_TROUBLES_CODES_AND_STORE_VAL -> TODO()
                Commands.PidMod.TEST_RESULTS_OXY_SENSORS -> TODO()
                Commands.PidMod.TEST_RESULTS_OXY_SENSORS_CAN -> TODO()
                Commands.PidMod.SHOW_PENDING_DIAGN_TROUBLES_CODES -> TODO()
                Commands.PidMod.CONTROL_ON_BOARD_SYS -> TODO()
                Commands.PidMod.VEHICLE_INFO_REQUEST -> TODO()
                Commands.PidMod.DELETED_ERRORS -> TODO()
                null -> false
            }
        }
    }

    private fun checkAnswer(message: ByteArray): Commands.PidMod? {
        val allMessage = message.decodeToString()
        val hexEcho = allMessage.take(2)
        if (allMessage.contains("data", true)) {
            return null
        }
        val mode = Commands.PidMod.values().find {
            it.positiveCode == hexEcho
        }
        return mode
    }

    override fun setSpecialEncoder(encoder: SpecialEncoder){
        canMode.set(true)
        encoder.bindMessagesFlow(eventFlow)
        specialEncoder = encoder
    }

}