package com.scentstation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "refills")
public class Refill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "slot_id", nullable = false)
    private MachineSlot slot;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "performed_by_user_id")
    private Long performedByUserId;

    @Column(name = "volume_added_ml", nullable = false, precision = 8, scale = 2)
    private BigDecimal volumeAddedMl;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "refilled_at", nullable = false, updatable = false)
    private LocalDateTime refilledAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Refill() {}

    public Long getId() { return id; }

    public MachineSlot getSlot() { return slot; }
    public void setSlot(MachineSlot slot) { this.slot = slot; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Long getPerformedByUserId() { return performedByUserId; }
    public void setPerformedByUserId(Long performedByUserId) { this.performedByUserId = performedByUserId; }

    public BigDecimal getVolumeAddedMl() { return volumeAddedMl; }
    public void setVolumeAddedMl(BigDecimal volumeAddedMl) { this.volumeAddedMl = volumeAddedMl; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getRefilledAt() { return refilledAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
