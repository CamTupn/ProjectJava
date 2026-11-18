package com.scentstation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UpdateMachineSlotStatusRequest {

    @NotBlank
    @Pattern(
            regexp = "AVAILABLE|LOW_STOCK|EMPTY|ERROR",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Status must be AVAILABLE, LOW_STOCK, EMPTY or ERROR"
    )
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
