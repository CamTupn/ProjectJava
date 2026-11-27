package com.scentstation.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InventoryTransactionResponse(
        Long id,
        Long slotId,
        Long orderId,
        Long executionId,
        Long refillId,
        Long performedByUserId,
        String transactionType,
        BigDecimal volumeChangeMl,
        String notes,
        LocalDateTime createdAt
) {}
