package com.scentstation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(

    @NotBlank(message = "Temporary password is required")
    @Size(
        min = 8,
        max = 72,
        message = "Temporary password must be between 8 and 72 characters"
    )
    String temporaryPassword

) {
}
