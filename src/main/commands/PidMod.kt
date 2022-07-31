package main.commands

enum class PidMod(val hex: String,val positiveCode: String) {

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