package obdKotlin.encoders

import kotlinx.coroutines.flow.MutableSharedFlow
import obdKotlin.*
import obdKotlin.commands.*
import obdKotlin.decoders.EncodingState
import obdKotlin.messages.Message
import obdKotlin.toBinary
import obdKotlin.toBinaryArray
import obdKotlin.toBoolean

class CurrentDataEncoder(private val eventFlow: MutableSharedFlow<Message?>): Encoder(eventFlow) {

    override suspend fun handleBytes(bytesBody: ByteArray, pid: String?): EncodingState {
        val encodedMessage: Message? = when (pid) {
            pid01 -> {
                decodeStatusSinceDTCCleared(bytesBody)
            }

            pid5B -> {
                decodeHybridBatteryLife(bytesBody)
            }

            pid04 -> {
                decodeEngineLoad(bytesBody)
            }

            pid05 -> {
                decodeCoolantTemperature(bytesBody)
            }

            pid13 -> {
                oxygenSensorsPresents(bytesBody)
            }

            pid0D -> {
                decodeCarSpeed(bytesBody)
            }

            pid10 -> {
                decodeMafFlowRate(bytesBody) //gram/sec
            }

            pid11 -> {
                decodeThrottlePosition(bytesBody)
            }

            pid0E -> {
                decodeTimingAdvance(bytesBody)  //Опережение зажигания
            }

            pid33 -> {
                decodeBarometricPressure(bytesBody)
            }

            pid5A -> {
                decodeAcceleratorPosition(bytesBody)
            }

            pid0C -> {
                decodeRPM(bytesBody)
            }

            pid23 -> {
                decodeFuelPressureDI(bytesBody)
            }

            pid46 -> {
                decodeAmbientAirTemperature(bytesBody)
            }

            pid00 -> {
                decodeSupportedPids(bytesBody, 1)
            }

            pid20 -> {
                decodeSupportedPids(bytesBody, 2)
            }

            pid40 -> {
                decodeSupportedPids(bytesBody, 3)
            }

            pid60 -> {
                decodeSupportedPids(bytesBody, 4)
            }

            pid80 -> {
                decodeSupportedPids(bytesBody, 5)
            }

            pidA0 -> {
                decodeSupportedPids(bytesBody, 6)
            }


            pidC0 -> {
                decodeSupportedPids(bytesBody, 7)
            }

            pid1F -> {
                decodeEngineRunTime(bytesBody)
            }

            pid06 -> {
                decodeFuelTrim(bytesBody, 1, true)
            }

            pid07 -> {
                decodeFuelTrim(bytesBody, 1, false)
            }

            pid08 -> {
                decodeFuelTrim(bytesBody, 2, true)
            }

            pid09 -> {
                decodeFuelTrim(bytesBody, 2, false)
            }

            pid3C -> {
                decodeCatalystTemperature(bytesBody, 1, 1)
            }

            pid3D -> {
                decodeCatalystTemperature(bytesBody, 2, 1)
            }

            pid3E -> {
                decodeCatalystTemperature(bytesBody, 1, 2)
            }

            pid3F -> {
                decodeCatalystTemperature(bytesBody, 2, 2)
            }

            pid0A -> {
                decodeGaugeFuelPressure(bytesBody)
            }

            pid44 -> {
                decodeAirFuelRatio(bytesBody)
            }

            pid63 -> {
                decodeTorqueInNM(bytesBody)
            }

            pid64 -> {
                decodeEngineTorqueGraph(bytesBody)
            }

            pid5C -> {
                decodeOilTemperature(bytesBody)
            }

            pid5D -> {
                decodeFuelInjectionTiming(bytesBody)
            }
            pid5E ->{
                decodeFuelRate(bytesBody)  //  L/h
            }
            pidA2 -> {
                decodeCylinderFuelRate(bytesBody)
            }
            pidA4 ->{
                decodeTransmissionActualGear(bytesBody)
            }
            pidA6 -> {
                decodeOdometre(bytesBody)
            }
            pid67 -> {
                decodeTwoSensorsCoolantTemperature(bytesBody)
            }
            else -> {
                null
            }
        }
        encodedMessage?.let{
            eventFlow.emit(encodedMessage)
            return EncodingState.SUCCESSFUL
        }
        return EncodingState.UNSUCCESSFUL

    }

