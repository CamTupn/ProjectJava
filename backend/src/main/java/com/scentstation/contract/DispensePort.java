package com.scentstation.contract;

import java.math.BigDecimal;

/**
 * Integration contract for dispatching a customer spray command.
 * Implemented by the Dispense module (TASK-023 / TASK-034).
 */
public interface DispensePort {

    /**
     * Requests creation and dispatch of a single dispense command for a paid order.
     * Implementations must enforce at-most-once command creation per order.
     */
    void dispatchForOrder(OrderSnapshot order, BigDecimal volumeMl);
}
