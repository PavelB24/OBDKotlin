package obdKotlin.decoders

sealed class EncodingState {
    object Successful : EncodingState()
    data class Unsuccessful(val onAnswer: String) : EncodingState()
    object WaitNext : EncodingState()
}
