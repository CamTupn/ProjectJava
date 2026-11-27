package com.scentstation.dto.response;

import com.scentstation.entity.MachineSlotStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * {@code slotCurrentVolumeMl} and {@code slotStatus} describe the slot right
 * after the refill and are only filled in the response of the create call.
 */
public record RefillResponse(
        Long id,
        Long slotId,
        Long machineId,
        Long productId,
        String productName,
        Long performedByUserId,
        BigDecimal volumeAddedMl,
        String notes,
        LocalDateTime refilledAt,
        LocalDateTime createdAt,
        BigDecimal slotCurrentVolumeMl,
        MachineSlotStatus slotStatus
) {}
