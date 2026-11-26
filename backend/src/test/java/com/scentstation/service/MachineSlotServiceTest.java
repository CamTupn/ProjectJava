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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.access.AccessDeniedException;
import static org.mockito.Mockito.verifyNoInteractions;

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
        machine.setTenantId(tenant.getId());

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
        Product product = createProduct(30L, 10L, "ACTIVE");

        CreateMachineSlotRequest request = createRequest(
            1,
            30L,
            "50.00",
            "20.00",
            "10.00"
        );
        when(machineRepository.findById(20L))
                .thenReturn(Optional.of(machine));
        when(productRepository.findById(30L))
                .thenReturn(Optional.of(product));
        when(machineSlotRepository.existsByMachine_IdAndSlotNumber(20L, 1))
                .thenReturn(false);
        when(machineSlotRepository.saveAndFlush(any(MachineSlot.class)))
                .thenAnswer(invocation -> {
                        MachineSlot saved = invocation.getArgument(0);
                        saved.setId(40L);
                        return saved;
                });

        var response = service.createSlot(20L, request);

        var transactionCaptor = forClass(InventoryTransaction.class);

        verify(inventoryTransactionRepository)
                .saveAndFlush(transactionCaptor.capture());

        InventoryTransaction transaction = transactionCaptor.getValue();

        assertEquals(MachineSlotStatus.AVAILABLE, response.status());
        assertEquals(new BigDecimal("20.00"), transaction.getVolumeChangeMl());
        assertEquals("ADJUSTMENT", transaction.getTransactionType());
        assertEquals(1L, transaction.getPerformedByUserId());
        assertEquals(40L, transaction.getSlot().getId());
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
        ReflectionTestUtils.setField(product, "id", 30L);
        product.setTenantId(otherTenant.getId());
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

        UpdateMachineSlotRequest request = updateRequest(2, "50.00", "10.00");
        request.setProductId(30L);

        when(machineSlotRepository.findById(40L))
                .thenReturn(Optional.of(slot));
        when(productRepository.findById(30L))
                .thenReturn(Optional.of(slot.getProduct()));

        when(machineSlotRepository.existsByMachine_IdAndSlotNumberAndIdNot(
                20L, 2, 40L
        )).thenReturn(false);

        when(machineSlotRepository.saveAndFlush(slot)).thenReturn(slot);

        var response = service.updateSlot(40L, request);

        assertEquals(new BigDecimal("8.00"), response.currentVolumeMl());
        assertEquals(MachineSlotStatus.LOW_STOCK, response.status());
        assertEquals(2, response.slotNumber());

        verify(inventoryTransactionRepository, never())
                .saveAndFlush(any());
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
        ReflectionTestUtils.setField(foreignProduct, "id", 30L);
        foreignProduct.setTenantId(otherTenant.getId());
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
        otherMachine.setTenantId(otherTenant.getId());
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
        slot.setProduct(createProduct(30L, 10L, "ACTIVE"));
        slot.setCapacityMl(new BigDecimal("50.00"));
        slot.setCurrentVolumeMl(new BigDecimal(currentVolume));
        slot.setLowStockThresholdMl(new BigDecimal("10.00"));

        BigDecimal volume = slot.getCurrentVolumeMl();
        if (volume.compareTo(BigDecimal.ZERO) == 0) {
                 slot.setStatus(MachineSlotStatus.EMPTY);
        } else if (volume.compareTo(slot.getLowStockThresholdMl()) <= 0) {
                slot.setStatus(MachineSlotStatus.LOW_STOCK);
        } else {
                slot.setStatus(MachineSlotStatus.AVAILABLE);
        }
        return slot;
    }

    private Product createProduct(Long id, Long tenantId, String status) {
        Product product = new Product();

        ReflectionTestUtils.setField(product, "id", id);

        product.setTenantId(tenantId);
        product.setName("Test Fragrance");
        product.setStatus(status);

        return product;
    }

    // Nhóm bảo vệ Product và tồn kho

    @Test
    void createSlotRejectsPositiveInventoryWithoutProduct() {

        CreateMachineSlotRequest request =
                createRequest(1, null, "50.00", "20.00", "10.00");

        when(machineRepository.findById(20L))
                .thenReturn(Optional.of(machine));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.createSlot(20L, request)
        );

        verify(machineSlotRepository, never()).saveAndFlush(any());
        verify(inventoryTransactionRepository, never()).saveAndFlush(any());
    }

    @Test
    void createSlotRejectsInactiveProduct() {

        Product product = createProduct(30L, 10L, "INACTIVE");

        CreateMachineSlotRequest request =
                createRequest(1, 30L, "50.00", "0.00", "10.00");

        when(machineRepository.findById(20L))
                .thenReturn(Optional.of(machine));

        when(productRepository.findById(30L))
                .thenReturn(Optional.of(product));

        assertThrows(
                ResourceConflictException.class,
                () -> service.createSlot(20L, request)
        );

        verify(machineSlotRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateSlotRejectsProductChangeWhileInventoryRemains() {

        MachineSlot slot = existingSlot("20.00");
        Product newProduct = createProduct(31L, 10L, "ACTIVE");

        UpdateMachineSlotRequest request =
                updateRequest(1, "50.00", "10.00");

        request.setProductId(31L);

        when(machineSlotRepository.findById(40L))
                .thenReturn(Optional.of(slot));

        when(productRepository.findById(31L))
                .thenReturn(Optional.of(newProduct));

        assertThrows(
                ResourceConflictException.class,
                () -> service.updateSlot(40L, request)
        );

        assertEquals(30L, slot.getProduct().getId());
        assertEquals(new BigDecimal("20.00"), slot.getCurrentVolumeMl());

        verify(machineSlotRepository, never()).saveAndFlush(any());
        verify(inventoryTransactionRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateSlotRejectsProductRemovalWhileInventoryRemains() {

        MachineSlot slot = existingSlot("20.00");

        UpdateMachineSlotRequest request =
                updateRequest(1, "50.00", "10.00");

        request.setProductId(null);

        when(machineSlotRepository.findById(40L))
                .thenReturn(Optional.of(slot));

        assertThrows(
                ResourceConflictException.class,
                () -> service.updateSlot(40L, request)
        );

        assertEquals(30L, slot.getProduct().getId());
        assertEquals(new BigDecimal("20.00"), slot.getCurrentVolumeMl());

        verify(machineSlotRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateSlotAllowsProductChangeWhenEmpty() {

        MachineSlot slot = existingSlot("0.00");
        Product newProduct = createProduct(31L, 10L, "ACTIVE");

        UpdateMachineSlotRequest request =
                updateRequest(1, "50.00", "10.00");

        request.setProductId(31L);

        when(machineSlotRepository.findById(40L))
                .thenReturn(Optional.of(slot));

        when(productRepository.findById(31L))
                .thenReturn(Optional.of(newProduct));

        when(machineSlotRepository.saveAndFlush(slot))
                .thenReturn(slot);

        var response = service.updateSlot(40L, request);

        assertEquals(31L, slot.getProduct().getId());
        assertEquals(new BigDecimal("0.00"), response.currentVolumeMl());
        assertEquals(MachineSlotStatus.EMPTY, response.status());

        verify(inventoryTransactionRepository, never()).saveAndFlush(any());
    }

    // Nhóm kiểm tra Machine DISABLED

    @Test
    void createSlotRejectsDisabledMachine() {

        machine.setOperationalStatus("DISABLED");

        CreateMachineSlotRequest request =
            createRequest(1, null, "50.00", "0.00", "10.00");

        when(machineRepository.findById(20L))
            .thenReturn(Optional.of(machine));

        assertThrows(
            ResourceConflictException.class,
            () -> service.createSlot(20L, request)
        );

        verify(machineSlotRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateSlotRejectsDisabledMachine() {

        machine.setOperationalStatus("DISABLED");

        MachineSlot slot = existingSlot("0.00");

        when(machineSlotRepository.findById(40L))
            .thenReturn(Optional.of(slot));

        assertThrows(
                ResourceConflictException.class,
                () -> service.updateSlot(
                    40L,
                    updateRequest(1, "50.00", "10.00")
                )
        );

        verify(machineSlotRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateSlotStatusRejectsDisabledMachine() {

        machine.setOperationalStatus("DISABLED");

        MachineSlot slot = existingSlot("0.00");

        when(machineSlotRepository.findById(40L))
            .thenReturn(Optional.of(slot));

        assertThrows(
            ResourceConflictException.class,
            () -> service.updateSlotStatus(40L, "ERROR")
        );

        verify(machineSlotRepository, never()).saveAndFlush(any());
    }

    // Nhóm Volume Validation
    @Test
    void createSlotRejectsNegativeThreshold() {

            when(machineRepository.findById(20L))
                            .thenReturn(Optional.of(machine));

            assertThrows(
                            IllegalArgumentException.class,
                            () -> service.createSlot(
                                            20L,
                                            createRequest(1, null, "50.00", "0.00", "-1.00")));

            verify(machineSlotRepository, never()).saveAndFlush(any());
    }


    @Test
    void createSlotRejectsZeroCapacity() {
        when(machineRepository.findById(20L))
                .thenReturn(Optional.of(machine));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.createSlot(
                        20L,
                        createRequest(1, null, "0.00", "0.00", "0.00")
                )
        );

        verify(machineSlotRepository, never()).saveAndFlush(any());
        verify(inventoryTransactionRepository, never()).saveAndFlush(any());
    }

    @Test
    void createSlotRejectsNegativeCurrentVolume() {
        when(machineRepository.findById(20L))
                .thenReturn(Optional.of(machine));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.createSlot(
                        20L,
                        createRequest(1, null, "50.00", "-1.00", "10.00")
                )
        );

        verify(machineSlotRepository, never()).saveAndFlush(any());
        verify(inventoryTransactionRepository, never()).saveAndFlush(any());
    }

        @Test
        void createSlotRejectsCurrentVolumeAboveCapacity() {
        when(machineRepository.findById(20L))
                .thenReturn(Optional.of(machine));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.createSlot(
                        20L,
                        createRequest(1, 30L, "50.00", "51.00", "10.00")
                )
        );

        verify(productRepository, never()).findById(any());
        verify(machineSlotRepository, never()).saveAndFlush(any());
        verify(inventoryTransactionRepository, never()).saveAndFlush(any());
        }

    //Nhóm bảo vệ trạng thái Slot

    @Test
    void updateSlotPreservesErrorStatus() {

        MachineSlot slot = existingSlot("20.00");
        slot.setStatus(MachineSlotStatus.ERROR);

        UpdateMachineSlotRequest request =
                updateRequest(1, "60.00", "10.00");

        request.setProductId(30L);

        when(machineSlotRepository.findById(40L))
                .thenReturn(Optional.of(slot));

        when(productRepository.findById(30L))
                .thenReturn(Optional.of(slot.getProduct()));

        when(machineSlotRepository.saveAndFlush(slot))
                .thenReturn(slot);

        var response = service.updateSlot(40L, request);

        assertEquals(MachineSlotStatus.ERROR, response.status());
        assertEquals(new BigDecimal("20.00"), response.currentVolumeMl());
    }

    @Test
    void updateSlotRejectsDisabledSlot() {

        MachineSlot slot = existingSlot("0.00");
        slot.setStatus(MachineSlotStatus.DISABLED);

        when(machineSlotRepository.findById(40L))
                .thenReturn(Optional.of(slot));

        assertThrows(
                ResourceConflictException.class,
                () -> service.updateSlot(
                        40L,
                        updateRequest(1, "50.00", "10.00")
                )
        );

        verify(machineSlotRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateSlotStatusRejectsDisabledSlot() {

        MachineSlot slot = existingSlot("0.00");
        slot.setStatus(MachineSlotStatus.DISABLED);

        when(machineSlotRepository.findById(40L))
                .thenReturn(Optional.of(slot));

        assertThrows(
                ResourceConflictException.class,
                () -> service.updateSlotStatus(40L, "AVAILABLE")
        );

        assertEquals(MachineSlotStatus.DISABLED, slot.getStatus());

        verify(machineSlotRepository, never()).saveAndFlush(any());
    }

    @Test
    void disableSlotIsIdempotent() {

        MachineSlot slot = existingSlot("0.00");
        slot.setStatus(MachineSlotStatus.DISABLED);

        when(machineSlotRepository.findById(40L))
                .thenReturn(Optional.of(slot));

        var response = service.disableSlot(40L);

        assertEquals(MachineSlotStatus.DISABLED, response.status());

        verify(machineSlotRepository, never()).saveAndFlush(any());
        verify(inventoryTransactionRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateSlotStatusRejectsInvalidStatus() {

        MachineSlot slot = existingSlot("20.00");

        when(machineSlotRepository.findById(40L))
                .thenReturn(Optional.of(slot));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateSlotStatus(40L, "INVALID")
        );

        verify(machineSlotRepository, never()).saveAndFlush(any());
    }

    private Machine foreignMachine() {
        Machine otherMachine = new Machine();
        otherMachine.setId(21L);
        otherMachine.setTenantId(99L);
        otherMachine.setOperationalStatus("ACTIVE");

        return otherMachine;
    }

    private MachineSlot foreignSlot() {
        MachineSlot slot = existingSlot("0.00");
        slot.setMachine(foreignMachine());
        slot.setProduct(createProduct(31L, 99L, "ACTIVE"));

        return slot;
    }


    @Test
    void tenantCannotCreateSlotOnAnotherTenantsMachine() {

        when(machineRepository.findById(21L))
                .thenReturn(Optional.of(foreignMachine()));

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.createSlot(
                        21L,
                        createRequest(1, null, "50.00", "0.00", "10.00")
                )
        );

        verify(machineSlotRepository, never()).saveAndFlush(any());
        verify(inventoryTransactionRepository, never()).saveAndFlush(any());
    }

    @Test
    void tenantCannotListSlotsOfAnotherTenantsMachine() {

        when(machineRepository.findById(21L))
                .thenReturn(Optional.of(foreignMachine()));

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.getSlotsByMachine(21L)
        );

        verify(machineSlotRepository, never())
                .findAllByMachine_IdOrderBySlotNumber(any());
    }

    @Test
    void tenantCannotUpdateAnotherTenantsSlot() {

        MachineSlot slot = foreignSlot();

        when(machineSlotRepository.findById(40L))
                .thenReturn(Optional.of(slot));

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.updateSlot(
                        40L,
                        updateRequest(1, "60.00", "10.00")
                )
        );

        assertEquals(new BigDecimal("50.00"), slot.getCapacityMl());

        verify(machineSlotRepository, never()).saveAndFlush(any());
    }

    @Test
    void tenantCannotUpdateAnotherTenantsSlotStatus() {

        MachineSlot slot = foreignSlot();

        when(machineSlotRepository.findById(40L))
                .thenReturn(Optional.of(slot));

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.updateSlotStatus(40L, "ERROR")
        );

        assertEquals(MachineSlotStatus.EMPTY, slot.getStatus());

        verify(machineSlotRepository, never()).saveAndFlush(any());
    }

    @Test
    void tenantCannotDisableAnotherTenantsSlot() {

        MachineSlot slot = foreignSlot();

        when(machineSlotRepository.findById(40L))
                .thenReturn(Optional.of(slot));

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.disableSlot(40L)
        );

        assertEquals(MachineSlotStatus.EMPTY, slot.getStatus());

        verify(machineSlotRepository, never()).saveAndFlush(any());
        verify(inventoryTransactionRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateSlotRejectsProductFromAnotherTenant() {

        MachineSlot slot = existingSlot("0.00");
        Product foreignProduct = createProduct(31L, 99L, "ACTIVE");

        UpdateMachineSlotRequest request =
                updateRequest(1, "50.00", "10.00");

        request.setProductId(31L);

        when(machineSlotRepository.findById(40L))
                .thenReturn(Optional.of(slot));

        when(productRepository.findById(31L))
                .thenReturn(Optional.of(foreignProduct));

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.updateSlot(40L, request)
        );

        assertEquals(30L, slot.getProduct().getId());

        verify(machineSlotRepository, never()).saveAndFlush(any());
        verify(inventoryTransactionRepository, never()).saveAndFlush(any());
    }

    @Test
    void superAdminCanReadSlotFromAnotherTenant() {

        when(currentUserService.getCurrentUser()).thenReturn(
                new CurrentUserPrincipal(
                        1L,
                        "super-admin",
                        null,
                        null,
                        Set.of("PLATFORM_SUPER_ADMIN")
                )
        );

        when(machineSlotRepository.findById(40L))
                .thenReturn(Optional.of(foreignSlot()));

        var response = service.getSlot(40L);

        assertEquals(40L, response.id());
        assertEquals(21L, response.machineId());
        assertEquals(31L, response.productId());
    }


    @Test
    void superAdminCannotAssignProductFromDifferentTenant() {

        when(currentUserService.getCurrentUser()).thenReturn(
                new CurrentUserPrincipal(
                        1L,
                        "super-admin",
                        null,
                        null,
                        Set.of("PLATFORM_SUPER_ADMIN")
                )
        );

        Product foreignProduct = createProduct(31L, 99L, "ACTIVE");

        when(machineRepository.findById(20L))
                .thenReturn(Optional.of(machine));

        when(productRepository.findById(31L))
                .thenReturn(Optional.of(foreignProduct));

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.createSlot(
                        20L,
                        createRequest(1, 31L, "50.00", "0.00", "10.00")
                )
        );

        verify(machineSlotRepository, never()).saveAndFlush(any());
        verify(inventoryTransactionRepository, never()).saveAndFlush(any());
    }

    private void mockCurrentRole(String role) {
        when(currentUserService.getCurrentUser()).thenReturn(
                new CurrentUserPrincipal(
                        1L,
                        "test-user",
                        10L,
                        "TENANT-10",
                        Set.of(role)
                )
        );
    }

    @Test
    void technicianCannotCreateSlot() {

        mockCurrentRole("TECHNICIAN");

        assertThrows(
                AccessDeniedException.class,
                () -> service.createSlot(
                        20L,
                        createRequest(1, null, "50.00", "0.00", "10.00")
                )
        );

        verifyNoInteractions(
                machineRepository,
                productRepository,
                machineSlotRepository,
                inventoryTransactionRepository
        );
    }

    @Test
    void technicianCannotUpdateSlotConfiguration() {

        mockCurrentRole("TECHNICIAN");

        assertThrows(
                AccessDeniedException.class,
                () -> service.updateSlot(
                        40L,
                        updateRequest(1, "50.00", "10.00")
                )
        );

        verifyNoInteractions(machineSlotRepository);
    }

    @Test
    void technicianCannotDisableSlot() {

        mockCurrentRole("TECHNICIAN");

        assertThrows(
                AccessDeniedException.class,
                () -> service.disableSlot(40L)
        );

        verifyNoInteractions(machineSlotRepository);
    }

    @Test
    void inventoryStaffCannotDisableSlot() {

        mockCurrentRole("INVENTORY_STAFF");

        assertThrows(
                AccessDeniedException.class,
                () -> service.disableSlot(40L)
        );

        verifyNoInteractions(machineSlotRepository);
    }

    @Test
    void inventoryStaffCannotUpdateSlotStatus() {

        mockCurrentRole("INVENTORY_STAFF");

        assertThrows(
                AccessDeniedException.class,
                () -> service.updateSlotStatus(40L, "ERROR")
        );

        verifyNoInteractions(machineSlotRepository);
    }

}
