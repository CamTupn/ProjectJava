package com.scentstation.contract;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSnapshot(
        Long id,
        Long tenantId,
        Long machineId,
        Long slotId,
        Long productId,
        String productNameSnapshot,
        BigDecimal unitPrice,
        String currency,
        String status,
        LocalDateTime expiresAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
