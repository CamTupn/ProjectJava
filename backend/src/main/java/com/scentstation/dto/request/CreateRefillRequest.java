package com.scentstation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Deliberately has no tenantId or productId: the tenant is derived from the
 * slot's machine and the product is the one assigned to the slot.
 */
public class CreateRefillRequest {

    @NotNull
    private Long slotId;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = false)
    @Digits(integer = 6, fraction = 2)
    private BigDecimal volumeMl;

    @Size(max = 1000)
    private String notes;

    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }

    public BigDecimal getVolumeMl() {
        return volumeMl;
    }

    public void setVolumeMl(BigDecimal volumeMl) {
        this.volumeMl = volumeMl;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
