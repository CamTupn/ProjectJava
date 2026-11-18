package com.scentstation.service;

import com.scentstation.common.security.CurrentUserPrincipal;
import com.scentstation.common.security.CurrentUserService;
import com.scentstation.dto.request.CreateMachineSlotRequest;
import com.scentstation.dto.request.UpdateMachineSlotRequest;
import com.scentstation.entity.InventoryTransaction;
import com.scentstation.entity.Machine;
import com.scentstation.entity.MachineSlot;
import com.scentstation.entity.MachineSlotStatus;
import com.scentstation.entity.Product;
import com.scentstation.entity.Tenant;
import com.scentstation.exception.ResourceConflictException;
import com.scentstation.exception.ResourceNotFoundException;
import com.scentstation.mapper.MachineSlotMapper;
import com.scentstation.repository.MachineRepository;
import com.scentstation.repository.MachineSlotRepository;
import com.scentstation.repository.InventoryTransactionRepository;
import com.scentstation.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MachineSlotServiceTest {

    @Mock
    private MachineRepository machineRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MachineSlotRepository machineSlotRepository;

    @Mock
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Mock
    private CurrentUserService currentUserService;

    private MachineSlotService service;
    private Tenant tenant;
    private Machine machine;

    @BeforeEach
    void setUp() {
        service = new MachineSlotService(
                machineRepository,
                productRepository,
                machineSlotRepository,
                inventoryTransactionRepository,
                new MachineSlotMapper(),
                currentUserService
        );

        tenant = new Tenant();
        tenant.setId(10L);

        machine = new Machine();
        machine.setId(20L);
        machine.setTenant(tenant);

        when(currentUserService.getCurrentUser()).thenReturn(
                new CurrentUserPrincipal(
                        1L,
                        "brand-admin",
                        10L,
                        "TENANT-10",
                        Set.of("BRAND_ADMIN")
                )
        );
    }

    @Test
    void createSlotCalculatesEmptyStatusAndStoresInitialVolume() {
        CreateMachineSlotRequest request = createRequest(
                1,
                null,
                "50.00",
                "0.00",
                "10.00"
        );
        when(machineRepository.findById(20L))
                .thenReturn(Optional.of(machine));
        when(machineSlotRepository.existsByMachine_IdAndSlotNumber(20L, 1))
                .thenReturn(false);
        when(machineSlotRepository.saveAndFlush(any(MachineSlot.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.createSlot(20L, request);

        assertEquals(MachineSlotStatus.EMPTY, response.status());
        assertEquals(new BigDecimal("0.00"), response.currentVolumeMl());
        assertEquals(1, response.slotNumber());
        verify(inventoryTransactionRepository, never()).saveAndFlush(any());
    }

    @Test
    void createSlotRecordsNonZeroInitialVolumeAsInventoryAdjustment() {
        CreateMachineSlotRequest request = createRequest(
                1,
                null,
                "50.00",
                "20.00",
                "10.00"
        );
        when(machineRepository.findById(20L))
                .thenReturn(Optional.of(machine));
        when(machineSlotRepository.existsByMachine_IdAndSlotNumber(20L, 1))
                .thenReturn(false);
        when(machineSlotRepository.saveAndFlush(any(MachineSlot.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(inventoryTransactionRepository.saveAndFlush(
                any(InventoryTransaction.class)
        )).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.createSlot(20L, request);

        var transactionCaptor = forClass(InventoryTransaction.class);
        verify(inventoryTransactionRepository)
                .saveAndFlush(transactionCaptor.capture());
        InventoryTransaction transaction = transactionCaptor.getValue();
        assertEquals(MachineSlotStatus.AVAILABLE, response.status());
        assertEquals(new BigDecimal("20.00"), transaction.getVolumeChangeMl());
        assertEquals("ADJUSTMENT", transaction.getTransactionType());
        assertEquals(1L, transaction.getPerformedByUserId());
        assertEquals(response.id(), transaction.getSlot().getId());
        assertEquals(
                "Initial stock recorded when the machine slot was created",
                transaction.getNotes()
        );
    }

    @Test
    void createSlotRejectsProductFromAnotherTenant() {
        Tenant otherTenant = new Tenant();
        otherTenant.setId(11L);
        Product product = new Product();
        product.setId(30L);
        product.setTenant(otherTenant);
        CreateMachineSlotRequest request = createRequest(
                1,
                30L,
                "50.00",
                "20.00",
                "10.00"
        );
        when(machineRepository.findById(20L))
                .thenReturn(Optional.of(machine));
        when(machineSlotRepository.existsByMachine_IdAndSlotNumber(20L, 1))
                .thenReturn(false);
        when(productRepository.findById(30L))
                .thenReturn(Optional.of(product));

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.createSlot(20L, request)
        );
        verify(machineSlotRepository, never()).saveAndFlush(any());
    }

    @Test
    void createSlotRejectsDuplicateSlotNumber() {
        CreateMachineSlotRequest request = createRequest(
                1,
                null,
                "50.00",
                "0.00",
                "10.00"
        );
        when(machineRepository.findById(20L))
                .thenReturn(Optional.of(machine));
        when(machineSlotRepository.existsByMachine_IdAndSlotNumber(20L, 1))
                .thenReturn(true);

        assertThrows(
                ResourceConflictException.class,
                () -> service.createSlot(20L, request)
        );
        verify(machineSlotRepository, never()).saveAndFlush(any());
    }

    @Test
    void createSlotRejectsThresholdAboveCapacity() {
        CreateMachineSlotRequest request = createRequest(
                1,
                null,
                "10.00",
                "5.00",
                "11.00"
        );
        when(machineRepository.findById(20L))
                .thenReturn(Optional.of(machine));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.createSlot(20L, request)
        );
        verify(machineSlotRepository, never())
                .existsByMachine_IdAndSlotNumber(20L, 1);
    }

    @Test
    void updateSlotDoesNotAllowCapacityBelowCurrentVolume() {
        MachineSlot slot = existingSlot("20.00");
        when(machineSlotRepository.findById(40L))
                .thenReturn(Optional.of(slot));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateSlot(
                        40L,
                        updateRequest(1, "10.00", "5.00")
                )
        );
        verify(machineSlotRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateSlotPreservesCurrentVolumeAndRecalculatesStatus() {
        MachineSlot slot = existingSlot("8.00");
        when(machineSlotRepository.findById(40L))
                .thenReturn(Optional.of(slot));
        when(machineSlotRepository.existsByMachine_IdAndSlotNumberAndIdNot(
                20L, 2, 40L
        )).thenReturn(false);
        when(machineSlotRepository.saveAndFlush(slot)).thenReturn(slot);

        var response = service.updateSlot(
                40L,
                updateRequest(2, "50.00", "10.00")
        );

        assertEquals(new BigDecimal("8.00"), response.currentVolumeMl());
        assertEquals(MachineSlotStatus.LOW_STOCK, response.status());
        assertEquals(2, response.slotNumber());
    }

    @Test
    void disableSlotRefusesToDiscardRemainingInventory() {
        MachineSlot slot = existingSlot("1.00");
        when(machineSlotRepository.findById(40L))
                .thenReturn(Optional.of(slot));

        assertThrows(
                ResourceConflictException.class,
                () -> service.disableSlot(40L)
        );
        verify(machineSlotRepository, never()).saveAndFlush(any());
    }

    @Test
    void disableSlotSoftDisablesAnEmptySlot() {
        MachineSlot slot = existingSlot("0.00");
        when(machineSlotRepository.findById(40L))
                .thenReturn(Optional.of(slot));
        when(machineSlotRepository.saveAndFlush(slot)).thenReturn(slot);

        var response = service.disableSlot(40L);

        assertEquals(MachineSlotStatus.DISABLED, response.status());
        verify(machineSlotRepository).saveAndFlush(slot);
    }

    @Test
    void updateSlotStatusRecalculatesInventoryStatuses() {
        MachineSlot slot = existingSlot("20.00");
        slot.setStatus(MachineSlotStatus.ERROR);
        when(machineSlotRepository.findById(40L))
                .thenReturn(Optional.of(slot));
        when(machineSlotRepository.saveAndFlush(slot)).thenReturn(slot);

        var response = service.updateSlotStatus(40L, "AVAILABLE");

        assertEquals(MachineSlotStatus.AVAILABLE, response.status());
    }

    @Test
    void listSlotsRejectsCrossTenantProductReference() {
        Tenant otherTenant = new Tenant();
        otherTenant.setId(11L);
        Product foreignProduct = new Product();
        foreignProduct.setId(30L);
        foreignProduct.setTenant(otherTenant);
        MachineSlot slot = existingSlot("20.00");
        slot.setProduct(foreignProduct);
        when(machineRepository.findById(20L))
                .thenReturn(Optional.of(machine));
        when(machineSlotRepository.findAllByMachine_IdOrderBySlotNumber(20L))
                .thenReturn(java.util.List.of(slot));

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.getSlotsByMachine(20L)
        );
    }

    @Test
    void tenantCannotReadAnotherTenantsSlot() {
        Tenant otherTenant = new Tenant();
        otherTenant.setId(99L);
        Machine otherMachine = new Machine();
        otherMachine.setId(21L);
        otherMachine.setTenant(otherTenant);
        MachineSlot slot = existingSlot("5.00");
        slot.setMachine(otherMachine);
        when(machineSlotRepository.findById(40L))
                .thenReturn(Optional.of(slot));

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.getSlot(40L)
        );
    }

    private CreateMachineSlotRequest createRequest(
            Integer slotNumber,
            Long productId,
            String capacity,
            String currentVolume,
            String threshold
    ) {
        CreateMachineSlotRequest request = new CreateMachineSlotRequest();
        request.setSlotNumber(slotNumber);
        request.setProductId(productId);
        request.setCapacityMl(new BigDecimal(capacity));
        request.setCurrentVolumeMl(new BigDecimal(currentVolume));
        request.setLowStockThresholdMl(new BigDecimal(threshold));
        return request;
    }

    private UpdateMachineSlotRequest updateRequest(
            Integer slotNumber,
            String capacity,
            String threshold
    ) {
        UpdateMachineSlotRequest request = new UpdateMachineSlotRequest();
        request.setSlotNumber(slotNumber);
        request.setCapacityMl(new BigDecimal(capacity));
        request.setLowStockThresholdMl(new BigDecimal(threshold));
        return request;
    }

    private MachineSlot existingSlot(String currentVolume) {
        MachineSlot slot = new MachineSlot();
        slot.setId(40L);
        slot.setMachine(machine);
        slot.setSlotNumber(1);
        slot.setCapacityMl(new BigDecimal("50.00"));
        slot.setCurrentVolumeMl(new BigDecimal(currentVolume));
        slot.setLowStockThresholdMl(new BigDecimal("10.00"));
        slot.setStatus(MachineSlotStatus.AVAILABLE);
        return slot;
    }
}
