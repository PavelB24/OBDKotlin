package main.messages

import main.protocol.Protocol

data class SelectedProtocolMessage(
    val protocol: Protocol
): Message()
