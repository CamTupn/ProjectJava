package com.scentstation.dto.response;

import java.time.LocalDateTime;

public record LocationResponse(Long id, Long tenantId, String name, String address,
                               String description, String status,
                               LocalDateTime createdAt, LocalDateTime updatedAt) {}
