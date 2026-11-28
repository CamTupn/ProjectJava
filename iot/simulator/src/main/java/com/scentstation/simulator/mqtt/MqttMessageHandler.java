package com.scentstation.simulator.mqtt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.scentstation.simulator.config.SimulatorConfig;
import com.scentstation.simulator.model.DispenseCommand;
import com.scentstation.simulator.service.DispenseCommandService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class MqttMessageHandler {
    private final SimulatorConfig config;
    private final DispenseCommandService dispenseCommandService;
    private final ExecutorService commandExecutor;
    private final Consumer<String> logger;
    private final ObjectMapper objectMapper;

    public MqttMessageHandler(
            SimulatorConfig config,
            DispenseCommandService dispenseCommandService,
            ExecutorService commandExecutor,
            Consumer<String> logger
    ) {
        this.config = config;
        this.dispenseCommandService = dispenseCommandService;
        this.commandExecutor = commandExecutor;
        this.logger = logger != null ? logger : msg -> {};
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    /**
     * Parses, validates, and dispatches incoming MQTT command JSON onto the single-device command executor.
     */
    public Optional<DispenseCommand> handleIncomingPayload(String topic, String rawJson) {
        DispenseCommand command;
        try {
            command = parseAndValidate(rawJson);
        } catch (IllegalArgumentException e) {
            logger.accept("[REJECTED] Invalid command on " + topic + ": " + e.getMessage());
            return Optional.empty();
        }

        if (commandExecutor != null) {
            commandExecutor.submit(() -> dispenseCommandService.processCommand(command));
        } else {
            dispenseCommandService.processCommand(command);
        }
        return Optional.of(command);
    }

    public DispenseCommand parseAndValidate(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            throw new IllegalArgumentException("Payload is empty");
        }
        JsonNode root;
        try {
            root = objectMapper.readTree(rawJson);
        } catch (Exception e) {
            throw new IllegalArgumentException("Malformed JSON payload: " + e.getMessage());
        }
        if (root == null || !root.isObject()) {
            throw new IllegalArgumentException("JSON payload must be an object");
        }

        String commandId = textOrNull(root, "commandId");
        if (commandId == null || commandId.isBlank()) {
            throw new IllegalArgumentException("Missing required field: commandId");
        }

        String machineId = textOrNull(root, "machineId");
        if (machineId == null || machineId.isBlank()) {
            throw new IllegalArgumentException("Missing required field: machineId");
        }
        if (!machineId.equals(config.getMachineId())) {
            throw new IllegalArgumentException(
                    "machineId mismatch: expected '" + config.getMachineId() + "' but got '" + machineId + "'");
        }

        JsonNode params = root.path("parameters");
        String deviceIdentity = textOrNull(root, "deviceIdentity");
        if ((deviceIdentity == null || deviceIdentity.isBlank()) && params.isObject()) {
            deviceIdentity = textOrNull(params, "deviceIdentity");
        }
        if (deviceIdentity != null && !deviceIdentity.isBlank()
                && !deviceIdentity.equals(config.getDeviceIdentity())) {
            throw new IllegalArgumentException(
                    "deviceIdentity mismatch: expected '" + config.getDeviceIdentity() + "' but got '" + deviceIdentity + "'");
        }
        String resolvedDeviceIdentity = (deviceIdentity == null || deviceIdentity.isBlank())
                ? config.getDeviceIdentity()
                : deviceIdentity;

        String commandType = textOrNull(root, "commandType");
        if (commandType == null || commandType.isBlank()) {
            commandType = "DISPENSE";
        }
        if (!"DISPENSE".equalsIgnoreCase(commandType)) {
            throw new IllegalArgumentException("Unsupported commandType: " + commandType);
        }

        String slotId = textOrNull(root, "slotId");
        if ((slotId == null || slotId.isBlank()) && params.isObject()) {
            slotId = textOrNull(params, "slotId");
        }
        if (slotId == null || slotId.isBlank()) {
            throw new IllegalArgumentException("Missing required field: slotId");
        }

        String orderId = textOrNull(root, "orderId");
        if ((orderId == null || orderId.isBlank()) && params.isObject()) {
            orderId = textOrNull(params, "orderId");
        }

        BigDecimal volumeMl = extractVolumeMl(root, params);
        if (volumeMl == null || volumeMl.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("volumeMl must be strictly positive (> 0)");
        }

        Integer durationMs = extractDurationMs(root, params);
        Instant expiresAt = extractExpiresAt(root);

        return new DispenseCommand(
                commandId.trim(),
                machineId.trim(),
                resolvedDeviceIdentity.trim(),
                commandType.trim().toUpperCase(),
                slotId.trim(),
                orderId == null ? null : orderId.trim(),
                volumeMl,
                durationMs,
                expiresAt
        );
    }

    private BigDecimal extractVolumeMl(JsonNode root, JsonNode params) {
        JsonNode node = root.get("volumeMl");
        if (node == null || node.isNull()) {
            node = root.get("volume_ml");
        }
        if ((node == null || node.isNull()) && params.isObject()) {
            node = params.get("volumeMl");
            if (node == null || node.isNull()) {
                node = params.get("volume_ml");
            }
        }
        if (node == null || node.isNull()) {
            return null;
        }
        try {
            if (node.isNumber()) {
                return node.decimalValue();
            }
            if (node.isTextual() && !node.asText().isBlank()) {
                return new BigDecimal(node.asText().trim());
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric volumeMl: " + node.asText());
        }
        return null;
    }

    private Integer extractDurationMs(JsonNode root, JsonNode params) {
        JsonNode node = root.get("durationMs");
        if ((node == null || node.isNull()) && params.isObject()) {
            node = params.get("durationMs");
        }
        if (node != null && node.isNumber()) {
            return node.asInt();
        }
        return null;
    }

    private Instant extractExpiresAt(JsonNode root) {
        String exp = textOrNull(root, "expiresAt");
        if (exp == null || exp.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(exp.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid expiresAt timestamp: " + exp);
        }
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode child = node.get(field);
        if (child == null || child.isNull()) {
            return null;
        }
        return child.asText();
    }
}
