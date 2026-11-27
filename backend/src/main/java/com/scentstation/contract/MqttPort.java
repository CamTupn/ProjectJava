package com.scentstation.contract;

/** Outbound port for publishing device commands. */
public interface MqttPort {
    void publishCommand(DeviceCommand command);
}
