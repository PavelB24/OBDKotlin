package main.messages

import Protocol

data class SelectedProtocolMessage(
    val protocol: Protocol
): Message()
