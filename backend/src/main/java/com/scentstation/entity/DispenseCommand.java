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
@Table(name = "dispense_commands", uniqueConstraints = {
        @UniqueConstraint(name = "uq_dispense_commands_token", columnNames = "command_token"),
        @UniqueConstraint(name = "uq_dispense_commands_order", columnNames = "order_id")
})
public class DispenseCommand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "command_token", nullable = false, unique = true, length = 150)
    private String commandToken;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "machine_id", nullable = false)
    private Machine machine;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "slot_id", nullable = false)
    private MachineSlot slot;
    @Enumerated(EnumType.STRING)
    @Column(name = "command_type", nullable = false, length = 30)
    private DispenseCommandType commandType;
    @Column(name = "volume_ml", nullable = false, precision = 8, scale = 2)
    private BigDecimal volumeMl;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DispenseCommandStatus status = DispenseCommandStatus.PENDING;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public Long getId() { return id; }
    public String getCommandToken() { return commandToken; }
    public Order getOrder() { return order; }
    public Machine getMachine() { return machine; }
    public MachineSlot getSlot() { return slot; }
    public DispenseCommandType getCommandType() { return commandType; }
    public BigDecimal getVolumeMl() { return volumeMl; }
    public DispenseCommandStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getSentAt() { return sentAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }

    public void setCommandToken(String value) { commandToken = value; }
    public void setId(Long value) { id = value; }
    public void setOrder(Order value) { order = value; }
    public void setMachine(Machine value) { machine = value; }
    public void setSlot(MachineSlot value) { slot = value; }
    public void setCommandType(DispenseCommandType value) { commandType = value; }
    public void setVolumeMl(BigDecimal value) { volumeMl = value; }
    public void setStatus(DispenseCommandStatus value) { status = value; }
    public void setCreatedAt(LocalDateTime value) { createdAt = value; }
    public void setSentAt(LocalDateTime value) { sentAt = value; }
    public void setCompletedAt(LocalDateTime value) { completedAt = value; }
}
