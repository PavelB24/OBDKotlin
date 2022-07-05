enum class PidCommands(val hexCommand: String) {

    SUPPORTED_PIDS0120("${PidMods.SHOW_CURRENT.hex} $pid00"),
    SUPPORTED_PIDS2140("${PidMods.SHOW_CURRENT.hex} $pid20"),
    SUPPORTED_PIDS4160("${PidMods.SHOW_CURRENT.hex} $pid40"),
    SUPPORTED_PIDS6180("${PidMods.SHOW_CURRENT.hex} $pid60"),
    GET_DETECTED_ERRORS("${PidMods.SHOW_CURRENT.hex} $pid02"),
    STATUS_SINCE_DTC_CLEARED("${PidMods.SHOW_CURRENT.hex} $pid01"),
    ENGINE_LOAD("${PidMods.SHOW_CURRENT.hex} $pid04"),
    COOLANT_TEMPERATURE("${PidMods.SHOW_CURRENT.hex} $pid05"),
    FUEL_PRESSURE("${PidMods.SHOW_CURRENT.hex} $pid0A"),
    MAP_VAL("${PidMods.SHOW_CURRENT.hex} $pid0B"),
    ENGINE_RPM("${PidMods.SHOW_CURRENT.hex} $pid0C"),
    CAR_SPEED("${PidMods.SHOW_CURRENT.hex} $pid0D"),
    TIMING_ADVANCE("${PidMods.SHOW_CURRENT.hex} $pid0E"),
    MAP_AIR_FLOW("${PidMods.SHOW_CURRENT.hex} $pid10"),
    INTAKE_AIR_TEMP("${PidMods.SHOW_CURRENT.hex} $pid0F"),
    THROTTLE_POSITION("${PidMods.SHOW_CURRENT.hex} $pid11"),
    HAS_OXYGEN_SENSORS("${PidMods.SHOW_CURRENT.hex} $pid13"),
    ENGINE_RUN_TIME("${PidMods.SHOW_CURRENT.hex} $pid1F"),
    CHECK_ENGINE_DISTANCE("${PidMods.SHOW_CURRENT.hex} $pid4D"),
    CHECK_ENGINE_TIME("${PidMods.SHOW_CURRENT.hex} $pid21"),
    FUEL_RAIL_PRESSURE("${PidMods.SHOW_CURRENT.hex} $pid23"), //Direct injection systems
    FUEL_LEVEL("${PidMods.SHOW_CURRENT.hex} $pid2F"),
    BAROMETRIC_PRESSURE("${PidMods.SHOW_CURRENT.hex} $pid33"),
    EGR_ERROR("${PidMods.SHOW_CURRENT.hex} $pid2D"),
    ABSOLUTE_LOAD("${PidMods.SHOW_CURRENT.hex} $pid43"),
    AMBIENT_AIR_TEMP("${PidMods.SHOW_CURRENT.hex} $pid46"),
    THROTTLE_B_POSITION("${PidMods.SHOW_CURRENT.hex} $pid47"),
    THROTTLE_C_POSITION("${PidMods.SHOW_CURRENT.hex} $pid48"),
    FUEL_TYPE("${PidMods.SHOW_CURRENT.hex} $pid52"),
    STORED_ERRORS_CODES(PidMods.SHOW_DIAGNOSTIC_TROUBLES_CODES.hex),
    CLEAR_ERRORS(PidMods.CLEAR_TROUBLES_CODES_AND_STORE_VAL.hex),
    GET_CIN("${PidMods.VEHICLE_INFO_REQUEST.hex} $pid02"),
    GET_ECU_NAME_MESSAGE_COUNT("${PidMods.VEHICLE_INFO_REQUEST.hex} $pid09"),
    GET_ECU_NAME("${PidMods.VEHICLE_INFO_REQUEST.hex} $pid0A"),
    ACCELERATION_PEDAL_POSITION("${PidMods.SHOW_CURRENT.hex} $pid5A"),
    HYBRID_REMAINING_BAT_LIFE("${PidMods.SHOW_CURRENT.hex} $pid5B"),
    ENGINE_OIL_TEMP("${PidMods.SHOW_CURRENT.hex} $pid5C"),
    FUEL_INJECTION_TIMING("${PidMods.SHOW_CURRENT.hex} $pid93"),
    FUEL_RATE("${PidMods.SHOW_CURRENT.hex} $pid94"), // Расход топлива
    ACTUAL_ENGINE_TORQUE("${PidMods.SHOW_CURRENT} $pid62"), //Тяга мотора в %
    ENGINE_TORQUE_DATA("${PidMods.SHOW_CURRENT.hex} $pid64"), //Тяга мотора в % by abcd points
    ENGINE_TORQUE_NM("${PidMods.SHOW_CURRENT.hex} $pid63"),
    TURBOCHARGER_RPM("${PidMods.SHOW_CURRENT.hex} $pid74"),
    TURBOCHARGER_1_TEMP("${PidMods.SHOW_CURRENT.hex} $pid75"),
    TURBOCHARGER_2_RPM("${PidMods.SHOW_CURRENT.hex} $pid76"),
    DPF_TEMP("${PidMods.SHOW_CURRENT.hex} $pid7C"), //Дизельный фильтр, температура


}

