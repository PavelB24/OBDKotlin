package obdKotlin

import org.junit.Assert.*
import org.junit.Test

class ExtensionsKtTest {

    @Test
    fun toBinaryArray() {
        val value = (44).toUByte()
        print(value)
        val arr = ubyteArrayOf(value)
        val charArr = arr.toBinaryArray()
        val result = StringBuilder()
        charArr.forEach {
            result.append(it)
        }
        assertEquals("00101100", result.toString())
    }

    @Test
    fun toHex() {
        val a: Byte = 40
        assertEquals("28", a.toHex())
    }

    @Test
    fun toBinary() {
        val u: UByte = 55u
        assertEquals("110111", u.toBinary())
    }

    @Test
    fun testToBinary() {
        val u = 55
        assertEquals("110111", u.toBinary())
    }

    @Test
    fun toBoolean() {
        assertEquals(true, '1'.toBoolean())
        assertEquals(false, '0'.toBoolean())
    }

    @Test
    fun testToHex() {
        assertEquals("ff", (255).toHex())
    }

    @Test
    fun toOneCharHex() {
        assertEquals("f", (255).toOneCharHex())
        assertEquals("2", "2")
    }

    @Test
    fun toThreeCharHex() {
        assertEquals("0FF", (255).toThreeCharHex())
        assertEquals("639C", (25500).toThreeCharHex())
    }

    @Test
    fun hexToBinaryList() {
        val res = "ffff".hexToBinaryList()
        assertEquals("11111111", res.first())
        assertEquals("11111111", res[1])
    }

    @Test
    fun hexToInt() {
        assertEquals(255, "ff".hexToInt())
    }

    @Test
    fun hexToBinary() {
        val bin = "FF".hexToBinary()
        val result = StringBuilder()
        bin.forEach { result.append(it) }
        assertEquals("11111111", result.toString())
    }
}
