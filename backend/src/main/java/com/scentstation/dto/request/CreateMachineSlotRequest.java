package com.scentstation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class CreateMachineSlotRequest {

    @NotNull
    @Positive
    private Integer slotNumber;

    private Long productId;

    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 6, fraction = 2)
    private BigDecimal capacityMl;

    @NotNull
    @DecimalMin("0.00")
    @Digits(integer = 6, fraction = 2)
    private BigDecimal currentVolumeMl;

    @NotNull
    @DecimalMin("0.00")
    @Digits(integer = 6, fraction = 2)
    private BigDecimal lowStockThresholdMl;

    public Integer getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(Integer slotNumber) {
        this.slotNumber = slotNumber;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public BigDecimal getCapacityMl() {
        return capacityMl;
    }

    public void setCapacityMl(BigDecimal capacityMl) {
        this.capacityMl = capacityMl;
    }

    public BigDecimal getCurrentVolumeMl() {
        return currentVolumeMl;
    }

    public void setCurrentVolumeMl(BigDecimal currentVolumeMl) {
        this.currentVolumeMl = currentVolumeMl;
    }

    public BigDecimal getLowStockThresholdMl() {
        return lowStockThresholdMl;
    }

    public void setLowStockThresholdMl(BigDecimal lowStockThresholdMl) {
        this.lowStockThresholdMl = lowStockThresholdMl;
    }
}
