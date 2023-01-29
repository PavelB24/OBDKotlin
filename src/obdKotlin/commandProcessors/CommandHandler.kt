package obdKotlin.commandProcessors

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import obdKotlin.WorkMode
import obdKotlin.commands.CommandContainer
import obdKotlin.utils.CommandUtil
import obdKotlin.utils.FrameGenerator
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

class CommandHandler : BaseCommandHandler() {

    private val commandQueue = ConcurrentLinkedQueue<CommandContainer>()

    override val commandFlow: MutableSharedFlow<String> = MutableSharedFlow()

    override val extended = AtomicBoolean(false)

    override val commandAllowed = AtomicBoolean(true)

    private val handlerMutex = Mutex()

    override fun isQueueEmpty(): Boolean = commandQueue.isEmpty()

    override fun getCurrentCommand(): String? = commandQueue.peek()?.command

    override suspend fun sendNextCommand(lastCommandFailed: Boolean?) {
        handlerMutex.withLock {
            if (commandQueue.isNotEmpty()) {
                lastCommandFailed?.let {
                    if (it) {
                        removeCommand(commandQueue.poll().command)
                    } else {
                        commandQueue.remove()
                    }
                }
            }

            // Конкарент кью не поможет,
            // так как я сохраняю в константу значение из очереди, а сразу после этого оно вполне может измениться

            if (commandQueue.isNotEmpty()) {
                val command = commandQueue.peek()
                command.delay?.let {
                    if (it != 0L) {
                        delay(it)
                    }
                }
                val handledCommand =
                    if (extended.get()) FrameGenerator.generateFrame(command.command) else command.command
                if (handledCommand.length <= 16) {
                    commandFlow.emit(CommandUtil.formatPid(handledCommand))
                } else {
                    handledCommand.chunked(16).forEach {
                        delay(100)
                        commandFlow.emit(CommandUtil.formatPid(it))
                    }
                }
                command.delay?.let {
                    commandQueue.add(CommandContainer(command.command, it))
                    // положить саму комманду в конец очереди если нужен повтор, фреймы положить в начало очереди
                    // возможно поставить поле в коммандах по типу форматтед, чтоб не форматировали фрейм дважды,
                    // можно мапить комманды в др класс
                }
            }
        }
    }

    override suspend fun removeCommand(command: String) {
        handlerMutex.withLock {
            commandQueue.removeIf { it.command == command }
            commandAllowed.set(commandQueue.isEmpty())
        }
    }

    override suspend fun clearCommandsQueue() {
        handlerMutex.withLock {
            commandQueue.clear()
            commandAllowed.set(true)
        }
    }

    override suspend fun receiveCommands(command: String, delay: Long?, workMode: WorkMode) {
        handlerMutex.withLock {
            if (commandQueue.find { it.command == command } == null) {
                commandQueue.add(CommandContainer(command, delay))
                if (workMode == WorkMode.COMMANDS && commandAllowed.get()) {
                    commandAllowed.set(false)
                    sendNextCommand()
                }
            }
        }
    }

    override suspend fun receiveCommands(commands: List<String>, delay: Long?, workMode: WorkMode) {
        handlerMutex.withLock {
            commands.forEach { command ->
                if (commandQueue.find { it.command == command } == null) {
                    commandQueue.add(CommandContainer(command, delay))
                }
            }
            if (workMode == WorkMode.COMMANDS && commandAllowed.get()) {
                commandAllowed.set(false)
                sendNextCommand()
            }
        }
    }
}
