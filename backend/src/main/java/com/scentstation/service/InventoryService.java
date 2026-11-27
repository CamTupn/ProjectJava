package com.scentstation.service;

import com.scentstation.common.security.CurrentUserPrincipal;
import com.scentstation.common.security.CurrentUserService;
import com.scentstation.contract.InventoryChanged;
import com.scentstation.contract.InventoryChanged.ChangeType;
import com.scentstation.contract.InventoryPort;
import com.scentstation.dto.response.InventoryResponse;
import com.scentstation.dto.response.InventoryTransactionResponse;
import com.scentstation.entity.InventoryTransaction;
import com.scentstation.entity.MachineSlot;
import com.scentstation.entity.MachineSlotStatus;
import com.scentstation.exception.ResourceConflictException;
import com.scentstation.exception.ResourceNotFoundException;
import com.scentstation.mapper.InventoryMapper;
import com.scentstation.repository.InventoryLockRepository;
import com.scentstation.repository.InventoryLockRepository.DispenseExecutionView;
import com.scentstation.repository.InventoryTransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InventoryService implements InventoryPort {

    static final Set<String> READ_ROLES = Set.of(
            "PLATFORM_SUPER_ADMIN",
            "BRAND_ADMIN",
            "OPERATIONS_MANAGER",
            "TECHNICIAN",
            "INVENTORY_STAFF"
    );

    static final int DEFAULT_TRANSACTION_LIMIT = 50;
    static final int MAX_TRANSACTION_LIMIT = 200;

    private final InventoryLockRepository inventoryLockRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final InventoryMapper inventoryMapper;
    private final CurrentUserService currentUserService;
    private final ApplicationEventPublisher eventPublisher;

    public InventoryService(
            InventoryLockRepository inventoryLockRepository,
            InventoryTransactionRepository transactionRepository,
            InventoryMapper inventoryMapper,
            CurrentUserService currentUserService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.inventoryLockRepository = inventoryLockRepository;
        this.transactionRepository = transactionRepository;
        this.inventoryMapper = inventoryMapper;
        this.currentUserService = currentUserService;
        this.eventPublisher = eventPublisher;
    }

    // ------------------------------------------------------------------
    // Read API
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<InventoryResponse> getInventory(Long machineId) {
        CurrentUserPrincipal current = requireRole(READ_ROLES);
        Long tenantFilter;
        if (current.isPlatformSuperAdmin()) {
            tenantFilter = null;
        } else if (current.tenantId() == null) {
            throw new ResourceNotFoundException("Inventory not found");
        } else {
            tenantFilter = current.tenantId();
        }
        return inventoryLockRepository.findInventory(tenantFilter, machineId)
                .stream()
                .map(inventoryMapper::toInventoryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InventoryResponse getSlotInventory(Long slotId) {
        CurrentUserPrincipal current = requireRole(READ_ROLES);
        return inventoryMapper.toInventoryResponse(findAccessibleSlot(slotId, current));
    }

    @Transactional(readOnly = true)
    public List<InventoryTransactionResponse> getTransactions(Long slotId, Integer limit) {
        CurrentUserPrincipal current = requireRole(READ_ROLES);
        int size = limit == null ? DEFAULT_TRANSACTION_LIMIT : limit;
        if (size < 1 || size > MAX_TRANSACTION_LIMIT) {
            throw new IllegalArgumentException(
                    "limit must be between 1 and " + MAX_TRANSACTION_LIMIT
            );
        }
        MachineSlot slot = findAccessibleSlot(slotId, current);
        return transactionRepository
                .findBySlot_IdOrderByCreatedAtDescIdDesc(slot.getId(), PageRequest.of(0, size))
                .stream()
                .map(inventoryMapper::toTransactionResponse)
                .toList();
    }

    // ------------------------------------------------------------------
    // InventoryPort
    // ------------------------------------------------------------------

    @Override
    public Optional<InventoryChanged> recordSuccessfulDispense(Long executionId) {
        if (executionId == null) {
            throw new IllegalArgumentException("executionId is required");
        }

        DispenseExecutionView execution = inventoryLockRepository
                .findExecutionView(executionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Dispense execution not found with ID: " + executionId
                ));

        ChangeType type = validateExecution(execution);

        // Lock first, then check for a duplicate with a locking read: two concurrent
        // deliveries of the same execution are serialized on the slot row, so the
        // second one sees the first one's committed transaction and does nothing.
        MachineSlot slot = inventoryLockRepository.lockById(execution.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Machine slot not found with ID: " + execution.getSlotId()
                ));

        if (!Objects.equals(slot.getMachine().getId(), execution.getMachineId())) {
            throw new ResourceConflictException(
                    "Dispense execution " + executionId
                            + " does not belong to the machine of slot " + slot.getId()
            );
        }

        if (!inventoryLockRepository.lockTransactionIdsByExecution(executionId).isEmpty()) {
            return Optional.empty();
        }

        String notes = "Dispense execution " + executionId + " (command "
                + execution.getCommandId() + ") completed successfully";
        return Optional.of(applyChange(
                slot,
                execution.getVolumeMl().negate(),
                type,
                null,
                execution.getOrderId(),
                executionId,
                null,
                notes
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUnreconciledCommands(Long slotId) {
        return inventoryLockRepository.countUnreconciledCommands(slotId) > 0;
    }

    // ------------------------------------------------------------------
    // Shared by RefillService
    // ------------------------------------------------------------------

    /**
     * Applies a volume change to an already locked slot, records the
     * inventory transaction and publishes {@link InventoryChanged}.
     * Callers must hold the slot lock ({@link InventoryLockRepository#lockById}).
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public InventoryChanged applyChange(
            MachineSlot slot,
            BigDecimal volumeChangeMl,
            ChangeType type,
            Long performedByUserId,
            Long orderId,
            Long executionId,
            Long refillId,
            String notes
    ) {
        if (volumeChangeMl == null || volumeChangeMl.signum() == 0) {
            throw new IllegalArgumentException("Volume change must not be zero");
        }

        BigDecimal previousVolume = slot.getCurrentVolumeMl();
        MachineSlotStatus previousStatus = slot.getStatus();
        BigDecimal newVolume = previousVolume.add(volumeChangeMl);

        if (newVolume.signum() < 0) {
            throw new ResourceConflictException(
                    "Inventory of slot " + slot.getId() + " cannot become negative"
                            + " (current " + previousVolume.toPlainString() + " ml, change "
                            + volumeChangeMl.toPlainString() + " ml)"
            );
        }
        if (newVolume.compareTo(slot.getCapacityMl()) > 0) {
            throw new ResourceConflictException(
                    "Inventory of slot " + slot.getId() + " cannot exceed its capacity"
            );
        }

        slot.setCurrentVolumeMl(newVolume);
        slot.setStatus(statusAfterChange(
                previousStatus, newVolume, slot.getLowStockThresholdMl()
        ));

        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setSlot(slot);
        transaction.setPerformedByUserId(performedByUserId);
        transaction.setOrderId(orderId);
        transaction.setExecutionId(executionId);
        transaction.setRefillId(refillId);
        transaction.setTransactionType(type.name());
        transaction.setVolumeChangeMl(volumeChangeMl);
        transaction.setNotes(notes);
        InventoryTransaction saved = transactionRepository.saveAndFlush(transaction);

        InventoryChanged event = new InventoryChanged(
                slot.getId(),
                slot.getMachine().getId(),
                slot.getMachine().getTenantId(),
                slot.getProduct() != null ? slot.getProduct().getId() : null,
                type,
                volumeChangeMl,
                previousVolume,
                newVolume,
                previousStatus,
                slot.getStatus(),
                saved.getId(),
                refillId,
                executionId,
                orderId,
                LocalDateTime.now()
        );
        eventPublisher.publishEvent(event);
        return event;
    }

    /** ERROR is an operational override and DISABLED is terminal; neither is overwritten. */
    static MachineSlotStatus statusAfterChange(
            MachineSlotStatus current,
            BigDecimal volume,
            BigDecimal lowStockThreshold
    ) {
        if (current == MachineSlotStatus.ERROR || current == MachineSlotStatus.DISABLED) {
            return current;
        }
        if (volume.signum() == 0) {
            return MachineSlotStatus.EMPTY;
        }
        if (volume.compareTo(lowStockThreshold) <= 0) {
            return MachineSlotStatus.LOW_STOCK;
        }
        return MachineSlotStatus.AVAILABLE;
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private ChangeType validateExecution(DispenseExecutionView execution) {
        if (!"SUCCESS".equals(execution.getExecutionStatus())) {
            throw new ResourceConflictException(
                    "Dispense execution " + execution.getExecutionId()
                            + " is " + execution.getExecutionStatus()
                            + "; only SUCCESS executions change inventory"
            );
        }
        if (execution.getSlotId() == null
                || execution.getVolumeMl() == null
                || execution.getVolumeMl().signum() <= 0) {
            throw new ResourceConflictException(
                    "Dispense execution " + execution.getExecutionId()
                            + " has no valid slot or volume"
            );
        }
        if ("CUSTOMER_ORDER".equals(execution.getCommandType())) {
            return ChangeType.DISPENSE_SALE;
        }
        if ("DIAGNOSTIC".equals(execution.getCommandType())) {
            return ChangeType.DIAGNOSTIC_SPRAY;
        }
        throw new ResourceConflictException(
                "Dispense execution " + execution.getExecutionId()
                        + " has unsupported command type " + execution.getCommandType()
        );
    }

    private MachineSlot findAccessibleSlot(Long slotId, CurrentUserPrincipal current) {
        MachineSlot slot = inventoryLockRepository.findDetailedById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Machine slot not found with ID: " + slotId
                ));
        ensureTenantAccess(slot, current);
        return slot;
    }

    static void ensureTenantAccess(MachineSlot slot, CurrentUserPrincipal current) {
        if (current.isPlatformSuperAdmin()) {
            return;
        }
        Long machineTenantId = slot.getMachine().getTenantId();
        if (current.tenantId() == null
                || !current.tenantId().equals(machineTenantId)) {
            throw new ResourceNotFoundException(
                    "Machine slot not found with ID: " + slot.getId()
            );
        }
    }

    private CurrentUserPrincipal requireRole(Set<String> allowedRoles) {
        CurrentUserPrincipal current = currentUserService.getCurrentUser();
        if (allowedRoles.stream().noneMatch(current::hasRole)) {
            throw new AccessDeniedException(
                    "You do not have permission to access inventory"
            );
        }
        return current;
    }
}
