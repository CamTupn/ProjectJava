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

@Entity
@Table(name = "payments", uniqueConstraints = {
        @UniqueConstraint(name = "uq_payments_payment_ref", columnNames = "payment_ref"),
        @UniqueConstraint(name = "uq_payments_gateway_transaction", columnNames = "gateway_transaction_id")
})
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "payment_ref", nullable = false, length = 150)
    private String paymentRef;
    @Column(nullable = false, length = 100)
    private String provider;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false, length = 3)
    private String currency = "VND";
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;
    @Column(name = "gateway_transaction_id", length = 150)
    private String gatewayTransactionId;
    @Column(name = "payload", columnDefinition = "json")
    private String payload;
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public Order getOrder() { return order; }
    public String getPaymentRef() { return paymentRef; }
    public String getProvider() { return provider; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public PaymentStatus getStatus() { return status; }
    public String getGatewayTransactionId() { return gatewayTransactionId; }
    public String getPayload() { return payload; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setId(Long value) { id = value; }
    public void setOrder(Order value) { order = value; }
    public void setPaymentRef(String value) { paymentRef = value; }
    public void setProvider(String value) { provider = value; }
    public void setAmount(BigDecimal value) { amount = value; }
    public void setCurrency(String value) { currency = value; }
    public void setStatus(PaymentStatus value) { status = value; }
    public void setGatewayTransactionId(String value) { gatewayTransactionId = value; }
    public void setPayload(String value) { payload = value; }
    public void setPaidAt(LocalDateTime value) { paidAt = value; }
}
