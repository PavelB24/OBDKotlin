package obdKotlin.core

import kotlinx.coroutines.flow.MutableSharedFlow
import obdKotlin.commands.Commands
import obdKotlin.protocol.BaseProtocolManager
import obdKotlin.messages.Message
import obdKotlin.profiles.CustomProfile
import obdKotlin.profiles.Profile
import obdKotlin.protocol.Protocol
import obdKotlin.protocol.ProtocolManager
import obdKotlin.source.Source

abstract class Commander(protoManager: BaseProtocolManager) {
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

    ИНФОРМАЦИЯ ПО ОБЩЕНИЮ НА КАН ШИНЕ
    https://en.wikipedia.org/wiki/Unified_Diagnostic_Services
    открытие сессии диагностики 10C0
    https://www.csselectronics.com/pages/uds-protocol-tutorial-unified-diagnostic-services
    https://automotive.wiki/index.php/ISO_14229
    https://community.carloop.io/t/list-of-can-id-descriptions-from-opengarages-org/104

    FORD
    https://sourceforge.net/p/ecu/wiki/canbus/
    сайт хохла петуха про ид фордов
    http://sergeyk.kiev.ua/avto/ford_CAN_bus/
    BMV
    http://www.loopybunny.co.uk/CarPC/k_can.html
    MAZDA
    http://www.madox.net/blog/projects/mazda-can-bus/
    LEXUS
    https://github.com/Paucpauc/lexus_canbus_id
    Dodge
    http://opengarages.org/index.php/Dodge_CAN_ID
    Volvo
    https://docs.google.com/spreadsheets/d/10vq5NIZu0Sd2SSoK2_YSrcsWrItZNC0X2rPcIWvLuS8/edit#gid=542587416
    Reno
    https://github.com/ashtorak/CanSeeNoiseGen/blob/main/src/zoe.cpp

    https://elm3.ru/instruktsii/pidy-dlya-torque

    https://www.csselectronics.com/pages/obd2-pid-table-on-board-diagnostics-j1979

     В планах вписать в билдер кастомный АТ и Пин декодеры
     */


    abstract val eventFlow: MutableSharedFlow<Message?>

    abstract fun startWithProto(protocol: Protocol)
    abstract fun switchSource(source: Source, resetStates: Boolean = false)
    abstract fun setNewSetting(
        command: Commands.AtCommands

    )
    abstract fun setSettingWithParameter(
        command: Commands.AtCommands,
        parameter: String
    )

    abstract  fun resetSettings()
    abstract fun startWithAuto()
    abstract fun startWithProtoAndRemember(protocol: Protocol)
    abstract fun setCustomCommand(customCommand: String, repeat: Boolean = false, repeatTime: Long= 500L)
    abstract fun setCommand(command: Commands.PidCommands, repeat: Boolean = false, repeatTime: Long= 500L )
    abstract  fun startWorkWithProfile(profile: Profile)
    abstract  fun startWorkWithProfile(profile: CustomProfile)
    abstract fun stop()

    class Builder {
        companion object {

            private var source: Source? = null
            private var protocolManager: BaseProtocolManager = ProtocolManager()

            init {
                resetStates()
            }

            private fun resetStates(){
                source = null
                protocolManager = ProtocolManager()
            }

            @JvmStatic
            fun addSource(source: Source): Companion {
                Companion.source = source
                return this
            }

            @JvmStatic
            fun addCustomProtocolManager(manager: BaseProtocolManager): Companion {
                protocolManager = manager
                return this
            }

            @JvmStatic
            fun build(): Commander {
                val commander = if (source != null) {
                    OBDCommander(protocolManager, source!!)
                } else {
                    OBDCommander(protocolManager)
                }
                resetStates()
                return commander
            }
        }
    }
}