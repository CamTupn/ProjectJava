package com.scentstation.contract;

import com.scentstation.entity.MachineSlotStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event published after the inventory of a machine slot has changed.
 *
 * <p>The event is published inside the transaction that changed the stock.
 * Consumers (for example the alert module) must listen with
 * {@code @TransactionalEventListener(phase = AFTER_COMMIT)} so that they never
 * react to a change that was rolled back. Producers never create alerts
 * directly; they only publish this event.
 */
public record InventoryChanged(
        Long slotId,
        Long machineId,
        Long tenantId,
        Long productId,
        ChangeType changeType,
        BigDecimal volumeChangeMl,
        BigDecimal previousVolumeMl,
        BigDecimal currentVolumeMl,
        MachineSlotStatus previousStatus,
        MachineSlotStatus currentStatus,
        Long transactionId,
        Long refillId,
        Long executionId,
        Long orderId,
        LocalDateTime occurredAt
) {

    public enum ChangeType {
        REFILL,
        DISPENSE_SALE,
        DIAGNOSTIC_SPRAY
    }

    public boolean statusChanged() {
        return previousStatus != currentStatus;
    }

    public boolean becameLowStock() {
        return statusChanged() && currentStatus == MachineSlotStatus.LOW_STOCK;
    }

    public boolean becameEmpty() {
        return statusChanged() && currentStatus == MachineSlotStatus.EMPTY;
    }
}
