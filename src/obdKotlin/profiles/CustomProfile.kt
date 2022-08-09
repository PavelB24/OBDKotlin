package obdKotlin.profiles

import obdKotlin.commands.Commands
import obdKotlin.protocol.Protocol

abstract class CustomProfile(
    val protocol: Protocol,
    val settingsAndParams: List<Pair<Commands.AtCommands, String?>> //setting and param
)