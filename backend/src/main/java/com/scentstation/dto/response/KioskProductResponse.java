package com.scentstation.dto.response;

import java.math.BigDecimal;

public record KioskProductResponse(
        Long slotId,
        Integer slotNumber,
        Long productId,
        String name,
        String description,
        String fragranceNotes,
        String imageUrl,
        BigDecimal price,
        String currency,
        BigDecimal volumePerSprayMl,
        boolean available
) {
}
