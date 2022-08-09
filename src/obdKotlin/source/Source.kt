package obdKotlin.source

import kotlinx.coroutines.flow.MutableSharedFlow


abstract class Source {


    abstract val inputByteFlow: MutableSharedFlow<ByteArray>

    abstract val outputByteFlow: MutableSharedFlow<ByteArray>

    abstract suspend fun observeByteCommands()


}