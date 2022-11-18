package obdKotlin.source

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

abstract class Source {

    companion object {
        private const val BUFFER_CAPACITY = 100
        private const val REPLAY = 1
        const val END_VALUE: Byte = -1
        const val CR_BYTE_VALUE: Byte = 32
        const val SPACE_BYTE_VALUE: Byte = 13
        const val NULL_BYTE_VALUE: Byte = 0
    }

    val inputByteFlow: MutableSharedFlow<ByteArray> = MutableSharedFlow(REPLAY, BUFFER_CAPACITY, BufferOverflow.SUSPEND)

    val outputByteFlow: MutableSharedFlow<ByteArray> = MutableSharedFlow(REPLAY, BUFFER_CAPACITY, BufferOverflow.SUSPEND)

    abstract suspend fun observeByteCommands(scope: CoroutineScope)
}
