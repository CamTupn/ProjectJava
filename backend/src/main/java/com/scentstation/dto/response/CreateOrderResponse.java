package com.scentstation.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateOrderResponse(
        Long id,
        Long machineId,
        Long slotId,
        Long productId,
        String productName,
        BigDecimal unitPrice,
        String currency,
        String status,
        LocalDateTime expiresAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String accessToken
) {
}
