package obdKotlin.utills

import obdKotlin.WorkMode
import obdKotlin.commands.AT_PREFIX
import obdKotlin.commands.CommandRout
import obdKotlin.exceptions.WrongInitCommandException
import obdKotlin.protocol.Protocol
import java.util.Locale

internal object CommandUtil {

    @Synchronized
    fun checkAndRout(
        canMode: Boolean,
        workMode: WorkMode,
        trimmedCommand: String
    ): CommandRout {
        if (workMode == WorkMode.IDLE) {
            return CommandRout.PASS
        }
        return when (trimmedCommand.uppercase(Locale.getDefault())) {
            "Z" -> {
                return CommandRout.RESET
            }
            "WS" -> {
                return CommandRout.RESET
            }
            "@1" -> {
                return CommandRout.TO_CH
            }
            "@2" -> {
                return CommandRout.TO_CH
            }
            "RV" -> {
                return CommandRout.TO_CH
            }
            "E1" -> {
                throw WrongInitCommandException(
                    "$trimmedCommand is not allowed" +
                        " in OBD Kotlin case it will break logic"
                )
            }
            "CAF1" -> {
                if (canMode) {
                    throw WrongInitCommandException(
                        "$trimmedCommand is not allowed in can mode." +
                            " Hint: To switch in standard mode use switchToStandardMode"
                    )
                } else CommandRout.PASS
            }
            "PC" -> {
                throw WrongInitCommandException(
                    "$trimmedCommand is not allowed." +
                        " Hint: To to using this in with tuned connection may occurs errors"
                )
            }
            "FI" -> {
                throw WrongInitCommandException(
                    "$trimmedCommand is not allowed." +
                        " Hint: This option will be automatically applied with ${Protocol.ISO_14230_4_FASTINIT.name}"
                )
            }
            "SP" -> {
                throw WrongInitCommandException(
                    "$trimmedCommand is not allowed." +
                        " Hint: To switch protocol use startWith... or profile"
                )
            }
            "TP" -> {
                throw WrongInitCommandException(
                    "$trimmedCommand is not allowed." +
                        " Hint: To switch protocol use startWith.. or profile"
                )
            }
            "D" -> {
                throw WrongInitCommandException(
                    "$trimmedCommand is not allowed." +
                        " To to using this in with tuned connection may occurs errors. " +
                        "Should use on Idle workMode state. Hint: Use reset()"
                )
            }

            else -> CommandRout.PASS
        }
    }

    @Synchronized
    fun checkPid(trimmedCommand: String): String {
        return if (trimmedCommand.contains("I", true) ||
            trimmedCommand.contains("RV", true)
        ) {
            when {
                !trimmedCommand.take(2).contains(AT_PREFIX, true) -> {
                    "$AT_PREFIX$trimmedCommand$\r"
                }

                trimmedCommand.take(2).contains(AT_PREFIX, true) && trimmedCommand.length == 3 -> {
                    "$AT_PREFIX$trimmedCommand$\r"
                }

                else -> {
                    trimmedCommand
                }
            }
        } else trimmedCommand
    }

    @Synchronized
    fun formatAT(command: String): String = "$AT_PREFIX$command\r"

    @Synchronized
    fun formatPid(command: String): String = "$command\r"
}
