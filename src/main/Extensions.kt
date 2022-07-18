package main



fun ByteArray.toBinaryList(): List<String> {
    val binList = mutableListOf<String>()
    this.forEach {
        binList.add(it.toString(2))
    }
    return binList
}

fun Byte.toHex(): String{
    return Integer.toHexString(this.toUByte().toInt())
}


fun String.hexToBinaryList(): List<String> {
    val strList = this.chunked(2)
    val intList = strList.map { it.toInt(16) }
    return intList.map { Integer.toBinaryString(it) }
}

fun String.hexToByte(): Byte{
    return this.toInt(16).toByte()
}