const val pid00 = "00"; const val pid20 = "20"; const val pid40 = "40"; const val pid60 = "60"
const val pid01 = "01"; const val pid02 = "02"; const val pid03 = "03"; const val pid04 = "04"
const val pid05 = "05"; const val pid06 = "06"; const val pid07 = "07"; const val pid08 = "08"
const val pid09 = "09"; const val pid0A = "0A"; const val pid0B = "0B"; const val pid0C = "0C"
const val pid0D = "0D"; const val pid0E = "0E"; const val pid10 = "10"; const val pid11 = "11"
const val pid12 = "12"; const val pid13 = "13"; const val pid14 = "14"; const val pid15 = "15"
const val pid16 = "16"; const val pid17 = "17"; const val pid18 = "18"; const val pid19 = "19"
const val pid1A = "1A"; const val pid1B = "1B"; const val pid1C = "1C"; const val pid1D = "1D"
const val pid1E = "1E"; const val pid0F = "0F"; const val pid1F = "1F"; const val pid4D = "4D"
const val pid21 = "21"; const val pid22 = "22"; const val pid23 = "23"; const val pid24 = "24"
const val pid25 = "25"; const val pid26 = "26"; const val pid27 = "27"; const val pid28 = "28"
const val pid29 = "29"; const val pid2F = "2F"; const val pid2A = "2A"; const val pid2B = "2B"
const val pid2C = "2C"; const val pid2E = "2E"; const val pid30 = "30"; const val pid31 = "31"
const val pid32 = "32"; const val pid33 = "33"; const val pid34 = "34"; const val pid35 = "35"
const val pid36 = "36"; const val pid37 = "37"; const val pid38 = "38"; const val pid39 = "39"
const val pid3A = "3A"; const val pid3B = "3B"; const val pid3C = "3C"; const val pid3D = "3D"
const val pid3E = "3E"; const val pid3F = "3F"; const val pid2D = "2D"; const val pid4A= "4A"
const val pid4B = "4B"; const val pid4C = "4C"; const val pid4E = "4E"; const val pid4F = "4F"
const val pid41 = "41"; const val pid42 = "42"; const val pid44 = "44"; const val pid45 = "45"
const val pid49 = "49"; const val pid43 = "43"; const val pid46 = "46"; const val pid47 = "47"
const val pid48 = "48"; const val pid50 = "50"; const val pid51 = "51"; const val pid52 = "52"
const val pid53 = "53"; const val pid54 = "54"; const val pid55 = "55"; const val pid56 = "56"
const val pid57 = "57"; const val pid58 = "58"; const val pid59 = "59"; const val pid5A = "5A"
const val pid5B = "5D"; const val pid5C = "5C"; const val pid5D = "5D"; const val pid5E = "5E"
const val pid5F = "5F"; const val pid61 = "61"; const val pid62 = "62"; const val pid63 = "63"
const val pid64 = "64"; const val pid65 = "65"; const val pid66 = "66"; const val pid67 = "67"
const val pid68 = "68"; const val pid69 = "69"; const val pid6A = "6A"; const val pid6B = "6B"
const val pid6C = "6C"; const val pid6D = "6D"; const val pid6E = "6E"; const val pid6F = "6F"
const val pid70 = "70"; const val pid71 = "21"; const val pid72 = "72"; const val pid73 = "73"
const val pid74 = "74"; const val pid75 = "75"; const val pid76 = "76"; const val pid77 = "77"
const val pid78 = "78"; const val pid79 = "79"; const val pid7A = "7A"; const val pid7B = "7B"
const val pid7C = "7C"; const val pid7D = "7D"; const val pid7E = "7E"; const val pid7F = "7F"
const val pid80 = "80"; const val pid81 = "81"; const val pid82 = "82"; const val pid83 = "83"
const val pid84 = "84"; const val pid85 = "85"; const val pid86 = "86"; const val pid87 = "87"
const val pid88 = "88"; const val pid89 = "89"; const val pid8A = "8A"; const val pid8B = "8B"
const val pid8C = "8C"; const val pid8D = "8D"; const val pid8E = "8E"; const val pid8F = "8F"
const val pid90 = "90"; const val pid91 = "91"; const val pid92 = "92"; const val pid93 = "93"
const val pid94 = "94"; const val pid95 = "95"; const val pid96 = "96"; const val pid97 = "97"
const val pid98 = "98"; const val pid99 = "99"; const val pid9A = "9A"; const val pid9B = "9B"
const val pid9D = "9D"; const val pid9C = "9C"; const val pid9E = "9E"; const val pid9F = "9F"
const val pidA0 = "A0"; const val pidA1 = "A1"; const val pidA2 = "A2"; const val pidA3 = "A3"
const val pidA4 = "A4"; const val pidA5 = "A5"; const val pidA6 = "A6"; const val pidA7 = "A7"
const val pidA8 = "A8"; const val pidA9 = "A9"; const val pidC0 = "C0"

