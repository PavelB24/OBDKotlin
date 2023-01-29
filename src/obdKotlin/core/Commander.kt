package obdKotlin.core

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import obdKotlin.WorkMode
import obdKotlin.commandProcessors.BaseCommandHandler
import obdKotlin.commandProcessors.CommandHandler
import obdKotlin.decoders.AtDecoder
import obdKotlin.decoders.Decoder
import obdKotlin.decoders.PinAnswerDecoder
import obdKotlin.decoders.SpecialEncoderHost
import obdKotlin.encoders.SpecialEncoder
import obdKotlin.exceptions.NoSourceProvidedException
import obdKotlin.messages.Message
import obdKotlin.profiles.Profile
import obdKotlin.protocol.BaseProtocolManager
import obdKotlin.protocol.Protocol
import obdKotlin.protocol.ProtocolManager
import obdKotlin.source.Source

abstract class Commander(
    protected val enableRawData: Boolean,
    protoManager: BaseProtocolManager
) {

    protected val _encodedDataMessages: MutableSharedFlow<Message?> = MutableSharedFlow()
    val encodedDataMessages: SharedFlow<Message?> = _encodedDataMessages

    protected val _rawDataFlow: MutableSharedFlow<String> = MutableSharedFlow()
    val rawDataFlow: SharedFlow<String> = _rawDataFlow

    protected val protocolManager: BaseProtocolManager = protoManager

    var workMode = WorkMode.IDLE
        protected set

    /**
     *  Header Address for ATSH receiver Address for ATCRA
     *  Encoder do encode the answer from CAN
     */

    @Throws(NoSourceProvidedException::class)
    abstract fun startAndRemember(
        protocol: Protocol?,
        extra: List<String>?,
        specialEncoder: SpecialEncoder?,
        extendedMode: Boolean = false
    )

    @Throws(NoSourceProvidedException::class)
    abstract fun startWithAuto(
        extra: List<String>? = null,
        specialEncoder: SpecialEncoder? = null,
        extendedMode: Boolean = false
    )

    abstract fun start(
        protocol: Protocol? = null,
        extra: List<String>? = null,
        specialEncoder: SpecialEncoder? = null,
        extendedMode: Boolean = false
    )
    abstract fun bindSource(source: Source, resetStates: Boolean = false)
    abstract fun setNewSetting(
        command: String
    )

    abstract fun sendRawCommand(command: String)

    abstract suspend fun resetSettings()

    abstract fun sendCommand(command: String, repeatTime: Long? = null)
    abstract fun startWithProfile(profile: Profile)
    abstract fun stop()

    abstract fun disconnect()

    class Builder {

        private var source: Source? = null
        private var useWS: Boolean = false
        private var enableRawData: Boolean = false
        private var protocolManager: BaseProtocolManager = ProtocolManager()
        private var atDecoderClass: Decoder = AtDecoder()
        private var pinDecoderClass: SpecialEncoderHost = PinAnswerDecoder()
        private var commandHandler: BaseCommandHandler = CommandHandler()
        private var eventListener: SystemEventListener? = null

        private fun resetStates() {
            source = null
            useWS = false
            protocolManager = ProtocolManager()
            atDecoderClass = AtDecoder()
            pinDecoderClass = PinAnswerDecoder()
            commandHandler = CommandHandler()
        }

        fun source(source: Source): Builder {
            this.source = source
            return this
        }

        fun customProtocolManager(manager: BaseProtocolManager): Builder {
            protocolManager = manager
            return this
        }

        fun customCommandHandler(handler: BaseCommandHandler): Builder {
            commandHandler = handler
            return this
        }

        fun customAtDecoder(atDecoderClass: Decoder): Builder {
            this.atDecoderClass = atDecoderClass
            return this
        }

        fun setEventListener(eventListener: SystemEventListener) {
            this.eventListener = eventListener
        }

        fun useWarmStarts(): Builder {
            useWS = true
            return this
        }

        fun customPinDecoder(pinDecoderClass: SpecialEncoderHost): Builder {
            this.pinDecoderClass = pinDecoderClass
            return this
        }

        fun enableRawData(): Builder {
            enableRawData = true
            return this
        }

        fun build(): Commander {
            val commander = if (source != null) {
                OBDCommander(
                    protocolManager,
                    useWS,
                    atDecoderClass,
                    pinDecoderClass,
                    commandHandler,
                    eventListener,
                    source!!,
                    enableRawData
                )
            } else {
                OBDCommander(
                    protocolManager,
                    useWS,
                    atDecoderClass,
                    pinDecoderClass,
                    commandHandler,
                    eventListener,
                    enableRawData
                )
            }
            return commander
        }
    }

    abstract fun removeRepeatedCommand(command: String)

    abstract fun removeAllCommands()

    abstract fun sendCommands(commands: List<String>, repeatTime: Long? = null)
    abstract fun sendMultiCommand()
}
