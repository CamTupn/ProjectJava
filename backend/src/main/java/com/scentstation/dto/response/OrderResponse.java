package com.scentstation.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(
        Long id,
        Long tenantId,
        Long machineId,
        Long slotId,
        Long productId,
        String productName,
        BigDecimal unitPrice,
        String currency,
        String status,
        LocalDateTime expiresAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
