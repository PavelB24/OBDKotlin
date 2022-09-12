package obdKotlin.exceptions

class NoSourceProvidedException() : Exception() {

    constructor(text: String) : this() {
        customMessage = text
    }

    var customMessage: String? = null

    override val message: String?
        get() = customMessage
}
