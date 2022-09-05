package obdKotlin.profiles

import obdKotlin.encoders.SpecialEncoder
import obdKotlin.protocol.Protocol

data class Profile(
    val protocol: Protocol,
<<<<<<< HEAD
    val settingsAndParams: List<String> ,//setting and param
=======
    val settingsAndParams: List<String>,//setting and param
>>>>>>> 61257416ebc4218fbd9b3c63ea2dcb4f83c64b4a
    val canMode: Boolean,
    val encoder: SpecialEncoder? = null
)