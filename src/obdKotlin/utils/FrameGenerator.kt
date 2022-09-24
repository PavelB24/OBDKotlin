package obdKotlin.utils

import obdKotlin.commands.Commands
import obdKotlin.toHex
import obdKotlin.toOneCharHex
import obdKotlin.toThreeCharHex
import java.lang.StringBuilder

object FrameGenerator {

    @Synchronized
    fun generateFrame(command: String): String {
        val findInStandard = Commands.PidMod.values().find {
            it.hex == command.take(2)
        }
        findInStandard?.let {
            return command
        }
        if (command.length <= 14) {
            val x = command.length % 2
            val frameControl = "0${(command.length / 2 + x).toHex()}"
            return "$frameControl$command"
        } else {
            val handledCommand = StringBuilder()
            val rawFramesCount = if (command.length % 16 > 0) command.length / 16 + 1 else command.length / 16
            val totalSize = command.length + 4 + rawFramesCount * 2
            val framesCount = if (totalSize % 16 > 0) command.length / 16 + 1 else command.length / 16
            var offset = 0
            for (i in 1..framesCount) {
                if (i == 1) {
                    val expectedSize = (command.length / 2).toThreeCharHex()
                    handledCommand.append("$i$expectedSize${command.take(12)}")
                    offset = 12
                } else if (i == framesCount) {
                    val substring = command.substring(offset, command.length)
                    if (substring.isNotEmpty()) {
                        handledCommand.append("2${i.toOneCharHex()}$substring")
                    }
                } else {
                    handledCommand.append("2${i.toHex()}${command.substring(offset, offset + 14)}")
                    offset += 14
                }
            }
            return handledCommand.toString()
        }
    }
}
