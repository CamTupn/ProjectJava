package com.scentstation.service;

import com.scentstation.common.security.CurrentUserPrincipal;
import com.scentstation.entity.Machine;
import com.scentstation.entity.MachineSlot;
import com.scentstation.entity.MachineSlotStatus;
import com.scentstation.entity.Product;
import com.scentstation.repository.InventoryLockRepository.DispenseExecutionView;
import java.math.BigDecimal;
import java.util.Set;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

/** Builders shared by the inventory and refill service tests. */
final class InventoryTestData {

    static final long TENANT_ID = 10L;
    static final long MACHINE_ID = 20L;
    static final long PRODUCT_ID = 30L;
    static final long SLOT_ID = 40L;

    private InventoryTestData() {}

    static CurrentUserPrincipal user(Long tenantId, String... roles) {
        return new CurrentUserPrincipal(
                7L, "user", tenantId, "TENANT", Set.of(roles)
        );
    }

    static Machine machine(long tenantId) {
        Machine machine = new Machine();
        machine.setId(MACHINE_ID);
        machine.setTenantId(tenantId);
        machine.setMachineCode("VM-001");
        return machine;
    }

    static Product product(long tenantId) {
        Product product = new Product();
        ReflectionTestUtils.setField(product, "id", PRODUCT_ID);
        product.setTenantId(tenantId);
        product.setName("Test Fragrance");
        product.setStatus("ACTIVE");
        return product;
    }

    static MachineSlot slot(
            String capacity,
            String current,
            String threshold,
            MachineSlotStatus status
    ) {
        MachineSlot slot = new MachineSlot();
        slot.setId(SLOT_ID);
        slot.setMachine(machine(TENANT_ID));
        slot.setSlotNumber(1);
        slot.setProduct(product(TENANT_ID));
        slot.setCapacityMl(new BigDecimal(capacity));
        slot.setCurrentVolumeMl(new BigDecimal(current));
        slot.setLowStockThresholdMl(new BigDecimal(threshold));
        slot.setStatus(status);
        return slot;
    }

    static DispenseExecutionView execution(
            long executionId,
            String executionStatus,
            String commandType,
            Long slotId,
            Long machineId,
            Long orderId,
            String volume
    ) {
        DispenseExecutionView view = Mockito.mock(
                DispenseExecutionView.class,
                Mockito.withSettings().lenient()
        );
        Mockito.when(view.getExecutionId()).thenReturn(executionId);
        Mockito.when(view.getExecutionStatus()).thenReturn(executionStatus);
        Mockito.when(view.getCommandId()).thenReturn(900L + executionId);
        Mockito.when(view.getCommandType()).thenReturn(commandType);
        Mockito.when(view.getSlotId()).thenReturn(slotId);
        Mockito.when(view.getMachineId()).thenReturn(machineId);
        Mockito.when(view.getOrderId()).thenReturn(orderId);
        Mockito.when(view.getVolumeMl())
                .thenReturn(volume == null ? null : new BigDecimal(volume));
        return view;
    }
}
