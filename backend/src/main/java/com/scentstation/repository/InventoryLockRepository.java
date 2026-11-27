package com.scentstation.repository;

import com.scentstation.entity.MachineSlot;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * Slot-level queries used by the inventory module: row locking, tenant-scoped
 * listing, and checks against the dispense tables.
 *
 * <p>The dispense tables are read with native queries on purpose: their
 * entities belong to the dispensing module and are not owned by this task.
 */
public interface InventoryLockRepository extends Repository<MachineSlot, Long> {

    /**
     * Locks the slot row (SELECT ... FOR UPDATE). Must run inside a transaction.
     * Deliberately no fetch joins, so only the slot row is locked; machine and
     * product are loaded lazily afterwards.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from MachineSlot s where s.id = :slotId")
    Optional<MachineSlot> lockById(@Param("slotId") Long slotId);

    @Query("""
            select s from MachineSlot s
            join fetch s.machine m
            left join fetch s.product
            where s.id = :slotId
            """)
    Optional<MachineSlot> findDetailedById(@Param("slotId") Long slotId);

    /** tenantId == null means "all tenants" and is only passed for platform admins. */
    @Query("""
            select s from MachineSlot s
            join fetch s.machine m
            left join fetch s.product
            where (:tenantId is null or m.tenantId = :tenantId)
              and (:machineId is null or m.id = :machineId)
            order by m.id, s.slotNumber
            """)
    List<MachineSlot> findInventory(
            @Param("tenantId") Long tenantId,
            @Param("machineId") Long machineId
    );

    /**
     * Number of commands on the slot that still need reconciliation: not final yet,
     * or SUCCESS without a recorded stock deduction.
     */
    @Query(value = """
            SELECT COUNT(*)
            FROM dispense_commands c
            LEFT JOIN dispense_executions e ON e.command_id = c.id
            WHERE c.slot_id = :slotId
              AND (
                    c.status IN ('PENDING', 'SENT', 'ACKNOWLEDGED', 'UNCERTAIN')
                 OR e.execution_status = 'UNCERTAIN'
                 OR (
                        c.status = 'SUCCESS'
                    AND NOT EXISTS (
                            SELECT 1 FROM inventory_transactions t
                            WHERE t.execution_id = e.id
                        )
                    )
              )
            """, nativeQuery = true)
    long countUnreconciledCommands(@Param("slotId") Long slotId);

    /**
     * Ids of the stock transactions already recorded for an execution, read with
     * FOR UPDATE on purpose. A plain SELECT would use the transaction's REPEATABLE
     * READ snapshot, taken before this thread waited for the slot lock, and would
     * miss a deduction committed by the thread that held the lock; a locking read
     * always sees the latest committed rows.
     */
    @Query(value = "SELECT id FROM inventory_transactions WHERE execution_id = :executionId FOR UPDATE",
            nativeQuery = true)
    List<Number> lockTransactionIdsByExecution(@Param("executionId") Long executionId);

    @Query(value = """
            SELECT e.id AS executionId,
                   e.execution_status AS executionStatus,
                   c.id AS commandId,
                   c.command_type AS commandType,
                   c.slot_id AS slotId,
                   c.machine_id AS machineId,
                   c.order_id AS orderId,
                   c.volume_ml AS volumeMl
            FROM dispense_executions e
            JOIN dispense_commands c ON c.id = e.command_id
            WHERE e.id = :executionId
            """, nativeQuery = true)
    Optional<DispenseExecutionView> findExecutionView(@Param("executionId") Long executionId);

    interface DispenseExecutionView {
        Long getExecutionId();
        String getExecutionStatus();
        Long getCommandId();
        String getCommandType();
        Long getSlotId();
        Long getMachineId();
        Long getOrderId();
        BigDecimal getVolumeMl();
    }
}
