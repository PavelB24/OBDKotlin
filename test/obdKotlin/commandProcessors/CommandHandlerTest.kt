package obdKotlin.commandProcessors

import org.junit.Test

import org.junit.Assert.*

class CommandHandlerTest {

    @Test
    fun getCommandFlow() {
    }

    @Test
    fun getCanMode() {
    }

    @Test
    fun sendNextCommand() {
    }

    @Test
    fun buildCommand() {
        val h = CommandHandler()
        val com = "05AC"
        assertEquals("0205AC", h.buildCommand(com))
    }

    @Test
    fun removeCommand() {
    }

    @Test
    fun receiveCommand() {
    }

    @Test
    fun receiveCanCommand() {
    }

    @Test
    fun receiveMultiCommand() {
    }
}