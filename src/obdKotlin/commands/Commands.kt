@file:Suppress("unused")

package obdKotlin.commands

const val AT_PREFIX = "AT"
const val POSTFIX = "\r"

internal abstract class Commands {
    enum class PidMod(val hex: String, val positiveCode: String) {

        SHOW_CURRENT("01", "41"),
        SHOW_FREEZE_FRAME("02", "42"),
        SHOW_DIAGNOSTIC_TROUBLES_CODES("03", "43"),
        CLEAR_TROUBLES_CODES_AND_STORE_VAL("04", "44"),
        TEST_RESULTS_OXY_SENSORS("05", "45"),
        TEST_RESULTS_OXY_SENSORS_CAN("06", "46"),
        SHOW_PENDING_DIAGN_TROUBLES_CODES("07", "47"),
        CONTROL_ON_BOARD_SYS("08", "48"),
        VEHICLE_INFO_REQUEST("09", "49"),
        DELETED_ERRORS("0A", "4A")
    }


    enum class AtCommands(val command: String) {




        Repeat(POSTFIX), //Repeat last command
        EchoOff("${AT_PREFIX}E0"), //Set Echo Off
        ResetAll("${AT_PREFIX}Z"),
        SetWakeUpMessagesOff("${AT_PREFIX}SW"),
        FastInit("${AT_PREFIX}FI"),
        AllowLongMessages("${AT_PREFIX}AL"),
        GetVehicleProtoAsNumber("${AT_PREFIX}DPN"),
        PrintingSpacesOff("${AT_PREFIX}S0"),
        SetHeader("${AT_PREFIX}SH"),
        SetProto("${AT_PREFIX}SP"),
        TryProto("${AT_PREFIX}TR"),
        CanExtAdr("${AT_PREFIX}CAE"), // Just CAE to turn off, use CAE hh for turn on
        FlowControlOff("${AT_PREFIX}CFC0"),
        FlowControlOn("${AT_PREFIX}CFC1"),
        SetReceiverAdrFilter("${AT_PREFIX}CRA"), //Just CRA for reset or hhh or hhhhhhhh for set
        AdaptiveTimingOn("${AT_PREFIX}AT1"),
        AutoFormatCanFramesOff("${AT_PREFIX}CAF0"),
        WarmStart("${AT_PREFIX}WS")
//        SetBaudRateDivisor("${AT_PREFIX}BRD"),
//        Defaults("${AT_PREFIX}D"), //Set All to Defaults

//        EchoOn("${AT_PREFIX}E1"), //Set Echo on
//        ForgetEvents("${AT_PREFIX}FE"), //ForgetEvents
//        PrintId("${AT_PREFIX}I"), //PrintVersionId
//        LineFeedsOn("${AT_PREFIX}L1"),
//        LineFeedsOff("${AT_PREFIX}L0"),
//        LowPowerMode("${AT_PREFIX}LP"),
//        MemoryOff("${AT_PREFIX}M0"),
//        MemoryOn("${AT_PREFIX}M1"),
//        ReadStoredData(""),
//        SaveDataByte(""),

//        DisplayTheDeviceDescription("${AT_PREFIX}@1"),
//        DisplayVIN("${AT_PREFIX}@2"),
//        StoreTheVIN("${AT_PREFIX}@3"),


