package com.scentstation.dto.response;

import com.scentstation.entity.MachineSlotStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MachineSlotResponse(
        Long id,
        Long machineId,
        Integer slotNumber,
        Long productId,
        String productName,
        BigDecimal capacityMl,
        BigDecimal currentVolumeMl,
        BigDecimal lowStockThresholdMl,
        MachineSlotStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
