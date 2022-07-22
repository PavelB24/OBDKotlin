package main

enum class WorkMode {
    IDLE,  //IDLE on Start
    PROTOCOL, //After reset
    SETTINGS, // After setting protocol
    CLARIFICATION,
    COMMANDS, //After settings
}