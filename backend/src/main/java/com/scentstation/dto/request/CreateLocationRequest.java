package com.scentstation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateLocationRequest(
    Long tenantId,
    @NotBlank @Size(max = 150) String name,
    @NotBlank @Size(max = 255) String address,
    String description,
    @Pattern(regexp = "ACTIVE|INACTIVE", message = "status must be ACTIVE or INACTIVE") String status
) {}
