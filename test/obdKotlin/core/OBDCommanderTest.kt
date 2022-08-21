package obdKotlin.core

import obdKotlin.commands.Commands
import obdKotlin.protocol.Protocol
import obdKotlin.protocol.ProtocolManager
import obdKotlin.source.SocketSource
import org.junit.Test

import org.junit.Assert.*
import java.net.Socket

class OBDCommanderTest {


    @Test
    fun getWorkMode() {
    }

    @Test
    fun getEventFlow() {
    }

    @Test
    fun setNewSetting() {
        val core = OBDCommander(ProtocolManager(), SocketSource(Socket()))
        core.setNewSetting(Commands.AtCommands.AdaptiveTimingOn)
        core.workMode = WorkMode.COMMANDS
        Thread.sleep(800)
        assertEquals(WorkMode.SETTINGS, core.workMode)

    }

    @Test
    fun startWithProto() {
        val core = OBDCommander(ProtocolManager(), SocketSource(Socket()))
        core.startWithProto(Protocol.ISO_14230_4_FASTINIT)
        Thread.sleep(250)
        assertEquals(WorkMode.IDLE, core.workMode)
    }

    @Test
    fun resetSettings() {
        val core = OBDCommander(ProtocolManager(), SocketSource(Socket()))
        core.workMode = WorkMode.SETTINGS
        core.resetSettings()
        Thread.sleep(250)
        assertEquals(WorkMode.IDLE, core.workMode)
    }

    @Test
    fun startWithAuto() {
        val core = OBDCommander(ProtocolManager(), SocketSource(Socket()))
        core.workMode = WorkMode.COMMANDS
        core.startWithAuto()
    }

    @Test
    fun startWithProtoAndRemember() {
        val core = OBDCommander(ProtocolManager(), SocketSource(Socket()))
        core.startWithProtoAndRemember(Protocol.ISO_14230_4_FASTINIT)
    }

    @Test
    fun setCommand() {
    }

    @Test
    fun setCustomCommand() {
    }

    @Test
    fun stop() {
        val core = OBDCommander(ProtocolManager(), SocketSource(Socket()))
        core.stop()
    }

    @Test
    fun switchSource() {
        val core = OBDCommander(ProtocolManager(), SocketSource(Socket()))
        core.switchSource(SocketSource(Socket()))
    }

    @Test
    fun configureForCanCommands() {
        val core = OBDCommander(ProtocolManager(), SocketSource(Socket()))
        core.configureForCanCommands("07E", "75A")
    }

    @Test
    fun setSettingWithParameter() {
        val core = OBDCommander(ProtocolManager(), SocketSource(Socket()))
        core.setSettingWithParameter(Commands.AtCommands.CanExtAdr, "07E")
    }

    @Test
    fun startWorkWithProfile() {
    }

    @Test
    fun testStartWorkWithProfile() {
    }

    @Test
    fun checkIfCanProto() {
    }
}