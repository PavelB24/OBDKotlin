package obdKotlin.commandProcessors

import kotlinx.coroutines.flow.SharedFlow
import obdKotlin.commands.Commands
import obdKotlin.commands.MultiCommand

abstract class CommandHandler() {

    abstract val commandFlow: SharedFlow<String>

    abstract fun receiveCommand(command: Commands.PidCommands)

    abstract fun receiveCommand(command: String)

    abstract fun receiveMultiCommand(commands: MultiCommand)

}