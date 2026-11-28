package com.scentstation.simulator.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

public class SlotState {
    private final String slotId;
    private final String machineId;
    private String fragranceName;
    private BigDecimal remainingMl;
    private BigDecimal capacityMl;
    private String status;
    private Instant updatedAt;

    public SlotState(String slotId, String machineId, String fragranceName,
                     BigDecimal remainingMl, BigDecimal capacityMl, String status, Instant updatedAt) {
        this.slotId = slotId;
        this.machineId = machineId;
        this.fragranceName = fragranceName;
        this.remainingMl = remainingMl;
        this.capacityMl = capacityMl;
        this.status = status != null ? status : computeStatus(remainingMl, capacityMl);
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public static String computeStatus(BigDecimal remaining, BigDecimal capacity) {
        if (remaining == null || remaining.compareTo(BigDecimal.ZERO) <= 0) {
            return "EMPTY";
        }
        if (capacity != null && capacity.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal ratio = remaining.divide(capacity, 4, RoundingMode.HALF_UP);
            if (ratio.compareTo(new BigDecimal("0.15")) <= 0) {
                return "LOW";
            }
        }
        return "READY";
    }

    public double getFillRatio() {
        if (capacityMl == null || capacityMl.compareTo(BigDecimal.ZERO) <= 0 || remainingMl == null) {
            return 0.0;
        }
        double ratio = remainingMl.divide(capacityMl, 4, RoundingMode.HALF_UP).doubleValue();
        return Math.max(0.0, Math.min(1.0, ratio));
    }

    public String getSlotId() { return slotId; }
    public String getMachineId() { return machineId; }
    public String getFragranceName() { return fragranceName; }
    public void setFragranceName(String fragranceName) { this.fragranceName = fragranceName; }
    public BigDecimal getRemainingMl() { return remainingMl; }
    public void setRemainingMl(BigDecimal remainingMl) {
        this.remainingMl = remainingMl;
        this.status = computeStatus(remainingMl, this.capacityMl);
        this.updatedAt = Instant.now();
    }
    public BigDecimal getCapacityMl() { return capacityMl; }
    public void setCapacityMl(BigDecimal capacityMl) { this.capacityMl = capacityMl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
