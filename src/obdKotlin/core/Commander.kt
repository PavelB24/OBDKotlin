package obdKotlin.core

import kotlinx.coroutines.flow.SharedFlow
import obdKotlin.commandProcessors.BaseCommandHandler
import obdKotlin.commandProcessors.CommandHandler
import obdKotlin.commands.CommandContainer
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

abstract class Commander(protoManager: BaseProtocolManager) {

    abstract val encodedDataMessages: SharedFlow<Message?>

    val protocolManager: BaseProtocolManager = protoManager

    /**
     *  Header Address for ATSH receiver Address for ATCRA
     *  Encoder do encode the answer from CAN
     */

    @Throws(NoSourceProvidedException::class)
    abstract fun startAndRemember(
        protocol: Protocol?,
        systemEventListener: SystemEventListener?,
        extra: List<String>?,
        specialEncoder: SpecialEncoder?
    )

    @Throws(NoSourceProvidedException::class)
    abstract fun startWithAuto(
        systemEventListener: SystemEventListener?,
        extra: List<String>?,
        specialEncoder: SpecialEncoder?
    )

    abstract fun start(
        protocol: Protocol?,
        systemEventListener: SystemEventListener?,
        extra: List<String>?,
        specialEncoder: SpecialEncoder?
    )
    abstract fun bindSource(source: Source, resetStates: Boolean = false)
    abstract fun setNewSetting(
        command: String
    )

    abstract fun resetSettings()

    abstract fun sendCommand(command: String, repeatTime: Long? = null)
    abstract fun startWithProfile(profile: Profile, systemEventListener: SystemEventListener?)
    abstract fun stop()

    class Builder {

        private var source: Source? = null
        private var useWS: Boolean = false
        private var protocolManager: BaseProtocolManager = ProtocolManager()
        private var atDecoderClass: Decoder = AtDecoder()
        private var pinDecoderClass: SpecialEncoderHost = PinAnswerDecoder()
        private var commandHandler: BaseCommandHandler = CommandHandler()

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

        fun useWarmStarts(): Builder {
            useWS = true
            return this
        }

        fun customPinDecoder(pinDecoderClass: SpecialEncoderHost): Builder {
            this.pinDecoderClass = pinDecoderClass
            return this
        }

        fun build(): Commander {
            val commander = if (source != null) {
                OBDCommander(protocolManager, useWS, atDecoderClass, pinDecoderClass, commandHandler, source!!)
            } else {
                OBDCommander(protocolManager, useWS, atDecoderClass, pinDecoderClass, commandHandler)
            }
            return commander
        }
    }

    abstract fun removeRepeatedCommand(command: String)
    abstract fun removeRepeatedCommands()
    abstract fun sendCommands(commands: List<CommandContainer>)
    abstract fun sendMultiCommand()
}
