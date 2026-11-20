package com.scentstation.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateMachineRequest(
        @Size(max = 100, message = "Machine code must not exceed 100 characters")
        String machineCode,

        @Size(max = 100, message = "Serial number must not exceed 100 characters")
        String serialNumber,

        @Size(max = 150, message = "Device identity must not exceed 150 characters")
        String deviceIdentity,

        Long locationId,

        @Size(max = 100, message = "Firmware version must not exceed 100 characters")
        String firmwareVersion
) {}
