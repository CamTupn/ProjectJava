package com.scentstation.simulator.mqtt;

public interface SimulatorMqttPublisher {
    void publishStatus(String jsonPayload);
    void publishEvent(String jsonPayload);
    boolean isConnected();
}
