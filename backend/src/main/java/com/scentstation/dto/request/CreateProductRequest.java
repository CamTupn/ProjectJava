package com.scentstation.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateProductRequest(
    Long tenantId,
    @NotBlank @Size(max = 100) String sku,
    @NotBlank @Size(max = 200) String name,
    String description,
    String fragranceNotes,
    @Size(max = 500) String imageUrl,
    @NotNull @DecimalMin(value = "0.00") @Digits(integer = 10, fraction = 2) BigDecimal price,
    @NotNull @DecimalMin(value = "0.00", inclusive = false) @Digits(integer = 6, fraction = 2) BigDecimal volumePerSprayMl,
    @Pattern(regexp = "ACTIVE|INACTIVE", message = "status must be ACTIVE or INACTIVE") String status
) {}
