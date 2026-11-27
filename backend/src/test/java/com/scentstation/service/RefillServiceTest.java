package com.scentstation.service;

import static com.scentstation.service.InventoryTestData.SLOT_ID;
import static com.scentstation.service.InventoryTestData.TENANT_ID;
import static com.scentstation.service.InventoryTestData.slot;
import static com.scentstation.service.InventoryTestData.user;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.scentstation.common.security.CurrentUserService;
import com.scentstation.contract.InventoryChanged;
import com.scentstation.contract.InventoryChanged.ChangeType;
import com.scentstation.dto.request.CreateRefillRequest;
import com.scentstation.entity.InventoryTransaction;
import com.scentstation.entity.MachineSlot;
import com.scentstation.entity.MachineSlotStatus;
import com.scentstation.entity.Refill;
import com.scentstation.exception.ResourceConflictException;
import com.scentstation.exception.ResourceNotFoundException;
import com.scentstation.mapper.InventoryMapper;
import com.scentstation.repository.InventoryLockRepository;
import com.scentstation.repository.InventoryTransactionRepository;
import com.scentstation.repository.RefillRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RefillServiceTest {

    @Mock
    private InventoryLockRepository lockRepository;

    @Mock
    private RefillRepository refillRepository;

    @Mock
    private InventoryTransactionRepository transactionRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private RefillService service;

    @BeforeEach
    void setUp() {
        InventoryMapper mapper = new InventoryMapper();
        InventoryService inventoryService = new InventoryService(
                lockRepository, transactionRepository, mapper,
                currentUserService, eventPublisher
        );
        service = new RefillService(
                lockRepository, refillRepository, inventoryService,
                mapper, currentUserService
        );
    }

    private void loginAs(Long tenantId, String... roles) {
        when(currentUserService.getCurrentUser())
                .thenReturn(user(tenantId, roles));
    }

    private void stubSaves() {
        when(refillRepository.saveAndFlush(any(Refill.class)))
                .thenAnswer(invocation -> {
                    Refill refill = invocation.getArgument(0);
                    ReflectionTestUtils.setField(refill, "id", 300L);
                    return refill;
                });
        when(transactionRepository.saveAndFlush(any(InventoryTransaction.class)))
                .thenAnswer(invocation -> {
                    InventoryTransaction tx = invocation.getArgument(0);
                    ReflectionTestUtils.setField(tx, "id", 500L);
                    return tx;
                });
    }

    private CreateRefillRequest request(String volume) {
        CreateRefillRequest request = new CreateRefillRequest();
        request.setSlotId(SLOT_ID);
        request.setVolumeMl(volume == null ? null : new BigDecimal(volume));
        request.setNotes("  Weekly refill  ");
        return request;
    }

    private MachineSlot lockedSlot(String capacity, String current, String threshold,
                                   MachineSlotStatus status) {
        MachineSlot slot = slot(capacity, current, threshold, status);
        when(lockRepository.lockById(SLOT_ID)).thenReturn(Optional.of(slot));
        return slot;
    }

    private void assertNothingWritten() {
        verify(refillRepository, never()).saveAndFlush(any());
        verify(transactionRepository, never()).saveAndFlush(any());
        verifyNoInteractions(eventPublisher);
    }

    // ---------------------------------------------------------------
    // Valid refill
    // ---------------------------------------------------------------

    @Test
    void validRefillIncreasesStockAndStoresRefillAndTransaction() {
        loginAs(TENANT_ID, "INVENTORY_STAFF");
        MachineSlot slot = lockedSlot("50.00", "20.00", "10.00", MachineSlotStatus.AVAILABLE);
        stubSaves();

        var response = service.createRefill(request("15.50"));

        assertEquals(new BigDecimal("35.50"), slot.getCurrentVolumeMl());

        // Refill row
        ArgumentCaptor<Refill> refill = ArgumentCaptor.forClass(Refill.class);
        verify(refillRepository).saveAndFlush(refill.capture());
        assertEquals(slot, refill.getValue().getSlot());
        assertEquals(slot.getProduct(), refill.getValue().getProduct());
        assertEquals(7L, refill.getValue().getPerformedByUserId());
        assertEquals(new BigDecimal("15.50"), refill.getValue().getVolumeAddedMl());
        assertEquals("Weekly refill", refill.getValue().getNotes());

        // Inventory transaction row
        ArgumentCaptor<InventoryTransaction> tx =
                ArgumentCaptor.forClass(InventoryTransaction.class);
        verify(transactionRepository).saveAndFlush(tx.capture());
        assertEquals("REFILL", tx.getValue().getTransactionType());
        assertEquals(new BigDecimal("15.50"), tx.getValue().getVolumeChangeMl());
        assertEquals(300L, tx.getValue().getRefillId());
        assertEquals(7L, tx.getValue().getPerformedByUserId());
        assertNull(tx.getValue().getExecutionId());

        // Event
        ArgumentCaptor<InventoryChanged> event =
                ArgumentCaptor.forClass(InventoryChanged.class);
        verify(eventPublisher).publishEvent(event.capture());
        assertEquals(ChangeType.REFILL, event.getValue().changeType());
        assertEquals(new BigDecimal("20.00"), event.getValue().previousVolumeMl());
        assertEquals(new BigDecimal("35.50"), event.getValue().currentVolumeMl());
        assertEquals(TENANT_ID, event.getValue().tenantId());
        assertEquals(300L, event.getValue().refillId());

        // Response
        assertEquals(300L, response.id());
        assertEquals(SLOT_ID, response.slotId());
        assertEquals(new BigDecimal("35.50"), response.slotCurrentVolumeMl());
        assertEquals(MachineSlotStatus.AVAILABLE, response.slotStatus());
    }

    @Test
    void refillUpToExactCapacityIsAccepted() {
        loginAs(TENANT_ID, "INVENTORY_STAFF");
        MachineSlot slot = lockedSlot("50.00", "40.00", "10.00", MachineSlotStatus.AVAILABLE);
        stubSaves();

        service.createRefill(request("10.00"));

        assertEquals(new BigDecimal("50.00"), slot.getCurrentVolumeMl());
        assertEquals(MachineSlotStatus.AVAILABLE, slot.getStatus());
    }

    @Test
    void refillAboveCapacityIsRejected() {
        loginAs(TENANT_ID, "INVENTORY_STAFF");
        MachineSlot slot = lockedSlot("50.00", "40.00", "10.00", MachineSlotStatus.AVAILABLE);

        assertThrows(ResourceConflictException.class,
                () -> service.createRefill(request("10.01")));

        assertEquals(new BigDecimal("40.00"), slot.getCurrentVolumeMl());
        assertNothingWritten();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-1.00", "-0.01"})
    void zeroOrNegativeVolumeIsRejectedBeforeTouchingTheSlot(String volume) {
        loginAs(TENANT_ID, "INVENTORY_STAFF");

        assertThrows(IllegalArgumentException.class,
                () -> service.createRefill(request(volume)));

        verifyNoInteractions(lockRepository);
        assertNothingWritten();
    }

    @Test
    void missingVolumeOrSlotIdOrTooManyDecimalsAreRejected() {
        loginAs(TENANT_ID, "INVENTORY_STAFF");

        assertThrows(IllegalArgumentException.class, () -> service.createRefill(request(null)));
        assertThrows(IllegalArgumentException.class, () -> service.createRefill(request("1.001")));
        CreateRefillRequest noSlot = request("5.00");
        noSlot.setSlotId(null);
        assertThrows(IllegalArgumentException.class, () -> service.createRefill(noSlot));
        verifyNoInteractions(lockRepository);
    }

    // ---------------------------------------------------------------
    // Slot state rules
    // ---------------------------------------------------------------

    @Test
    void slotWithoutProductIsRejected() {
        loginAs(TENANT_ID, "INVENTORY_STAFF");
        MachineSlot slot = lockedSlot("50.00", "0.00", "10.00", MachineSlotStatus.EMPTY);
        slot.setProduct(null);

        assertThrows(ResourceConflictException.class,
                () -> service.createRefill(request("5.00")));
        assertNothingWritten();
    }

    @Test
    void disabledSlotIsRejected() {
        loginAs(TENANT_ID, "INVENTORY_STAFF");
        lockedSlot("50.00", "0.00", "10.00", MachineSlotStatus.DISABLED);

        assertThrows(ResourceConflictException.class,
                () -> service.createRefill(request("5.00")));
        assertNothingWritten();
    }

    @Test
    void slotOfDisabledMachineIsRejected() {
        loginAs(TENANT_ID, "INVENTORY_STAFF");
        MachineSlot slot = lockedSlot("50.00", "0.00", "10.00", MachineSlotStatus.EMPTY);
        slot.getMachine().setOperationalStatus("DISABLED");

        assertThrows(ResourceConflictException.class,
                () -> service.createRefill(request("5.00")));
        assertNothingWritten();
    }

    @Test
    void slotWithUnreconciledCommandIsRejected() {
        loginAs(TENANT_ID, "INVENTORY_STAFF");
        MachineSlot slot = lockedSlot("50.00", "20.00", "10.00", MachineSlotStatus.AVAILABLE);
        when(lockRepository.countUnreconciledCommands(SLOT_ID)).thenReturn(1L);

        assertThrows(ResourceConflictException.class,
                () -> service.createRefill(request("5.00")));

        assertEquals(new BigDecimal("20.00"), slot.getCurrentVolumeMl());
        assertNothingWritten();
    }

    @Test
    void unknownSlotIsNotFound() {
        loginAs(TENANT_ID, "INVENTORY_STAFF");
        when(lockRepository.lockById(SLOT_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.createRefill(request("5.00")));
        assertNothingWritten();
    }

    // ---------------------------------------------------------------
    // Status updates
    // ---------------------------------------------------------------

    @Test
    void refillFromEmptyBelowThresholdBecomesLowStock() {
        loginAs(TENANT_ID, "INVENTORY_STAFF");
        MachineSlot slot = lockedSlot("50.00", "0.00", "10.00", MachineSlotStatus.EMPTY);
        stubSaves();

        var response = service.createRefill(request("5.00"));

        assertEquals(MachineSlotStatus.LOW_STOCK, slot.getStatus());
        assertEquals(MachineSlotStatus.LOW_STOCK, response.slotStatus());
    }

    @Test
    void refillAboveThresholdBecomesAvailable() {
        loginAs(TENANT_ID, "INVENTORY_STAFF");
        MachineSlot slot = lockedSlot("50.00", "5.00", "10.00", MachineSlotStatus.LOW_STOCK);
        stubSaves();

        service.createRefill(request("30.00"));

        assertEquals(MachineSlotStatus.AVAILABLE, slot.getStatus());
    }

    @Test
    void refillDoesNotOverwriteErrorStatus() {
        loginAs(TENANT_ID, "INVENTORY_STAFF");
        MachineSlot slot = lockedSlot("50.00", "5.00", "10.00", MachineSlotStatus.ERROR);
        stubSaves();

        service.createRefill(request("30.00"));

        assertEquals(new BigDecimal("35.00"), slot.getCurrentVolumeMl());
        assertEquals(MachineSlotStatus.ERROR, slot.getStatus());
    }

    // ---------------------------------------------------------------
    // Tenant isolation and RBAC
    // ---------------------------------------------------------------

    @Test
    void otherTenantCannotRefillAndGetsNotFound() {
        loginAs(99L, "INVENTORY_STAFF");
        MachineSlot slot = lockedSlot("50.00", "20.00", "10.00", MachineSlotStatus.AVAILABLE);

        assertThrows(ResourceNotFoundException.class,
                () -> service.createRefill(request("5.00")));

        assertEquals(new BigDecimal("20.00"), slot.getCurrentVolumeMl());
        assertNothingWritten();
    }

    @Test
    void tenantLessNonPlatformUserCannotRefill() {
        loginAs(null, "INVENTORY_STAFF");
        lockedSlot("50.00", "20.00", "10.00", MachineSlotStatus.AVAILABLE);

        assertThrows(ResourceNotFoundException.class,
                () -> service.createRefill(request("5.00")));
        assertNothingWritten();
    }

    @Test
    void platformAdminCanRefillAnyTenantsSlot() {
        loginAs(null, "PLATFORM_SUPER_ADMIN");
        MachineSlot slot = lockedSlot("50.00", "20.00", "10.00", MachineSlotStatus.AVAILABLE);
        stubSaves();

        service.createRefill(request("5.00"));

        assertEquals(new BigDecimal("25.00"), slot.getCurrentVolumeMl());
    }

    @ParameterizedTest
    @ValueSource(strings = {"OPERATIONS_MANAGER", "TECHNICIAN", "PUBLIC_KIOSK"})
    void rolesWithoutRefillPermissionAreDenied(String role) {
        loginAs(TENANT_ID, role);

        assertThrows(AccessDeniedException.class,
                () -> service.createRefill(request("5.00")));

        verifyNoInteractions(lockRepository);
        assertNothingWritten();
    }

    @ParameterizedTest
    @ValueSource(strings = {"PLATFORM_SUPER_ADMIN", "BRAND_ADMIN", "INVENTORY_STAFF"})
    void refillRolesAreAccepted(String role) {
        loginAs(role.equals("PLATFORM_SUPER_ADMIN") ? null : TENANT_ID, role);
        lockedSlot("50.00", "20.00", "10.00", MachineSlotStatus.AVAILABLE);
        stubSaves();

        assertEquals(300L, service.createRefill(request("5.00")).id());
    }

    // ---------------------------------------------------------------
    // Refill history
    // ---------------------------------------------------------------

    @Test
    void getRefillReturnsRefillOfOwnTenant() {
        loginAs(TENANT_ID, "INVENTORY_STAFF");
        Refill refill = refillOf(slot("50.00", "20.00", "10.00", MachineSlotStatus.AVAILABLE));
        when(refillRepository.findById(300L)).thenReturn(Optional.of(refill));

        var response = service.getRefill(300L);

        assertEquals(300L, response.id());
        assertEquals(new BigDecimal("5.00"), response.volumeAddedMl());
        assertNull(response.slotStatus());
    }

    @Test
    void getRefillOfOtherTenantIsNotFound() {
        loginAs(99L, "BRAND_ADMIN");
        Refill refill = refillOf(slot("50.00", "20.00", "10.00", MachineSlotStatus.AVAILABLE));
        when(refillRepository.findById(300L)).thenReturn(Optional.of(refill));

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> service.getRefill(300L));
        assertEquals("Refill not found with ID: 300", ex.getMessage());
    }

    @Test
    void getRefillsBySlotChecksTenantAndReturnsHistory() {
        loginAs(TENANT_ID, "INVENTORY_STAFF");
        MachineSlot slot = slot("50.00", "20.00", "10.00", MachineSlotStatus.AVAILABLE);
        when(lockRepository.findDetailedById(SLOT_ID)).thenReturn(Optional.of(slot));
        when(refillRepository.findBySlot_IdOrderByRefilledAtDescIdDesc(
                any(Long.class), any(Pageable.class))).thenReturn(List.of(refillOf(slot)));

        assertEquals(1, service.getRefillsBySlot(SLOT_ID, null).size());
        assertThrows(IllegalArgumentException.class,
                () -> service.getRefillsBySlot(null, null));
        assertThrows(IllegalArgumentException.class,
                () -> service.getRefillsBySlot(SLOT_ID, 0));
    }

    @Test
    void getRefillsBySlotOfOtherTenantIsNotFound() {
        loginAs(99L, "BRAND_ADMIN");
        when(lockRepository.findDetailedById(SLOT_ID)).thenReturn(Optional.of(
                slot("50.00", "20.00", "10.00", MachineSlotStatus.AVAILABLE)));

        assertThrows(ResourceNotFoundException.class,
                () -> service.getRefillsBySlot(SLOT_ID, null));
        verifyNoInteractions(refillRepository);
    }

    private Refill refillOf(MachineSlot slot) {
        Refill refill = new Refill();
        ReflectionTestUtils.setField(refill, "id", 300L);
        refill.setSlot(slot);
        refill.setProduct(slot.getProduct());
        refill.setPerformedByUserId(7L);
        refill.setVolumeAddedMl(new BigDecimal("5.00"));
        return refill;
    }
}