    private fun decodeCylinderFuelRate(bytesBody: ByteArray): Message {
        val hexA = bytesBody.decodeToString(0,2)
        val hexB = bytesBody.decodeToString(2,4)
        val rate = (256 * hexA.hexToInt() + hexB.hexToInt()) /32
    }

    private fun decodeTwoSensorsCoolantTemperature(bytesBody: ByteArray): Message {
        val binaryA = bytesBody.decodeToString(0,2).hexToBinary()
        val coolant1 = bytesBody.decodeToString(2,4).hexToInt() -40
        val coolant2 = bytesBody.decodeToString(4,6).hexToInt() -40
        val map = mapOf<Int, Boolean>(Pair(coolant1, binaryA[0].toBoolean()), Pair(coolant2, binaryA[0].toBoolean()))
    }

    private fun decodeFuelRate(bytesBody: ByteArray): Message {
        val hexA = bytesBody.decodeToString(0,2)
        val hexB = bytesBody.decodeToString(2,4)
        val fuelRate = (256*hexA.hexToInt() + hexB.hexToInt()) /20
    }

    private fun decodeFuelInjectionTiming(bytesBody: ByteArray): Message {
        val hexA = bytesBody.decodeToString(0,2)
        val hexB = bytesBody.decodeToString(2,4)
        val timing = (256*hexA.hexToInt() + hexB.hexToInt()) /128 -210

    }

    private fun decodeOilTemperature(bytesBody: ByteArray): Message {
        val oilTemperature = bytesBody.decodeToString(0,2).hexToInt() -40

    }

    private fun decodeHybridBatteryLife(bytesBody: ByteArray): Message {
        val hexA = bytesBody.decodeToString(0,2)
        val remainingPercent = 100/255.00 * hexA.hexToInt()
    }

    private fun decodeEngineTorqueGraph(bytesBody: ByteArray): Message {
        //? Should check
        val idlePoint = bytesBody.decodeToString(0,2).hexToInt() - 125
        val pointB = bytesBody.decodeToString(2,4).hexToInt() - 125
        val pointC = bytesBody.decodeToString(4,6).hexToInt() - 125
        val pointD = bytesBody.decodeToString(6,8).hexToInt() - 125
        val pointE = bytesBody.decodeToString(6,8).hexToInt() - 125
    }

    private fun decodeTorqueInNM(bytesBody: ByteArray): Message {
        val hexA = bytesBody.decodeToString(0,2)
        val hexB = bytesBody.decodeToString(2,4)
        val torque = 256 * hexA.hexToInt() + hexB.hexToInt()
    }

    private fun decodeAirFuelRatio(bytesBody: ByteArray): Message {
        val hexA = bytesBody.decodeToString(0,2)
        val hexB = bytesBody.decodeToString(2,4)
        val airFuelRatio = 2/65536.0 * (256 * hexA.hexToInt() + hexB.hexToInt())
    }

    private fun decodeGaugeFuelPressure(bytesBody: ByteArray): Message {
        val pressure = bytesBody.decodeToString(0, 2).hexToInt() * 3
    }


    private fun decodeFuelTrim(bytesBody: ByteArray, bank: Int, shortTerm: Boolean ): Message {
        val hexA = bytesBody.decodeToString(0,2)
        val trim: Float = hexA.hexToInt() / 1.28f -100
    }

    private fun decodeCatalystTemperature(
        bytesBody: ByteArray,
        bank: Int,
        sensor: Int
    ): Message {
        val hexA = bytesBody.decodeToString(0,2)
        val hexB = bytesBody.decodeToString(2,4)
        val catalystTemperature = (256*hexA.hexToInt() + hexB.hexToInt())/10 -40
        return Message.CatalystTemperature(catalystTemperature, bank, sensor)
    }

    private fun decodeEngineRunTime(bytesBody: ByteArray): Message {
        val hexA= bytesBody.decodeToString(0,2)
        val hexB= bytesBody.decodeToString(2,4)
        val runTime= 256*hexA.hexToInt() + hexB.hexToInt()
    }

