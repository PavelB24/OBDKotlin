package obdKotlin.messages

import obdKotlin.WorkMode
import obdKotlin.protocol.Protocol

sealed class Message() {

    /**
     * If encoder cant encode the data, it emits that message in the flow
     */
    data class UnknownAnswer(
        val hex: String,
        val bin: ByteArray
    ) : Message()

    data class FuelTrim(
        val trim: Float,
        val bank: Int,
        val isShortTerm: Boolean
    ) : Message()
    data class HybridBatteryLife(val remainingPercent: Double) : Message()
    data class AmbientAirTemperature(val ambientAir: Int) : Message()
    data class AcceleratorPosition(val percent: Float) : Message()
    data class Voltage(val voltage: String) : Message()

    data class BarometricPressure(val atmPressure: Int) : Message()

    data class TimingAdvance(val advance: Int) : Message()
    data class ThrottlePosition(val percent: Float) : Message()

    /**
     * Provides Map with two values: Temperature and is supported
     * If is supported is false, when data may be not valid
     */
    data class TwoSensorsCoolantData(val data: Map<Int, Boolean>) : Message()

    data class ActualGear(val gear: Double) : Message()
    data class EngineTorqueDataPercent(
        val point0: Int,
        val point1: Int,
        val point2: Int,
        val point3: Int,
        val point4: Int
    ) : Message()
    data class FuelPressureDI(val pressure: Double) : Message()
    data class RPM(val rpm: Int) : Message()
    data class OxygenSensorsPresents(val sensors: Map<Pair<Int, Int>, Boolean>) : Message()
    data class MafAirFlowRate(val rateGramPerSec: Float) : Message()
    data class OdometerData(val currentKms: Float) : Message()
    data class AirToFuelRatio(val ratio: Double) : Message()

    data class FuelRate(val fuelRatePerHour: Int) : Message()
    data class TorqueNM(val torque: Int) : Message()
    data class EngineRunTime(val seconds: Int) : Message()
    data class GaugeFuelPressure(val gaugeFuelPressure: Int) : Message()
    data class OilTemperature(val oilTemp: Int) : Message()
    data class CylinderFuelRate(val fuelRatePerHour: Int) : Message()

    data class FuelInjectionTiming(val timing: Int) : Message()

    data class TroubleCodes(
        val codes: List<String>
    ) : Message()

    /**
     * To use in special encoder, put data in your class inherited from CustomData
     */
    data class SpecialObdAnswer(
        val answerData: CustomData
    ) : Message() {
        abstract inner class CustomData
    }
    data class ElmDeviceName(
        val elmVersion: String
    ) : Message()

    data class CommonAtAnswer(
        val messages: String
    ) : Message()

    data class WrongCommandMessage(
        val phase: WorkMode
    ) : Message()

    data class SelectedProtocolMessage(
        val protocol: Protocol
    ) : Message()

    data class MonitorDTCsStatus(
        val isMilOn: Boolean,
        val numberOfErrors: UByte,
        val isSparkTestAvailable: Boolean
    ) : Message()

    data class CatalystTemperature(
        val temperature: Int,
        val bank: Int,
        val sensor: Int
    ) : Message()

    data class SupportedCommands(
        val commands: Map<String, Boolean>,
        val fromHex: String,
        val toHex: String
    ) : Message()

    data class Supported2140Commands(
        val commands: Map<String, Boolean>
    ) : Message()

    data class Supported4160Commands(
        val commands: Map<String, Boolean>
    ) : Message()

    data class Supported6180Commands(
        val commands: Map<String, Boolean>
    ) : Message()

    data class Supported81A0Commands(
        val commands: Map<String, Boolean>
    ) : Message()

    data class SupportedA1C0Commands(
        val commands: Map<String, Boolean>
    ) : Message()

    data class SupportedC1E0Commands(
        val commands: Map<String, Boolean>
    ) : Message()

    data class TemperatureMessage(
        val temperature: Int
    ) : Message()

    data class EngineLoadMessage(
        val load: Float
    ) : Message()

    data class VehicleSpeed(
        val speed: Int
    ) : Message()
}
