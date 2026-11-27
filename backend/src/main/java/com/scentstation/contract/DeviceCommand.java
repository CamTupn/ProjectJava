package com.scentstation.contract;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

/** Command envelope shared by Backend and the device simulator. */
public record DeviceCommand(
        String commandId, String machineId, String commandType,
        String slotId, String orderId, Instant expiresAt, JsonNode parameters) {
}
