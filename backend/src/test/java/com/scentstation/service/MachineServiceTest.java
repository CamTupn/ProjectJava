package com.scentstation.service;

import com.scentstation.common.security.CurrentUserPrincipal;
import com.scentstation.common.security.CurrentUserService;
import com.scentstation.dto.request.CreateMachineRequest;
import com.scentstation.dto.response.MachineResponse;
import com.scentstation.entity.Location;
import com.scentstation.entity.Machine;
import com.scentstation.entity.Tenant;
import com.scentstation.exception.ResourceConflictException;
import com.scentstation.exception.ResourceNotFoundException;
import com.scentstation.mapper.MachineMapper;
import com.scentstation.repository.LocationRepository;
import com.scentstation.repository.MachineRepository;
import com.scentstation.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MachineServiceTest {

    @Mock private MachineRepository machineRepository;
    @Mock private LocationRepository locationRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private CurrentUserService currentUserService;

    private MachineService machineService;

    private CurrentUserPrincipal brandAdminTenant1;
    private CurrentUserPrincipal technicianTenant1;
    private CurrentUserPrincipal inventoryStaffTenant1;
    private Tenant activeTenant1;
    private Location locationTenant1;

    @BeforeEach
    void setUp() {
        machineService = new MachineService(
                machineRepository,
                locationRepository,
                tenantRepository,
                new MachineMapper(),
                currentUserService
        );

        brandAdminTenant1 = new CurrentUserPrincipal(1L, "brandadmin", 1L, "SCENT-DEMO", Set.of("BRAND_ADMIN"));
        technicianTenant1 = new CurrentUserPrincipal(4L, "tech_scent", 1L, "SCENT-DEMO", Set.of("TECHNICIAN"));
        inventoryStaffTenant1 = new CurrentUserPrincipal(5L, "stock_scent", 1L, "SCENT-DEMO", Set.of("INVENTORY_STAFF"));

        activeTenant1 = new Tenant();
        activeTenant1.setId(1L);
        activeTenant1.setCode("SCENT-DEMO");
        activeTenant1.setStatus("ACTIVE");

        locationTenant1 = new Location();
        locationTenant1.setId(1L);
        locationTenant1.setTenantId(1L);
        locationTenant1.setName("Scent Demo - District 1");
        locationTenant1.setAddress("123 Nguyen Hue");
        locationTenant1.setStatus("ACTIVE");
    }

    @Test
    void createMachine_ByBrandAdmin_DefaultsOfflineAndActive() {
        when(currentUserService.getCurrentUser()).thenReturn(brandAdminTenant1);
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(activeTenant1));
        when(machineRepository.existsByMachineCode("SCENT-M099")).thenReturn(false);
        when(machineRepository.existsBySerialNumber("SN-099")).thenReturn(false);
        when(machineRepository.existsByDeviceIdentity("DEV-099")).thenReturn(false);
        when(locationRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(locationTenant1));
        when(machineRepository.save(any(Machine.class))).thenAnswer(inv -> {
            Machine m = inv.getArgument(0);
            m.setId(99L);
            return m;
        });

        CreateMachineRequest req = new CreateMachineRequest(
                null, "SCENT-M099", "SN-099", "DEV-099", 1L, "1.0.0", null, null
        );

        MachineResponse res = machineService.create(req);

        assertEquals(99L, res.id());
        assertEquals(1L, res.tenantId());
        assertEquals("OFFLINE", res.connectionStatus());
        assertEquals("ACTIVE", res.operationalStatus());
        assertEquals(1L, res.locationId());
    }

    @Test
    void createMachine_WithDuplicateMachineCode_Throws409() {
        when(currentUserService.getCurrentUser()).thenReturn(brandAdminTenant1);
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(activeTenant1));
        when(machineRepository.existsByMachineCode("SCENT-M001")).thenReturn(true);

        CreateMachineRequest req = new CreateMachineRequest(
                null, "SCENT-M001", "SN-NEW", "DEV-NEW", 1L, "1.0.0", null, null
        );

        assertThrows(ResourceConflictException.class, () -> machineService.create(req));
    }

    @Test
    void createMachine_WithOtherTenantLocation_Throws404() {
        when(currentUserService.getCurrentUser()).thenReturn(brandAdminTenant1);
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(activeTenant1));
        when(machineRepository.existsByMachineCode("SCENT-M099")).thenReturn(false);
        when(machineRepository.existsBySerialNumber("SN-099")).thenReturn(false);
        when(machineRepository.existsByDeviceIdentity("DEV-099")).thenReturn(false);
        when(locationRepository.findByIdAndTenantId(3L, 1L)).thenReturn(Optional.empty());

        CreateMachineRequest req = new CreateMachineRequest(
                null, "SCENT-M099", "SN-099", "DEV-099", 3L, "1.0.0", null, null
        );

        assertThrows(ResourceNotFoundException.class, () -> machineService.create(req));
    }

    @Test
    void updateOperationalStatus_DoesNotModifyConnectionStatus() {
        when(currentUserService.getCurrentUser()).thenReturn(brandAdminTenant1);

        Machine machine = new Machine();
        machine.setId(1L);
        machine.setTenantId(1L);
        machine.setMachineCode("SCENT-M001");
        machine.setConnectionStatus("ONLINE");
        machine.setOperationalStatus("ACTIVE");

        when(machineRepository.findWithLocationByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(machine));
        when(machineRepository.save(any(Machine.class))).thenAnswer(inv -> inv.getArgument(0));

        MachineResponse res = machineService.updateOperationalStatus(1L, "MAINTENANCE");

        assertEquals("MAINTENANCE", res.operationalStatus());
        assertEquals("ONLINE", res.connectionStatus());
    }

    @Test
    void updateConnectionStatus_ByTechnician_UpdatesConnectionOnly() {
        when(currentUserService.getCurrentUser()).thenReturn(technicianTenant1);

        Machine machine = new Machine();
        machine.setId(1L);
        machine.setTenantId(1L);
        machine.setConnectionStatus("ONLINE");
        machine.setOperationalStatus("ACTIVE");

        when(machineRepository.findWithLocationByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(machine));
        when(machineRepository.save(any(Machine.class))).thenAnswer(inv -> inv.getArgument(0));

        MachineResponse res = machineService.updateConnectionStatus(1L, "OFFLINE");

        assertEquals("OFFLINE", res.connectionStatus());
        assertEquals("ACTIVE", res.operationalStatus());
    }

    @Test
    void softDelete_SetsOperationalStatusDisabled() {
        when(currentUserService.getCurrentUser()).thenReturn(brandAdminTenant1);

        Machine machine = new Machine();
        machine.setId(1L);
        machine.setTenantId(1L);
        machine.setOperationalStatus("ACTIVE");

        when(machineRepository.findWithLocationByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(machine));

        machineService.delete(1L);

        assertEquals("DISABLED", machine.getOperationalStatus());
        verify(machineRepository).save(machine);
        verify(machineRepository, never()).delete(any());
    }

    @Test
    void inventoryStaff_CannotAccessMachines() {
        when(currentUserService.getCurrentUser()).thenReturn(inventoryStaffTenant1);

        assertThrows(AccessDeniedException.class,
                () -> machineService.getAll(null, null, null, null));
    }
}
