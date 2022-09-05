package obdKotlin.commandProcessors

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
<<<<<<< HEAD
import obdKotlin.core.WorkMode
=======
import obdKotlin.WorkMode
>>>>>>> 61257416ebc4218fbd9b3c63ea2dcb4f83c64b4a
import obdKotlin.commands.CommandContainer
import obdKotlin.toHex
import obdKotlin.toOneCharHex
import obdKotlin.toThreeCharHex
<<<<<<< HEAD
=======
import obdKotlin.utills.CommandFormatter
import obdKotlin.utills.FrameGenerator
>>>>>>> 61257416ebc4218fbd9b3c63ea2dcb4f83c64b4a
import java.lang.StringBuilder
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

class CommandHandler() : BaseCommandHandler() {

    private val commandQueue = ConcurrentLinkedQueue<CommandContainer>()

    override val commandFlow: MutableSharedFlow<String> = MutableSharedFlow()

    override val canMode = AtomicBoolean(false)

    override val commandAllowed = AtomicBoolean(true)

    override fun isQueueEmpty(): Boolean = commandQueue.isEmpty()

<<<<<<< HEAD
    override fun getLastCommand(): String? = commandQueue.peek()?.command

    override suspend fun sendNextCommand(lastCommandFailed: Boolean?) {
        lastCommandFailed?.let {
            if (it) {
                removeCommand(commandQueue.poll().command)
            } else {
                commandQueue.remove()
=======
    override fun getCurrentCommand(): String? = commandQueue.peek()?.command

    override suspend fun sendNextCommand(lastCommandFailed: Boolean?) {
        if (commandQueue.isNotEmpty()) {
            lastCommandFailed?.let {
                if (it) {
                    removeCommand(commandQueue.poll().command)
                } else {
                    commandQueue.remove()
                }
>>>>>>> 61257416ebc4218fbd9b3c63ea2dcb4f83c64b4a
            }
        }
        if (commandQueue.isNotEmpty()) {
            val command = commandQueue.peek()
            command.delay?.let {
                delay(it)
            }
<<<<<<< HEAD
            val handledCommand = if (canMode.get()) buildCommand(command.command) else command.command
            if (handledCommand.length <= 16) {
                commandFlow.emit(handledCommand)
                command.delay?.let {
                    commandQueue.add(CommandContainer(command.command, it))
                }
            } else {
                handledCommand.chunked(16).forEach {
                    delay(22)
                    commandFlow.emit(it)
                }
                command.delay?.let {
                    if (commandQueue.isNotEmpty()) {
                        commandQueue.add(CommandContainer(command.command, it))
                    }
=======
            val handledCommand = if (canMode.get()) FrameGenerator.generateFrame(command.command) else command.command
            if (handledCommand.length <= 16) {
                commandFlow.emit(CommandFormatter.formatPid(handledCommand))
            } else {
                handledCommand.chunked(16).forEach {
                    delay(100)
                    commandFlow.emit(CommandFormatter.formatPid(it))
                }

            }
            command.delay?.let {
                if (commandQueue.isNotEmpty()) {
                    commandQueue.add(CommandContainer(command.command, it))
>>>>>>> 61257416ebc4218fbd9b3c63ea2dcb4f83c64b4a
                }

                //положить саму комманду в конец очереди если нужен повтор, фреймы положить в начало очереди
                //возможно поставить поле в коммандах по типу форматтед, чтоб не форматировали фрейм дважды,
                // можно мапить комманды в др класс
            }
        }
    }

<<<<<<< HEAD
    private fun buildCommand(command: String): String {

        if (command.length <= 14) {
            val x = command.length % 2
            val frameControl = "0${(command.length / 2 + x).toHex()}"
            return "$frameControl$command"
        } else {
            val handledCommand = StringBuilder()
            val lastFrameSize = command.length % 16
            val rawFramesCount = if (lastFrameSize > 0) command.length / 16 + 1 else command.length / 16
            val totalSize = command.length + 4 + rawFramesCount * 2
            val framesCount = if (totalSize % 16 > 0) command.length / 16 + 1 else command.length / 16
            var offset = 0
            for (i in 1..framesCount) {
                if (i == 1) {
                    val expectedSize = (command.length / 2 + framesCount - 1).toThreeCharHex()
                    handledCommand.append("$i$expectedSize${command.take(12)}")
                    offset = 12
                } else if (i == framesCount) {
                    val substring = command.substring(offset, command.length)
                    if (substring.isNotEmpty()) {
                        handledCommand.append("2${i.toOneCharHex()}${substring}")
                    }
                } else {
                    handledCommand.append("2${i.toHex()}${command.substring(offset, offset + 14)}")
                    offset += 14
                }
            }
            return handledCommand.toString()
        }
    }
=======
>>>>>>> 61257416ebc4218fbd9b3c63ea2dcb4f83c64b4a

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
<<<<<<< HEAD
        commandQueue.add(CommandContainer(command.replace(" ", ""), delay))
        if (workMode == WorkMode.COMMANDS && commandAllowed.get()) {
            sendNextCommand()
            commandAllowed.set(false)
=======
        commandQueue.add(CommandContainer(command, delay))
        if (workMode == WorkMode.COMMANDS && commandAllowed.get()) {
            commandAllowed.set(false)
            sendNextCommand()
>>>>>>> 61257416ebc4218fbd9b3c63ea2dcb4f83c64b4a
        }
    }


    override fun receiveMultiCommand(commands: List<String>) {
        TODO("Not yet implemented")
    }
}