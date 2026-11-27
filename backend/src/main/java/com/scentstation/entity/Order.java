package com.scentstation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    @Column(name = "machine_id", nullable = false)
    private Long machineId;
    @Column(name = "slot_id", nullable = false)
    private Long slotId;
    @Column(name = "product_id", nullable = false)
    private Long productId;
    @Column(name = "product_name_snapshot", nullable = false)
    private String productNameSnapshot;
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;
    @Column(nullable = false, length = 3)
    private String currency = "VND";
    @Column(nullable = false, length = 30)
    private String status = "PENDING_PAYMENT";
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public Long getMachineId() { return machineId; }
    public Long getSlotId() { return slotId; }
    public Long getProductId() { return productId; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public String getCurrency() { return currency; }
    public String getStatus() { return status; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setId(Long value) { id = value; }
    public void setTenantId(Long value) { tenantId = value; }
    public void setMachineId(Long value) { machineId = value; }
    public void setSlotId(Long value) { slotId = value; }
    public void setProductId(Long value) { productId = value; }
    public void setUnitPrice(BigDecimal value) { unitPrice = value; }
    public void setCurrency(String value) { currency = value; }
    public void setStatus(String value) { status = value; }
    public void setExpiresAt(LocalDateTime value) { expiresAt = value; }
}
