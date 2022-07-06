package main

enum class WorkMode {
    IDLE,  //IDLE on Start
    PROTOCOL, //After reset
    SETTINGS, // After setting protocol
    COMMANDS, //After settings
    CAN_COMMANDS //After settings
}