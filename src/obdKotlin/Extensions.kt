package obdKotlin

import java.lang.StringBuilder


internal fun UByteArray.toBinaryArray(): CharArray {
    val strb = StringBuilder()
    this.forEach {
        strb.append(it.toBinary())
    }
    return strb.toString().toCharArray()
}

internal fun Byte.toHex(): String{
    return Integer.toHexString(this.toUByte().toInt())
}
internal fun UByte.toBinary(len: Int = 1): String{
    return String.format("%" + len + "s", this.toInt().toString(2)).replace(" ".toRegex(), "0")
}

internal fun Char.toBoolean(): Boolean{
    if(this != '1' && this != '0'){
        throw TypeCastException()
    }
    return this == '1'
}

fun String.hexToBinaryList(): List<String> {
    val strList = this.chunked(2)
    val intList = strList.map { it.toInt(16) }
    return intList.map { Integer.toBinaryString(it) }
}

fun String.hexToInt(): Int{
    return this.toInt(16)
}

