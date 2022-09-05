package obdKotlin.commandProcessors

import kotlinx.coroutines.flow.MutableSharedFlow
<<<<<<< HEAD
import obdKotlin.core.WorkMode
=======
import obdKotlin.WorkMode
import obdKotlin.commands.CommandContainer
>>>>>>> 61257416ebc4218fbd9b3c63ea2dcb4f83c64b4a
import java.util.concurrent.atomic.AtomicBoolean

abstract class BaseCommandHandler() {

    abstract val commandFlow: MutableSharedFlow<String>
    abstract val commandAllowed: AtomicBoolean


    /**
     * Remove specific command or remove all from queue to send, if id==null
     */
    abstract fun removeCommand(command: String? = null)

    abstract val canMode: AtomicBoolean

    abstract suspend fun sendNextCommand(lastCommandFailed: Boolean? = null)

    abstract fun isQueueEmpty(): Boolean

    abstract suspend fun receiveCommand(command: String,  delay: Long?, workMode: WorkMode)

<<<<<<< HEAD
    abstract fun receiveMultiCommand(commands: List<String>)

    abstract fun getLastCommand(): String?
=======
    abstract suspend fun receiveCommand(commands: List<CommandContainer>, workMode: WorkMode)

    abstract fun receiveMultiCommand(commands: List<String>)

    abstract fun getCurrentCommand(): String?
>>>>>>> 61257416ebc4218fbd9b3c63ea2dcb4f83c64b4a
}