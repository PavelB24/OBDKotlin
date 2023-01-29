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
            0x00 -> {
                decodeSupportedPids(pid, bytesBody, 1)
            }

            0x01 -> {
                decodeStatusSinceDTCCleared(pid, bytesBody)
            }

            0x5B -> {
                decodeHybridBatteryLife(pid, bytesBody)
            }

            0x03 -> {
                decodeFuelSystemStatus(pid, bytesBody)
            }

            0x04 -> {
                decodeEngineLoad(pid, bytesBody)
            }

            0x05 -> {
                decodeCoolantTemperature(pid, bytesBody)
            }

            0x13 -> {
                oxygenSensorsPresents(pid, bytesBody)
            }

            0x0D -> {
                decodeCarSpeed(pid, bytesBody)
            }

            0x10 -> {
                decodeMafFlowRate(pid, bytesBody) // gram/sec
            }

            0x11 -> {
                decodeThrottlePosition(pid, bytesBody)
            }

            0x14 -> {
                decodeOxygenSensorData(pid, bytesBody, 1)
            }

            0x15 -> {
                decodeOxygenSensorData(pid, bytesBody, 2)
            }

            0x16 -> {
                decodeOxygenSensorData(pid, bytesBody, 3)
            }

            0x17 -> {
                decodeOxygenSensorData(pid, bytesBody, 4)
            }

            0x18 -> {
                decodeOxygenSensorData(pid, bytesBody, 5)
            }

            0x19 -> {
                decodeOxygenSensorData(pid, bytesBody, 6)
            }

            0x1A -> {
                decodeOxygenSensorData(pid, bytesBody, 7)
            }

            0x1B -> {
                decodeOxygenSensorData(pid, bytesBody, 8)
            }

            0x0B -> {
                decodeManifoldPressure(pid, bytesBody)
            }

            0x0E -> {
                decodeTimingAdvance(pid, bytesBody) // Опережение зажигания
            }

            0x33 -> {
                decodeBarometricPressure(pid, bytesBody)
            }

            0x5A -> {
                decodeAcceleratorPosition(pid, bytesBody)
            }

            0x0C -> {
                decodeRPM(pid, bytesBody)
            }

            0x0F -> {
                decodeIntakeAirTemperature(pid, bytesBody)
            }

            0x23 -> {
                decodeFuelPressureDI(pid, bytesBody)
            }

            0x24 -> {
                decodeAirRatioByOxySensorVoltage(pid, bytesBody, 1)
            }

            0x25 -> {
                decodeAirRatioByOxySensorVoltage(pid, bytesBody, 2)
            }

            0x26 -> {
                decodeAirRatioByOxySensorVoltage(pid, bytesBody, 3)
            }

            0x27 -> {
                decodeAirRatioByOxySensorVoltage(pid, bytesBody, 4)
            }

            0x28 -> {
                decodeAirRatioByOxySensorVoltage(pid, bytesBody, 5)
            }

            0x29 -> {
                decodeAirRatioByOxySensorVoltage(pid, bytesBody, 6)
            }

            0x2A -> {
                decodeAirRatioByOxySensorVoltage(pid, bytesBody, 7)
            }

            0x2B -> {
                decodeAirRatioByOxySensorVoltage(pid, bytesBody, 8)
            }

            0x2C -> {
                decodeCommandedEgr(pid, bytesBody)
            }

            0x2D -> {
                decodeEgrError(pid, bytesBody)
            }

            0x2F -> {
                decodeFuelTankLevel(pid, bytesBody)
            }

            0x30 -> {
                decodeWarmUpsAfterCodesCleared(pid, bytesBody)
            }

            0x31 -> {
                decodeDistanceAfterCodesCleared(pid, bytesBody)
            }

            0x34 -> {
                decodeAirRatioByOxySensorCurrent(pid, bytesBody, 1)
            }

            0x35 -> {
                decodeAirRatioByOxySensorCurrent(pid, bytesBody, 2)
            }

            0x36 -> {
                decodeAirRatioByOxySensorCurrent(pid, bytesBody, 3)
            }

            0x37 -> {
                decodeAirRatioByOxySensorCurrent(pid, bytesBody, 4)
            }

            0x38 -> {
                decodeAirRatioByOxySensorCurrent(pid, bytesBody, 5)
            }

            0x39 -> {
                decodeAirRatioByOxySensorCurrent(pid, bytesBody, 6)
            }

            0x3A -> {
                decodeAirRatioByOxySensorCurrent(pid, bytesBody, 7)
            }

            0x3B -> {
                decodeAirRatioByOxySensorCurrent(pid, bytesBody, 8)
            }

            0x42 -> {
                decodeEcuVoltage(pid, bytesBody)
            }

            0x43 -> {
                decodeAbsLoad(pid, bytesBody)
            }

            0x45 -> {
                decodeRelativeThrottlePosition(pid, bytesBody)
            }

            0x46 -> {
                decodeAmbientAirTemperature(pid, bytesBody)
            }

            0x20 -> {
                decodeSupportedPids(pid, bytesBody, 2)
            }

            0x40 -> {
                decodeSupportedPids(pid, bytesBody, 3)
            }

            0x60 -> {
                decodeSupportedPids(pid, bytesBody, 4)
            }

            0x80 -> {
                decodeSupportedPids(pid, bytesBody, 5)
            }

            0xA0 -> {
                decodeSupportedPids(pid, bytesBody, 6)
            }

            0xC0 -> {
                decodeSupportedPids(pid, bytesBody, 7)
            }

            0x1F -> {
                decodeEngineRunTime(pid, bytesBody)
            }

            0x06 -> {
                decodeFuelTrim(pid, bytesBody, 1, true)
            }

            0x07 -> {
                decodeFuelTrim(pid, bytesBody, 1, false)
            }

            0x08 -> {
                decodeFuelTrim(pid, bytesBody, 2, true)
            }

            0x09 -> {
                decodeFuelTrim(pid, bytesBody, 2, false)
            }

            0x3C -> {
                decodeCatalystTemperature(pid, bytesBody, 1, 1)
            }

            0x3D -> {
                decodeCatalystTemperature(pid, bytesBody, 2, 1)
            }

            0x3E -> {
                decodeCatalystTemperature(pid, bytesBody, 1, 2)
            }

            0x3F -> {
                decodeCatalystTemperature(pid, bytesBody, 2, 2)
            }

            0x0A -> {
                decodeGaugeFuelPressure(pid, bytesBody)
            }

            0x44 -> {
                decodeAirFuelRatio(pid, bytesBody)
            }

            0x63 -> {
                decodeTorqueInNM(pid, bytesBody)
            }

            0x64 -> {
                decodeEngineTorqueGraph(pid, bytesBody)
            }

            0x5C -> {
                decodeOilTemperature(pid, bytesBody)
            }

            0x5D -> {
                decodeFuelInjectionTiming(pid, bytesBody)
            }

            0x5E -> {
                decodeFuelRate(pid, bytesBody) //  L/h
            }

            0xA2 -> {
                decodeCylinderFuelRate(pid, bytesBody)
            }

            0xA4 -> {
                decodeTransmissionActualGear(pid, bytesBody)
            }

            0xA6 -> {
                decodeOdometer(pid, bytesBody)
            }

            0x67 -> {
                decodeTwoSensorsCoolantTemperature(pid, bytesBody)
            }

            else -> {
                pid?.let {
                    eventFlow.emit(Message.UnknownAnswer(it, bytesBody))
                }
                null
            }
        }
        encodedMessage?.let {
            eventFlow.emit(encodedMessage)
            return EncodingState.Successful
        }
        return EncodingState.Unsuccessful(bytesBody.decodeToString())
    }

    private fun decodeRelativeThrottlePosition(pid: Int, bytesBody: ByteArray): Message {
        val position = 2.55f * bytesBody.decodeToString(0, 2).toInt(16)
        return Message.RelativeThrottlePosition(pid, position)
    }

    private fun decodeDistanceAfterCodesCleared(pid: Int, bytesBody: ByteArray): Message {
        val distance = 256 * bytesBody.decodeToString(0, 2).toInt(16) -
            bytesBody.decodeToString(2, 4).toInt(16)
        return Message.DistanceAfterCodesCleared(pid, distance)
    }

    private fun decodeEgrError(pid: Int, bytesBody: ByteArray): Message {
        val error = 100 / 128f *
            bytesBody.decodeToString(0, 2).toInt(16) - 100
        return Message.EgrError(pid, error)
    }

    private fun decodeCommandedEgr(pid: Int, bytesBody: ByteArray): Message {
        val commandedEgr = 2.55f *
            bytesBody.decodeToString(0, 2).toInt(16)
        return Message.CommandedEgr(pid, commandedEgr)
    }

    private fun decodeFuelTankLevel(pid: Int, bytesBody: ByteArray): Message {
        val level = 2.55f *
            bytesBody.decodeToString(0, 2).toInt(16)
        return Message.FuelLevel(pid, level)
    }

    private fun decodeWarmUpsAfterCodesCleared(pid: Int, bytesBody: ByteArray): Message {
        return Message.WarmUpsSinceCodesCleaned(
            pid,
            bytesBody.decodeToString(0, 2).toInt(16)
        )
    }

    private fun decodeAbsLoad(pid: Int, bytesBody: ByteArray): Message {
        val load = 2.55f * (
            256 * bytesBody.decodeToString(0, 2).toInt(16) +
                bytesBody.decodeToString(2, 4).toInt(16)
            )
        return Message.AbsoluteLoad(pid, load)
    }

    private fun decodeEcuVoltage(pid: Int, bytesBody: ByteArray): Message {
        val voltage = (
            256 * bytesBody.decodeToString(0, 2).toInt(16) +
                bytesBody.decodeToString(2, 4).toInt(16)
            ) / 1000
        return Message.EcuVoltage(pid, voltage)
    }

    private fun decodeAirRatioByOxySensorCurrent(
        pid: Int,
        bytesBody: ByteArray,
        sensor: Int
    ): Message {
        val ratio = 2f / 65536 * (
            256 * bytesBody.decodeToString(0, 2).toInt(16) +
                bytesBody.decodeToString(2, 4).toInt(16)
            )
        val ma = (
            256 * bytesBody.decodeToString(4, 6).toInt(16) +
                bytesBody.decodeToString(6, 8).toInt(16)
            ) / 256 - 128
        return Message.OxygenSensorDataByCurrent(pid, sensor, ratio, ma)
    }

    private fun decodeAirRatioByOxySensorVoltage(
        pid: Int,
        bytesBody: ByteArray,
        sensor: Int
    ): Message {
        val ratio = 2f / 65536 * (
            256 * bytesBody.decodeToString(0, 2).toInt(16) +
                bytesBody.decodeToString(2, 4).toInt(16)
            )
        val voltage = 8f / 65536 * (
            256 * bytesBody.decodeToString(4, 6).toInt(16) +
                bytesBody.decodeToString(6, 8).toInt(16)
            )
        return Message.OxygenSensorDataByVoltage(pid, sensor, ratio, voltage)
    }

    private fun decodeIntakeAirTemperature(pid: Int, bytesBody: ByteArray): Message {
        return Message.IntakeAirTemperature(
            pid,
            bytesBody.decodeToString(0, 2).toInt(16) - 40
        )
    }

    private fun decodeManifoldPressure(pid: Int, bytesBody: ByteArray): Message {
        return Message.ManifoldPressure(
            pid,
            bytesBody.decodeToString(0, 2).toInt(16) * 3
        )
    }

    private fun decodeFuelSystemStatus(pid: Int, bytesBody: ByteArray): Message {
        val state = when (bytesBody.decodeToString(0, 2).toInt(16)) {
            0x00 -> {
                Message.FuelSysStatus.FuelSystemState.MOTOR_OFF
            }

            0x01 -> {
                Message.FuelSysStatus.FuelSystemState.OPEN_LOOP_DUE_LOW_ENG_TEMP
            }

            0x02 -> {
                Message.FuelSysStatus.FuelSystemState.CLOSED_LOOP_DUE_OXY_SENSOR
            }

            0x04 -> {
                Message.FuelSysStatus.FuelSystemState.OPEN_LOOP_DUE_LOAD_OR_DECELERATION
            }

            0x08 -> {
                Message.FuelSysStatus.FuelSystemState.OPEN_LOOP_DUE_FAILTURE
            }

            0x16 -> {
                Message.FuelSysStatus.FuelSystemState.CLOSED_LOOP_DUE_FEEDBACK_SYS_FAULT
            }

            else -> {
                null
            }
        }
        return Message.FuelSysStatus(pid, state)
    }

    private fun decodeOxygenSensorData(pid: Int, bytesBody: ByteArray, oSensorNumber: Int): Message {
        val first = bytesBody.decodeToString(0, 2).toInt(16)
        val second = bytesBody.decodeToString(2, 4).toInt(16)
        return Message.OxygenSensorData(pid, oSensorNumber, first / 200f, 0.28f * second - 100)
    }

    private fun decodeTransmissionActualGear(pid: Int, bytesBody: ByteArray): Message {
        // todo refact
        val isSupported = bytesBody.decodeToString(0, 2).toInt(16).toBinary()[6]
        val hex = bytesBody.decodeToString(4, 8)
        val gear = (256 * hex.toInt(16)) / 1000.0
        return Message.ActualGear(pid, gear)
    }

    private fun decodeOdometer(pid: Int, bytesBody: ByteArray): Message {
        val kilometers = (bytesBody.decodeToString().toUInt(16) / 10u).toFloat()
        return Message.OdometerData(pid, kilometers)
    }

    private fun decodeCylinderFuelRate(pid: Int, bytesBody: ByteArray): Message {
        val hex = bytesBody.decodeToString(0, 4)
        val rate = (256 * hex.toInt(16)) / 32
        return Message.CylinderFuelRate(pid, rate)
    }

    private fun decodeTwoSensorsCoolantTemperature(pid: Int, bytesBody: ByteArray): Message {
        val sourceInt = bytesBody.decodeToString(0, 2).toInt(16)
        val aSupported = (sourceInt and 1) == 1
        val bSupported = (sourceInt shr 1 and 1) == 1
        val coolant1 = bytesBody.decodeToString(2, 4).toInt(16) - 40
        val coolant2 = bytesBody.decodeToString(4, 6).toInt(16) - 40
        val map = mapOf(Pair(coolant1, aSupported), Pair(coolant2, bSupported))
        return Message.TwoSensorsCoolantData(pid, map)
    }

    private fun decodeFuelRate(pid: Int, bytesBody: ByteArray): Message {
        val hex = bytesBody.decodeToString(0, 4)
        val fuelRate = (256 * hex.toInt(16)) / 20
        return Message.FuelRate(pid, fuelRate)
    }

    private fun decodeFuelInjectionTiming(pid: Int, bytesBody: ByteArray): Message {
        val hex = bytesBody.decodeToString(0, 4)
        val timing = (256 * hex.toInt(16)) / 128 - 210
        return Message.FuelInjectionTiming(pid, timing)
    }

    private fun decodeOilTemperature(pid: Int, bytesBody: ByteArray): Message {
        val oilTemperature = bytesBody.decodeToString(0, 2).toInt(16) - 40
        return Message.OilTemperature(pid, oilTemperature)
    }

    private fun decodeHybridBatteryLife(pid: Int, bytesBody: ByteArray): Message {
        val hex = bytesBody.decodeToString(0, 2)
        val remainingPercent = 100 / 255.00 * hex.toInt(16)
        return Message.HybridBatteryLife(pid, remainingPercent)
    }

    private fun decodeEngineTorqueGraph(pid: Int, bytesBody: ByteArray): Message {
        // ? Should check
        val idlePoint = bytesBody.decodeToString(0, 2).toInt(16) - 125
        val pointB = bytesBody.decodeToString(2, 4).toInt(16) - 125
        val pointC = bytesBody.decodeToString(4, 6).toInt(16) - 125
        val pointD = bytesBody.decodeToString(6, 8).toInt(16) - 125
        val pointE = bytesBody.decodeToString(6, 8).toInt(16) - 125
        return Message.EngineTorqueDataPercent(pid, idlePoint, pointB, pointC, pointD, pointE)
    }

    private fun decodeTorqueInNM(pid: Int, bytesBody: ByteArray): Message {
        val hex = bytesBody.decodeToString(0, 4)
        val torque = 256 * hex.toInt(16)
        return Message.TorqueNM(pid, torque)
    }

    private fun decodeAirFuelRatio(pid: Int, bytesBody: ByteArray): Message {
        val hex = bytesBody.decodeToString(0, 4)
        val airFuelRatio = 2 / 65536.0 * (256 * hex.toInt(16))
        return Message.AirToFuelRatio(pid, airFuelRatio)
    }

    private fun decodeGaugeFuelPressure(pid: Int, bytesBody: ByteArray): Message {
        val pressure = bytesBody.decodeToString(0, 2).toInt(16) * 3
        return Message.GaugeFuelPressure(pid, pressure)
    }

    private fun decodeFuelTrim(pid: Int, bytesBody: ByteArray, bank: Int, shortTerm: Boolean): Message {
        val hex = bytesBody.decodeToString(0, 2)
        val trim: Float = hex.toInt(16) / 1.28f - 100
        return Message.FuelTrim(pid, trim, bank, shortTerm)
    }

    private fun decodeCatalystTemperature(
        pid: Int,
        bytesBody: ByteArray,
        bank: Int,
        sensor: Int
    ): Message {
        val hex = bytesBody.decodeToString(0, 4)
        val catalystTemperature = (256 * hex.toInt(16)) / 10 - 40
        return Message.CatalystTemperature(pid, catalystTemperature, bank, sensor)
    }

    private fun decodeEngineRunTime(pid: Int, bytesBody: ByteArray): Message {
        val hex = bytesBody.decodeToString(0, 4)
        val runTime = 256 * hex.toInt(16)
        return Message.EngineRunTime(pid, runTime)
    }

    private fun oxygenSensorsPresents(pid: Int, bytesBody: ByteArray): Message {
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
        return Message.OxygenSensorsPresents(pid, map)
    }

    private fun decodeMafFlowRate(pid: Int, bytesBody: ByteArray): Message {
        val hex = bytesBody.decodeToString(0, 4)
        val rate: Float = (256 * hex.toInt(16)) / 100.00f
        return Message.MafAirFlowRate(pid, rate)
    }

    private fun decodeAmbientAirTemperature(pid: Int, bytesBody: ByteArray): Message {
        val ambientTemperature = bytesBody.decodeToString(0, 2)
            .toInt(16) - 40
        return Message.AmbientAirTemperature(pid, ambientTemperature)
    }

    private fun decodeFuelPressureDI(pid: Int, bytesBody: ByteArray): Message {
        val hex = bytesBody.decodeToString(0, 4)
        val pressure = 10.0 * (256 * hex.toInt(16))
        return Message.FuelPressureDI(pid, pressure)
    }

    private fun decodeRPM(pid: Int, bytesBody: ByteArray): Message {
        val hex = bytesBody.decodeToString(0, 4)
        val rpm = (256 * hex.toInt(16)) / 4
        return Message.RPM(pid, rpm)
    }

    private fun decodeAcceleratorPosition(pid: Int, bytesBody: ByteArray): Message {
        val acceleratorPosition = bytesBody.decodeToString(0, 2).toInt(16) * 2.55f
        return Message.AcceleratorPosition(pid, acceleratorPosition)
    }

    private fun decodeBarometricPressure(pid: Int, bytesBody: ByteArray): Message {
        val atmPress = bytesBody.decodeToString(0, 2).toInt(16)
        return Message.BarometricPressure(pid, atmPress)
    }

    private fun decodeTimingAdvance(pid: Int, bytesBody: ByteArray): Message {
        val advance = bytesBody.decodeToString(0, 2).toInt(16) / 2 - 64
        return Message.TimingAdvance(pid, advance)
    }

    private fun decodeThrottlePosition(pid: Int, bytesBody: ByteArray): Message {
        val position = bytesBody.decodeToString(0, 2).toInt(16) / 2.55f
        return Message.ThrottlePosition(pid, position)
    }

    private fun decodeCarSpeed(pid: Int, bytesBody: ByteArray): Message {
        val speed = bytesBody.decodeToString(0, 2).toInt(16)
        return Message.VehicleSpeed(pid, speed)
    }

    private fun decodeEngineLoad(pid: Int, bytesBody: ByteArray): Message {
        val load = bytesBody.decodeToString(0, 2).toInt(16) / 2.55f
        return Message.EngineLoad(pid, load)
    }

    private fun decodeSupportedPids(pid: Int, dataBytes: ByteArray, part: Int): Message {
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
        return Message.SupportedCommands(pid, map, pidOffset.toHex(), (pidOffset + 32).toHex())
    }

    private fun decodeStatusSinceDTCCleared(pid: Int, dataBytes: ByteArray): Message {
        // todo refact
        val bitsOfAByte = dataBytes.first().toUByte().toBinary(8).toCharArray()
        val bitsOfBByte = dataBytes[1].toUByte().toBinary(8).toCharArray()
        val milIsOn = bitsOfAByte.last().toBoolean()
        val numberOfErrors = dataBytes.copyOfRange(0, 6).toString().toUByte(2)
        val isSparkTestAvailable = bitsOfBByte[3].toBoolean()
        return Message.MonitorDTCsStatus(
            pid,
            milIsOn,
            numberOfErrors,
            isSparkTestAvailable
        )
    }

    private fun decodeCoolantTemperature(pid: Int, bytesBody: ByteArray): Message {
        val temperature = bytesBody.decodeToString().toInt(16) - 40
        return Message.CoolantTemperature(pid, temperature)
    }
}
