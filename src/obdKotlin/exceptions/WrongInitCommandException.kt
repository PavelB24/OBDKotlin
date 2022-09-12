package obdKotlin.exceptions

class WrongInitCommandException() : Exception() {

    constructor(text: String) : this() {
        customMessage = text
    }

    var customMessage: String? = null

    override val message: String?
        get() = customMessage
}
