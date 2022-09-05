package obdKotlin.commands

data class CommandContainer(
    val command: String,
    val delay: Long?
)