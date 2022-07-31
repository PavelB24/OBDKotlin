package main.exceptions

import java.lang.IllegalArgumentException

class WrongMessageTypeException(): IllegalArgumentException() {

    constructor(reason: String): this(){
        customMessage = reason
    }

    var customMessage: String? = null

    override val message: String?
        get() = customMessage
}
