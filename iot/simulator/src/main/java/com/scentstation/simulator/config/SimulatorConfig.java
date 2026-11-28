package com.scentstation.simulator.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class SimulatorConfig {
    private String brokerUrl = "tcp://localhost:1883";
    private int qos = 1;
    private int heartbeatIntervalSeconds = 10;
    private int telemetryIntervalSeconds = 10;
    private long dispenseDurationMs = 1000L;
    private String dataDirectory = "data";
    private String topicPrefix = "scentstation";
    private String machineId = "simulator-001";
    private String deviceIdentity = "simulator-device-001";
    private String mqttUsername = "simulator-001";
    private String mqttPassword = "";
    private String firmwareVersion = "esp32-sim-1.0.0";

    public static SimulatorConfig loadDefault(String[] args) {
        SimulatorConfig config = new SimulatorConfig();
        Properties props = new Properties();
        try (InputStream in = SimulatorConfig.class.getClassLoader()
                .getResourceAsStream("config/simulator.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load config/simulator.properties", e);
        }

        config.brokerUrl = props.getProperty("mqtt.broker-url", config.brokerUrl).trim();
        config.qos = Integer.parseInt(props.getProperty("mqtt.qos", String.valueOf(config.qos)).trim());
        config.heartbeatIntervalSeconds = Integer.parseInt(
                props.getProperty("heartbeat.interval-seconds", String.valueOf(config.heartbeatIntervalSeconds)).trim());
        config.telemetryIntervalSeconds = Integer.parseInt(
                props.getProperty("telemetry.interval-seconds", String.valueOf(config.telemetryIntervalSeconds)).trim());
        config.dispenseDurationMs = Long.parseLong(
                props.getProperty("dispense.duration-ms", String.valueOf(config.dispenseDurationMs)).trim());
        config.dataDirectory = props.getProperty("simulator.data-directory", config.dataDirectory).trim();
        config.topicPrefix = props.getProperty("mqtt.topic-prefix", config.topicPrefix).trim();

        if (args != null) {
            for (String arg : args) {
                if (arg == null) continue;
                if (arg.startsWith("--machine-id=")) {
                    String val = arg.substring("--machine-id=".length()).trim();
                    if (!val.isEmpty()) {
                        config.machineId = val;
                        config.mqttUsername = val;
                    }
                } else if (arg.startsWith("--device-identity=")) {
                    String val = arg.substring("--device-identity=".length()).trim();
                    if (!val.isEmpty()) {
                        config.deviceIdentity = val;
                    }
                } else if (arg.startsWith("--broker-url=")) {
                    String val = arg.substring("--broker-url=".length()).trim();
                    if (!val.isEmpty()) {
                        config.brokerUrl = val;
                    }
                } else if (arg.startsWith("--mqtt-username=")) {
                    config.mqttUsername = arg.substring("--mqtt-username=".length()).trim();
                } else if (arg.startsWith("--mqtt-password=")) {
                    config.mqttPassword = arg.substring("--mqtt-password=".length());
                } else if (arg.startsWith("--data-directory=")) {
                    String val = arg.substring("--data-directory=".length()).trim();
                    if (!val.isEmpty()) {
                        config.dataDirectory = val;
                    }
                } else if (arg.startsWith("--dispense-duration-ms=")) {
                    config.dispenseDurationMs = Long.parseLong(arg.substring("--dispense-duration-ms=".length()).trim());
                }
            }
        }
        return config;
    }

    public Path resolveDatabasePath() {
        String safeMachineId = (machineId == null || machineId.isBlank())
                ? "default"
                : machineId.replaceAll("[^A-Za-z0-9_-]", "_");
        return Paths.get(dataDirectory, "simulator-" + safeMachineId + ".db");
    }

    public String getBrokerUrl() { return brokerUrl; }
    public void setBrokerUrl(String brokerUrl) { this.brokerUrl = brokerUrl; }
    public int getQos() { return qos; }
    public void setQos(int qos) { this.qos = qos; }
    public int getHeartbeatIntervalSeconds() { return heartbeatIntervalSeconds; }
    public void setHeartbeatIntervalSeconds(int heartbeatIntervalSeconds) { this.heartbeatIntervalSeconds = heartbeatIntervalSeconds; }
    public int getTelemetryIntervalSeconds() { return telemetryIntervalSeconds; }
    public void setTelemetryIntervalSeconds(int telemetryIntervalSeconds) { this.telemetryIntervalSeconds = telemetryIntervalSeconds; }
    public long getDispenseDurationMs() { return dispenseDurationMs; }
    public void setDispenseDurationMs(long dispenseDurationMs) { this.dispenseDurationMs = dispenseDurationMs; }
    public String getDataDirectory() { return dataDirectory; }
    public void setDataDirectory(String dataDirectory) { this.dataDirectory = dataDirectory; }
    public String getTopicPrefix() { return topicPrefix; }
    public void setTopicPrefix(String topicPrefix) { this.topicPrefix = topicPrefix; }
    public String getMachineId() { return machineId; }
    public void setMachineId(String machineId) { this.machineId = machineId; }
    public String getDeviceIdentity() { return deviceIdentity; }
    public void setDeviceIdentity(String deviceIdentity) { this.deviceIdentity = deviceIdentity; }
    public String getMqttUsername() { return mqttUsername; }
    public void setMqttUsername(String mqttUsername) { this.mqttUsername = mqttUsername; }
    public String getMqttPassword() { return mqttPassword; }
    public void setMqttPassword(String mqttPassword) { this.mqttPassword = mqttPassword; }
    public String getFirmwareVersion() { return firmwareVersion; }
    public void setFirmwareVersion(String firmwareVersion) { this.firmwareVersion = firmwareVersion; }
}
