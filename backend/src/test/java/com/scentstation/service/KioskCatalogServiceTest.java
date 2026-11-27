package com.scentstation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.scentstation.dto.response.KioskProductResponse;
import com.scentstation.entity.Machine;
import com.scentstation.entity.MachineSlot;
import com.scentstation.entity.MachineSlotStatus;
import com.scentstation.entity.Product;
import com.scentstation.exception.ResourceConflictException;
import com.scentstation.exception.ResourceNotFoundException;
import com.scentstation.repository.MachineRepository;
import com.scentstation.repository.MachineSlotRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KioskCatalogServiceTest {

    @Mock
    private MachineRepository machineRepository;

    @Mock
    private MachineSlotRepository machineSlotRepository;

    @InjectMocks
    private KioskCatalogService kioskCatalogService;

    @Test
    void getAvailableProducts_returnsOnlyOrderableActiveProducts() {
        Machine machine = createMachine(10L, 1L, "ONLINE", "ACTIVE");

        Product activeProduct = createProduct(100L, 1L, "Ocean Mist", "15000.00", "2.00", "ACTIVE");
        Product inactiveProduct = createProduct(101L, 1L, "Old Scent", "12000.00", "2.00", "INACTIVE");

        MachineSlot validSlot = createSlot(1L, machine, 1, activeProduct, "50.00", MachineSlotStatus.AVAILABLE);
        MachineSlot lowStockValidSlot = createSlot(2L, machine, 2, activeProduct, "2.00", MachineSlotStatus.LOW_STOCK);
        MachineSlot emptySlot = createSlot(3L, machine, 3, activeProduct, "1.00", MachineSlotStatus.AVAILABLE);
        MachineSlot disabledSlot = createSlot(4L, machine, 4, activeProduct, "50.00", MachineSlotStatus.DISABLED);
        MachineSlot inactiveProdSlot = createSlot(5L, machine, 5, inactiveProduct, "50.00", MachineSlotStatus.AVAILABLE);

        when(machineRepository.findById(10L)).thenReturn(Optional.of(machine));
        when(machineSlotRepository.findAllByMachine_IdOrderBySlotNumber(10L))
                .thenReturn(List.of(validSlot, lowStockValidSlot, emptySlot, disabledSlot, inactiveProdSlot));

        List<KioskProductResponse> result = kioskCatalogService.getAvailableProducts(10L);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).slotId());
        assertEquals("Ocean Mist", result.get(0).name());
        assertEquals(new BigDecimal("15000.00"), result.get(0).price());
        assertTrue(result.get(0).available());
        assertEquals(2L, result.get(1).slotId());
    }

    @Test
    void getAvailableProducts_throwsWhenMachineOffline() {
        Machine offlineMachine = createMachine(10L, 1L, "OFFLINE", "ACTIVE");
        when(machineRepository.findById(10L)).thenReturn(Optional.of(offlineMachine));

        assertThrows(ResourceConflictException.class, () -> kioskCatalogService.getAvailableProducts(10L));
    }

    @Test
    void getAvailableProducts_throwsWhenMachineNotFound() {
        when(machineRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> kioskCatalogService.getAvailableProducts(99L));
    }

    private Machine createMachine(Long id, Long tenantId, String conn, String op) {
        Machine machine = new Machine();
        machine.setId(id);
        machine.setTenantId(tenantId);
        machine.setConnectionStatus(conn);
        machine.setOperationalStatus(op);
        return machine;
    }

    private Product createProduct(Long id, Long tenantId, String name, String price, String sprayVol, String status) {
        Product product = new Product();
        setField(product, "id", id);
        product.setTenantId(tenantId);
        product.setName(name);
        product.setPrice(new BigDecimal(price));
        product.setVolumePerSprayMl(new BigDecimal(sprayVol));
        product.setStatus(status);
        return product;
    }

    private MachineSlot createSlot(Long id, Machine machine, int number, Product product, String currentVol, MachineSlotStatus status) {
        MachineSlot slot = new MachineSlot();
        slot.setId(id);
        slot.setMachine(machine);
        slot.setSlotNumber(number);
        slot.setProduct(product);
        slot.setCurrentVolumeMl(new BigDecimal(currentVol));
        slot.setStatus(status);
        return slot;
    }

    private void setField(Object target, String name, Object value) {
        try {
            var field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
