package main.decoders

import Event
import OBDMessage
import Protocol
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import main.OnPositiveAnswerStrategy
import main.WorkMode
import java.lang.IllegalArgumentException
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentLinkedQueue

class ObdFrameDecoder(private val socketEventFlow: MutableSharedFlow<Event<OBDMessage?>>) : Decoder(socketEventFlow) {

    override val buffer = ConcurrentLinkedQueue<ByteArray>()


    override fun decode(bytes: ByteArray) {
        TODO("Not yet implemented")
    }

    fun isPositiveOBDAnswer(): Boolean {
        return buffer.poll().decodeToString() == "OK"
    }

    fun isPositiveProtoOBDAnswer(strategy: OnPositiveAnswerStrategy): Boolean{
        if (strategy == OnPositiveAnswerStrategy.ASK_RECOMMENDED) {
            Protocol.values().forEach {
                if(it.hexOrdinal == buffer.peek().decodeToString()){
                    return true
                }
            }
            return false
        } else
    }

    fun isReadyForNewCommand(bytes: ByteArray): Boolean{
        return bytes.decodeToString() == AtCommands.Repeat.command
    }

    fun handleDataAnswer(bytes: ByteArray) {
        if(bytes.decodeToString() != "?"){
            buffer.add(bytes)
        } else {
            //TODO
        }

    }

    fun getProtoByCachedHex(): Protocol{
        Protocol.values().forEach {
            if(it.hexOrdinal == buffer.peek().decodeToString()){
                return it
            }
        }
        throw IllegalArgumentException()
    }

    fun getSavedAnswer(): String = buffer.poll().decodeToString()
}