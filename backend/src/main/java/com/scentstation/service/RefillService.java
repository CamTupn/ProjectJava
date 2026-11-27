package com.scentstation.service;

import com.scentstation.common.security.CurrentUserPrincipal;
import com.scentstation.common.security.CurrentUserService;
import com.scentstation.contract.InventoryChanged.ChangeType;
import com.scentstation.dto.request.CreateRefillRequest;
import com.scentstation.dto.response.RefillResponse;
import com.scentstation.entity.MachineSlot;
import com.scentstation.entity.MachineSlotStatus;
import com.scentstation.entity.Product;
import com.scentstation.entity.Refill;
import com.scentstation.exception.ResourceConflictException;
import com.scentstation.exception.ResourceNotFoundException;
import com.scentstation.mapper.InventoryMapper;
import com.scentstation.repository.InventoryLockRepository;
import com.scentstation.repository.RefillRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RefillService {

    static final Set<String> REFILL_ROLES = Set.of(
            "PLATFORM_SUPER_ADMIN",
            "BRAND_ADMIN",
            "INVENTORY_STAFF"
    );

    private final InventoryLockRepository inventoryLockRepository;
    private final RefillRepository refillRepository;
    private final InventoryService inventoryService;
    private final InventoryMapper inventoryMapper;
    private final CurrentUserService currentUserService;

    public RefillService(
            InventoryLockRepository inventoryLockRepository,
            RefillRepository refillRepository,
            InventoryService inventoryService,
            InventoryMapper inventoryMapper,
            CurrentUserService currentUserService
    ) {
        this.inventoryLockRepository = inventoryLockRepository;
        this.refillRepository = refillRepository;
        this.inventoryService = inventoryService;
        this.inventoryMapper = inventoryMapper;
        this.currentUserService = currentUserService;
    }

    public RefillResponse createRefill(CreateRefillRequest request) {
        CurrentUserPrincipal current = requireRole(REFILL_ROLES);
        validateRequest(request);

        MachineSlot slot = inventoryLockRepository.lockById(request.getSlotId())
                .orElseThrow(() -> slotNotFound(request.getSlotId()));
        // Tenant comes from the slot's machine, never from the client.
        InventoryService.ensureTenantAccess(slot, current);

        if (slot.getStatus() == MachineSlotStatus.DISABLED) {
            throw new ResourceConflictException("Disabled machine slots cannot be refilled");
        }
        if ("DISABLED".equals(slot.getMachine().getOperationalStatus())) {
            throw new ResourceConflictException("Cannot refill a slot of a disabled machine");
        }

        Product product = slot.getProduct();
        if (product == null) {
            throw new ResourceConflictException(
                    "A product must be assigned to the slot before it can be refilled"
            );
        }
        if (!Objects.equals(product.getTenantId(), slot.getMachine().getTenantId())) {
            throw slotNotFound(slot.getId());
        }

        if (inventoryService.hasUnreconciledCommands(slot.getId())) {
            throw new ResourceConflictException(
                    "Slot has dispense commands that are not reconciled yet; "
                            + "wait for them to finish before refilling"
            );
        }

        BigDecimal volume = request.getVolumeMl();
        if (slot.getCurrentVolumeMl().add(volume).compareTo(slot.getCapacityMl()) > 0) {
            throw new ResourceConflictException(
                    "Refill exceeds slot capacity: at most "
                            + slot.getCapacityMl().subtract(slot.getCurrentVolumeMl()).toPlainString()
                            + " ml can be added"
            );
        }

        String notes = normalizeNotes(request.getNotes());

        Refill refill = new Refill();
        refill.setSlot(slot);
        refill.setProduct(product);
        refill.setPerformedByUserId(current.userId());
        refill.setVolumeAddedMl(volume);
        refill.setNotes(notes);
        Refill saved = refillRepository.saveAndFlush(refill);

        inventoryService.applyChange(
                slot,
                volume,
                ChangeType.REFILL,
                current.userId(),
                null,
                null,
                saved.getId(),
                notes
        );

        return inventoryMapper.toCreatedRefillResponse(saved);
    }

    @Transactional(readOnly = true)
    public RefillResponse getRefill(Long id) {
        CurrentUserPrincipal current = requireRole(InventoryService.READ_ROLES);
        Refill refill = refillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Refill not found with ID: " + id
                ));
        try {
            InventoryService.ensureTenantAccess(refill.getSlot(), current);
        } catch (ResourceNotFoundException ex) {
            throw new ResourceNotFoundException("Refill not found with ID: " + id);
        }
        return inventoryMapper.toRefillResponse(refill);
    }

    @Transactional(readOnly = true)
    public List<RefillResponse> getRefillsBySlot(Long slotId, Integer limit) {
        CurrentUserPrincipal current = requireRole(InventoryService.READ_ROLES);
        if (slotId == null) {
            throw new IllegalArgumentException("slotId is required");
        }
        int size = limit == null ? InventoryService.DEFAULT_TRANSACTION_LIMIT : limit;
        if (size < 1 || size > InventoryService.MAX_TRANSACTION_LIMIT) {
            throw new IllegalArgumentException(
                    "limit must be between 1 and " + InventoryService.MAX_TRANSACTION_LIMIT
            );
        }
        MachineSlot slot = inventoryLockRepository.findDetailedById(slotId)
                .orElseThrow(() -> slotNotFound(slotId));
        InventoryService.ensureTenantAccess(slot, current);

        return refillRepository
                .findBySlot_IdOrderByRefilledAtDescIdDesc(slotId, PageRequest.of(0, size))
                .stream()
                .map(inventoryMapper::toRefillResponse)
                .toList();
    }

    private void validateRequest(CreateRefillRequest request) {
        if (request == null || request.getSlotId() == null) {
            throw new IllegalArgumentException("slotId is required");
        }
        BigDecimal volume = request.getVolumeMl();
        if (volume == null) {
            throw new IllegalArgumentException("volumeMl is required");
        }
        if (volume.signum() <= 0) {
            throw new IllegalArgumentException("Refill volume must be greater than zero");
        }
        if (volume.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("Refill volume supports at most 2 decimal places");
        }
    }

    private String normalizeNotes(String notes) {
        if (notes == null || notes.isBlank()) {
            return null;
        }
        return notes.trim();
    }

    private ResourceNotFoundException slotNotFound(Long slotId) {
        return new ResourceNotFoundException("Machine slot not found with ID: " + slotId);
    }

    private CurrentUserPrincipal requireRole(Set<String> allowedRoles) {
        CurrentUserPrincipal current = currentUserService.getCurrentUser();
        if (allowedRoles.stream().noneMatch(current::hasRole)) {
            throw new AccessDeniedException(
                    "You do not have permission to manage refills"
            );
        }
        return current;
    }
}
