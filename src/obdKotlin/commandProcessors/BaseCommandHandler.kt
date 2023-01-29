package obdKotlin.commandProcessors

import kotlinx.coroutines.flow.MutableSharedFlow
import obdKotlin.WorkMode
import java.util.concurrent.atomic.AtomicBoolean

abstract class BaseCommandHandler() {

    abstract val commandFlow: MutableSharedFlow<String>
    abstract val commandAllowed: AtomicBoolean

    /**
     * Remove specific command or remove all from queue to send, if command==null
     */
    abstract suspend fun removeCommand(command: String)

    abstract suspend fun clearCommandsQueue()

    abstract val extended: AtomicBoolean

    abstract suspend fun sendNextCommand(lastCommandFailed: Boolean? = null)

    abstract fun isQueueEmpty(): Boolean

    abstract suspend fun receiveCommands(command: String, delay: Long?, workMode: WorkMode)

    abstract suspend fun receiveCommands(commands: List<String>, delay: Long?, workMode: WorkMode)

    abstract fun getCurrentCommand(): String?
}
