package obdKotlin.decoders

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import obdKotlin.*
import obdKotlin.commands.*
import obdKotlin.encoders.CurrentDataEncoder
import obdKotlin.encoders.SpecialEncoder
import obdKotlin.encoders.TroubleCodesEncoder
import obdKotlin.messages.Message
import java.util.concurrent.atomic.AtomicBoolean


internal class PinAnswerDecoder() : Decoder(), SpecialEncoderHost {

    companion object{
        private const val REPLAY = 1
        private const val BUFFER_CAPACITY = 100
        const val END_BYTE: Byte = 62
    }

    override val eventFlow: MutableSharedFlow<Message?> = MutableSharedFlow(
        REPLAY,
        BUFFER_CAPACITY,
        BufferOverflow.SUSPEND
    )

    override val canMode = AtomicBoolean(false)

    private val currentDataEncoder by lazy { CurrentDataEncoder(eventFlow) }

    private val troubleCodesEncoder by lazy {TroubleCodesEncoder(eventFlow)}

    private var specialEncoder: SpecialEncoder? = null

    /**
     * Empty spaces and cariege return filtration should be in source class
     */

    override suspend fun decode(message: ByteArray, workMode: WorkMode): EncodingState {

            if (message.decodeToString().contains("?")){
                return EncodingState.UNSUCCESSFUL
            }
            val pid = message.decodeToString(2, 4)
            val bytesCurrentBody = message.copyOfRange(4, message.size-1)

            return when (checkAnswer(message)) {
                Commands.PidMod.SHOW_CURRENT -> {
                    currentDataEncoder.handleBytes(bytesCurrentBody, pid)
                }

                Commands.PidMod.SHOW_FREEZE_FRAME -> TODO()
                Commands.PidMod.SHOW_DIAGNOSTIC_TROUBLES_CODES -> {
                     troubleCodesEncoder.handleBytes(
                        message.copyOfRange(4, message.size),
                        pid)

                }
                Commands.PidMod.CLEAR_TROUBLES_CODES_AND_STORE_VAL -> TODO()
                Commands.PidMod.TEST_RESULTS_OXY_SENSORS -> TODO()
                Commands.PidMod.TEST_RESULTS_OXY_SENSORS_CAN -> TODO()
                Commands.PidMod.SHOW_PENDING_DIAGN_TROUBLES_CODES -> TODO()
                Commands.PidMod.CONTROL_ON_BOARD_SYS -> TODO()
                Commands.PidMod.VEHICLE_INFO_REQUEST -> TODO()
                Commands.PidMod.DELETED_ERRORS -> TODO()
                Commands.PidMod.CHECK_ON_CAN -> {
                    if (canMode.get() && specialEncoder != null){
                        specialEncoder!!.handleBytes(message)
                    } else EncodingState.UNSUCCESSFUL
                }
                null -> EncodingState.UNSUCCESSFUL
            }
        }

    private fun checkAnswer(message: ByteArray): Commands.PidMod? {
        val allMessage = message.decodeToString()
        val hexEcho = allMessage.take(2)
        if (allMessage.contains("No data", true)) {
            return null
        }
        val mode = Commands.PidMod.values().find {
            it.positiveCode == hexEcho
        }
        return mode ?: Commands.PidMod.CHECK_ON_CAN
    }

    override fun setSpecialEncoder(encoder: SpecialEncoder){
        canMode.set(true)
        encoder.bindMessagesFlow(eventFlow)
        specialEncoder = encoder
    }

}