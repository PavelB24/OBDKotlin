package main.protocol

enum class Protocol(val hexOrdinal: String) {

    AUTOMATIC("0"),
    SAE_J1850_PWM("1"),
    SAE_J1850_VPW("2"),
    ISO_9141_2("3"),
    ISO_14230_4_5kbaud("4"),
    ISO_14230_4_FASTINIT("5"),
    ISO_15765_4_CAN_11_bit_ID_500kbaud("6"),
    ISO_15765_4_CAN_29_bit_ID_500kbaud("7"),
    ISO_15765_4_CAN_11_bit_ID_250kbaud("8"),
    ISO_15765_4_CAN_29_bit_ID_250kbaud("9"),
    SAE_J1939_CAN_29_bit_ID_250kbaud("A")


}