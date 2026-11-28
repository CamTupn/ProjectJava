package com.scentstation.simulator.model;

public enum SimulationScenario {
    NORMAL("Normal Operation"),
    DEVICE_ERROR("Device / Pump Error"),
    EMPTY_SLOT("Empty Slot / Low Fluid"),
    TIMEOUT("Command Timeout (No Result)"),
    OFFLINE("Device Offline (Stop Heartbeat)");

    private final String displayName;
    SimulationScenario(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
