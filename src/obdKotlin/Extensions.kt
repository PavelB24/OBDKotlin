package obdKotlin

import java.lang.StringBuilder
import java.util.*

@OptIn(ExperimentalUnsignedTypes::class)
internal fun UByteArray.toBinaryArray(): CharArray {
    val strb = StringBuilder()
    this.forEach {
        strb.append(it.toBinary(8))
    }
    return strb.toString().toCharArray()
}

internal fun Byte.toHex(): String {
    return Integer.toHexString(this.toUByte().toInt())
}

internal fun UByte.toBinary(len: Int = 1): String {
    return String.format("%" + len + "s", this.toInt().toString(2)).replace(" ".toRegex(), "0")
}

internal fun Int.toBinary(len: Int = 1): String {
    return String.format("%" + len + "s", this.toString(2)).replace(" ".toRegex(), "0")
}

internal fun Char.toBoolean(): Boolean {
    if (this != '1' && this != '0') {
        throw TypeCastException()
    }
    return this == '1'
}

internal fun Int.toHex(): String {
    return Integer.toHexString(this)
}

internal fun Int.toOneCharHex(): String {
    return if (this >= 16) Integer.toHexString(this % 16) else Integer.toHexString(this)
}

internal fun Int.toThreeCharHex(): String = "%03x".format(this)

internal fun String.hexToBinaryList(): List<String> {
    val strList = this.chunked(2)
    val intList = strList.map { it.toInt(16) }
    return intList.map { Integer.toBinaryString(it) }
}

internal fun String.hexToBinary(): CharArray {
    return String.format(
        "%" + 8 + "s",
        this.toInt(16).toString(2)
    ).replace(
        " ".toRegex(),
        "0"
    ).toCharArray()
}
