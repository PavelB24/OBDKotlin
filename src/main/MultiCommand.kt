package main

import main.commands.PidMod


data class MultiCommand(
    val mode: PidMod,
    val pids: List<String>
)