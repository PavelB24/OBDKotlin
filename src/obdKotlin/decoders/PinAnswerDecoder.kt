package obdKotlin.decoders

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import obdKotlin.WorkMode
import obdKotlin.commands.Commands
import obdKotlin.encoders.CurrentDataEncoder
import obdKotlin.encoders.SpecialEncoder
import obdKotlin.encoders.TroubleCodesEncoder
import obdKotlin.messages.Message
import java.util.concurrent.atomic.AtomicBoolean

internal class PinAnswerDecoder() : Decoder(), SpecialEncoderHost {

    companion object {
        private const val REPLAY = 1
        private const val BUFFER_CAPACITY = 100
        const val END_BYTE: Byte = 62
    }

    override val eventFlow: MutableSharedFlow<Message?> = MutableSharedFlow(
        REPLAY,
        BUFFER_CAPACITY,
        BufferOverflow.SUSPEND
    )

    override val extended = AtomicBoolean(false)

    private val currentDataEncoder by lazy { CurrentDataEncoder(eventFlow) }

    private val troubleCodesEncoder by lazy { TroubleCodesEncoder(eventFlow) }

    private var specialEncoder: SpecialEncoder? = null

    /**
     * Empty spaces and cariege return filtration should be in source class
     */

    override suspend fun decode(message: ByteArray, workMode: WorkMode): EncodingState {
        var startIndex = 0
        val decoded = message.decodeToString().trim()
        when {
            decoded.contains("?") -> {
                return EncodingState.Unsuccessful("?")
            }

            decoded.contains("SEARCHING...", true) && decoded.length > 12 -> {
                startIndex = 12
                // 12 bytes == SEARCHING...
            }

            decoded == "SEARCHING..." || decoded == "SEARCHING" || decoded == "SEARCHING.." -> {
                return EncodingState.WaitNext
            }

            decoded.contains("NO DATA", true) -> {
                return EncodingState.Unsuccessful("NO DATA")
            }

            (decoded.length == 4 || decoded.length == 5) && (decoded.last() == 'V' || decoded.last() == 'v') -> {
                eventFlow.emit(Message.Voltage(decoded))
                return EncodingState.Successful
            }

            decoded.contains(AtDecoder.DEVICE_NAME, true) &&
                (
                    !decoded.contains(
                        AtDecoder.WARM_RES_ANSWER,
                        true
                    ) && !decoded.contains(AtDecoder.HARD_RES_ANSWER)
                    ) -> {
                eventFlow.emit(Message.ElmDeviceName(decoded))
            }
        }

        return when (checkMode(message)) {
            Commands.PidMod.SHOW_CURRENT -> {
                currentDataEncoder.handleBytes(
                    message.copyOfRange(startIndex + 4, message.size),
                    decoded.substring(startIndex + 2, startIndex + 4)
                )
            }

            Commands.PidMod.SHOW_FREEZE_FRAME -> TODO()
            Commands.PidMod.SHOW_DIAGNOSTIC_TROUBLES_CODES -> {
                troubleCodesEncoder.handleBytes(
                    message.copyOfRange(startIndex + 2, message.size),
                    null
                )
            }

            Commands.PidMod.CLEAR_TROUBLES_CODES_AND_STORE_VAL -> TODO()
            Commands.PidMod.TEST_RESULTS_OXY_SENSORS -> TODO()
            Commands.PidMod.TEST_RESULTS_OXY_SENSORS_CAN -> TODO()
            Commands.PidMod.SHOW_PENDING_DIAGN_TROUBLES_CODES -> TODO()
            Commands.PidMod.CONTROL_ON_BOARD_SYS -> TODO()
            Commands.PidMod.VEHICLE_INFO_REQUEST -> TODO()
            Commands.PidMod.DELETED_ERRORS -> TODO()
            Commands.PidMod.CHECK_ON_CAN -> {
                if (extended.get() && specialEncoder != null) {
                    specialEncoder!!.handleBytes(message)
                } else EncodingState.Unsuccessful(decoded)
            }
        }
    }

    private fun checkMode(message: ByteArray): Commands.PidMod {
        val allMessage = message.decodeToString()
        val hexEcho = allMessage.take(2)
        val mode = Commands.PidMod.values().find {
            it.positiveCode == hexEcho
        }
        return mode ?: Commands.PidMod.CHECK_ON_CAN
    }

    override fun setSpecialEncoder(encoder: SpecialEncoder) {
        extended.set(true)
        encoder.bindMessagesFlow(eventFlow)
        specialEncoder = encoder
    }
}
