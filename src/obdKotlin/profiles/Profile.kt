package obdKotlin.profiles

import obdKotlin.encoders.SpecialEncoder
import obdKotlin.protocol.Protocol

data class Profile(
    val protocol: Protocol,
    val settingsAndParams: List<String>, // setting and param
    val encoder: SpecialEncoder? = null
)
