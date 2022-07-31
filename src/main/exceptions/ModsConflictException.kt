package main.exceptions

class ModsConflictException() : Exception() {

    constructor(text: String): this(){
        customMessage = text
    }

    var customMessage: String? = null

    override val message: String?
        get() = customMessage
}
