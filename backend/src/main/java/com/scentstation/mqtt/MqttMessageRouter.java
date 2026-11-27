package com.scentstation.mqtt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scentstation.contract.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class MqttMessageRouter {
    private static final Logger log = LoggerFactory.getLogger(MqttMessageRouter.class);
    private final ObjectMapper mapper;
    private final ApplicationEventPublisher events;
    private final MqttProperties properties;

    public MqttMessageRouter(ObjectMapper mapper, ApplicationEventPublisher events, MqttProperties properties) {
        this.mapper = mapper; this.events = events; this.properties = properties;
    }

    public void route(String topic, byte[] payload) {
        try {
            if (topic == null || payload == null || payload.length == 0) throw new IllegalArgumentException("Empty topic/payload");
            String[] parts = topic.split("/");
            if (parts.length != 3 || !properties.getTopicPrefix().equals(parts[0]) || parts[1].isBlank())
                throw new IllegalArgumentException("Unexpected MQTT topic: " + topic);
            String topicMachineId = parts[1];
            JsonNode json = mapper.readTree(payload);
            String machineId = requiredText(json, "machineId");
            String identity = requiredText(json, "deviceIdentity");
            if (!topicMachineId.equals(machineId)) throw new IllegalArgumentException("machineId does not match topic");
            String expectedIdentity = properties.getDevices().get(machineId);
            if (expectedIdentity == null || !expectedIdentity.equals(identity))
                throw new IllegalArgumentException("deviceIdentity is not allowlisted for machine");

            switch (parts[2]) {
                case "status" -> routeStatus(json);
                case "event" -> routeEvent(json);
                default -> throw new IllegalArgumentException("Unsupported inbound topic: " + parts[2]);
            }
        } catch (Exception ex) {
            // MQTT callback must never be terminated by malformed/untrusted input.
            log.warn("Discarding invalid MQTT message on topic {}: {}", topic, ex.getMessage());
        }
    }

    private void routeStatus(JsonNode json) throws Exception {
        switch (requiredText(json, "type")) {
            case "HEARTBEAT" -> events.publishEvent(mapper.treeToValue(json, Heartbeat.class));
            case "TELEMETRY" -> events.publishEvent(mapper.treeToValue(json, Telemetry.class));
            default -> throw new IllegalArgumentException("Unknown status message type");
        }
    }
    private void routeEvent(JsonNode json) throws Exception {
        switch (requiredText(json, "type")) {
            case "ACK" -> events.publishEvent(mapper.treeToValue(json, DeviceAck.class));
            case "RESULT" -> events.publishEvent(mapper.treeToValue(json, DeviceResult.class));
            case "FAULT" -> events.publishEvent(mapper.treeToValue(json, DeviceFault.class));
            default -> throw new IllegalArgumentException("Unknown event message type");
        }
    }
    private String requiredText(JsonNode json, String field) {
        JsonNode value = json.get(field);
        if (value == null || !value.isTextual() || value.asText().isBlank())
            throw new IllegalArgumentException("Missing/invalid field: " + field);
        return value.asText();
    }
}
