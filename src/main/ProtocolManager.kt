import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import main.OnPositiveAnswerStrategy
import main.WorkMode
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

class ProtocolManager() {

    var usersProtocolSet: Set<Protocol>? = null

    constructor(protocols: Set<Protocol>) : this() {
        usersProtocolSet = protocols
    }

    constructor(protocol: Protocol): this(){
        selectedOneProto = protocol
    }

    private var selectedOneProto: Protocol? = null

    private val _obdCommandFlow = MutableSharedFlow<String>()
    val obdCommandFlow: SharedFlow<String> = _obdCommandFlow

    private val standardSettingsSet: Set<AtCommands> = setOf(
        AtCommands.ResetAll, AtCommands.PrintingSpacesOff,
        AtCommands.EchoOff, AtCommands.AllowLongMessages
    )

    val standardSettingsSet2: Set<AtCommands>
        get() = TODO("Not yet implemented")


    private val standardProtocolSet: Set<Protocol> = setOf(
        Protocol.ISO_15765_4_CAN_11_bit_ID_500kbaud, Protocol.AUTOMATIC
    )

    private val tryProtocolQueue = ConcurrentLinkedQueue<Protocol>()
    private val settingsQueue = ConcurrentLinkedQueue<AtCommands>(standardSettingsSet)

    //invoke this first time with WM idle when switch into proto
    suspend fun handlePositiveAnswer(mode: WorkMode, strategy: OnPositiveAnswerStrategy) {
        when (strategy) {
            OnPositiveAnswerStrategy.IDLE -> Unit //готов
            OnPositiveAnswerStrategy.TRY -> if (mode == WorkMode.IDLE) tryNextProto() else setTriedProto() //готов
            OnPositiveAnswerStrategy.ASK_RECOMMENDED -> askObdProto() //готов
            OnPositiveAnswerStrategy.SET -> setProto() //готов
        }
    }

    suspend fun handleNegativeAnswer(strategy: OnPositiveAnswerStrategy) {
        when (strategy) {
            OnPositiveAnswerStrategy.IDLE -> Unit
            OnPositiveAnswerStrategy.TRY -> {
                tryProtocolQueue.poll()
                tryNextProto()
            }
            OnPositiveAnswerStrategy.ASK_RECOMMENDED -> askObdProto()
            OnPositiveAnswerStrategy.SET -> TODO()
        }
    }

    private suspend fun setTriedProto() {
        if (tryProtocolQueue.isNotEmpty()) {
            _obdCommandFlow.emit("${OBDCommander.OBD_PREFIX} ${AtCommands.SetProto} ${tryProtocolQueue.poll().hexOrdinal}")
        }
    }

    private suspend fun setProto() {
        if (selectedOneProto != null) {
            _obdCommandFlow.emit("${OBDCommander.OBD_PREFIX} ${AtCommands.SetProto} ${selectedOneProto!!.hexOrdinal}")
        } else if(tryProtocolQueue.isNotEmpty()){
            _obdCommandFlow.emit("${OBDCommander.OBD_PREFIX} ${AtCommands.SetProto} ${tryProtocolQueue.poll().hexOrdinal}")
        } else{
            _obdCommandFlow.emit("${OBDCommander.OBD_PREFIX} ${AtCommands.SetProto} ${Protocol.AUTOMATIC.hexOrdinal}")
        }
    }


    private suspend fun tryNextProto() {
        if (tryProtocolQueue.isNotEmpty()) {
            _obdCommandFlow.emit("${OBDCommander.OBD_PREFIX} ${AtCommands.TryProto} ${tryProtocolQueue.peek().hexOrdinal}")
        } else if (tryProtocolQueue.isNotEmpty() && tryProtocolQueue.size == 1) {
            setTriedProto()
        }
    }

    private suspend fun askObdProto() {  //Use only when we want set recommended proto
        _obdCommandFlow.emit("${OBDCommander.OBD_PREFIX} ${AtCommands.GetVehicleProtoAsNumber.command}")
    }

    //Set proto without try or checking results only after obd answer with hexNum of proto
    suspend fun setRecommendedProto(hex: String) {
        Protocol.values().forEach {
            if (it.hexOrdinal == hex) {
                _obdCommandFlow.emit("${OBDCommander.OBD_PREFIX} ${AtCommands.SetProto} ${it.hexOrdinal}")
            }
        }
        sendNextSettings()
    }

    suspend fun resetSettings(auto: Boolean = false) {
        prepareOBD(auto)
        _obdCommandFlow.emit("${OBDCommander.OBD_PREFIX} ${settingsQueue.poll().command}")
    }

    private suspend fun sendNextSettings() {
        _obdCommandFlow.emit("${OBDCommander.OBD_PREFIX} ${settingsQueue.poll().command}")
    }

    private fun prepareOBD(auto: Boolean) {
        if (usersProtocolSet != null && !auto) {
            tryProtocolQueue.addAll(usersProtocolSet!!)

        } else if (auto) {
            tryProtocolQueue.add(Protocol.AUTOMATIC)
        } else {
            tryProtocolQueue.addAll(standardProtocolSet)
        }
    }


}