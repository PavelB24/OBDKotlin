enum class PidMods(val hex: String) {

    SHOW_CURRENT("01"),
    SHOW_FREEZE_FRAME("02"),
    SHOW_DIAGNOSTIC_TROUBLES_CODES("03"),
    CLEAR_TROUBLES_CODES_AND_STORE_VAL("04"),
    TEST_RESULTS_OXY_SENSORS("05"),
    TEST_RESULTS_OXY_SENSORS_CAN("06"),
    SHOW_PENDING_DIAGN_TROUBLES_CODES("07"),
    CONTROL_ON_BOARD_SYS("08"),
    VEHICLE_INFO_REQUEST("09"),
    DELETED_ERRORS("0A")
}