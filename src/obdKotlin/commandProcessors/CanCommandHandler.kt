package obdKotlin.commandProcessors

import kotlinx.coroutines.flow.SharedFlow
import obdKotlin.commands.Commands
import obdKotlin.commands.MultiCommand

class CanCommandHandler: CommandHandler() {
    override val commandFlow: SharedFlow<String>
        get() = TODO("Not yet implemented")

    override fun receiveCommand(command: Commands.PidCommands) {
        TODO("Not yet implemented")
    }

    override fun receiveCommand(command: String) {
        TODO("Not yet implemented")
    }

    override fun receiveMultiCommand(commands: MultiCommand) {
        TODO("Not yet implemented")
    }
}