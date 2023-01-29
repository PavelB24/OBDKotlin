package obdKotlin.messages

import obdKotlin.WorkMode
import obdKotlin.protocol.Protocol

sealed class Message(
    val entriesCount: Int,
    open val pidOctal: Int?
) {

    /**
     * If encoder cant encode the data, it emits that message in the flow
     */
    data class UnknownAnswer(
        override val pidOctal: Int,
        val answer: ByteArray
    ) : Message(1, pidOctal)

    data class FuelTrim(
        override val pidOctal: Int,
        val trim: Float,
        val bank: Int,
        val isShortTerm: Boolean
    ) : Message(3, pidOctal)

    data class HybridBatteryLife(
        override val pidOctal: Int,
        val remainingPercent: Double
    ) : Message(1, pidOctal)

    data class AmbientAirTemperature(
        override val pidOctal: Int,
        val ambientAir: Int
    ) : Message(1, pidOctal)

    data class AcceleratorPosition(
        override val pidOctal: Int,
        val percent: Float
    ) : Message(1, pidOctal)

    data class Voltage(
        val voltage: String
    ) : Message(1, null)

    data class BarometricPressure(
        override val pidOctal: Int,
        val atmPressure: Int
    ) : Message(1, pidOctal)

    data class TimingAdvance(
        override val pidOctal: Int,
        val advance: Int
    ) : Message(1, pidOctal)
    data class ThrottlePosition(
        override val pidOctal: Int,
        val percent: Float
    ) : Message(1, pidOctal)

    /**
     * Provides Map with two values: Temperature and is supported
     * If is supported is false, when data may be not valid
     */
    data class TwoSensorsCoolantData(
        override val pidOctal: Int,
        val data: Map<Int, Boolean>
    ) : Message(1, pidOctal)

    data class ActualGear(
        override val pidOctal: Int,
        val gear: Double
    ) : Message(1, pidOctal)
    data class EngineTorqueDataPercent(
        override val pidOctal: Int,
        val point0: Int,
        val point1: Int,
        val point2: Int,
        val point3: Int,
        val point4: Int
    ) : Message(5, pidOctal)

    data class FuelPressureDI(
        override val pidOctal: Int,
        val pressure: Double
    ) : Message(1, pidOctal)
    data class RPM(
        override val pidOctal: Int,
        val rpm: Int
    ) : Message(1, pidOctal)
    data class OxygenSensorsPresents(
        override val pidOctal: Int,
        val sensors: Map<Pair<Int, Int>, Boolean>
    ) : Message(1, pidOctal)
    data class MafAirFlowRate(
        override val pidOctal: Int,
        val rateGramPerSec: Float
    ) : Message(1, pidOctal)
    data class OdometerData(
        override val pidOctal: Int,
        val currentKms: Float
    ) : Message(1, pidOctal)
    data class AirToFuelRatio(
        override val pidOctal: Int,
        val ratio: Double
    ) : Message(1, pidOctal)

    data class FuelRate(
        override val pidOctal: Int,
        val fuelRatePerHour: Int
    ) : Message(1, pidOctal)
    data class TorqueNM(
        override val pidOctal: Int,
        val torque: Int
    ) : Message(1, pidOctal)
    data class EngineRunTime(
        override val pidOctal: Int,
        val seconds: Int
    ) : Message(1, pidOctal)
    data class GaugeFuelPressure(
        override val pidOctal: Int,
        val gaugeFuelPressure: Int
    ) : Message(1, pidOctal)
    data class OilTemperature(
        override val pidOctal: Int,
        val oilTemp: Int
    ) : Message(1, pidOctal)
    data class CylinderFuelRate(
        override val pidOctal: Int,
        val fuelRatePerHour: Int
    ) : Message(1, pidOctal)

    data class FuelInjectionTiming(
        override val pidOctal: Int,
        val timing: Int
    ) : Message(1, pidOctal)

    data class TroubleCodes(
        override val pidOctal: Int,
        val codes: List<String>
    ) : Message(1, pidOctal)

    /**
     * To use in special encoder, put data in your class inherited from CustomData
     */
    class SpecialObdAnswer(
        override val pidOctal: Int,
        val answerData: CustomData
    ) : Message(1, pidOctal) {
        abstract inner class CustomData
    }

    data class ElmDeviceName(
        val elmVersion: String
    ) : Message(1, null)

    data class CommonAtAnswer(
        val messages: String
    ) : Message(1, null)

    data class WrongCommandMessage(
        val phase: WorkMode
    ) : Message(1, null)

    data class SelectedProtocolMessage(
        val protocol: Protocol
    ) : Message(1, null)

    data class MonitorDTCsStatus(
        override val pidOctal: Int,
        val isMilOn: Boolean,
        val numberOfErrors: UByte,
        val isSparkTestAvailable: Boolean
    ) : Message(3, pidOctal)

    data class CatalystTemperature(
        override val pidOctal: Int,
        val temperature: Int,
        val bank: Int,
        val sensor: Int
    ) : Message(3, pidOctal)

    data class SupportedCommands(
        override val pidOctal: Int,
        val commands: Map<String, Boolean>,
        val fromHex: String,
        val toHex: String
    ) : Message(3, pidOctal)

    data class CoolantTemperature(
        override val pidOctal: Int,
        val temperature: Int
    ) : Message(1, pidOctal)

    data class EngineLoad(
        override val pidOctal: Int,
        val load: Float
    ) : Message(1, pidOctal)

    data class VehicleSpeed(
        override val pidOctal: Int,
        val speed: Int
    ) : Message(1, pidOctal)

    data class OxygenSensorData(
        override val pidOctal: Int,
        val sensorNum: Int,
        val voltage: Float,
        val shortTermFuelTrim: Float
    ) : Message(3, pidOctal)

    data class IntakeAirTemperature(
        override val pidOctal: Int,
        val celsius: Int
    ) : Message(1, pidOctal)

    data class ManifoldPressure(
        override val pidOctal: Int,
        val pressureKpa: Int
    ) : Message(1, pidOctal)

    data class FuelSysStatus(
        override val pidOctal: Int,
        val fuelSystemState: FuelSystemState?
    ) : Message(1, pidOctal) {

        enum class FuelSystemState {
            MOTOR_OFF, OPEN_LOOP_DUE_LOW_ENG_TEMP,
            CLOSED_LOOP_DUE_OXY_SENSOR,
            OPEN_LOOP_DUE_LOAD_OR_DECELERATION, OPEN_LOOP_DUE_FAILTURE,
            CLOSED_LOOP_DUE_FEEDBACK_SYS_FAULT
        }
    }

    data class RelativeThrottlePosition(
        override val pidOctal: Int,
        val position: Float
    ) : Message(1, pidOctal)

    data class DistanceAfterCodesCleared(
        override val pidOctal: Int,
        val distance: Int
    ) : Message(1, pidOctal)

    data class EgrError(
        override val pidOctal: Int,
        val error: Float
    ) : Message(1, pidOctal)

    data class CommandedEgr(
        override val pidOctal: Int,
        val commandedEgr: Float
    ) : Message(1, pidOctal)

    data class FuelLevel(
        override val pidOctal: Int,
        val level: Float
    ) : Message(1, pidOctal)

    data class WarmUpsSinceCodesCleaned(
        override val pidOctal: Int,
        val toInt: Int
    ) : Message(1, pidOctal)

    data class AbsoluteLoad(
        override val pidOctal: Int,
        val load: Float
    ) : Message(1, pidOctal)

    data class EcuVoltage(
        override val pidOctal: Int,
        val voltage: Int
    ) : Message(1, pidOctal)

    data class OxygenSensorDataByCurrent(
        override val pidOctal: Int,
        val sensor: Int,
        val ratio: Float,
        val ma: Int
    ) : Message(3, pidOctal)

    data class OxygenSensorDataByVoltage(
        override val pidOctal: Int,
        val sensor: Int,
        val ratio: Float,
        val voltage: Float
    ) : Message(3, pidOctal)
}
