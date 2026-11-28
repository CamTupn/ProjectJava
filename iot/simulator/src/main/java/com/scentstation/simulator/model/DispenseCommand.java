package com.scentstation.simulator.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record DispenseCommand(
        String commandId,
        String machineId,
        String deviceIdentity,
        String commandType,
        String slotId,
        String orderId,
        BigDecimal volumeMl,
        Integer durationMs,
        Instant expiresAt
) {
    public String businessFingerprint() {
        String normalizedVol = volumeMl == null
                ? "0"
                : volumeMl.stripTrailingZeros().toPlainString();
        return String.join("|",
                Objects.toString(commandId, ""),
                Objects.toString(machineId, ""),
                Objects.toString(deviceIdentity, ""),
                Objects.toString(slotId, ""),
                normalizedVol
        );
    }
}
