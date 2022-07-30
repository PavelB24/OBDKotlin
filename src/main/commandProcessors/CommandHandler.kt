package main.commandProcessors

import PidCommands
import kotlinx.coroutines.flow.SharedFlow
import main.MultiCommand

abstract class CommandHandler() {

    abstract val commandFlow: SharedFlow<String>

    abstract fun receiveCommand(command: PidCommands)

    abstract fun receiveMultiCommand(commands: MultiCommand)

}