package com.scentstation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateLocationRequest(
    @NotBlank @Size(max = 150) String name,
    @NotBlank @Size(max = 255) String address,
    String description
) {}
