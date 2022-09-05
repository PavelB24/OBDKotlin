package obdKotlin.commandProcessors

<<<<<<< HEAD
=======
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import obdKotlin.WorkMode
import obdKotlin.messages.Message
import org.junit.After
>>>>>>> 61257416ebc4218fbd9b3c63ea2dcb4f83c64b4a
import org.junit.Test

import org.junit.Assert.*

class CommandHandlerTest {

<<<<<<< HEAD
    @Test
    fun getCommandFlow() {
=======
    var subject = CommandHandler()

    @After
    fun newSubject(){
        subject = CommandHandler()
    }

    @Test
    fun getCommandFlow() {
        assertNotEquals(subject.commandFlow, MutableSharedFlow<Message>())

>>>>>>> 61257416ebc4218fbd9b3c63ea2dcb4f83c64b4a
    }

    @Test
    fun getCanMode() {
<<<<<<< HEAD
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
=======
        assertEquals(subject.canMode.get(), false)
        subject.canMode.set(true)
        assertEquals(subject.canMode.get(), true)

    }

    @Test
    fun getCommandAllowed() = runTest {
        assertEquals(subject.commandAllowed.get(), true)
        subject.receiveCommand("ffg", null, WorkMode.COMMANDS)
        subject.commandAllowed.set(false)
        subject.sendNextCommand(false)
        if (subject.isQueueEmpty()){
            subject.commandAllowed.set(true)
        }
        assertEquals(subject.commandAllowed.get(), true)

    }

    @Test
    fun isQueueEmpty() = runTest {
        subject.receiveCommand("ffg", null, WorkMode.COMMANDS)
        subject.removeCommand()
        assertEquals(subject.isQueueEmpty(), true)

    }

    @Test
    fun getCurrentCommand()= runTest {
        subject.receiveCommand("arbuz", null, WorkMode.COMMANDS)
        assertEquals( subject.getCurrentCommand(), "arbuz")

    }

    @Test
    fun sendNextCommand() = runTest{
        subject.receiveCommand("arbuz", null, WorkMode.COMMANDS)
        subject.sendNextCommand(false)
        assertEquals(subject.isQueueEmpty(), true)
        subject.receiveCommand("arbuz", null, WorkMode.COMMANDS)
        subject.receiveCommand("arbuz", null, WorkMode.COMMANDS)
        subject.receiveCommand("arbuz", null, WorkMode.COMMANDS)
        subject.sendNextCommand(true)
        assertEquals(subject.isQueueEmpty(), true)
        subject.receiveCommand("arbuz", null, WorkMode.COMMANDS)
        subject.receiveCommand("arbuz", null, WorkMode.COMMANDS)
        subject.receiveCommand("arbuz", null, WorkMode.COMMANDS)
        subject.sendNextCommand(false)
        assertEquals(subject.isQueueEmpty(), false)
        subject.removeCommand()
        subject.receiveCommand("hello", null, WorkMode.COMMANDS)
        subject.sendNextCommand()
        assertEquals(subject.getCurrentCommand(), "hello")
        subject.removeCommand("hello")
        subject.sendNextCommand(true)

    }

    @Test
    fun removeCommand() = runTest {
        subject.receiveCommand("0153", null, WorkMode.COMMANDS)
        subject.receiveCommand("0105", null, WorkMode.COMMANDS)
        subject.receiveCommand("0106", null, WorkMode.COMMANDS)
        subject.receiveCommand("0107", null, WorkMode.COMMANDS)
        subject.removeCommand()
        assertEquals(subject.isQueueEmpty(), true)
        subject.receiveCommand("0153", null, WorkMode.COMMANDS)
        subject.receiveCommand("0105", null, WorkMode.COMMANDS)
        subject.receiveCommand("0106", null, WorkMode.COMMANDS)
        subject.receiveCommand("0107", null, WorkMode.COMMANDS)
        subject.removeCommand("0153")
        subject.removeCommand("0105")
        assertEquals(subject.getCurrentCommand(), "0106")

    }

    @Test
    fun receiveCommand() = runTest {
        subject.receiveCommand("0103", null, WorkMode.COMMANDS)
        assertEquals(subject.isQueueEmpty(), false)
        subject.receiveCommand("0102", null, WorkMode.IDLE)
        assertEquals(subject.isQueueEmpty(), false)
        assertEquals( subject.getCurrentCommand(), "0103")
        subject.removeCommand("0103")
        assertEquals( subject.getCurrentCommand(), "0102")

>>>>>>> 61257416ebc4218fbd9b3c63ea2dcb4f83c64b4a
    }

    @Test
    fun receiveMultiCommand() {
    }
}