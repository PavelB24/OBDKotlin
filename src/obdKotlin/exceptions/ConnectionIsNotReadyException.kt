package obdKotlin.exceptions

class ConnectionIsNotReadyException(private val case: String): IllegalStateException(case) {
}