package obdKotlin.source

import kotlinx.coroutines.flow.MutableSharedFlow

abstract class Source {

    val inputByteFlow: MutableSharedFlow<ByteArray> = MutableSharedFlow()

    val outputByteFlow: MutableSharedFlow<ByteArray> = MutableSharedFlow()

    abstract suspend fun observeByteCommands()
}
