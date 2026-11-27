package com.scentstation.dto.request;

import jakarta.validation.constraints.NotNull;

public class CreateOrderRequest {

    @NotNull(message = "machineId is required")
    private Long machineId;

    @NotNull(message = "slotId is required")
    private Long slotId;

    @NotNull(message = "productId is required")
    private Long productId;

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(Long machineId, Long slotId, Long productId) {
        this.machineId = machineId;
        this.slotId = slotId;
        this.productId = productId;
    }

    public Long getMachineId() {
        return machineId;
    }

    public void setMachineId(Long machineId) {
        this.machineId = machineId;
    }

    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }
}
