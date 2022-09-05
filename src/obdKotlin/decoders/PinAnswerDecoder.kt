package obdKotlin.decoders

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import obdKotlin.commands.*
<<<<<<< HEAD
import obdKotlin.core.WorkMode
import obdKotlin.encoders.SpecialEncoder
=======
import obdKotlin.encoders.CurrentDataEncoder
import obdKotlin.encoders.SpecialEncoder
import obdKotlin.encoders.TroubleCodesEncoder
>>>>>>> 61257416ebc4218fbd9b3c63ea2dcb4f83c64b4a
import obdKotlin.messages.Message
import java.util.concurrent.atomic.AtomicBoolean


internal class PinAnswerDecoder() : Decoder(), SpecialEncoderHost {

    companion object{
        private const val REPLAY = 1
        private const val BUFFER_CAPACITY = 100
<<<<<<< HEAD
=======
        const val END_BYTE: Byte = 62
>>>>>>> 61257416ebc4218fbd9b3c63ea2dcb4f83c64b4a
    }

    override val eventFlow: MutableSharedFlow<Message?> = MutableSharedFlow(
        REPLAY,
        BUFFER_CAPACITY,
        BufferOverflow.SUSPEND
    )

    override val canMode = AtomicBoolean(false)

<<<<<<< HEAD
    private val currentDataEncoder by lazy {CurrentDataEncoder(eventFlow)}

    private var specialEncoder: SpecialEncoder? = null




=======
    private val currentDataEncoder by lazy { CurrentDataEncoder(eventFlow) }

    private val troubleCodesEncoder by lazy {TroubleCodesEncoder(eventFlow)}

    private var specialEncoder: SpecialEncoder? = null
>>>>>>> 61257416ebc4218fbd9b3c63ea2dcb4f83c64b4a

    /**
     * Empty spaces and cariege return filtration should be in source class
     */

<<<<<<< HEAD
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
=======
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
>>>>>>> 61257416ebc4218fbd9b3c63ea2dcb4f83c64b4a
                Commands.PidMod.CLEAR_TROUBLES_CODES_AND_STORE_VAL -> TODO()
                Commands.PidMod.TEST_RESULTS_OXY_SENSORS -> TODO()
                Commands.PidMod.TEST_RESULTS_OXY_SENSORS_CAN -> TODO()
                Commands.PidMod.SHOW_PENDING_DIAGN_TROUBLES_CODES -> TODO()
                Commands.PidMod.CONTROL_ON_BOARD_SYS -> TODO()
                Commands.PidMod.VEHICLE_INFO_REQUEST -> TODO()
                Commands.PidMod.DELETED_ERRORS -> TODO()
<<<<<<< HEAD
                null -> false
            }
        }
    }
=======
                Commands.PidMod.CHECK_ON_CAN -> {
                    if (canMode.get() && specialEncoder != null){
                        specialEncoder!!.handleBytes(message)
                    } else EncodingState.UNSUCCESSFUL
                }
                null -> EncodingState.UNSUCCESSFUL
            }
        }
>>>>>>> 61257416ebc4218fbd9b3c63ea2dcb4f83c64b4a

    private fun checkAnswer(message: ByteArray): Commands.PidMod? {
        val allMessage = message.decodeToString()
        val hexEcho = allMessage.take(2)
<<<<<<< HEAD
        if (allMessage.contains("data", true)) {
=======
        if (allMessage.contains("No data", true)) {
>>>>>>> 61257416ebc4218fbd9b3c63ea2dcb4f83c64b4a
            return null
        }
        val mode = Commands.PidMod.values().find {
            it.positiveCode == hexEcho
        }
<<<<<<< HEAD
        return mode
=======
        return mode ?: Commands.PidMod.CHECK_ON_CAN
>>>>>>> 61257416ebc4218fbd9b3c63ea2dcb4f83c64b4a
    }

    override fun setSpecialEncoder(encoder: SpecialEncoder){
        canMode.set(true)
        encoder.bindMessagesFlow(eventFlow)
        specialEncoder = encoder
    }

}