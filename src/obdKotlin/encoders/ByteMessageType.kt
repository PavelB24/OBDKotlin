package obdKotlin.encoders

enum class ByteMessageType(val hex: String) {

    SINGLE("0"), FIRST("1"), SECONDARY("2")
}
