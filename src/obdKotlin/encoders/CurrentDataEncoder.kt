package obdKotlin.encoders

import kotlinx.coroutines.flow.MutableSharedFlow
import obdKotlin.decoders.EncodingState
import obdKotlin.messages.Message
import obdKotlin.toBinary
import obdKotlin.toBoolean
import obdKotlin.toHex

class CurrentDataEncoder(eventFlow: MutableSharedFlow<Message?>) : Encoder(eventFlow) {

    override suspend fun handleBytes(bytesBody: ByteArray, pid: Int?): EncodingState {
        val encodedMessage: Message? = when (pid) {
            0x01 -> {
                decodeStatusSinceDTCCleared(bytesBody)
            }

            0x5B -> {
                decodeHybridBatteryLife(bytesBody)
            }

            0x04 -> {
                decodeEngineLoad(bytesBody)
            }

            0x05 -> {
                decodeCoolantTemperature(bytesBody)
            }

            0x13 -> {
                oxygenSensorsPresents(bytesBody)
            }

            0x0D -> {
                decodeCarSpeed(bytesBody)
            }

            0x10 -> {
                decodeMafFlowRate(bytesBody) // gram/sec
            }

            0x11 -> {
                decodeThrottlePosition(bytesBody)
            }

            0x0E -> {
                decodeTimingAdvance(bytesBody) // Опережение зажигания
            }

            0x33 -> {
                decodeBarometricPressure(bytesBody)
            }

            0x5A -> {
                decodeAcceleratorPosition(bytesBody)
            }

            0x0C -> {
                decodeRPM(bytesBody)
            }

            0x23 -> {
                decodeFuelPressureDI(bytesBody)
            }

            0x46 -> {
                decodeAmbientAirTemperature(bytesBody)
            }

            0x00 -> {
                decodeSupportedPids(bytesBody, 1)
            }

            0x20 -> {
                decodeSupportedPids(bytesBody, 2)
            }

            0x40 -> {
                decodeSupportedPids(bytesBody, 3)
            }

            0x60 -> {
                decodeSupportedPids(bytesBody, 4)
            }

            0x80 -> {
                decodeSupportedPids(bytesBody, 5)
            }

            0xA0 -> {
                decodeSupportedPids(bytesBody, 6)
            }

            0xC0 -> {
                decodeSupportedPids(bytesBody, 7)
            }

            0x1F -> {
                decodeEngineRunTime(bytesBody)
            }

            0x06 -> {
                decodeFuelTrim(bytesBody, 1, true)
            }

            0x07 -> {
                decodeFuelTrim(bytesBody, 1, false)
            }

            0x08 -> {
                decodeFuelTrim(bytesBody, 2, true)
            }

            0x09 -> {
                decodeFuelTrim(bytesBody, 2, false)
            }

            0x3C -> {
                decodeCatalystTemperature(bytesBody, 1, 1)
            }

            0x3D -> {
                decodeCatalystTemperature(bytesBody, 2, 1)
            }

            0x3E -> {
                decodeCatalystTemperature(bytesBody, 1, 2)
            }

            0x3F -> {
                decodeCatalystTemperature(bytesBody, 2, 2)
            }

            0x0A -> {
                decodeGaugeFuelPressure(bytesBody)
            }

            0x44 -> {
                decodeAirFuelRatio(bytesBody)
            }

            0x63 -> {
                decodeTorqueInNM(bytesBody)
            }

            0x64 -> {
                decodeEngineTorqueGraph(bytesBody)
            }

            0x5C -> {
                decodeOilTemperature(bytesBody)
            }

            0x5D -> {
                decodeFuelInjectionTiming(bytesBody)
            }
            0x5E -> {
                decodeFuelRate(bytesBody) //  L/h
            }
            0xA2 -> {
                decodeCylinderFuelRate(bytesBody)
            }
            0xA4 -> {
                decodeTransmissionActualGear(bytesBody)
            }
            0xA6 -> {
                decodeOdometer(bytesBody)
            }
            0x67 -> {
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
        // todo refact
        val isSupported = bytesBody.decodeToString(0, 2).toInt(16).toBinary()[6]
        val hex = bytesBody.decodeToString(4, 8)
        val gear = (256 * hex.toInt(16)) / 1000.0
        return Message.ActualGear(gear)
    }
    private fun decodeOdometer(bytesBody: ByteArray): Message {
        val kilometers = (bytesBody.decodeToString().toUInt(16) / 10u).toFloat()
        return Message.OdometerData(kilometers)
    }

    private fun decodeCylinderFuelRate(bytesBody: ByteArray): Message {
        val hex = bytesBody.decodeToString(0, 4)
        val rate = (256 * hex.toInt(16)) / 32
        return Message.CylinderFuelRate(rate)
    }

    private fun decodeTwoSensorsCoolantTemperature(bytesBody: ByteArray): Message {
        val sourceInt = bytesBody.decodeToString(0, 2).toInt(16)
        val aSupported = (sourceInt and 1) == 1
        val bSupported = (sourceInt shr 1 and 1) == 1
        val coolant1 = bytesBody.decodeToString(2, 4).toInt(16) - 40
        val coolant2 = bytesBody.decodeToString(4, 6).toInt(16) - 40
        val map = mapOf<Int, Boolean>(Pair(coolant1, aSupported), Pair(coolant2, bSupported))
        return Message.TwoSensorsCoolantData(map)
    }

    private fun decodeFuelRate(bytesBody: ByteArray): Message {
        val hex = bytesBody.decodeToString(0, 4)
        val fuelRate = (256 * hex.toInt(16)) / 20
        return Message.FuelRate(fuelRate)
    }

    private fun decodeFuelInjectionTiming(bytesBody: ByteArray): Message {
        val hex = bytesBody.decodeToString(0, 4)
        val timing = (256 * hex.toInt(16)) / 128 - 210
        return Message.FuelInjectionTiming(timing)
    }

    private fun decodeOilTemperature(bytesBody: ByteArray): Message {
        val oilTemperature = bytesBody.decodeToString(0, 2).toInt(16) - 40
        return Message.OilTemperature(oilTemperature)
    }

    private fun decodeHybridBatteryLife(bytesBody: ByteArray): Message {
        val hex = bytesBody.decodeToString(0, 2)
        val remainingPercent = 100 / 255.00 * hex.toInt(16)
        return Message.HybridBatteryLife(remainingPercent)
    }

    private fun decodeEngineTorqueGraph(bytesBody: ByteArray): Message {
        // ? Should check
        val idlePoint = bytesBody.decodeToString(0, 2).toInt(16) - 125
        val pointB = bytesBody.decodeToString(2, 4).toInt(16) - 125
        val pointC = bytesBody.decodeToString(4, 6).toInt(16) - 125
        val pointD = bytesBody.decodeToString(6, 8).toInt(16) - 125
        val pointE = bytesBody.decodeToString(6, 8).toInt(16) - 125
        return Message.EngineTorqueDataPercent(idlePoint, pointB, pointC, pointD, pointE)
    }

    private fun decodeTorqueInNM(bytesBody: ByteArray): Message {
        val hex = bytesBody.decodeToString(0, 4)
        val torque = 256 * hex.toInt(16)
        return Message.TorqueNM(torque)
    }

    private fun decodeAirFuelRatio(bytesBody: ByteArray): Message {
        val hex = bytesBody.decodeToString(0, 4)
        val airFuelRatio = 2 / 65536.0 * (256 * hex.toInt(16))
        return Message.AirToFuelRatio(airFuelRatio)
    }

    private fun decodeGaugeFuelPressure(bytesBody: ByteArray): Message {
        val pressure = bytesBody.decodeToString(0, 2).toInt(16) * 3
        return Message.GaugeFuelPressure(pressure)
    }

    private fun decodeFuelTrim(bytesBody: ByteArray, bank: Int, shortTerm: Boolean): Message {
        val hex = bytesBody.decodeToString(0, 2)
        val trim: Float = hex.toInt(16) / 1.28f - 100
        return Message.FuelTrim(trim, bank, shortTerm)
    }

    private fun decodeCatalystTemperature(
        bytesBody: ByteArray,
        bank: Int,
        sensor: Int
    ): Message {
        val hex = bytesBody.decodeToString(0, 4)
        val catalystTemperature = (256 * hex.toInt(16)) / 10 - 40
        return Message.CatalystTemperature(catalystTemperature, bank, sensor)
    }

    private fun decodeEngineRunTime(bytesBody: ByteArray): Message {
        val hex = bytesBody.decodeToString(0, 4)
        val runTime = 256 * hex.toInt(16)
        return Message.EngineRunTime(runTime)
    }

    private fun oxygenSensorsPresents(bytesBody: ByteArray): Message {
        // todo refact
        val binary = bytesBody.decodeToString(0, 2).toInt(16).toBinary(8).toCharArray()
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
        val hex = bytesBody.decodeToString(0, 4)
        val rate: Float = (256 * hex.toInt(16)) / 100.00f
        return Message.MafAirFlowRate(rate)
    }

    private fun decodeAmbientAirTemperature(bytesBody: ByteArray): Message {
        val ambientTemperature = bytesBody.decodeToString(0, 2)
            .toInt(16) - 40
        return Message.AmbientAirTemperature(ambientTemperature)
    }

    private fun decodeFuelPressureDI(bytesBody: ByteArray): Message {
        val hex = bytesBody.decodeToString(0, 4)
        val pressure = 0.079 * (256 * hex.toInt(16))
        return Message.FuelPressureDI(pressure)
    }

    private fun decodeRPM(bytesBody: ByteArray): Message {
        val hex = bytesBody.decodeToString(0, 4)
        val rpm = (256 * hex.toInt(16)) / 4
        return Message.RPM(rpm)
    }

    private fun decodeAcceleratorPosition(bytesBody: ByteArray): Message {
        val acceleratorPosition = bytesBody.decodeToString(0, 2).toInt(16) * 2.55f
        return Message.AcceleratorPosition(acceleratorPosition)
    }

    private fun decodeBarometricPressure(bytesBody: ByteArray): Message {
        val atmPress = bytesBody.decodeToString(0, 2).toInt(16)
        return Message.BarometricPressure(atmPress)
    }

    private fun decodeTimingAdvance(bytesBody: ByteArray): Message {
        val advance = bytesBody.decodeToString(0, 2).toInt(16) / 2 - 64
        return Message.TimingAdvance(advance)
    }

    private fun decodeThrottlePosition(bytesBody: ByteArray): Message {
        val position = bytesBody.decodeToString(0, 2).toInt(16) / 2.55f
        return Message.ThrottlePosition(position)
    }

    private fun decodeCarSpeed(bytesBody: ByteArray): Message {
        val speed = bytesBody.decodeToString(0, 2).toInt(16)
        return Message.VehicleSpeed(speed)
    }

    private fun decodeEngineLoad(bytesBody: ByteArray): Message {
        val load = bytesBody.decodeToString(0, 2).toInt(16) / 2.55f
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
        // todo refact
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
        val temperature = bytesBody.decodeToString().toInt(16) - 40
        return Message.TemperatureMessage(temperature)
    }
}
