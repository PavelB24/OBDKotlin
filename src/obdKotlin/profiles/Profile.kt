package obdKotlin.profiles

import obdKotlin.encoders.SpecialEncoder
import obdKotlin.protocol.Protocol

abstract class Profile(
    val protocol: Protocol?,
    val settingsAndParams: List<String>, // setting and params
    val encoder: SpecialEncoder? = null,
    val commands: List<String>? = null,
    val extendedMode: Boolean = true
)
