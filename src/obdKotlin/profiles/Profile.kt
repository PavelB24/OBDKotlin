package obdKotlin.profiles

import obdKotlin.commands.Commands
import obdKotlin.protocol.Protocol

enum class Profile(
    val protocol: Protocol,
    val settings: List<Pair<Commands.AtCommands, String?>>
) {

    DEFAULT(Protocol.AUTOMATIC, listOf()),
    ACCORD(Protocol.ISO_14230_4_5kbaud, listOf(Pair(Commands.AtCommands.SetHeader, "th"))),

}