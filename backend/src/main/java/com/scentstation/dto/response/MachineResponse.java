package com.scentstation.dto.response;

import java.time.LocalDateTime;

public record MachineResponse(
        Long id,
        Long tenantId,
        Long locationId,
        LocationInfo location,
        String machineCode,
        String serialNumber,
        String deviceIdentity,
        String connectionStatus,
        String operationalStatus,
        String firmwareVersion,
        LocalDateTime lastPingAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record LocationInfo(
            Long id,
            String name,
            String address,
            String status
    ) {}
}
