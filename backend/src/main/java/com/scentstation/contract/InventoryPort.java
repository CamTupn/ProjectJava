package com.scentstation.contract;

import java.math.BigDecimal;

/**
 * Integration contract for deducting slot inventory after confirmed dispense.
 * Implemented by the Inventory module (TASK-026).
 */
public interface InventoryPort {

    /**
     * Deducts inventory for a confirmed successful customer dispense.
     */
    void deductForDispense(Long orderId, Long slotId, BigDecimal quantityMl);
}
