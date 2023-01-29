package obdKotlin.utils

import obdKotlin.WorkMode
import obdKotlin.commands.AT_PREFIX
import obdKotlin.commands.CommandRout
import obdKotlin.commands.Commands
import obdKotlin.exceptions.WrongInitCommandException
import obdKotlin.protocol.Protocol
import java.util.Locale

internal object CommandUtil {

    fun checkAndRout(
        canMode: Boolean,
        workMode: WorkMode,
        trimmedCommand: String
    ): CommandRout {
        if (workMode == WorkMode.IDLE) {
            print(workMode)
            return CommandRout.PASS
        }
        val command = trimmedCommand.uppercase(Locale.getDefault())
        print(command)
        return when {
            command == "Z" -> {
                return CommandRout.RESET
            }
            command == "WS" -> {
                return CommandRout.RESET
            }
            command == "@1" -> {
                return CommandRout.TO_CH
            }
            command == "@2" -> {
                return CommandRout.TO_CH
            }
            command == "RV" -> {
                return CommandRout.TO_CH
            }
            command == "E1" -> {
                throw WrongInitCommandException(
                    "$trimmedCommand is not allowed" +
                        " in OBD Kotlin case it will break logic"
                )
            }
            command == "CAF1" -> {
                if (canMode) {
                    throw WrongInitCommandException(
                        "$trimmedCommand is not allowed in can mode." +
                            " Hint: To switch in standard mode use switchToStandardMode"
                    )
                } else CommandRout.PASS
            }
            command == "PC" -> {
                throw WrongInitCommandException(
                    "$trimmedCommand is not allowed." +
                        " Hint: To to using this in with tuned connection may occurs errors"
                )
            }
            command == "FI" -> {
                throw WrongInitCommandException(
                    "$trimmedCommand is not allowed." +
                        " Hint: This command should go with one of init methods ${Protocol.ISO_14230_4_FASTINIT.name}"
                )
            }
            command.contains("SP") -> {
                throw WrongInitCommandException(
                    "$trimmedCommand is not allowed." +
                        " Hint: To switch protocol use start() or profile"
                )
            }
            command.contains("TP") -> {
                throw WrongInitCommandException(
                    "$trimmedCommand is not allowed." +
                        " Hint: To switch protocol use startWith.. or profile"
                )
            }
            command == "D" -> {
                throw WrongInitCommandException(
                    "$trimmedCommand is not allowed." +
                        " Using this  with tuned connection may occurs errors. " +
                        "Should use on Idle workMode state. Hint: Use reset()"
                )
            }

            else -> CommandRout.PASS
        }
    }

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
    fun formatAT(command: String): String = "$AT_PREFIX$command\r"

    fun formatPid(command: String): String = "$command\r"

    fun filterExtraAndFormat(extra: List<String>, extendedMode: Boolean): List<String> {
        val filtered = extra.filter {
            when {
                it.contains("Z", true) && it.length == 1 -> false
                it.contains("WS", true) -> false
                it.contains("PC", true) -> false
                it.contains("SP", true) -> false
                it.contains("TP", true) -> false
                it.contains("D", true) && it.length == 1 -> false
                else -> true
            }
        }.map { formatAT(it) }.toMutableList()
        if (extendedMode) {
            filtered.add(Commands.AtCommands.AutoFormatCanFramesOff.command)
        }
        return filtered
    }
}
