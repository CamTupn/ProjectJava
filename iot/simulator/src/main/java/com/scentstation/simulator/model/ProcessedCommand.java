package com.scentstation.simulator.model;

import java.math.BigDecimal;
import java.time.Instant;

public record ProcessedCommand(
        String commandId,
        String machineId,
        String deviceIdentity,
        String slotId,
        String orderId,
        BigDecimal volumeMl,
        String payloadFingerprint,
        CommandStatus status,
        String result,
        String errorCode,
        BigDecimal volumeDispensedMl,
        Instant createdAt,
        Instant updatedAt
) {
}