        /**
         * Здесь мы указываем, что elm в качастве фрейма fc всегда будет сообщать ЭБУ, что тот может посылать
         * любое количество оставшихся фреймов ответа, с любым временным интервалом между ними.
         * Когда команда или ответ должны состоять больше чем из одного фрейма, отправляющая сторона всегда посылает
         * только один, первый фрейм.
         * Принимающая сторона должна послать фрейм fc, в котором сообщает как слать оставшиеся фреймы.
         */
//        SetTimeOutToMax("${AT_PREFIX}FCSD300000"), //
//        ReadVoltage("${AT_PREFIX}RV"),

//        GetActiveMonitors("${AT_PREFIX}AMC"),
//        AutoReceive("${AT_PREFIX}AR"),
//        GetReceivedBytes("${AT_PREFIX}BD"),
//        GetCurrentProto("${AT_PREFIX}DP"),

//        HeadersOn("${AT_PREFIX}H1"),
//        HeadersOff("${AT_PREFIX}H0"),
//        MonitorAll("${AT_PREFIX}MA"),
//        CloseCurrentProto("${AT_PREFIX}PC"),
//        ResponsesOff("${AT_PREFIX}R0"),
//        ResponsesOn("${AT_PREFIX}R1"),

//        PrintingSpacesOn("${AT_PREFIX}S1"),

//        SetProtoAndAutoSearch("${AT_PREFIX}SPA"),
//        SetReceiverAddress("${AT_PREFIX}SR"),
//        UseStandardSearchOrder("${AT_PREFIX}SS"),

//        TryWithAutoSearch("${AT_PREFIX}TRA"),
//        EraseStoredProto("${AT_PREFIX}SP00"),


//        PerformFastInit("${AT_PREFIX}FI"),
//        SetIsoBaud("${AT_PREFIX}IB"),
//        DisplayDLCOn("${AT_PREFIX}D1"),
//        DisplayDLCOff("${AT_PREFIX}D0"),
//        SetFlowControlMode("${AT_PREFIX}FCSM"),
//        SetFlowControlHeader("${AT_PREFIX}FCSH"),
//        FlowControlSetDataTo("${AT_PREFIX}FCSD"),
//        SentRTRMsg("${AT_PREFIX}RTR"),
//        UseVarDLCOn("${AT_PREFIX}V1"),
//        UseVarDLCOff("${AT_PREFIX}V0"),


//        SetCanIdFilter("${AT_PREFIX}CF"), //hhh or hhhhhhhh
//        FlowControlOn("${AT_PREFIX}CFC1"),

//        GetCanStatusCount("${AT_PREFIX}CS"),
//        ClientMonitoringOff("${AT_PREFIX}CSM0"),
//        ClientMonitoringOn("${AT_PREFIX}CSM1"),
//        SetIdMask("${AT_PREFIX}CM"), //hhh hhhhhhhh

//        AdaptiveTimingOff("${AT_PREFIX}AT0"),
//        AutoFormatCanFramesOn("${AT_PREFIX}CAF1"),



    }

//    enum class PidCommands(val hexCommand: String) {
//
//        SUPPORTED_PIDS0120("${PidMod.SHOW_CURRENT.hex}$pid00$POSTFIX"),
//        SUPPORTED_PIDS2140("${PidMod.SHOW_CURRENT.hex}$pid20$POSTFIX"),
//        SUPPORTED_PIDS4160("${PidMod.SHOW_CURRENT.hex}$pid40$POSTFIX"),
//        SUPPORTED_PIDS6180("${PidMod.SHOW_CURRENT.hex}$pid60$POSTFIX"),
//        GET_DETECTED_ERRORS("${PidMod.SHOW_CURRENT.hex}$pid02$POSTFIX"),
//        STATUS_SINCE_DTC_CLEARED("${PidMod.SHOW_CURRENT.hex}$pid01$POSTFIX"),
//        ENGINE_LOAD("${PidMod.SHOW_CURRENT.hex}$pid04$POSTFIX"),
//        COOLANT_TEMPERATURE("${PidMod.SHOW_CURRENT.hex}$pid05$POSTFIX"),
//        FUEL_PRESSURE("${PidMod.SHOW_CURRENT.hex}$pid0A$POSTFIX"),
//        MAP_VAL("${PidMod.SHOW_CURRENT.hex}$pid0B$POSTFIX"),
//        ENGINE_RPM("${PidMod.SHOW_CURRENT.hex}$pid0C$POSTFIX"),
//        CAR_SPEED("${PidMod.SHOW_CURRENT.hex}$pid0D$POSTFIX"),
//        TIMING_ADVANCE("${PidMod.SHOW_CURRENT.hex}$pid0E$POSTFIX"),
//        MAP_AIR_FLOW("${PidMod.SHOW_CURRENT.hex}$pid10$POSTFIX"),
//        INTAKE_AIR_TEMP("${PidMod.SHOW_CURRENT.hex}$pid0F$POSTFIX"),
//        THROTTLE_POSITION("${PidMod.SHOW_CURRENT.hex}$pid11$POSTFIX"),
//        HAS_OXYGEN_SENSORS("${PidMod.SHOW_CURRENT.hex}$pid13$POSTFIX"),
//        ENGINE_RUN_TIME("${PidMod.SHOW_CURRENT.hex}$pid1F$POSTFIX"),
//        CHECK_ENGINE_DISTANCE("${PidMod.SHOW_CURRENT.hex}$pid4D$POSTFIX"),
//        CHECK_ENGINE_TIME("${PidMod.SHOW_CURRENT.hex}$pid21$POSTFIX"),
//        FUEL_RAIL_PRESSURE("${PidMod.SHOW_CURRENT.hex}$pid23$POSTFIX"), //Direct injection systems
//        FUEL_LEVEL("${PidMod.SHOW_CURRENT.hex}$pid2F$POSTFIX"),
//        BAROMETRIC_PRESSURE("${PidMod.SHOW_CURRENT.hex}$pid33$POSTFIX"),
//        EGR_ERROR("${PidMod.SHOW_CURRENT.hex}$pid2D$POSTFIX"),
//        ABSOLUTE_LOAD("${PidMod.SHOW_CURRENT.hex}$pid43$POSTFIX"),
//        AMBIENT_AIR_TEMP("${PidMod.SHOW_CURRENT.hex}$pid46$POSTFIX"),
//        THROTTLE_B_POSITION("${PidMod.SHOW_CURRENT.hex}$pid47$POSTFIX"),
//        THROTTLE_C_POSITION("${PidMod.SHOW_CURRENT.hex}$pid48$POSTFIX"),
//        FUEL_TYPE("${PidMod.SHOW_CURRENT.hex}$pid52$POSTFIX"),
//        STORED_ERRORS_CODES("${PidMod.SHOW_DIAGNOSTIC_TROUBLES_CODES.hex}$POSTFIX"),
//        CLEAR_ERRORS("${PidMod.CLEAR_TROUBLES_CODES_AND_STORE_VAL.hex}$POSTFIX"),
//        GET_CIN("${PidMod.VEHICLE_INFO_REQUEST.hex}$pid02$POSTFIX"),
//        GET_ECU_NAME_MESSAGE_COUNT("${PidMod.VEHICLE_INFO_REQUEST.hex}$pid09$POSTFIX"),
//        GET_ECU_NAME("${PidMod.VEHICLE_INFO_REQUEST.hex}$pid0A$POSTFIX"),
//        ACCELERATION_PEDAL_POSITION("${PidMod.SHOW_CURRENT.hex}$pid5A$POSTFIX"),
//        HYBRID_REMAINING_BAT_LIFE("${PidMod.SHOW_CURRENT.hex}$pid5B$POSTFIX"),
//        ENGINE_OIL_TEMP("${PidMod.SHOW_CURRENT.hex}$pid5C$POSTFIX"),
//        FUEL_INJECTION_TIMING("${PidMod.SHOW_CURRENT.hex}$pid93$POSTFIX"),
//        FUEL_RATE("${PidMod.SHOW_CURRENT.hex}$pid94$POSTFIX"), // Расход топлива
//        ACTUAL_ENGINE_TORQUE("${PidMod.SHOW_CURRENT}$pid62$POSTFIX"), //Тяга мотора в %
//        ENGINE_TORQUE_DATA("${PidMod.SHOW_CURRENT.hex}$pid64$POSTFIX"), //Тяга мотора в % by abcd points
//        ENGINE_TORQUE_NM("${PidMod.SHOW_CURRENT.hex}$pid63$POSTFIX"),
//        TURBOCHARGER_RPM("${PidMod.SHOW_CURRENT.hex}$pid74$POSTFIX"),
//        TURBOCHARGER_1_TEMP("${PidMod.SHOW_CURRENT.hex}$pid75$POSTFIX"),
//        TURBOCHARGER_2_RPM("${PidMod.SHOW_CURRENT.hex}$pid76$POSTFIX"),
//        DPF_TEMP("${PidMod.SHOW_CURRENT.hex}$pid7C$POSTFIX"); //Дизельный фильтр, температура
//
//
//    }


}
const val pid00 = "00"
const val pid20 = "20"
const val pid40 = "40"
const val pid60 = "60"
const val pid01 = "01"
const val pid02 = "02"
const val pid03 = "03"
const val pid04 = "04"
const val pid05 = "05"
const val pid06 = "06"
const val pid07 = "07"
const val pid08 = "08"
const val pid09 = "09"
const val pid0A = "0A"
const val pid0B = "0B"
const val pid0C = "0C"
const val pid0D = "0D"
const val pid0E = "0E"
const val pid10 = "10"
const val pid11 = "11"
const val pid12 = "12"
const val pid13 = "13"
const val pid14 = "14"
const val pid15 = "15"
const val pid16 = "16"
const val pid17 = "17"
const val pid18 = "18"
const val pid19 = "19"
const val pid1A = "1A"
const val pid1B = "1B"
const val pid1C = "1C"
const val pid1D = "1D"
const val pid1E = "1E"
const val pid0F = "0F"
const val pid1F = "1F"
const val pid4D = "4D"
const val pid21 = "21"
const val pid22 = "22"
const val pid23 = "23"
const val pid24 = "24"
const val pid25 = "25"
const val pid26 = "26"
const val pid27 = "27"
const val pid28 = "28"
const val pid29 = "29"
const val pid2F = "2F"
const val pid2A = "2A"
const val pid2B = "2B"
const val pid2C = "2C"
const val pid2E = "2E"
const val pid30 = "30"
const val pid31 = "31"
const val pid32 = "32"
const val pid33 = "33"
const val pid34 = "34"
const val pid35 = "35"
const val pid36 = "36"
const val pid37 = "37"
const val pid38 = "38"
const val pid39 = "39"
const val pid3A = "3A"
const val pid3B = "3B"
const val pid3C = "3C"
const val pid3D = "3D"
const val pid3E = "3E"
const val pid3F = "3F"
const val pid2D = "2D"
const val pid4A = "4A"
const val pid4B = "4B"
const val pid4C = "4C"
const val pid4E = "4E"
const val pid4F = "4F"
const val pid41 = "41"
const val pid42 = "42"
const val pid44 = "44"
const val pid45 = "45"
const val pid49 = "49"
const val pid43 = "43"
const val pid46 = "46"
const val pid47 = "47"
const val pid48 = "48"
const val pid50 = "50"
const val pid51 = "51"
const val pid52 = "52"
const val pid53 = "53"
const val pid54 = "54"
const val pid55 = "55"
const val pid56 = "56"
const val pid57 = "57"
const val pid58 = "58"
const val pid59 = "59"
const val pid5A = "5A"
const val pid5B = "5D"
const val pid5C = "5C"
const val pid5D = "5D"
const val pid5E = "5E"
const val pid5F = "5F"
const val pid61 = "61"
const val pid62 = "62"
const val pid63 = "63"
const val pid64 = "64"
const val pid65 = "65"
const val pid66 = "66"
const val pid67 = "67"
const val pid68 = "68"
const val pid69 = "69"
const val pid6A = "6A"
const val pid6B = "6B"
const val pid6C = "6C"
const val pid6D = "6D"
const val pid6E = "6E"
const val pid6F = "6F"
const val pid70 = "70"
const val pid71 = "21"
const val pid72 = "72"
const val pid73 = "73"
const val pid74 = "74"
const val pid75 = "75"
const val pid76 = "76"
const val pid77 = "77"
const val pid78 = "78"
const val pid79 = "79"
const val pid7A = "7A"
const val pid7B = "7B"
const val pid7C = "7C"
const val pid7D = "7D"
const val pid7E = "7E"
const val pid7F = "7F"
const val pid80 = "80"
const val pid81 = "81"
const val pid82 = "82"
const val pid83 = "83"
const val pid84 = "84"
const val pid85 = "85"
const val pid86 = "86"
const val pid87 = "87"
const val pid88 = "88"
const val pid89 = "89"
const val pid8A = "8A"
const val pid8B = "8B"
const val pid8C = "8C"
const val pid8D = "8D"
const val pid8E = "8E"
const val pid8F = "8F"
const val pid90 = "90"
const val pid91 = "91"
const val pid92 = "92"
const val pid93 = "93"
const val pid94 = "94"
const val pid95 = "95"
const val pid96 = "96"
const val pid97 = "97"
const val pid98 = "98"
const val pid99 = "99"
const val pid9A = "9A"
const val pid9B = "9B"
const val pid9D = "9D"
const val pid9C = "9C"
const val pid9E = "9E"
const val pid9F = "9F"
const val pidA0 = "A0"
const val pidA1 = "A1"
const val pidA2 = "A2"
const val pidA3 = "A3"
const val pidA4 = "A4"
const val pidA5 = "A5"
const val pidA6 = "A6"
const val pidA7 = "A7"
const val pidA8 = "A8"
const val pidA9 = "A9"
const val pidC0 = "C0"