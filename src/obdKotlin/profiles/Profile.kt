package obdKotlin.profiles

import obdKotlin.encoders.SpecialEncoder
import obdKotlin.protocol.Protocol
abstract class Profile(
    val protocol: Protocol?,
    val settingsAndParams: List<String>, // setting and params
    val encoder: SpecialEncoder? = null,
    val notAT: List<String>? = null,
    val extendedMode: Boolean = true
) {

    enum class Profiles {
        BYD_F3, BYD_F3_ABS, CHERRY_TIGO_DELPHI, PEUGEOT_308_20120, BMW320E91AT,
        FIAT_PRE_OBD, YANVAR_72, YANVAR_5_11, ACCORD_24, LEXUS_RX330, UAZ_1797
    }

    private class Standard(
        protocol: Protocol = Protocol.ISO_14230_4_FASTINIT,
        settingsAndParams: List<String> = listOf()
    ) : Profile(protocol, settingsAndParams)

    private class BydF3Abs(
        protocol: Protocol = Protocol.ISO_14230_4_FASTINIT,
        settingsAndParams: List<String> = listOf("SH8128f1", "fi"),
        encoder: SpecialEncoder? = SpecialEncoder.getInstance(Profiles.BYD_F3_ABS)
    ) : Profile(protocol, settingsAndParams, encoder)

    private class CherryTiggoDelphi(
        protocol: Protocol = Protocol.ISO_14230_4_FASTINIT,
        settingsAndParams: List<String> = listOf("IB10", "SH8011f1", "st10", "sw00"),
        encoder: SpecialEncoder? = SpecialEncoder.getInstance(Profiles.CHERRY_TIGO_DELPHI)
    ) : Profile(protocol, settingsAndParams, encoder)

    private class Peugeot308Y2010(
        protocol: Protocol = Protocol.ISO_14230_4_FASTINIT,
        settingsAndParams: List<String> = listOf(
            "sh6a9",
            "fcsh6a9",
            "fcsd300000",
            "fcsm1",
            "cra689",
            "stc4",
            "81",
            "st64"
        ),
        commands: List<String> = listOf("1003", "10c0"),
        encoder: SpecialEncoder? = SpecialEncoder.getInstance(Profiles.PEUGEOT_308_20120)
    ) : Profile(protocol, settingsAndParams, encoder, commands)

    private class BMW320E91AT(
        protocol: Protocol? = null,
        settingsAndParams: List<String> = listOf(
            "PBE101", "CRA618", "SH6F1", "FCSH6F1", "FCSD18300F02", "FCSM1", "CEA18", "CM600", "H1"
        ),
        encoder: SpecialEncoder? = SpecialEncoder.getInstance(Profiles.BMW320E91AT)
    ) : Profile(protocol, settingsAndParams, encoder)

    private class FiatPreObd(
        protocol: Protocol = Protocol.ISO_15765_4_CAN_11_bit_ID_500kbaud,
        settingsAndParams: List<String> = listOf("SH8110F1"),
        encoder: SpecialEncoder? = SpecialEncoder.getInstance(Profiles.FIAT_PRE_OBD)
    ) : Profile(protocol, settingsAndParams, encoder)

    private class VazYanvar72(
        protocol: Protocol = Protocol.ISO_14230_4_FASTINIT,
        settingsAndParams: List<String> = listOf("IB10", "SH8110F1", "ST10", "SW00"),
        encoder: SpecialEncoder? = SpecialEncoder.getInstance(Profiles.YANVAR_72)
    ) : Profile(protocol, settingsAndParams, encoder)

    private class VazYanvar511(
        protocol: Protocol = Protocol.ISO_14230_4_FASTINIT,
        settingsAndParams: List<String> = listOf("ib10", "sh8110f1", "st32", "sw00", "fi"),
        encoder: SpecialEncoder? = SpecialEncoder.getInstance(Profiles.YANVAR_5_11)
    ) : Profile(protocol, settingsAndParams, encoder)

    private class HondaAccord24(
        protocol: Protocol = Protocol.ISO_15765_4_CAN_29_bit_ID_500kbaud,
        settingsAndParams: List<String> = listOf("shda1df1"),
        encoder: SpecialEncoder? = SpecialEncoder.getInstance(Profiles.ACCORD_24)
    ) : Profile(protocol, settingsAndParams, encoder)

    private class LexusRX330(
        protocol: Protocol = Protocol.ISO_15765_4_CAN_11_bit_ID_500kbaud,
        settingsAndParams: List<String> = listOf("sh7e0"),
        encoder: SpecialEncoder? = SpecialEncoder.getInstance(Profiles.LEXUS_RX330)
    ) : Profile(protocol, settingsAndParams, encoder)

    private class Uaz1797(
        protocol: Protocol = Protocol.ISO_14230_4_FASTINIT,
        settingsAndParams: List<String> = listOf("SH8110F"),
        encoder: SpecialEncoder? = SpecialEncoder.getInstance(Profiles.UAZ_1797)
    ) : Profile(protocol, settingsAndParams, encoder)

    private class BydF3(
        protocol: Protocol = Protocol.ISO_14230_4_FASTINIT,
        settingsAndParams: List<String> = listOf("SH8111F1", "SW00"),
        encoder: SpecialEncoder? = SpecialEncoder.getInstance(Profiles.BYD_F3)
    ) : Profile(protocol, settingsAndParams, encoder)
    companion object {
        fun getProfile(type: Profiles): Profile {
            return when (type) {
                Profiles.BYD_F3 -> TODO()
                Profiles.BYD_F3_ABS -> TODO()
                Profiles.CHERRY_TIGO_DELPHI -> TODO()
                Profiles.PEUGEOT_308_20120 -> TODO()
                Profiles.BMW320E91AT -> TODO()
                Profiles.FIAT_PRE_OBD -> TODO()
                Profiles.YANVAR_72 -> TODO()
                Profiles.YANVAR_5_11 -> TODO()
                Profiles.ACCORD_24 -> TODO()
                Profiles.LEXUS_RX330 -> TODO()
                Profiles.UAZ_1797 -> TODO()
            }
        }
    }
}
