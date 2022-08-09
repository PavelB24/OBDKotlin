package obdKotlin.commands


data class MultiCommand(
    val mode: Commands.PidMod,
    val pids: List<String>
)