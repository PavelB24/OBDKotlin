package obdKotlin

import org.junit.Test

import org.junit.Assert.*

class ExtensionsKtTest {

    @Test
    fun toBinaryArray() {
        val bytes = byteArrayOf(41, 10, 14, 12).toUByteArray().toBinaryArray()
        assertEquals(32, bytes.size)
    }

    @Test
    fun toHex() {
        val byte: Byte = -1
        assertEquals("ff", byte.toHex())
        val byte2: Byte = 80
        assertEquals("50", byte2.toHex())
    }

    @Test
    fun toBinary() {
    }

    @Test
    fun toBoolean() {
        assertEquals(true, '1'.toBoolean())

    }

    @Test
    fun hexToBinaryList() {
    }

    @Test
    fun hexToInt() {
        var data =  byteArrayOf(57, 50).decodeToString().hexToInt()
        assertEquals(146, data)
        data =  byteArrayOf(56, 50).decodeToString().hexToInt()
        assertEquals(130, data)
    }
}