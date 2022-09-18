package obdKotlin.core

import obdKotlin.WorkMode

data class FailOn(
    val onWorkMode: WorkMode,
    val answer: String,
    val command: String?
)
