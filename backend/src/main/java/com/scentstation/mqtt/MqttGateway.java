package com.scentstation.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scentstation.contract.DeviceCommand;
import com.scentstation.contract.MqttPort;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "scentstation.mqtt", name = "enabled", havingValue = "true")
public class MqttGateway implements MqttPort {
    private static final Logger log = LoggerFactory.getLogger(MqttGateway.class);
    private final MqttClient client;
    private final MqttProperties properties;
    private final ObjectMapper objectMapper;

    public MqttGateway(MqttClient client, MqttProperties properties, ObjectMapper objectMapper) {
        this.client = client; this.properties = properties; this.objectMapper = objectMapper;
    }

    @Override public void publishCommand(DeviceCommand command) {
        if (command == null || blank(command.machineId()) || blank(command.commandId())) {
            throw new IllegalArgumentException("command, machineId and commandId are required");
        }
        if (!command.machineId().matches("[A-Za-z0-9_-]{1,64}")) {
            throw new IllegalArgumentException("machineId contains characters not allowed in an MQTT topic segment");
        }
        String topic = properties.getTopicPrefix() + "/" + command.machineId() + "/command";
        try {
            byte[] payload = objectMapper.writeValueAsBytes(command);
            MqttMessage message = new MqttMessage(payload);
            message.setQos(properties.getQos());
            message.setRetained(properties.isCommandRetained());
            client.publish(topic, message);
            log.debug("Published MQTT command {} to {}", command.commandId(), topic);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot serialize MQTT command", e);
        } catch (MqttException e) {
            throw new IllegalStateException("Cannot publish MQTT command to " + topic, e);
        }
    }
    private boolean blank(String value) { return value == null || value.isBlank(); }
}