    private fun oxygenSensorsPresents(bytesBody: ByteArray): Message {
        val binary = bytesBody.decodeToString(0, 2).hexToInt().toBinary(8).toCharArray()
        val map = mutableMapOf<Pair<Int, Int>, Boolean>()
        for (i in binary.indices){
            if(i <= 3){
                map.put(Pair(1, i), binary[i].toBoolean()) // 1- bank, i - sensor ordinal, 3 - isPresents
            } else {
                map.put(Pair(2, i), binary[i].toBoolean())
            }
        }
    }

    private fun decodeMafFlowRate(bytesBody: ByteArray): Message {
        val hexA = bytesBody.decodeToString(0, 2)
        val hexB = bytesBody.decodeToString(2, 4)
        val waste: Float = (256 * hexA.hexToInt() + hexB.hexToInt() ) /100.00f
    }

    private fun decodeAmbientAirTemperature(bytesBody: ByteArray): Message {
        val ambientTemperature = bytesBody.decodeToString(0,2)
            .hexToInt() - 40
    }

    private fun decodeFuelPressureDI(bytesBody: ByteArray): Message {
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
        return Message.VehicleSpeed(speed)
    }

    private fun decodeEngineLoad(bytesBody: ByteArray): Message {
        val load = bytesBody.decodeToString(0, 2).hexToInt() / 2.55f
        return Message.EngineLoadMessage(load)
    }

    private fun decodeSupportedPids(dataBytes: ByteArray, part: Int): Message {
        val pidOffset = when(part){
            1-> 1
            2-> 33
            3-> 65
            4-> 97
            5-> 129
            6-> 161
            else -> 193
        }
        val binary = dataBytes.toUByteArray().toBinaryArray()
        val map = mutableMapOf<String, Boolean>()
        for (i in binary.indices){
            map.put((i+pidOffset).toHex(), binary[i].toBoolean())
        }
//        map[Commands.PidCommands.STATUS_SINCE_DTC_CLEARED] = binary[0].toBoolean()
//        map[Commands.PidCommands.ENGINE_LOAD] = binary[3].toBoolean()
//        map[Commands.PidCommands.COOLANT_TEMPERATURE] = binary[4].toBoolean()
//        map[Commands.PidCommands.FUEL_PRESSURE] = binary[9].toBoolean()
//        map[Commands.PidCommands.MAP_VAL] = binary[10].toBoolean()
//        map[Commands.PidCommands.ENGINE_RPM] = binary[11].toBoolean()
//        map[Commands.PidCommands.CAR_SPEED] = binary[12].toBoolean()
//        map[Commands.PidCommands.TIMING_ADVANCE] = binary[13].toBoolean()
//        map[Commands.PidCommands.INTAKE_AIR_TEMP] = binary[14].toBoolean()
//        map[Commands.PidCommands.MAP_AIR_FLOW] = binary[15].toBoolean()
//        map[Commands.PidCommands.THROTTLE_POSITION] = binary[16].toBoolean()
//        map[Commands.PidCommands.HAS_OXYGEN_SENSORS] = binary[18].toBoolean()
//        map[Commands.PidCommands.ENGINE_RUN_TIME] = binary[30].toBoolean()
        return Message.SupportedCommands(map, pidOffset.toHex(), binary.size.toHex())
    }

    private fun decodeStatusSinceDTCCleared(dataBytes: ByteArray): Message {
        val bitsOfAByte = dataBytes.first().toUByte().toBinary(8).toCharArray()
        val bitsOfBByte = dataBytes[1].toUByte().toBinary(8).toCharArray()
        val milIsOn = bitsOfAByte.last().toBoolean()
        val numberOfErrors = dataBytes.copyOfRange(0, 6).toString().toUByte(2)
        val isSparkTestAvailable = bitsOfBByte[3].toBoolean()
        return Message.MonitorDTCsStatus(
            milIsOn,
            numberOfErrors,
            isSparkTestAvailable
        )

    }

    private fun decodeCoolantTemperature(bytesBody: ByteArray): Message {
        val temperature = bytesBody.decodeToString().hexToInt() -40
        return Message.TemperatureMessage(temperature)
    }


}