val pids0120 = setOf(
    pid01, pid02, pid03, pid04, pid05, pid06, pid07, pid08, pid09, pid0A, pid0B, pid0C, pid0D, pid0E, pid0F, pid10,
    pid11, pid12, pid13, pid14, pid15, pid16, pid17, pid18, pid19, pid1A, pid1B, pid1C, pid1D, pid1E, pid1F, pid20
)

val pids2140 = setOf(
    pid21, pid22, pid23, pid24, pid25, pid26, pid27, pid28, pid29, pid2A, pid2B, pid2C, pid2D, pid2E, pid2F, pid30,
    pid31, pid32, pid33, pid34, pid35, pid36, pid37, pid38, pid39, pid3A, pid3B, pid3C, pid3D, pid3E, pid3F, pid40
)

val pids4160 = setOf(
    pid41, pid42, pid43, pid44, pid45, pid46, pid47, pid48, pid49, pid4A, pid4B, pid4C, pid4D, pid4E, pid4F, pid50,
    pid51, pid52, pid53, pid54, pid55, pid56, pid57, pid58, pid59, pid5A, pid5B, pid5C, pid5D, pid5E, pid5F, pid60
)



val pids6180 = setOf(
    pid61, pid62, pid63, pid64, pid65, pid66, pid67, pid68, pid69, pid6A, pid6B, pid6C, pid6D, pid6E, pid6F, pid70,
    pid71, pid72, pid73, pid74, pid75, pid76, pid77, pid78, pid79, pid7A, pid7B, pid7C, pid7D, pid7E, pid7F, pid80
)

val pids81A0 = setOf(
    pid81, pid82, pid83, pid84, pid85, pid86, pid87, pid88, pid89, pid8A, pid8B, pid8C, pid8D, pid8E, pid8F, pid90,
    pid91, pid92, pid93, pid94, pid95, pid96, pid97, pid98, pid99, pid9A, pid9B, pid9C, pid9D, pid9E, pid9F, pidA0
)

val pidsA1C0 = setOf(
    pidA1, pidA2, pidA3, pidA4, pidA5, pidA6, pidA7, pidA8, pidA9, pidC0
)