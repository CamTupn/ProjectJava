package com.scentstation.service;

import com.scentstation.common.security.CurrentUserPrincipal;
import com.scentstation.common.security.CurrentUserService;
import com.scentstation.dto.request.CreateMachineSlotRequest;
import com.scentstation.dto.request.UpdateMachineSlotRequest;
import com.scentstation.dto.response.MachineSlotListResponse;
import com.scentstation.dto.response.MachineSlotResponse;
import com.scentstation.entity.InventoryTransaction;
import com.scentstation.entity.Machine;
import com.scentstation.entity.MachineSlot;
import com.scentstation.entity.MachineSlotStatus;
import com.scentstation.entity.Product;
import com.scentstation.exception.ResourceConflictException;
import com.scentstation.exception.ResourceNotFoundException;
import com.scentstation.mapper.MachineSlotMapper;
import com.scentstation.repository.InventoryTransactionRepository;
import com.scentstation.repository.MachineRepository;
import com.scentstation.repository.MachineSlotRepository;
import com.scentstation.repository.ProductRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class MachineSlotService {

    private static final Set<String> READ_ROLES = Set.of(
            "PLATFORM_SUPER_ADMIN",
            "BRAND_ADMIN",
            "OPERATIONS_MANAGER",
            "TECHNICIAN",
            "INVENTORY_STAFF"
    );

    private static final Set<String> CONFIGURATION_ROLES = Set.of(
            "PLATFORM_SUPER_ADMIN",
            "BRAND_ADMIN",
            "OPERATIONS_MANAGER",
            "INVENTORY_STAFF"
    );

    private static final Set<String> DELETE_ROLES = Set.of(
            "PLATFORM_SUPER_ADMIN",
            "BRAND_ADMIN",
            "OPERATIONS_MANAGER"
    );

    private static final Set<String> STATUS_ROLES = Set.of(
            "PLATFORM_SUPER_ADMIN",
            "BRAND_ADMIN",
            "OPERATIONS_MANAGER",
            "TECHNICIAN"
    );

    private final MachineRepository machineRepository;
    private final ProductRepository productRepository;
    private final MachineSlotRepository machineSlotRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final MachineSlotMapper machineSlotMapper;
    private final CurrentUserService currentUserService;

    public MachineSlotService(
            MachineRepository machineRepository,
            ProductRepository productRepository,
            MachineSlotRepository machineSlotRepository,
            InventoryTransactionRepository inventoryTransactionRepository,
            MachineSlotMapper machineSlotMapper,
            CurrentUserService currentUserService
    ) {
        this.machineRepository = machineRepository;
        this.productRepository = productRepository;
        this.machineSlotRepository = machineSlotRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.machineSlotMapper = machineSlotMapper;
        this.currentUserService = currentUserService;
    }

    public MachineSlotListResponse getSlotsByMachine(Long machineId) {
        CurrentUserPrincipal current = requireRole(READ_ROLES);
        Machine machine = findAccessibleMachine(machineId, current);
        List<MachineSlot> machineSlots = machineSlotRepository
                .findAllByMachine_IdOrderBySlotNumber(machine.getId());
        machineSlots.forEach(this::ensureProductTenantMatchesMachine);
        List<MachineSlotResponse> slots = machineSlots.stream()
                .map(machineSlotMapper::toResponse)
                .toList();

        return new MachineSlotListResponse(machine.getId(), slots.size(), slots);
    }

    public MachineSlotResponse getSlot(Long id) {
        CurrentUserPrincipal current = requireRole(READ_ROLES);
        MachineSlot slot = findAccessibleSlot(id, current);
        return machineSlotMapper.toResponse(slot);
    }

    public MachineSlotResponse createSlot(
            Long machineId,
            CreateMachineSlotRequest request
    ) {
        CurrentUserPrincipal current = requireRole(CONFIGURATION_ROLES);
        Machine machine = findAccessibleMachine(machineId, current);

        validateVolumeConfiguration(
                request.getCapacityMl(),
                request.getCurrentVolumeMl(),
                request.getLowStockThresholdMl()
        );

        if (machineSlotRepository.existsByMachine_IdAndSlotNumber(
                machineId, request.getSlotNumber())) {
            throw duplicateSlotNumber(machineId, request.getSlotNumber());
        }

        MachineSlot slot = new MachineSlot();
        slot.setMachine(machine);
        slot.setSlotNumber(request.getSlotNumber());
        slot.setProduct(findProductForMachine(request.getProductId(), machine));
        slot.setCapacityMl(request.getCapacityMl());
        slot.setCurrentVolumeMl(request.getCurrentVolumeMl());
        slot.setLowStockThresholdMl(request.getLowStockThresholdMl());
        slot.setStatus(calculateStatus(
                request.getCurrentVolumeMl(),
                request.getLowStockThresholdMl()
        ));

        MachineSlot savedSlot = saveSlot(slot);
        recordInitialInventory(savedSlot, request.getCurrentVolumeMl(), current);
        return machineSlotMapper.toResponse(savedSlot);
    }

    public MachineSlotResponse updateSlot(
            Long id,
            UpdateMachineSlotRequest request
    ) {
        CurrentUserPrincipal current = requireRole(CONFIGURATION_ROLES);
        MachineSlot slot = findAccessibleSlot(id, current);
        ensureNotDisabled(slot);

        validateVolumeConfiguration(
                request.getCapacityMl(),
                slot.getCurrentVolumeMl(),
                request.getLowStockThresholdMl()
        );

        if (machineSlotRepository.existsByMachine_IdAndSlotNumberAndIdNot(
                slot.getMachine().getId(),
                request.getSlotNumber(),
                slot.getId())) {
            throw duplicateSlotNumber(
                    slot.getMachine().getId(),
                    request.getSlotNumber()
            );
        }

        slot.setSlotNumber(request.getSlotNumber());
        slot.setProduct(findProductForMachine(
                request.getProductId(),
                slot.getMachine()
        ));
        slot.setCapacityMl(request.getCapacityMl());
        slot.setLowStockThresholdMl(request.getLowStockThresholdMl());
        if (slot.getStatus() != MachineSlotStatus.ERROR) {
            slot.setStatus(calculateStatus(
                    slot.getCurrentVolumeMl(),
                    slot.getLowStockThresholdMl()
            ));
        }

        return machineSlotMapper.toResponse(
                saveSlot(slot)
        );
    }

    public MachineSlotResponse updateSlotStatus(
            Long id,
            String requestedStatus
    ) {
        CurrentUserPrincipal current = requireRole(STATUS_ROLES);
        MachineSlot slot = findAccessibleSlot(id, current);
        ensureNotDisabled(slot);

        MachineSlotStatus status;
        try {
            status = MachineSlotStatus.valueOf(
                    requestedStatus.trim().toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Invalid slot status. Allowed: AVAILABLE, LOW_STOCK, EMPTY, ERROR",
                    ex
            );
        }

        if (status == MachineSlotStatus.DISABLED) {
            throw new IllegalArgumentException(
                    "Use DELETE to disable a machine slot"
            );
        }

        slot.setStatus(status == MachineSlotStatus.ERROR
                ? MachineSlotStatus.ERROR
                : calculateStatus(
                        slot.getCurrentVolumeMl(),
                        slot.getLowStockThresholdMl()
                ));

        return machineSlotMapper.toResponse(
                saveSlot(slot)
        );
    }

    public MachineSlotResponse disableSlot(Long id) {
        CurrentUserPrincipal current = requireRole(DELETE_ROLES);
        MachineSlot slot = findAccessibleSlot(id, current);

        if (slot.getStatus() == MachineSlotStatus.DISABLED) {
            return machineSlotMapper.toResponse(slot);
        }

        if (slot.getCurrentVolumeMl().compareTo(BigDecimal.ZERO) > 0) {
            throw new ResourceConflictException(
                    "Remove remaining inventory through the inventory removal workflow before disabling this slot"
            );
        }

        slot.setStatus(MachineSlotStatus.DISABLED);
        return machineSlotMapper.toResponse(
                saveSlot(slot)
        );
    }

    private MachineSlot saveSlot(MachineSlot slot) {
        try {
            return machineSlotRepository.saveAndFlush(slot);
        } catch (DataIntegrityViolationException ex) {
            throw new ResourceConflictException(
                    "Machine slot conflicts with existing data",
                    ex
            );
        }
    }

    private void recordInitialInventory(
            MachineSlot slot,
            BigDecimal initialVolumeMl,
            CurrentUserPrincipal current
    ) {
        if (initialVolumeMl.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setSlot(slot);
        transaction.setPerformedByUserId(current.userId());
        transaction.setTransactionType("ADJUSTMENT");
        transaction.setVolumeChangeMl(initialVolumeMl);
        transaction.setNotes("Initial stock recorded when the machine slot was created");
        inventoryTransactionRepository.saveAndFlush(transaction);
    }

    private Machine findAccessibleMachine(
            Long machineId,
            CurrentUserPrincipal current
    ) {
        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Machine not found with ID: " + machineId
                ));
        ensureTenantAccess(machine.getTenant().getId(), current, "Machine");
        return machine;
    }

    private MachineSlot findAccessibleSlot(
            Long id,
            CurrentUserPrincipal current
    ) {
        MachineSlot slot = machineSlotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Machine slot not found with ID: " + id
                ));
        ensureTenantAccess(
                slot.getMachine().getTenant().getId(),
                current,
                "Machine slot"
        );
        ensureProductTenantMatchesMachine(slot);
        return slot;
    }

    private Product findProductForMachine(Long productId, Machine machine) {
        if (productId == null) {
            return null;
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with ID: " + productId
                ));

        if (!machine.getTenant().getId().equals(product.getTenant().getId())) {
            throw new ResourceNotFoundException(
                    "Product not found with ID: " + productId
            );
        }

        return product;
    }

    private void ensureTenantAccess(
            Long resourceTenantId,
            CurrentUserPrincipal current,
            String resourceName
    ) {
        if (current.isPlatformSuperAdmin()) {
            return;
        }

        if (current.tenantId() == null
                || !current.tenantId().equals(resourceTenantId)) {
            throw new ResourceNotFoundException(
                    resourceName + " not found"
            );
        }
    }

    private void ensureProductTenantMatchesMachine(MachineSlot slot) {
        Product product = slot.getProduct();
        if (product != null
                && (product.getTenant() == null
                || !slot.getMachine().getTenant().getId()
                        .equals(product.getTenant().getId()))) {
            throw new ResourceNotFoundException(
                    "Machine slot not found with ID: " + slot.getId()
            );
        }
    }

    private CurrentUserPrincipal requireRole(Set<String> allowedRoles) {
        CurrentUserPrincipal current = currentUserService.getCurrentUser();
        boolean authorized = allowedRoles.stream().anyMatch(current::hasRole);
        if (!authorized) {
            throw new AccessDeniedException(
                    "You do not have permission to access machine slots"
            );
        }
        return current;
    }

    private void validateVolumeConfiguration(
            BigDecimal capacityMl,
            BigDecimal currentVolumeMl,
            BigDecimal lowStockThresholdMl
    ) {
        if (capacityMl == null
                || currentVolumeMl == null
                || lowStockThresholdMl == null) {
            throw new IllegalArgumentException(
                    "Capacity, current volume and low-stock threshold are required"
            );
        }
        if (currentVolumeMl.compareTo(capacityMl) > 0) {
            throw new IllegalArgumentException(
                    "Current volume must not exceed capacity"
            );
        }
        if (lowStockThresholdMl.compareTo(capacityMl) > 0) {
            throw new IllegalArgumentException(
                    "Low-stock threshold must not exceed capacity"
            );
        }
    }

    private void ensureNotDisabled(MachineSlot slot) {
        if (slot.getStatus() == MachineSlotStatus.DISABLED) {
            throw new ResourceConflictException(
                    "Disabled machine slots cannot be updated"
            );
        }
    }

    private MachineSlotStatus calculateStatus(
            BigDecimal currentVolumeMl,
            BigDecimal lowStockThresholdMl
    ) {
        if (currentVolumeMl.compareTo(BigDecimal.ZERO) == 0) {
            return MachineSlotStatus.EMPTY;
        }
        if (currentVolumeMl.compareTo(lowStockThresholdMl) <= 0) {
            return MachineSlotStatus.LOW_STOCK;
        }
        return MachineSlotStatus.AVAILABLE;
    }

    private ResourceConflictException duplicateSlotNumber(
            Long machineId,
            Integer slotNumber
    ) {
        return new ResourceConflictException(
                "Slot number " + slotNumber
                        + " already exists for machine " + machineId
        );
    }
}
