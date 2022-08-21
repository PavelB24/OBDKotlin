package obdKotlin.core

import kotlinx.coroutines.flow.SharedFlow
import obdKotlin.commandProcessors.BaseCommandHandler
import obdKotlin.commandProcessors.CommandHandler
import obdKotlin.decoders.*
import obdKotlin.decoders.PinAnswerDecoder
import obdKotlin.encoders.SpecialEncoder
import obdKotlin.protocol.BaseProtocolManager
import obdKotlin.messages.Message
import obdKotlin.profiles.Profile
import obdKotlin.protocol.Protocol
import obdKotlin.protocol.ProtocolManager
import obdKotlin.source.Source

abstract class Commander(protoManager: BaseProtocolManager) {



    abstract val encodedDataMessages: SharedFlow<Message?>

    /**
     *  Header Address for ATSH receiver Address for ATCRA
     *  Encoder do encode the answer from CAN
     */
    abstract fun switchToCanMode(
        headerAddress: String,
        receiverAddress: String?,
        specialEncoder: SpecialEncoder,
        extra: List<String>
    )

    abstract fun switchProtocol(protocol: Protocol)

    abstract fun startWithProto(protocol: Protocol, systemEventListener: SystemEventListener? = null)
    abstract fun bindSource(source: Source, resetStates: Boolean = false)
    abstract fun setNewSetting(
        command: String
    )

    abstract  fun resetSettings()
    abstract fun startWithAuto(systemEventListener: SystemEventListener? = null)
    abstract fun startWithProtoAndRemember(protocol: Protocol, systemEventListener: SystemEventListener? = null)
    abstract fun setCommand(command: String, repeatTime: Long? = null)
    abstract fun startWithProfile(profile: Profile, systemEventListener: SystemEventListener?)
    abstract fun stop()

    abstract fun switchToStandardMode(extra: List<String>)

    class Builder {

            private var source: Source? = null
            private var useWS: Boolean = false
            private var protocolManager: BaseProtocolManager = ProtocolManager()
            private var atDecoderClass: Decoder = AtDecoder()
            private var pinDecoderClass: SpecialEncoderHost = PinAnswerDecoder()
            private var commandHandler: BaseCommandHandler = CommandHandler()



            private fun resetStates(){
                source = null
                useWS = false
                protocolManager = ProtocolManager()
                atDecoderClass = AtDecoder()
                pinDecoderClass= PinAnswerDecoder()
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

            fun customAtDecoder(atDecoderClass: Decoder): Builder{
                this.atDecoderClass = atDecoderClass
                return this
            }


            fun useWarmStarts(): Builder{
                useWS = true
                return this
            }


            fun customPinDecoder(pinDecoderClass: SpecialEncoderHost): Builder{
                this.pinDecoderClass = pinDecoderClass
                return this
            }


            fun build(): Commander {
                val commander = if (source != null) {
                    OBDCommander(protocolManager, useWS,  atDecoderClass, pinDecoderClass, commandHandler, source!!)
                } else {
                    OBDCommander(protocolManager, useWS, atDecoderClass, pinDecoderClass, commandHandler)
                }
                resetStates()
                return commander
            }
        }


}