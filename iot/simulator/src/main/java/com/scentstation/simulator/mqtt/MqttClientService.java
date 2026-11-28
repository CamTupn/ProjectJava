package com.scentstation.simulator.mqtt;

import com.scentstation.simulator.config.SimulatorConfig;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MqttClientService implements SimulatorMqttPublisher {
    private final SimulatorConfig config;
    private final Consumer<String> activityLogger;
    private final BiConsumer<Boolean, String> connectionStateListener;

    private volatile MqttClient client;
    private volatile BiConsumer<String, String> messageConsumer;

    public MqttClientService(
            SimulatorConfig config,
            Consumer<String> activityLogger,
            BiConsumer<Boolean, String> connectionStateListener
    ) {
        this.config = config;
        this.activityLogger = activityLogger != null ? activityLogger : msg -> {};
        this.connectionStateListener = connectionStateListener != null ? connectionStateListener : (c, r) -> {};
    }

    public void setMessageConsumer(BiConsumer<String, String> messageConsumer) {
        this.messageConsumer = messageConsumer;
    }

    public synchronized void connect() throws MqttException {
        if (client != null && client.isConnected()) {
            return;
        }
        String machineId = requireNonBlank(config.getMachineId(), "machineId");
        String clientId = "scentstation-sim-" + machineId + "-" + UUID.randomUUID().toString().substring(0, 8);
        String commandTopic = getCommandTopic();

        MqttClient mqttClient = new MqttClient(config.getBrokerUrl(), clientId, new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setAutomaticReconnect(false);
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(20);

        if (config.getMqttUsername() != null && !config.getMqttUsername().isBlank()) {
            options.setUserName(config.getMqttUsername().trim());
        }
        if (config.getMqttPassword() != null && !config.getMqttPassword().isEmpty()) {
            options.setPassword(config.getMqttPassword().toCharArray());
        }

        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                String reason = cause != null ? cause.getMessage() : "Connection closed";
                activityLogger.accept("[MQTT] Connection lost: " + reason);
                connectionStateListener.accept(false, reason);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                activityLogger.accept("[MQTT IN] " + topic + " -> " + payload);
                BiConsumer<String, String> consumer = messageConsumer;
                if (consumer != null) {
                    consumer.accept(topic, payload);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // QoS 1 delivery confirmed
            }
        });

        activityLogger.accept("[MQTT] Connecting to " + config.getBrokerUrl() + " as machineId=" + machineId + " ...");
        mqttClient.connect(options);
        mqttClient.subscribe(commandTopic, config.getQos());
        this.client = mqttClient;

        activityLogger.accept("[MQTT] Connected & subscribed to " + commandTopic);
        connectionStateListener.accept(true, "Connected");
    }

    public synchronized void disconnect() {
        MqttClient current = this.client;
        this.client = null;
        if (current != null) {
            try {
                if (current.isConnected()) {
                    current.disconnect();
                }
                current.close();
                activityLogger.accept("[MQTT] Disconnected from broker.");
            } catch (MqttException e) {
                activityLogger.accept("[MQTT] Disconnect warning: " + e.getMessage());
            }
        }
        connectionStateListener.accept(false, "Disconnected");
    }

    @Override
    public void publishStatus(String jsonPayload) {
        publish(getStatusTopic(), jsonPayload);
    }

    @Override
    public void publishEvent(String jsonPayload) {
        publish(getEventTopic(), jsonPayload);
    }

    private void publish(String topic, String jsonPayload) {
        MqttClient current = this.client;
        if (current == null || !current.isConnected()) {
            throw new IllegalStateException("MQTT client is not connected");
        }
        try {
            MqttMessage msg = new MqttMessage(jsonPayload.getBytes(StandardCharsets.UTF_8));
            msg.setQos(config.getQos());
            msg.setRetained(false);
            current.publish(topic, msg);
            activityLogger.accept("[MQTT OUT] " + topic + " -> " + jsonPayload);
        } catch (MqttException e) {
            throw new IllegalStateException("Failed to publish to " + topic + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isConnected() {
        MqttClient current = this.client;
        return current != null && current.isConnected();
    }

    public String getStatusTopic() {
        return config.getTopicPrefix() + "/" + config.getMachineId() + "/status";
    }

    public String getEventTopic() {
        return config.getTopicPrefix() + "/" + config.getMachineId() + "/event";
    }

    public String getCommandTopic() {
        return config.getTopicPrefix() + "/" + config.getMachineId() + "/command";
    }

    private static String requireNonBlank(String val, String field) {
        if (val == null || val.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return val.trim();
    }
}
