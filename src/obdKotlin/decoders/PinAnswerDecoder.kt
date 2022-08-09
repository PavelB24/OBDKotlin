package obdKotlin.decoders

import kotlinx.coroutines.flow.MutableSharedFlow
import obdKotlin.*
import obdKotlin.commands.*
import obdKotlin.messages.CommonMessages
import obdKotlin.messages.EngineMessages
import obdKotlin.messages.Message


internal class PinAnswerDecoder(
    private val eventFlow: MutableSharedFlow<Message?>
) : Decoder(eventFlow) {

    companion object {
        const val SERVICE_BYTES_SIZE = 2
    }

    /**
     * Empty spaces and cariege return filtration should be in source class
     */

    override suspend fun decode(message: ByteArray, workMode: WorkMode): Boolean {
        val size = message.size - SERVICE_BYTES_SIZE
        val dataBytes = message.copyInto(
            ByteArray(size),
            0,
            SERVICE_BYTES_SIZE,
            message.size
        )
        return if (checkAnswer(message.decodeToString(0, 2))) {
            val bytesBody = dataBytes.copyOfRange(2, dataBytes.size)
            val decodedMessage: Message? = when (dataBytes.decodeToString(0, 2)) {
                pid00 -> {
                    decodeSupportedPids0120(bytesBody)
                }

                pid01 -> {
                    decodeStatusSinceDTCCleared(bytesBody)
                }

                pid04 -> {
                    decodeEngineLoad(bytesBody)
                }

                pid05 -> {
                    decodeCoolantTemperature(bytesBody)
                }

                pid0D -> {
                    decodeCarSpeed(bytesBody)
                }

                pid11 -> {
                    decodeThrottlePosition(bytesBody)
                }

                pid0E ->{
                    decodeTimingAdvance(bytesBody)
                }
                pid33 -> {
                    decodeBarometricPressure(bytesBody)
                }
                pid5A -> {
                    decodeAcceleratorPosition(bytesBody)
                }
                pid0C ->{
                    decodeRPM(bytesBody)
                }
                pid23 -> {
                    decodeFuelPressure(bytesBody)
                }

                else -> {
                    null
                }

            }
            eventFlow.emit(decodedMessage)
            true
        } else false
    }

    private fun decodeFuelPressure(bytesBody: ByteArray): Message {
        val hexA = bytesBody.decodeToString(0,2)
        val hexB = bytesBody.decodeToString(2,4)
        val pressure = 0.079*(256*hexA.hexToInt() + hexB.hexToInt())
    }

    private fun decodeRPM(bytesBody: ByteArray): Message {
        val hexA = bytesBody.decodeToString(0,2)
        val hexB = bytesBody.decodeToString(2,4)
        val rpm = (256 * hexA.hexToInt() + hexB.hexToInt()) / 4

    }

    private fun decodeAcceleratorPosition(bytesBody: ByteArray): Message {
        val acceleratorPosition = bytesBody.decodeToString(0, 2).hexToInt() * 2.55f
    }

    private fun decodeBarometricPressure(bytesBody: ByteArray): Message {
        val atmPress = bytesBody.decodeToString(0, 2).hexToInt()
    }

    private fun decodeTimingAdvance(bytesBody: ByteArray): Message {
        val advance = bytesBody.decodeToString(0, 2).hexToInt()/2 -64

    }

    private fun decodeThrottlePosition(bytesBody: ByteArray): Message {
        val position = bytesBody.decodeToString(0, 2).hexToInt() /2.55f

    }

    private fun decodeCarSpeed(bytesBody: ByteArray): Message {
        val speed = bytesBody.decodeToString(0,2).hexToInt()
        return EngineMessages.VehicleSpeed(speed, Message.MessageType.ENGINE)
    }

    private fun decodeEngineLoad(bytesBody: ByteArray): Message {
        val load = bytesBody.decodeToString(0, 2).hexToInt() / 2.55f
        return EngineMessages.EngineLoadMessage(load, Message.MessageType.ENGINE)
    }

    private fun decodeSupportedPids0120(dataBytes: ByteArray): Message {
        val binary = dataBytes.toUByteArray().toBinaryArray()
        val map = mutableMapOf<Commands.PidCommands, Boolean>()
        map[Commands.PidCommands.STATUS_SINCE_DTC_CLEARED] = binary[0].toBoolean()
        map[Commands.PidCommands.ENGINE_LOAD] = binary[3].toBoolean()
        map[Commands.PidCommands.COOLANT_TEMPERATURE] = binary[4].toBoolean()
        map[Commands.PidCommands.FUEL_PRESSURE] = binary[9].toBoolean()
        map[Commands.PidCommands.MAP_VAL] = binary[10].toBoolean()
        map[Commands.PidCommands.ENGINE_RPM] = binary[11].toBoolean()
        map[Commands.PidCommands.CAR_SPEED] = binary[12].toBoolean()
        map[Commands.PidCommands.TIMING_ADVANCE] = binary[13].toBoolean()
        map[Commands.PidCommands.INTAKE_AIR_TEMP] = binary[14].toBoolean()
        map[Commands.PidCommands.MAP_AIR_FLOW] = binary[15].toBoolean()
        map[Commands.PidCommands.THROTTLE_POSITION] = binary[16].toBoolean()
        map[Commands.PidCommands.HAS_OXYGEN_SENSORS] = binary[18].toBoolean()
        map[Commands.PidCommands.ENGINE_RUN_TIME] = binary[30].toBoolean()
        return CommonMessages.Supported0120Commands(map, Message.MessageType.COMMON)
    }

    private fun decodeStatusSinceDTCCleared(dataBytes: ByteArray): Message {
        val bitsOfAByte = dataBytes.first().toUByte().toBinary(8).toCharArray()
        val bitsOfBByte = dataBytes[1].toUByte().toBinary(8).toCharArray()
        val milIsOn = bitsOfAByte.last().toBoolean()
        val numberOfErrors = dataBytes.copyOfRange(0, 6).toString().toUByte(2)
        val isSparkTestAvailable = bitsOfBByte[3].toBoolean()
        return CommonMessages.MonitorDTCsStatus(
            milIsOn,
            numberOfErrors,
            isSparkTestAvailable,
            Message.MessageType.COMMON
        )

    }

    private fun decodeCoolantTemperature(bytesBody: ByteArray): Message {
        val temperature = bytesBody.decodeToString().hexToInt() -40
        return EngineMessages.TemperatureMessage(temperature, Message.MessageType.ENGINE)
    }

    private fun checkAnswer(hex: String): Boolean {
        val answered = Commands.PidMod.values().find {
            it.positiveCode == hex
        }
        answered?.let {
            return true
        }
        return false
    }
}