package main.commands

enum class AtCommands(val command: String) {

    Repeat("\r"), //Repeat last command
    SetBaudRateDivisor("BRD"),
    Defaults("D"), //Set All to Defaults
    EchoOff("E0"), //Set Echo Off
    EchoOn("E1"), //Set Echo on
    ForgetEvents("FE"), //ForgetEvents
    PrintId("I"), //PrintVersionId
    LineFeedsOn("L1"),
    LineFeedsOff("L0"),
    LowPowerMode("LP"),
    MemoryOff("M0"),
    MemoryOn("M1"),
    ReadStoredData(""),
    SaveDataByte(""),
    WarmStart("WS"),
    DisplayTheDeviceDescription("@1"),
    DisplayVIN("@2"),
    StoreTheVIN("@3"),
    ResetAll("Z"),
    SetWakeUpMessagesOff("SW"),
    FastInit("FI"),

    /**
     * Здесь мы указываем, что elm в качастве фрейма fc всегда будет сообщать ЭБУ, что тот может посылать
     * любое количество оставшихся фреймов ответа, с любым временным интервалом между ними.
     * Когда команда или ответ должны состоять больше чем из одного фрейма, отправляющая сторона всегда посылает
     * только один, первый фрейм.
     * Принимающая сторона должна послать фрейм fc, в котором сообщает как слать оставшиеся фреймы.
     */
    SetTimeOutToMax("FCSD300000"), //



    ReadVoltage("RV"),


    AllowLongMessages("AL"),
    GetActiveMonitors("AMC"),
    AutoReceive("AR"),
    GetReceivedBytes("BD"),
    GetCurrentProto("DP"),
    GetVehicleProtoAsNumber("DPN"),
    HeadersOn("H1"),
    HeadersOff("H0"),
    MonitorAll("MA"),
    CloseCurrentProto("PC"),
    ResponsesOff("R0"),
    ResponsesOn("R1"),
    PrintingSpacesOff("S0"),
    PrintingSpacesOn("S1"),
    SetHeader("SH"),
    SetProto("SP"),
    SetProtoAndAutoSearch("SP A"),
    SetReceiverAddress("SR"),
    UseStandardSearchOrder("SS"),
    TryProto("TR"),
    TryWithAutoSearch("TR A"),
    EraseStoredProto("SP 00"),


    PerformFastInit("FI"),
    SetIsoBaud("IB"),
    DisplayDLCOn("D1"),
    DisplayDLCOff("D0"),
    SetFlowControlMode("FC SM"),
    SetFlowControlHeader("FC SH"),
    FlowControlSetDataTo("FC SD"),
    SentRTRMsg("RTR"),
    UseVarDLCOn("V1"),
    UseVarDLCOff("V0"),

    CanExtAdr("CAE"), // Just CAE to turn off, use CAE hh for turn on
    SetCanIdFilter("CF"), //hhh or hhhhhhhh
    FlowControlOn("CFC1"),
    FlowControlOff("CFC0"),
    SetReceiverAdrFilter("CRA"), //Just CRA for reset or hhh or hhhhhhhh for set
    GetCanStatusCount("CS"),
    ClientMonitoringOff("CSM0"),
    ClientMonitoringOn("CSM1"),
    SetIdMask("CM"), //hhh hhhhhhhh
    AdaptiveTimingOn("AT1"),
    AdaptiveTimingOff("AT0"),
    AutoFormatCanFramesOn("CAF1"),
    AutoFormatCanFramesOff("CAF0"),




}