package obdKotlin.commandProcessors

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import obdKotlin.WorkMode
import obdKotlin.commands.CommandContainer
import obdKotlin.utills.CommandUtil
import obdKotlin.utills.FrameGenerator
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

class CommandHandler() : BaseCommandHandler() {

    private val commandQueue = ConcurrentLinkedQueue<CommandContainer>()

    override val commandFlow: MutableSharedFlow<String> = MutableSharedFlow()

    override val canMode = AtomicBoolean(false)

    override val commandAllowed = AtomicBoolean(true)

    override fun isQueueEmpty(): Boolean = commandQueue.isEmpty()

    override fun getCurrentCommand(): String? = commandQueue.peek()?.command

    override suspend fun sendNextCommand(lastCommandFailed: Boolean?) {
        if (commandQueue.isNotEmpty()) {
            lastCommandFailed?.let {
                if (it) {
                    removeCommand(commandQueue.poll().command)
                } else {
                    commandQueue.remove()
                }
            }
        }
        if (commandQueue.isNotEmpty()) {
            val command = commandQueue.peek()
            command.delay?.let {
                delay(it)
            }
            val handledCommand = if (canMode.get()) FrameGenerator.generateFrame(command.command) else command.command
            if (handledCommand.length <= 16) {
                commandFlow.emit(CommandUtil.formatPid(handledCommand))
            } else {
                handledCommand.chunked(16).forEach {
                    delay(100)
                    commandFlow.emit(CommandUtil.formatPid(it))
                }
            }
            command.delay?.let {
                if (commandQueue.isNotEmpty()) {
                    commandQueue.add(CommandContainer(command.command, it))
                }

                // положить саму комманду в конец очереди если нужен повтор, фреймы положить в начало очереди
                // возможно поставить поле в коммандах по типу форматтед, чтоб не форматировали фрейм дважды,
                // можно мапить комманды в др класс
            }
        }
    }

    /**
     * Put null for delete all commands in queue
     */
    override fun removeCommand(command: String?) {
        command?.let { command ->
            commandQueue.removeIf { it.command.contains(command, true) }
            return@removeCommand
        }
        commandQueue.clear()
        commandAllowed.set(true)
    }

    override suspend fun receiveCommand(command: String, delay: Long?, workMode: WorkMode) {
        commandQueue.add(CommandContainer(command, delay))
        if (workMode == WorkMode.COMMANDS && commandAllowed.get()) {
            commandAllowed.set(false)
            sendNextCommand()
        }
    }

    override suspend fun receiveCommand(commands: List<CommandContainer>, workMode: WorkMode) {
        TODO("Not yet implemented")
    }

    override fun receiveMultiCommand(commands: List<String>) {
        TODO("Not yet implemented")
    }
}
