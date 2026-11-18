package com.scentstation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@DynamicUpdate
@Table(
        name = "machine_slots",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_machine_slots_number",
                columnNames = {"machine_id", "slot_number"}
        )
)
public class MachineSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "machine_id", nullable = false)
    private Machine machine;

    @Column(name = "slot_number", nullable = false)
    private Integer slotNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "capacity_ml", nullable = false, precision = 8, scale = 2)
    private BigDecimal capacityMl;

    @Column(name = "current_volume_ml", nullable = false, precision = 8, scale = 2)
    private BigDecimal currentVolumeMl;

    @Column(name = "low_stock_threshold_ml", nullable = false, precision = 8, scale = 2)
    private BigDecimal lowStockThresholdMl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MachineSlotStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public MachineSlot() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    public Integer getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(Integer slotNumber) {
        this.slotNumber = slotNumber;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
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

    public MachineSlotStatus getStatus() {
        return status;
    }

    public void setStatus(MachineSlotStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
