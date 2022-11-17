package obdKotlin.encoders

import kotlinx.coroutines.flow.MutableSharedFlow
import obdKotlin.commands.pid00
import obdKotlin.commands.pid01
import obdKotlin.commands.pid04
import obdKotlin.commands.pid05
import obdKotlin.commands.pid06
import obdKotlin.commands.pid07
import obdKotlin.commands.pid08
import obdKotlin.commands.pid09
import obdKotlin.commands.pid0A
import obdKotlin.commands.pid0C
import obdKotlin.commands.pid0D
import obdKotlin.commands.pid0E
import obdKotlin.commands.pid10
import obdKotlin.commands.pid11
import obdKotlin.commands.pid13
import obdKotlin.commands.pid1F
import obdKotlin.commands.pid20
import obdKotlin.commands.pid23
import obdKotlin.commands.pid33
import obdKotlin.commands.pid3C
import obdKotlin.commands.pid3D
import obdKotlin.commands.pid3E
import obdKotlin.commands.pid3F
import obdKotlin.commands.pid40
import obdKotlin.commands.pid44
import obdKotlin.commands.pid46
import obdKotlin.commands.pid5A
import obdKotlin.commands.pid5B
import obdKotlin.commands.pid5C
import obdKotlin.commands.pid5D
import obdKotlin.commands.pid5E
import obdKotlin.commands.pid60
import obdKotlin.commands.pid63
import obdKotlin.commands.pid64
import obdKotlin.commands.pid67
import obdKotlin.commands.pid80
import obdKotlin.commands.pidA0
import obdKotlin.commands.pidA2
import obdKotlin.commands.pidA4
import obdKotlin.commands.pidA6
import obdKotlin.commands.pidC0
import obdKotlin.decoders.EncodingState
import obdKotlin.hexToBinary
import obdKotlin.hexToInt
import obdKotlin.messages.Message
import obdKotlin.toBinary
import obdKotlin.toBinaryArray
import obdKotlin.toBoolean
import obdKotlin.toHex

