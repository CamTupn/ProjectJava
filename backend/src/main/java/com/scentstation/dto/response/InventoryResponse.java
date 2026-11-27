package com.scentstation.dto.response;

import com.scentstation.entity.MachineSlotStatus;
import java.math.BigDecimal;

public record InventoryResponse(
        Long slotId,
        Long machineId,
        String machineCode,
        Integer slotNumber,
        Long productId,
        String productName,
        BigDecimal capacityMl,
        BigDecimal currentVolumeMl,
        BigDecimal remainingCapacityMl,
        BigDecimal lowStockThresholdMl,
        MachineSlotStatus status
) {}
