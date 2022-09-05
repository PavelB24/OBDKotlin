package obdKotlin.profiles

import obdKotlin.encoders.SpecialEncoder
import obdKotlin.protocol.Protocol

data class Profile(
    val protocol: Protocol,
    val settingsAndParams: List<String>,//setting and param
    val canMode: Boolean,
    val encoder: SpecialEncoder? = null
)