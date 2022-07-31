package main.messages

import main.WorkMode
import main.messages.SystemMessage

data class OBDDataMessage(
    val binaryData: ByteArray,
    val workMode: WorkMode? = null
) : SystemMessage()

