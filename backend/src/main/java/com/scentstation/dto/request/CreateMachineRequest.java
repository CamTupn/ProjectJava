package com.scentstation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateMachineRequest(
        Long tenantId,

        @NotBlank(message = "Machine code is required")
        @Size(max = 100, message = "Machine code must not exceed 100 characters")
        String machineCode,

        @NotBlank(message = "Serial number is required")
        @Size(max = 100, message = "Serial number must not exceed 100 characters")
        String serialNumber,

        @NotBlank(message = "Device identity is required")
        @Size(max = 150, message = "Device identity must not exceed 150 characters")
        String deviceIdentity,

        Long locationId,

        @Size(max = 100, message = "Firmware version must not exceed 100 characters")
        String firmwareVersion,

        @Pattern(
                regexp = "ONLINE|OFFLINE",
                message = "Connection status must be ONLINE or OFFLINE"
        )
        String connectionStatus,

        @Pattern(
                regexp = "ACTIVE|MAINTENANCE|DISABLED|ERROR",
                message = "Operational status must be ACTIVE, MAINTENANCE, DISABLED, or ERROR"
        )
        String operationalStatus
) {}
