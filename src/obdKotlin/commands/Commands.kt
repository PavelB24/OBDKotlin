@file:Suppress("unused")

package obdKotlin.commands

const val AT_PREFIX = "AT"

internal abstract class Commands {
    internal enum class PidMod(val hex: String, val positiveCode: String) {

        SHOW_CURRENT("01", "41"),
        SHOW_FREEZE_FRAME("02", "42"),
        SHOW_DIAGNOSTIC_TROUBLES_CODES("03", "43"),
        CLEAR_TROUBLES_CODES_AND_STORE_VAL("04", "44"),
        TEST_RESULTS_OXY_SENSORS("05", "45"),
        TEST_RESULTS_OXY_SENSORS_CAN("06", "46"),
        SHOW_PENDING_DIAGN_TROUBLES_CODES("07", "47"),
        CONTROL_ON_BOARD_SYS("08", "48"),
        VEHICLE_INFO_REQUEST("09", "49"),
        DELETED_ERRORS("0A", "4A"),
        CHECK_ON_CAN("", "")
    }

    internal enum class AtCommands(val command: String) {

        Repeat("\r"), // Repeat last command
        EchoOff("${AT_PREFIX}E0\r"), // Set Echo Off
        ResetAll("${AT_PREFIX}Z\r"),
        SetWakeUpMessagesOff("${AT_PREFIX}SW\r"),
        FastInit("${AT_PREFIX}FI\r"),
        AllowLongMessages("${AT_PREFIX}AL\r"),
        GetVehicleProtoAsNumber("${AT_PREFIX}DPN\r"),
        PrintingSpacesOff("${AT_PREFIX}S0\r"),
        SetHeader("${AT_PREFIX}SH"),
        SetProto("${AT_PREFIX}SP"),
        TryProto("${AT_PREFIX}TR\r"),
        CanExtAdr("${AT_PREFIX}CAE\r"), // Just CAE to turn off, use CAE hh for turn on
        FlowControlOff("${AT_PREFIX}CFC0\r"),
        FlowControlOn("${AT_PREFIX}CFC1\r"),
        SetReceiverAdrFilter("${AT_PREFIX}CRA"), // Just CRA for reset or hhh or hhhhhhhh for set
        AdaptiveTimingOn("${AT_PREFIX}AT1\r"),
        AutoFormatCanFramesOff("${AT_PREFIX}CAF0\r"),
        WarmStart("${AT_PREFIX}WS\r")
    }
}