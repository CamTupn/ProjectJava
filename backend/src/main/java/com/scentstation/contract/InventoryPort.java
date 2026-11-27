package com.scentstation.contract;

import java.util.Optional;

/**
 * Business interface other modules (dispensing, IoT, alerts) use to interact
 * with inventory without depending on {@code InventoryService} internals.
 */
public interface InventoryPort {

    /**
     * Deducts stock for a dispense execution that finished with SUCCESS.
     *
     * <p>The operation is idempotent: an execution is deducted at most once.
     * Replays (for example a retried MQTT message) return
     * {@link Optional#empty()} and change nothing.
     *
     * @param executionId id of the {@code dispense_executions} row
     * @return the published event, or empty if this execution was already deducted
     * @throws com.scentstation.exception.ResourceNotFoundException if the execution does not exist
     * @throws com.scentstation.exception.ResourceConflictException if the execution is not SUCCESS,
     *         is inconsistent with its command/slot, or would make stock negative
     */
    Optional<InventoryChanged> recordSuccessfulDispense(Long executionId);

    /**
     * @return true if the slot has dispense commands that are not yet final
     *         (PENDING/SENT/ACKNOWLEDGED/UNCERTAIN) or successful commands whose
     *         stock deduction has not been recorded yet
     */
    boolean hasUnreconciledCommands(Long slotId);
}