class CurrentDataEncoder(eventFlow: MutableSharedFlow<Message?>) : Encoder(eventFlow) {

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
                decodeMafFlowRate(bytesBody) // gram/sec
            }

            pid11 -> {
                decodeThrottlePosition(bytesBody)
            }

            pid0E -> {
                decodeTimingAdvance(bytesBody) // Опережение зажигания
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
            pid5E -> {
                decodeFuelRate(bytesBody) //  L/h
            }
            pidA2 -> {
                decodeCylinderFuelRate(bytesBody)
            }
            pidA4 -> {
                decodeTransmissionActualGear(bytesBody)
            }
            pidA6 -> {
                decodeOdometer(bytesBody)
            }
            pid67 -> {
                decodeTwoSensorsCoolantTemperature(bytesBody)
            }
            else -> {
                eventFlow.emit(Message.UnknownAnswer(bytesBody.decodeToString(), bytesBody))
                null
            }
        }
        encodedMessage?.let {
            eventFlow.emit(encodedMessage)
            return EncodingState.Successful
        }
        return EncodingState.Unsuccessful(bytesBody.decodeToString())
    }

    private fun decodeTransmissionActualGear(bytesBody: ByteArray): Message {
        val isSupported = bytesBody.decodeToString(0, 2).hexToInt().toBinary()[6]
        val hexC = bytesBody.decodeToString(4, 6)
        val hexD = bytesBody.decodeToString(6, 8)
        val gear = (256 * hexC.hexToInt() + hexD.hexToInt()) / 1000.0
        return Message.ActualGear(gear)
    }

    private fun decodeOdometer(bytesBody: ByteArray): Message {
        val kilometers = (bytesBody.decodeToString().toUInt(16) / 10u).toFloat()
        return Message.OdometerData(kilometers)
    }

    private fun decodeCylinderFuelRate(bytesBody: ByteArray): Message {
        val hexA = bytesBody.decodeToString(0, 2)
        val hexB = bytesBody.decodeToString(2, 4)
        val rate = (256 * hexA.hexToInt() + hexB.hexToInt()) / 32
        return Message.CylinderFuelRate(rate)
    }

    private fun decodeTwoSensorsCoolantTemperature(bytesBody: ByteArray): Message {
        val binaryA = bytesBody.decodeToString(0, 2).hexToBinary()
        val coolant1 = bytesBody.decodeToString(2, 4).hexToInt() - 40
        val coolant2 = bytesBody.decodeToString(4, 6).hexToInt() - 40
        val map = mapOf<Int, Boolean>(Pair(coolant1, binaryA.last().toBoolean()), Pair(coolant2, binaryA[6].toBoolean()))
        return Message.TwoSensorsCoolantData(map)
    }

    private fun decodeFuelRate(bytesBody: ByteArray): Message {
        val hexA = bytesBody.decodeToString(0, 2)
        val hexB = bytesBody.decodeToString(2, 4)
        val fuelRate = (256 * hexA.hexToInt() + hexB.hexToInt()) / 20
        return Message.FuelRate(fuelRate)
    }

    private fun decodeFuelInjectionTiming(bytesBody: ByteArray): Message {
        val hexA = bytesBody.decodeToString(0, 2)
        val hexB = bytesBody.decodeToString(2, 4)
        val timing = (256 * hexA.hexToInt() + hexB.hexToInt()) / 128 - 210
        return Message.FuelInjectionTiming(timing)
    }

    private fun decodeOilTemperature(bytesBody: ByteArray): Message {
        val oilTemperature = bytesBody.decodeToString(0, 2).hexToInt() - 40
        return Message.OilTemperature(oilTemperature)
    }

    private fun decodeHybridBatteryLife(bytesBody: ByteArray): Message {
        val hexA = bytesBody.decodeToString(0, 2)
        val remainingPercent = 100 / 255.00 * hexA.hexToInt()
        return Message.HybridBatteryLife(remainingPercent)
    }

    private fun decodeEngineTorqueGraph(bytesBody: ByteArray): Message {
        // ? Should check
        val idlePoint = bytesBody.decodeToString(0, 2).hexToInt() - 125
        val pointB = bytesBody.decodeToString(2, 4).hexToInt() - 125
        val pointC = bytesBody.decodeToString(4, 6).hexToInt() - 125
        val pointD = bytesBody.decodeToString(6, 8).hexToInt() - 125
        val pointE = bytesBody.decodeToString(6, 8).hexToInt() - 125
        return Message.EngineTorqueDataPercent(idlePoint, pointB, pointC, pointD, pointE)
    }

    private fun decodeTorqueInNM(bytesBody: ByteArray): Message {
        val hexA = bytesBody.decodeToString(0, 2)
        val hexB = bytesBody.decodeToString(2, 4)
        val torque = 256 * hexA.hexToInt() + hexB.hexToInt()
        return Message.TorqueNM(torque)
    }

    private fun decodeAirFuelRatio(bytesBody: ByteArray): Message {
        val hexA = bytesBody.decodeToString(0, 2)
        val hexB = bytesBody.decodeToString(2, 4)
        val airFuelRatio = 2 / 65536.0 * (256 * hexA.hexToInt() + hexB.hexToInt())
        return Message.AirToFuelRatio(airFuelRatio)
    }

    private fun decodeGaugeFuelPressure(bytesBody: ByteArray): Message {
        val pressure = bytesBody.decodeToString(0, 2).hexToInt() * 3
        return Message.GaugeFuelPressure(pressure)
    }

    private fun decodeFuelTrim(bytesBody: ByteArray, bank: Int, shortTerm: Boolean): Message {
        val hexA = bytesBody.decodeToString(0, 2)
        val trim: Float = hexA.hexToInt() / 1.28f - 100
        return Message.FuelTrim(trim, bank, shortTerm)
    }

    private fun decodeCatalystTemperature(
        bytesBody: ByteArray,
        bank: Int,
        sensor: Int
    ): Message {
        val hexA = bytesBody.decodeToString(0, 2)
        val hexB = bytesBody.decodeToString(2, 4)
        val catalystTemperature = (256 * hexA.hexToInt() + hexB.hexToInt()) / 10 - 40
        return Message.CatalystTemperature(catalystTemperature, bank, sensor)
    }

    private fun decodeEngineRunTime(bytesBody: ByteArray): Message {
        val hexA = bytesBody.decodeToString(0, 2)
        val hexB = bytesBody.decodeToString(2, 4)
        val runTime = 256 * hexA.hexToInt() + hexB.hexToInt()
        return Message.EngineRunTime(runTime)
    }

    private fun oxygenSensorsPresents(bytesBody: ByteArray): Message {
        val binary = bytesBody.decodeToString(0, 2).hexToInt().toBinary(8).toCharArray()
        val map = mutableMapOf<Pair<Int, Int>, Boolean>()
        for (i in binary.indices) {
            if (i <= 3) {
                map.put(Pair(1, i), binary[i].toBoolean()) // 1- bank, i - sensor ordinal, 3 - isPresents
            } else {
                map.put(Pair(2, i), binary[i].toBoolean())
            }
        }
        return Message.OxygenSensorsPresents(map)
    }

    private fun decodeMafFlowRate(bytesBody: ByteArray): Message {
        val hexA = bytesBody.decodeToString(0, 2)
        val hexB = bytesBody.decodeToString(2, 4)
        val rate: Float = (256 * hexA.hexToInt() + hexB.hexToInt()) / 100.00f
        return Message.MafAirFlowRate(rate)
    }

    private fun decodeAmbientAirTemperature(bytesBody: ByteArray): Message {
        val ambientTemperature = bytesBody.decodeToString(0, 2)
            .hexToInt() - 40
        return Message.AmbientAirTemperature(ambientTemperature)
    }

    private fun decodeFuelPressureDI(bytesBody: ByteArray): Message {
        val hexA = bytesBody.decodeToString(0, 2)
        val hexB = bytesBody.decodeToString(2, 4)
        val pressure = 0.079 * (256 * hexA.hexToInt() + hexB.hexToInt())
        return Message.FuelPressureDI(pressure)
    }

    private fun decodeRPM(bytesBody: ByteArray): Message {
        val hexA = bytesBody.decodeToString(0, 2)
        val hexB = bytesBody.decodeToString(2, 4)
        val rpm = (256 * hexA.hexToInt() + hexB.hexToInt()) / 4
        return Message.RPM(rpm)
    }

    private fun decodeAcceleratorPosition(bytesBody: ByteArray): Message {
        val acceleratorPosition = bytesBody.decodeToString(0, 2).hexToInt() * 2.55f
        return Message.AcceleratorPosition(acceleratorPosition)
    }

    private fun decodeBarometricPressure(bytesBody: ByteArray): Message {
        val atmPress = bytesBody.decodeToString(0, 2).hexToInt()
        return Message.BarometricPressure(atmPress)
    }

    private fun decodeTimingAdvance(bytesBody: ByteArray): Message {
        val advance = bytesBody.decodeToString(0, 2).hexToInt() / 2 - 64
        return Message.TimingAdvance(advance)
    }

    private fun decodeThrottlePosition(bytesBody: ByteArray): Message {
        val position = bytesBody.decodeToString(0, 2).hexToInt() / 2.55f
        return Message.ThrottlePosition(position)
    }

    private fun decodeCarSpeed(bytesBody: ByteArray): Message {
        val speed = bytesBody.decodeToString(0, 2).hexToInt()
        return Message.VehicleSpeed(speed)
    }

    private fun decodeEngineLoad(bytesBody: ByteArray): Message {
        val load = bytesBody.decodeToString(0, 2).hexToInt() / 2.55f
        return Message.EngineLoadMessage(load)
    }

    private fun decodeSupportedPids(dataBytes: ByteArray, part: Int): Message {
        val pidOffset = when (part) {
            1 -> 1
            2 -> 33
            3 -> 65
            4 -> 97
            5 -> 129
            6 -> 161
            else -> 193
        }
        val map = mutableMapOf<String, Boolean>()
//        val binary = dataBytes.toUByteArray().toBinaryArray()
//        for (i in binary.indices) {
//            map.put((i + pidOffset).toHex(), binary[i].toBoolean())
//        }

        val total = dataBytes.decodeToString().toUInt(16)
        for (i in 31 downTo 0) {
            val bit = total shl i and 1U
            val isSupported = bit == 1U
            map[(i + pidOffset).toHex()] = isSupported
        }
        return Message.SupportedCommands(map, pidOffset.toHex(), (pidOffset + 32).toHex())
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
        val temperature = bytesBody.decodeToString().hexToInt() - 40
        return Message.TemperatureMessage(temperature)
    }
}
