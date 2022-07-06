import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.IOException
import java.nio.channels.SocketChannel
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.jvm.Throws

abstract class BusCommander( protoManager: ProtocolManager) {
/**
    <-----> Основной источник по 15765-4 <----->
    https://www.drive2.ru/b/472129194529128744/?m=526712250266812507&page=0#a526712250266812507
    <-----> Хак шины <----->
    https://habr.com/ru/post/544144/
    <-----> Коды ошибок от коллеги на питоне <----->
    https://gitlab.com/py_ren/pyren/-/blob/master/pyren/mod_elm.py
    <-----> Список машин по протоколам <----->
    https://elm-scanner.ru/protocols/iso-15765-4-can.html

    ATZ // сброс настроек
    AT E0 // отключаем эхо
    AT L0 // отключаем перенос строки
    AT SP 6 // Задаем протокол ISO 15765-4 CAN (11 bit ID, 500 kbaud)
    AT ST 10 // Таймаут 10 * 4 мс, иначе EBU шлет повторные ответы каждые 100 мс, а мы не отвечаем, потому что ожидаем конца, а нам нужен только первый ответ
    AT CA F0
    AT AL // Allow Long (>7 byte) messages
    AT SH 7E0 // задаем ID, к кому обращаемся (двигатель)
    AT CRA 7E8 // CAN Receive Address. Можно задать несколько 7Xe
    AT FC SH 7E0
    AT FC SD 30 00 00
    AT FC SM 1 // Режим Flow Control 1 должен быть определен после FC SH и FC SD, иначе в ответ придет "?"
    03 22 F4 0С 55 55 55 55 // UDS запрос оборотов двигателя
*/


    abstract val socketEventFlow: MutableSharedFlow<Event<OBDMessage?>>

    abstract fun tryProtos()

    abstract fun switchToCanMode()

    abstract suspend fun resetSettings()

    abstract fun obdAutoAll()

    abstract fun setProto()

    abstract fun setCustomOBDSettings(obdCommands: Set<String>)

    abstract fun setCommand(command: String)

    abstract fun setPinCommand(command: String)

    abstract fun stopJob()

    abstract fun askAndSetRecommendedProto()


}