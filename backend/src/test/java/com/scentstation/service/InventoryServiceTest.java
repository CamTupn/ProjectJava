package com.scentstation.service;

import static com.scentstation.service.InventoryTestData.MACHINE_ID;
import static com.scentstation.service.InventoryTestData.SLOT_ID;
import static com.scentstation.service.InventoryTestData.TENANT_ID;
import static com.scentstation.service.InventoryTestData.execution;
import static com.scentstation.service.InventoryTestData.slot;
import static com.scentstation.service.InventoryTestData.user;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.scentstation.common.security.CurrentUserService;
import com.scentstation.contract.InventoryChanged;
import com.scentstation.contract.InventoryChanged.ChangeType;
import com.scentstation.entity.InventoryTransaction;
import com.scentstation.entity.MachineSlot;
import com.scentstation.entity.MachineSlotStatus;
import com.scentstation.exception.ResourceConflictException;
import com.scentstation.exception.ResourceNotFoundException;
import com.scentstation.mapper.InventoryMapper;
import com.scentstation.repository.InventoryLockRepository;
import com.scentstation.repository.InventoryTransactionRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryLockRepository lockRepository;

    @Mock
    private InventoryTransactionRepository transactionRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private InventoryService service;

    @BeforeEach
    void setUp() {
        service = new InventoryService(
                lockRepository,
                transactionRepository,
                new InventoryMapper(),
                currentUserService,
                eventPublisher
        );
    }

    private void loginAs(Long tenantId, String... roles) {
        when(currentUserService.getCurrentUser())
                .thenReturn(user(tenantId, roles));
    }

    private void stubSaveAssignsId() {
        when(transactionRepository.saveAndFlush(any(InventoryTransaction.class)))
                .thenAnswer(invocation -> {
                    InventoryTransaction tx = invocation.getArgument(0);
                    ReflectionTestUtils.setField(tx, "id", 500L);
                    return tx;
                });
    }

    // ---------------------------------------------------------------
    // View inventory
    // ---------------------------------------------------------------

    @Test
    void getInventoryIsScopedToTheCallersTenant() {
        loginAs(TENANT_ID, "INVENTORY_STAFF");
        MachineSlot slot = slot("50.00", "20.00", "10.00", MachineSlotStatus.AVAILABLE);
        when(lockRepository.findInventory(TENANT_ID, MACHINE_ID))
                .thenReturn(List.of(slot));

        var result = service.getInventory(MACHINE_ID);

        assertEquals(1, result.size());
        assertEquals(SLOT_ID, result.get(0).slotId());
        assertEquals("VM-001", result.get(0).machineCode());
        assertEquals(new BigDecimal("20.00"), result.get(0).currentVolumeMl());
        assertEquals(new BigDecimal("30.00"), result.get(0).remainingCapacityMl());
        assertEquals(MachineSlotStatus.AVAILABLE, result.get(0).status());
        verify(lockRepository).findInventory(TENANT_ID, MACHINE_ID);
    }

    @Test
    void getInventoryForPlatformAdminHasNoTenantFilter() {
        loginAs(null, "PLATFORM_SUPER_ADMIN");
        when(lockRepository.findInventory(null, null)).thenReturn(List.of());

        assertTrue(service.getInventory(null).isEmpty());
        verify(lockRepository).findInventory(null, null);
    }

    @Test
    void getInventoryRejectsTenantLessNonPlatformUser() {
        loginAs(null, "BRAND_ADMIN");

        assertThrows(ResourceNotFoundException.class, () -> service.getInventory(null));
        verifyNoInteractions(lockRepository);
    }

    @Test
    void getSlotInventoryReturnsSlot() {
        loginAs(TENANT_ID, "BRAND_ADMIN");
        when(lockRepository.findDetailedById(SLOT_ID)).thenReturn(Optional.of(
                slot("50.00", "5.00", "10.00", MachineSlotStatus.LOW_STOCK)));

        var response = service.getSlotInventory(SLOT_ID);

        assertEquals(MachineSlotStatus.LOW_STOCK, response.status());
        assertEquals("Test Fragrance", response.productName());
    }

    @Test
    void getTransactionsReturnsHistoryNewestFirstWithLimit() {
        loginAs(TENANT_ID, "INVENTORY_STAFF");
        when(lockRepository.findDetailedById(SLOT_ID)).thenReturn(Optional.of(
                slot("50.00", "5.00", "10.00", MachineSlotStatus.LOW_STOCK)));
        InventoryTransaction tx = new InventoryTransaction();
        tx.setSlot(slot("50.00", "5.00", "10.00", MachineSlotStatus.LOW_STOCK));
        tx.setTransactionType("REFILL");
        tx.setVolumeChangeMl(new BigDecimal("10.00"));
        when(transactionRepository.findBySlot_IdOrderByCreatedAtDescIdDesc(
                anyLong(), any(Pageable.class))).thenReturn(List.of(tx));

        var result = service.getTransactions(SLOT_ID, 25);

        assertEquals(1, result.size());
        assertEquals("REFILL", result.get(0).transactionType());
        ArgumentCaptor<Pageable> page = ArgumentCaptor.forClass(Pageable.class);
        verify(transactionRepository)
                .findBySlot_IdOrderByCreatedAtDescIdDesc(anyLong(), page.capture());
        assertEquals(25, page.getValue().getPageSize());
    }

    @Test
    void getTransactionsRejectsInvalidLimit() {
        loginAs(TENANT_ID, "INVENTORY_STAFF");

        assertThrows(IllegalArgumentException.class,
                () -> service.getTransactions(SLOT_ID, 0));
        assertThrows(IllegalArgumentException.class,
                () -> service.getTransactions(SLOT_ID, 201));
    }

    // ---------------------------------------------------------------
    // Tenant isolation and RBAC on reads
    // ---------------------------------------------------------------

    @Test
    void otherTenantCannotSeeSlotInventoryOrTransactions() {
        loginAs(99L, "BRAND_ADMIN");
        when(lockRepository.findDetailedById(SLOT_ID)).thenReturn(Optional.of(
                slot("50.00", "5.00", "10.00", MachineSlotStatus.LOW_STOCK)));

        assertThrows(ResourceNotFoundException.class,
                () -> service.getSlotInventory(SLOT_ID));
        assertThrows(ResourceNotFoundException.class,
                () -> service.getTransactions(SLOT_ID, null));
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void platformAdminCanReadAnyTenantsSlot() {
        loginAs(null, "PLATFORM_SUPER_ADMIN");
        when(lockRepository.findDetailedById(SLOT_ID)).thenReturn(Optional.of(
                slot("50.00", "5.00", "10.00", MachineSlotStatus.LOW_STOCK)));

        assertEquals(SLOT_ID, service.getSlotInventory(SLOT_ID).slotId());
    }

    @Test
    void rolesOutsideTheReadSetAreDenied() {
        loginAs(TENANT_ID, "PUBLIC_KIOSK");

        assertThrows(AccessDeniedException.class, () -> service.getInventory(null));
        assertThrows(AccessDeniedException.class, () -> service.getSlotInventory(SLOT_ID));
        assertThrows(AccessDeniedException.class,
                () -> service.getTransactions(SLOT_ID, null));
        verifyNoInteractions(lockRepository, transactionRepository);
    }

    // ---------------------------------------------------------------
    // Deduction on SUCCESS execution
    // ---------------------------------------------------------------

    @Test
    void successfulDispenseDeductsStockAndRecordsTransaction() {
        MachineSlot slot = slot("50.00", "40.00", "10.00", MachineSlotStatus.AVAILABLE);
        when(lockRepository.findExecutionView(100L)).thenReturn(Optional.of(
                execution(100L, "SUCCESS", "CUSTOMER_ORDER", SLOT_ID, MACHINE_ID, 77L, "5.00")));
        when(lockRepository.lockById(SLOT_ID)).thenReturn(Optional.of(slot));
        when(lockRepository.lockTransactionIdsByExecution(100L)).thenReturn(List.of());
        stubSaveAssignsId();

        Optional<InventoryChanged> result = service.recordSuccessfulDispense(100L);

        assertTrue(result.isPresent());
        assertEquals(new BigDecimal("35.00"), slot.getCurrentVolumeMl());
        assertEquals(MachineSlotStatus.AVAILABLE, slot.getStatus());

        ArgumentCaptor<InventoryTransaction> tx =
                ArgumentCaptor.forClass(InventoryTransaction.class);
        verify(transactionRepository).saveAndFlush(tx.capture());
        assertEquals("DISPENSE_SALE", tx.getValue().getTransactionType());
        assertEquals(new BigDecimal("-5.00"), tx.getValue().getVolumeChangeMl());
        assertEquals(100L, tx.getValue().getExecutionId());
        assertEquals(77L, tx.getValue().getOrderId());
        assertNull(tx.getValue().getRefillId());
        assertNull(tx.getValue().getPerformedByUserId());

        InventoryChanged event = result.get();
        assertEquals(ChangeType.DISPENSE_SALE, event.changeType());
        assertEquals(new BigDecimal("40.00"), event.previousVolumeMl());
        assertEquals(new BigDecimal("35.00"), event.currentVolumeMl());
        assertEquals(TENANT_ID, event.tenantId());
        assertEquals(500L, event.transactionId());
        assertFalse(event.statusChanged());

        ArgumentCaptor<InventoryChanged> published =
                ArgumentCaptor.forClass(InventoryChanged.class);
        verify(eventPublisher).publishEvent(published.capture());
        assertEquals(100L, published.getValue().executionId());
    }

    @Test
    void diagnosticSprayIsRecordedAsDiagnosticSpray() {
        MachineSlot slot = slot("50.00", "40.00", "10.00", MachineSlotStatus.AVAILABLE);
        when(lockRepository.findExecutionView(101L)).thenReturn(Optional.of(
                execution(101L, "SUCCESS", "DIAGNOSTIC", SLOT_ID, MACHINE_ID, null, "1.00")));
        when(lockRepository.lockById(SLOT_ID)).thenReturn(Optional.of(slot));
        stubSaveAssignsId();

        InventoryChanged event = service.recordSuccessfulDispense(101L).orElseThrow();

        assertEquals(ChangeType.DIAGNOSTIC_SPRAY, event.changeType());
        ArgumentCaptor<InventoryTransaction> tx =
                ArgumentCaptor.forClass(InventoryTransaction.class);
        verify(transactionRepository).saveAndFlush(tx.capture());
        assertEquals("DIAGNOSTIC_SPRAY", tx.getValue().getTransactionType());
        assertNull(tx.getValue().getOrderId());
    }

    @Test
    void duplicateExecutionDoesNotDeductTwice() {
        MachineSlot slot = slot("50.00", "35.00", "10.00", MachineSlotStatus.AVAILABLE);
        when(lockRepository.findExecutionView(100L)).thenReturn(Optional.of(
                execution(100L, "SUCCESS", "CUSTOMER_ORDER", SLOT_ID, MACHINE_ID, 77L, "5.00")));
        when(lockRepository.lockById(SLOT_ID)).thenReturn(Optional.of(slot));
        when(lockRepository.lockTransactionIdsByExecution(100L)).thenReturn(List.of(500L));

        Optional<InventoryChanged> result = service.recordSuccessfulDispense(100L);

        assertTrue(result.isEmpty());
        assertEquals(new BigDecimal("35.00"), slot.getCurrentVolumeMl());
        verify(transactionRepository, never()).saveAndFlush(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void unknownExecutionIsRejected() {
        when(lockRepository.findExecutionView(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.recordSuccessfulDispense(404L));
        assertThrows(IllegalArgumentException.class,
                () -> service.recordSuccessfulDispense(null));
        verify(lockRepository, never()).lockById(anyLong());
    }

    @Test
    void failedOrUncertainExecutionDoesNotChangeStock() {
        for (String status : List.of("FAILED", "UNCERTAIN", "PENDING")) {
            when(lockRepository.findExecutionView(102L)).thenReturn(Optional.of(
                    execution(102L, status, "CUSTOMER_ORDER", SLOT_ID, MACHINE_ID, 77L, "5.00")));

            assertThrows(ResourceConflictException.class,
                    () -> service.recordSuccessfulDispense(102L), status);
        }
        verify(lockRepository, never()).lockById(anyLong());
        verify(transactionRepository, never()).saveAndFlush(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void inconsistentExecutionIsRejected() {
        // non-positive volume, missing volume, unknown command type
        when(lockRepository.findExecutionView(103L)).thenReturn(Optional.of(
                execution(103L, "SUCCESS", "CUSTOMER_ORDER", SLOT_ID, MACHINE_ID, 77L, "0.00")));
        assertThrows(ResourceConflictException.class,
                () -> service.recordSuccessfulDispense(103L));

        when(lockRepository.findExecutionView(104L)).thenReturn(Optional.of(
                execution(104L, "SUCCESS", "CUSTOMER_ORDER", SLOT_ID, MACHINE_ID, 77L, null)));
        assertThrows(ResourceConflictException.class,
                () -> service.recordSuccessfulDispense(104L));

        when(lockRepository.findExecutionView(105L)).thenReturn(Optional.of(
                execution(105L, "SUCCESS", "MAINTENANCE", SLOT_ID, MACHINE_ID, null, "5.00")));
        assertThrows(ResourceConflictException.class,
                () -> service.recordSuccessfulDispense(105L));

        verify(lockRepository, never()).lockById(anyLong());
    }

    @Test
    void executionOfAnotherMachineIsRejected() {
        MachineSlot slot = slot("50.00", "40.00", "10.00", MachineSlotStatus.AVAILABLE);
        when(lockRepository.findExecutionView(106L)).thenReturn(Optional.of(
                execution(106L, "SUCCESS", "CUSTOMER_ORDER", SLOT_ID, 999L, 77L, "5.00")));
        when(lockRepository.lockById(SLOT_ID)).thenReturn(Optional.of(slot));

        assertThrows(ResourceConflictException.class,
                () -> service.recordSuccessfulDispense(106L));
        assertEquals(new BigDecimal("40.00"), slot.getCurrentVolumeMl());
        verify(transactionRepository, never()).saveAndFlush(any());
    }

    @Test
    void stockNeverGoesNegative() {
        MachineSlot slot = slot("50.00", "3.00", "10.00", MachineSlotStatus.LOW_STOCK);
        when(lockRepository.findExecutionView(107L)).thenReturn(Optional.of(
                execution(107L, "SUCCESS", "CUSTOMER_ORDER", SLOT_ID, MACHINE_ID, 77L, "5.00")));
        when(lockRepository.lockById(SLOT_ID)).thenReturn(Optional.of(slot));

        assertThrows(ResourceConflictException.class,
                () -> service.recordSuccessfulDispense(107L));

        assertEquals(new BigDecimal("3.00"), slot.getCurrentVolumeMl());
        assertEquals(MachineSlotStatus.LOW_STOCK, slot.getStatus());
        verify(transactionRepository, never()).saveAndFlush(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void deductionBelowThresholdMarksLowStock() {
        MachineSlot slot = slot("100.00", "25.00", "20.00", MachineSlotStatus.AVAILABLE);
        when(lockRepository.findExecutionView(108L)).thenReturn(Optional.of(
                execution(108L, "SUCCESS", "CUSTOMER_ORDER", SLOT_ID, MACHINE_ID, 77L, "10.00")));
        when(lockRepository.lockById(SLOT_ID)).thenReturn(Optional.of(slot));
        stubSaveAssignsId();

        InventoryChanged event = service.recordSuccessfulDispense(108L).orElseThrow();

        assertEquals(MachineSlotStatus.LOW_STOCK, slot.getStatus());
        assertTrue(event.becameLowStock());
        assertFalse(event.becameEmpty());
        assertEquals(MachineSlotStatus.AVAILABLE, event.previousStatus());
        assertEquals(MachineSlotStatus.LOW_STOCK, event.currentStatus());
    }

    @Test
    void deductionToZeroMarksEmpty() {
        MachineSlot slot = slot("100.00", "5.00", "20.00", MachineSlotStatus.LOW_STOCK);
        when(lockRepository.findExecutionView(109L)).thenReturn(Optional.of(
                execution(109L, "SUCCESS", "CUSTOMER_ORDER", SLOT_ID, MACHINE_ID, 77L, "5.00")));
        when(lockRepository.lockById(SLOT_ID)).thenReturn(Optional.of(slot));
        stubSaveAssignsId();

        InventoryChanged event = service.recordSuccessfulDispense(109L).orElseThrow();

        assertEquals(MachineSlotStatus.EMPTY, slot.getStatus());
        assertEquals(0, slot.getCurrentVolumeMl().signum());
        assertTrue(event.becameEmpty());
    }

    @Test
    void deductionDoesNotOverwriteErrorStatus() {
        MachineSlot slot = slot("100.00", "5.00", "20.00", MachineSlotStatus.ERROR);
        when(lockRepository.findExecutionView(110L)).thenReturn(Optional.of(
                execution(110L, "SUCCESS", "CUSTOMER_ORDER", SLOT_ID, MACHINE_ID, 77L, "5.00")));
        when(lockRepository.lockById(SLOT_ID)).thenReturn(Optional.of(slot));
        stubSaveAssignsId();

        InventoryChanged event = service.recordSuccessfulDispense(110L).orElseThrow();

        assertEquals(MachineSlotStatus.ERROR, slot.getStatus());
        assertEquals(0, slot.getCurrentVolumeMl().signum());
        assertFalse(event.statusChanged());
    }

    // ---------------------------------------------------------------
    // Reconciliation check and status rules
    // ---------------------------------------------------------------

    @Test
    void hasUnreconciledCommandsReflectsRepositoryCount() {
        when(lockRepository.countUnreconciledCommands(SLOT_ID)).thenReturn(2L);
        assertTrue(service.hasUnreconciledCommands(SLOT_ID));

        when(lockRepository.countUnreconciledCommands(SLOT_ID)).thenReturn(0L);
        assertFalse(service.hasUnreconciledCommands(SLOT_ID));
    }

    @Test
    void statusRulesFollowVolumeAndThreshold() {
        BigDecimal threshold = new BigDecimal("10.00");
        assertEquals(MachineSlotStatus.EMPTY, InventoryService.statusAfterChange(
                MachineSlotStatus.AVAILABLE, new BigDecimal("0.00"), threshold));
        assertEquals(MachineSlotStatus.LOW_STOCK, InventoryService.statusAfterChange(
                MachineSlotStatus.AVAILABLE, new BigDecimal("10.00"), threshold));
        assertEquals(MachineSlotStatus.AVAILABLE, InventoryService.statusAfterChange(
                MachineSlotStatus.EMPTY, new BigDecimal("10.01"), threshold));
        assertEquals(MachineSlotStatus.ERROR, InventoryService.statusAfterChange(
                MachineSlotStatus.ERROR, new BigDecimal("0.00"), threshold));
        assertEquals(MachineSlotStatus.DISABLED, InventoryService.statusAfterChange(
                MachineSlotStatus.DISABLED, new BigDecimal("50.00"), threshold));
    }
}
