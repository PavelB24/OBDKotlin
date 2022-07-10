import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import main.ModsConflictException
import main.OBDCommander
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.jvm.Throws

class ProtocolManager() {

    constructor(protocols: Set<Protocol>) : this() {
        usersProtocolSet.addAll(protocols)
    }

    constructor(protocol: Protocol) : this() {
        usersProtocolSet.add(protocol)
    }

    private var usersProtocolSet: MutableSet<Protocol> = mutableSetOf()
    private val _obdCommandFlow = MutableSharedFlow<String>()
    val obdCommandFlow: SharedFlow<String> = _obdCommandFlow

    private val standardSettingsSet: Set<AtCommands> = setOf(
        AtCommands.ResetAll, AtCommands.PrintingSpacesOff,
        AtCommands.EchoOff, AtCommands.AllowLongMessages
    )

    private val canCommandsSet: Set<AtCommands> = setOf(
        AtCommands.ResetAll, AtCommands.PrintingSpacesOff,
        AtCommands.EchoOff, AtCommands.AllowLongMessages
    )


    private val standardProtocolSet: Set<Protocol> = setOf(
        Protocol.ISO_15765_4_CAN_11_bit_ID_500kbaud, Protocol.AUTOMATIC
    )

    private val tryProtocolQueue = ConcurrentLinkedQueue<Protocol>()
    private val settingsQueue = ConcurrentLinkedQueue<AtCommands>()

    //invoke this first time with WM idle when switch into proto




    suspend fun setTriedProto() {
        if (tryProtocolQueue.isNotEmpty()) {
            _obdCommandFlow.emit("${OBDCommander.OBD_PREFIX} ${AtCommands.SetProto} ${tryProtocolQueue.poll().hexOrdinal}")
        }
    }

     suspend fun setProto() {
        if (tryProtocolQueue.isNotEmpty()) {
            _obdCommandFlow.emit("${OBDCommander.OBD_PREFIX} ${AtCommands.SetProto} ${tryProtocolQueue.poll().hexOrdinal}")
        } else {
            _obdCommandFlow.emit("${OBDCommander.OBD_PREFIX} ${AtCommands.SetProto} ${Protocol.AUTOMATIC.hexOrdinal}")
        }
    }

    suspend fun skipProto(){
        tryProtocolQueue.poll()
        tryNextProto()
    }

    suspend fun tryNextProto() {
        if (tryProtocolQueue.isNotEmpty()) {
            _obdCommandFlow.emit("${OBDCommander.OBD_PREFIX} ${AtCommands.TryProto} ${tryProtocolQueue.peek().hexOrdinal}")
        } else if (tryProtocolQueue.isNotEmpty() && tryProtocolQueue.size == 1) {
            setTriedProto()
        }
    }

    suspend fun askObdProto() {  //Use only when we want set recommended proto
        _obdCommandFlow.emit("${OBDCommander.OBD_PREFIX} ${AtCommands.GetVehicleProtoAsNumber.command}")
    }

    //Set proto without try or checking results only after obd answer with hexNum of proto
    suspend fun setRecommendedProto(hex: String) {
        Protocol.values().forEach {
            if (it.hexOrdinal == hex) {
                _obdCommandFlow.emit("${OBDCommander.OBD_PREFIX} ${AtCommands.SetProto} ${it.hexOrdinal}")
            }
        }
    }

    suspend fun onRestart(canMode: Boolean = false, auto: Boolean = false) {
        checkMods(canMode, auto)
        prepareOBD(canMode, auto)
        _obdCommandFlow.emit("${OBDCommander.OBD_PREFIX} ${settingsQueue.poll().command}")
    }

    @Throws(ModsConflictException::class)
    private fun checkMods(canMode: Boolean , auto: Boolean) {
        if(canMode && auto){
            throw ModsConflictException()
        }
    }

    fun isLastCommandSend(): Boolean =  settingsQueue.isEmpty()

    suspend fun sendNextSettings() {
        _obdCommandFlow.emit("${OBDCommander.OBD_PREFIX} ${settingsQueue.poll().command}")
    }

    private fun prepareOBD(canMode: Boolean, auto: Boolean) {
        if (usersProtocolSet.isNotEmpty() && !auto && !canMode) {
            tryProtocolQueue.addAll(usersProtocolSet)
            settingsQueue.addAll(standardSettingsSet)
        } else if (auto) {
            tryProtocolQueue.add(Protocol.AUTOMATIC)
            settingsQueue.addAll(standardSettingsSet)
        } else if (canMode){
            tryProtocolQueue.add(Protocol.ISO_15765_4_CAN_11_bit_ID_500kbaud)
            settingsQueue.addAll(canCommandsSet)
        } else {
            tryProtocolQueue.addAll(standardProtocolSet)
            settingsQueue.addAll(standardSettingsSet)
        }

    }

    suspend fun setSetting(command: AtCommands) {
        _obdCommandFlow.emit("${OBDCommander.OBD_PREFIX} ${command.command}")
    }


}