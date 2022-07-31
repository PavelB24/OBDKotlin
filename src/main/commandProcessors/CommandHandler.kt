package main.commandProcessors

import main.PidCommands
import kotlinx.coroutines.flow.SharedFlow
import main.MultiCommand

abstract class CommandHandler() {

    abstract val commandFlow: SharedFlow<String>

    abstract fun receiveCommand(command: PidCommands)

    abstract fun receiveCommand(command: String)

    abstract fun receiveMultiCommand(commands: MultiCommand)

}