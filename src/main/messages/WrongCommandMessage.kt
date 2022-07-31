package main.messages

import main.WorkMode


data class WrongCommandMessage(
    val phase: WorkMode,
): Message()
