package com.scentstation.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(Long id, Long tenantId, String sku, String name,
                              String description, String fragranceNotes, String imageUrl,
                              BigDecimal price, BigDecimal volumePerSprayMl, String status,
                              LocalDateTime createdAt, LocalDateTime updatedAt) {}
