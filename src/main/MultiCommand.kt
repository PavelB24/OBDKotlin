package main

import PidMod


data class MultiCommand(
    val mode: PidMod,
    val pids: List<String>
)