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
import com.scentstation.dto.request.UpdateMachineRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.Set;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
    private CurrentUserPrincipal platformAdmin;
    private CurrentUserPrincipal operationsManagerTenant1;
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
        platformAdmin = new CurrentUserPrincipal(10L, "platformadmin", null, null, Set.of("PLATFORM_SUPER_ADMIN"));
        operationsManagerTenant1 = new CurrentUserPrincipal(11L, "ops_scent", 1L, "SCENT-DEMO", Set.of("OPERATIONS_MANAGER"));

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
        when(machineRepository.saveAndFlush(any(Machine.class))).thenAnswer(inv -> {
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

    @Test
    void updateConnectionStatus_ByTechnician_ToOnline_Throws403() {
        when(currentUserService.getCurrentUser())
                .thenReturn(technicianTenant1);

        Machine machine = new Machine();
        machine.setId(1L);
        machine.setTenantId(1L);
        machine.setConnectionStatus("OFFLINE");
        machine.setOperationalStatus("ACTIVE");

        when(machineRepository.findWithLocationByIdAndTenantId(1L, 1L))
                .thenReturn(Optional.of(machine));

        assertThrows(AccessDeniedException.class,
                () -> machineService.updateConnectionStatus(1L, "ONLINE"));

        assertEquals("OFFLINE", machine.getConnectionStatus());
        assertNull(machine.getLastPingAt());

        verify(machineRepository, never()).save(any(Machine.class));
    }

    @Test
    void updateOperationalStatus_ByTechnician_ToDisabled_Throws403() {
        when(currentUserService.getCurrentUser())
                .thenReturn(technicianTenant1);

        Machine machine = new Machine();
        machine.setId(1L);
        machine.setTenantId(1L);
        machine.setOperationalStatus("ACTIVE");

        when(machineRepository.findWithLocationByIdAndTenantId(1L, 1L))
                .thenReturn(Optional.of(machine));

        assertThrows(AccessDeniedException.class,
                () -> machineService.updateOperationalStatus(1L, "DISABLED"));

        assertEquals("ACTIVE", machine.getOperationalStatus());

        verify(machineRepository, never()).save(any(Machine.class));
    }

    @Test
    void updateOperationalStatus_ByTechnician_ReenableDisabled_Throws403() {
        when(currentUserService.getCurrentUser())
                .thenReturn(technicianTenant1);

        Machine machine = new Machine();
        machine.setId(1L);
        machine.setTenantId(1L);
        machine.setOperationalStatus("DISABLED");

        when(machineRepository.findWithLocationByIdAndTenantId(1L, 1L))
                .thenReturn(Optional.of(machine));

        assertThrows(AccessDeniedException.class,
                () -> machineService.updateOperationalStatus(1L, "ACTIVE"));

        assertEquals("DISABLED", machine.getOperationalStatus());

        verify(machineRepository, never()).save(any(Machine.class));
    }

    @Test
    void updateOperationalStatus_ByBrandAdmin_ReenableDisabled_Success() {
        when(currentUserService.getCurrentUser())
                .thenReturn(brandAdminTenant1);

        Machine machine = new Machine();
        machine.setId(1L);
        machine.setTenantId(1L);
        machine.setOperationalStatus("DISABLED");
        machine.setConnectionStatus("OFFLINE");

        when(machineRepository.findWithLocationByIdAndTenantId(1L, 1L))
                .thenReturn(Optional.of(machine));

        when(machineRepository.save(any(Machine.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        MachineResponse res =
                machineService.updateOperationalStatus(1L, "ACTIVE");

        assertEquals("ACTIVE", res.operationalStatus());
        assertEquals("OFFLINE", res.connectionStatus());

        verify(machineRepository).save(machine);
    }

    @Test
    void updateConnectionStatus_ByBrandAdmin_Throws403() {
        when(currentUserService.getCurrentUser())
                .thenReturn(brandAdminTenant1);

        assertThrows(AccessDeniedException.class,
                () -> machineService.updateConnectionStatus(1L, "OFFLINE"));

        verifyNoInteractions(machineRepository);
    }

    @Test
    void updateOperationalStatus_OtherTenantMachine_Throws404() {
        when(currentUserService.getCurrentUser())
                .thenReturn(brandAdminTenant1);

        when(machineRepository.findWithLocationByIdAndTenantId(99L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> machineService.updateOperationalStatus(
                        99L, "MAINTENANCE"));

        verify(machineRepository, never()).save(any(Machine.class));
    }

    @Test
    void deleteMachine_OtherTenantMachine_Throws404() {
        when(currentUserService.getCurrentUser())
                .thenReturn(brandAdminTenant1);

        when(machineRepository.findWithLocationByIdAndTenantId(99L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> machineService.delete(99L));

        verify(machineRepository, never()).save(any(Machine.class));
        verify(machineRepository, never()).delete(any(Machine.class));
        verify(machineRepository, never()).deleteById(anyLong());
    }

    @Test
    void updateOperationalStatus_InvalidStatus_ThrowsException() {
        when(currentUserService.getCurrentUser())
                .thenReturn(brandAdminTenant1);

        assertThrows(IllegalArgumentException.class,
                () -> machineService.updateOperationalStatus(1L, "INVALID"));

        verifyNoInteractions(machineRepository);
    }
    // Trùng serialNumber
    @Test
    void createMachine_WithDuplicateSerialNumber_Throws409() {
        when(currentUserService.getCurrentUser())
                .thenReturn(brandAdminTenant1);

        when(tenantRepository.findById(1L))
                .thenReturn(Optional.of(activeTenant1));

        when(machineRepository.existsBySerialNumber("SN-001"))
                .thenReturn(true);

        CreateMachineRequest req = new CreateMachineRequest(
                null,
                "SCENT-M100",
                "SN-001",
                "DEV-100",
                1L,
                "1.0.0",
                null,
                null
        );

        assertThrows(ResourceConflictException.class,
                () -> machineService.create(req));

        verify(machineRepository, never())
                .save(any(Machine.class));
    }
    // Trùng deviceIdentity
    @Test
    void createMachine_WithDuplicateDeviceIdentity_Throws409() {
        when(currentUserService.getCurrentUser())
                .thenReturn(brandAdminTenant1);

        when(tenantRepository.findById(1L))
                .thenReturn(Optional.of(activeTenant1));

        when(machineRepository.existsByDeviceIdentity("DEV-001"))
                .thenReturn(true);

        CreateMachineRequest req = new CreateMachineRequest(
                null,
                "SCENT-M100",
                "SN-100",
                "DEV-001",
                1L,
                "1.0.0",
                null,
                null
        );

        assertThrows(ResourceConflictException.class,
                () -> machineService.create(req));

        verify(machineRepository, never())
            .save(any(Machine.class));
    }
    // Location đã bị vô hiệu hóa
    @Test
    void createMachine_WithInactiveLocation_Throws409() {
        when(currentUserService.getCurrentUser())
                .thenReturn(brandAdminTenant1);

        when(tenantRepository.findById(1L))
                .thenReturn(Optional.of(activeTenant1));

        locationTenant1.setStatus("INACTIVE");

        when(locationRepository.findByIdAndTenantId(1L, 1L))
                .thenReturn(Optional.of(locationTenant1));

        CreateMachineRequest req = new CreateMachineRequest(
            null,
            "SCENT-M100",
            "SN-100",
            "DEV-100",
            1L,
            "1.0.0",
            null,
            null
        );

        assertThrows(ResourceConflictException.class,
                () -> machineService.create(req));

        verify(machineRepository, never())
                .save(any(Machine.class));
    }
    // Brand Admin tạo máy thuộc Tenant khác
    @Test
    void createMachine_ByBrandAdmin_ForOtherTenant_Throws403() {
        when(currentUserService.getCurrentUser())
            .thenReturn(brandAdminTenant1);

        CreateMachineRequest req = new CreateMachineRequest(
            2L,
            "SCENT-M100",
            "SN-100",
            "DEV-100",
            1L,
            "1.0.0",
            null,
            null
    );

    assertThrows(AccessDeniedException.class,
            () -> machineService.create(req));

    verify(machineRepository, never())
            .save(any(Machine.class));
    }
    // Update Machine thuộc Tenant khác
    @Test
    void updateMachine_OtherTenantMachine_Throws404() {
        when(currentUserService.getCurrentUser())
                .thenReturn(brandAdminTenant1);

        when(machineRepository.findWithLocationByIdAndTenantId(99L, 1L))
                .thenReturn(Optional.empty());

        UpdateMachineRequest req = new UpdateMachineRequest(
            null,
            null,
            null,
            null,
            "2.0.0"
        );

        assertThrows(ResourceNotFoundException.class,
                () -> machineService.update(99L, req));

        verify(machineRepository, never())
                .save(any(Machine.class));
    }
    // Update Machine sang Location khác Tenant
    @Test
    void updateMachine_WithOtherTenantLocation_Throws404() {
        when(currentUserService.getCurrentUser())
                .thenReturn(brandAdminTenant1);

        Machine machine = new Machine();
        machine.setId(1L);
        machine.setTenantId(1L);
        machine.setLocation(locationTenant1);
        machine.setOperationalStatus("ACTIVE");
        machine.setConnectionStatus("OFFLINE");

        when(machineRepository.findWithLocationByIdAndTenantId(1L, 1L))
                .thenReturn(Optional.of(machine));

        when(locationRepository.findByIdAndTenantId(6L, 1L))
                .thenReturn(Optional.empty());

        UpdateMachineRequest req = new UpdateMachineRequest(
            null,
            null,
            null,
            6L,
            null
        );

        assertThrows(ResourceNotFoundException.class,
                () -> machineService.update(1L, req));

        assertEquals(1L, machine.getLocation().getId());

        verify(machineRepository, never())
                .save(any(Machine.class));
    }

    @Test
    void getAll_ByBrandAdmin_OnlySearchOwnTenant() {
        when(currentUserService.getCurrentUser())
                .thenReturn(brandAdminTenant1);

        Machine machine = new Machine();
        machine.setId(1L);
        machine.setTenantId(1L);
        machine.setMachineCode("SCENT-M001");

        when(machineRepository.search(1L, null, null, null))
                .thenReturn(List.of(machine));

        List<MachineResponse> result =
                machineService.getAll(null, null, null, null);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).tenantId());

        verify(machineRepository)
                .search(1L, null, null, null);
        }

    @Test
    void getAll_ByPlatformAdmin_SearchAllTenants() {
        when(currentUserService.getCurrentUser())
                .thenReturn(platformAdmin);

        Machine machine1 = new Machine();
        machine1.setId(1L);
        machine1.setTenantId(1L);

        Machine machine2 = new Machine();
        machine2.setId(2L);
        machine2.setTenantId(2L);

        when(machineRepository.search(null, null, null, null))
                .thenReturn(List.of(machine1, machine2));

        List<MachineResponse> result =
                machineService.getAll(null, null, null, null);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).tenantId());
        assertEquals(2L, result.get(1).tenantId());

        verify(machineRepository)
                .search(null, null, null, null);
        }

    @Test
    void getAll_RequestOtherTenant_Throws404() {
        when(currentUserService.getCurrentUser())
                .thenReturn(brandAdminTenant1);

        assertThrows(
                ResourceNotFoundException.class,
                () -> machineService.getAll(null, null, null, 2L)
        );

        verifyNoInteractions(machineRepository);
        }

    @Test
    void getById_OtherTenantMachine_Throws404() {
        when(currentUserService.getCurrentUser())
                .thenReturn(brandAdminTenant1);

        when(machineRepository.findWithLocationByIdAndTenantId(7L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(
                 ResourceNotFoundException.class,
                () -> machineService.getById(7L)
        );

        verify(machineRepository, never())
                .findWithLocationById(anyLong());
        }

    // Filter Machine
    @Test
    void getAll_FilterByConnectionStatus() {
        when(currentUserService.getCurrentUser())
                .thenReturn(brandAdminTenant1);

        Machine machine = new Machine();
        machine.setId(8L);
        machine.setTenantId(1L);
        machine.setConnectionStatus("OFFLINE");

        when(machineRepository.search(1L, "OFFLINE", null, null))
                .thenReturn(List.of(machine));

        List<MachineResponse> result =
                machineService.getAll(" offline ", null, null, null);

        assertEquals(1, result.size());
        assertEquals("OFFLINE", result.get(0).connectionStatus());

        verify(machineRepository)
                .search(1L, "OFFLINE", null, null);
        }

    @Test
    void getAll_FilterByOperationalStatus() {
        when(currentUserService.getCurrentUser())
                .thenReturn(brandAdminTenant1);

        Machine machine = new Machine();
        machine.setId(8L);
        machine.setTenantId(1L);
        machine.setOperationalStatus("MAINTENANCE");

        when(machineRepository.search(1L, null, "MAINTENANCE", null))
                .thenReturn(List.of(machine));

        List<MachineResponse> result =
                 machineService.getAll(null, "maintenance", null, null);

        assertEquals(1, result.size());
        assertEquals("MAINTENANCE", result.get(0).operationalStatus());
    }

    @Test
    void getAll_FilterByLocationId() {
        when(currentUserService.getCurrentUser())
                .thenReturn(brandAdminTenant1);

        Machine machine = new Machine();
        machine.setId(8L);
        machine.setTenantId(1L);
        machine.setLocation(locationTenant1);

        when(machineRepository.search(1L, null, null, 1L))
                .thenReturn(List.of(machine));

        List<MachineResponse> result =
                machineService.getAll(null, null, 1L, null);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).locationId());
    }

    @Test
    void getAll_InvalidConnectionStatus_ThrowsException() {
        when(currentUserService.getCurrentUser())
                .thenReturn(brandAdminTenant1);

        assertThrows(
                IllegalArgumentException.class,
                () -> machineService.getAll("INVALID", null, null, null)
        );

        verifyNoInteractions(machineRepository);
    }
    // Update Machine
    @Test
    void updateMachine_ValidRequest_Success() {
        when(currentUserService.getCurrentUser())
                .thenReturn(brandAdminTenant1);

        Machine machine = new Machine();
        machine.setId(8L);
        machine.setTenantId(1L);
        machine.setLocation(locationTenant1);
                machine.setMachineCode("SCENT-M001");
        machine.setSerialNumber("SN-001");
        machine.setDeviceIdentity("DEV-001");
        machine.setConnectionStatus("OFFLINE");
        machine.setOperationalStatus("ACTIVE");
        machine.setFirmwareVersion("1.0.0");

        when(machineRepository.findWithLocationByIdAndTenantId(8L, 1L))
                .thenReturn(Optional.of(machine));

        when(machineRepository.saveAndFlush(any(Machine.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        UpdateMachineRequest request = new UpdateMachineRequest(
            "SCENT-M001-UPDATED",
            null,
            null,
            null,
            "2.0.0"
        );

        MachineResponse result = machineService.update(8L, request);

        assertEquals("SCENT-M001-UPDATED", result.machineCode());
        assertEquals("2.0.0", result.firmwareVersion());
        assertEquals("OFFLINE", result.connectionStatus());
        assertEquals("ACTIVE", result.operationalStatus());
        assertEquals(1L, result.tenantId());
        assertEquals(1L, result.locationId());

        verify(machineRepository).saveAndFlush(machine);
    }

    @Test
    void updateMachine_DuplicateMachineCode_Throws409() {
        when(currentUserService.getCurrentUser())
                .thenReturn(brandAdminTenant1);

        Machine machine = new Machine();
        machine.setId(8L);
        machine.setTenantId(1L);
        machine.setMachineCode("SCENT-M001");

        when(machineRepository.findWithLocationByIdAndTenantId(8L, 1L))
            .thenReturn(Optional.of(machine));

        when(machineRepository.existsByMachineCodeAndIdNot("SCENT-M002", 8L))
            .thenReturn(true);

        UpdateMachineRequest request = new UpdateMachineRequest(
            "SCENT-M002", null, null, null, null
        );

        assertThrows(
            ResourceConflictException.class,
            () -> machineService.update(8L, request)
        );

        assertEquals("SCENT-M001", machine.getMachineCode());

        verify(machineRepository, never())
            .saveAndFlush(any(Machine.class));
    }

    @Test
    void updateMachine_DuplicateSerialNumber_Throws409() {
        when(currentUserService.getCurrentUser())
            .thenReturn(brandAdminTenant1);

        Machine machine = new Machine();
        machine.setId(8L);
        machine.setTenantId(1L);
        machine.setSerialNumber("SN-001");

        when(machineRepository.findWithLocationByIdAndTenantId(8L, 1L))
            .thenReturn(Optional.of(machine));

        when(machineRepository.existsBySerialNumberAndIdNot("SN-002", 8L))
            .thenReturn(true);

        UpdateMachineRequest request = new UpdateMachineRequest(
            null, "SN-002", null, null, null
        );

        assertThrows(
            ResourceConflictException.class,
            () -> machineService.update(8L, request)
        );

        assertEquals("SN-001", machine.getSerialNumber());

        verify(machineRepository, never())
            .saveAndFlush(any(Machine.class));
    }

    @Test
    void updateMachine_DuplicateDeviceIdentity_Throws409() {
        when(currentUserService.getCurrentUser())
            .thenReturn(brandAdminTenant1);

        Machine machine = new Machine();
        machine.setId(8L);
        machine.setTenantId(1L);
        machine.setDeviceIdentity("DEV-001");

        when(machineRepository.findWithLocationByIdAndTenantId(8L, 1L))
            .thenReturn(Optional.of(machine));

        when(machineRepository.existsByDeviceIdentityAndIdNot("DEV-002", 8L))
            .thenReturn(true);

        UpdateMachineRequest request = new UpdateMachineRequest(
            null, null, "DEV-002", null, null
        );

        assertThrows(
            ResourceConflictException.class,
            () -> machineService.update(8L, request)
        );

        assertEquals("DEV-001", machine.getDeviceIdentity());

        verify(machineRepository, never())
            .saveAndFlush(any(Machine.class));
    }

    // Connection Status
    @Test
    void updateConnectionStatus_InvalidStatus_ThrowsException() {
        when(currentUserService.getCurrentUser())
            .thenReturn(technicianTenant1);

        assertThrows(
            IllegalArgumentException.class,
            () -> machineService.updateConnectionStatus(8L, "INVALID")
        );

        verifyNoInteractions(machineRepository);
        }

        @Test
        void updateConnectionStatus_OtherTenantMachine_Throws404() {
        when(currentUserService.getCurrentUser())
            .thenReturn(technicianTenant1);

        when(machineRepository.findWithLocationByIdAndTenantId(7L, 1L))
            .thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> machineService.updateConnectionStatus(7L, "OFFLINE")
        );

        verify(machineRepository, never())
            .save(any(Machine.class));
        }

    @Test
    void updateConnectionStatus_ByOperationsManager_Throws403() {
        when(currentUserService.getCurrentUser())
                .thenReturn(operationsManagerTenant1);

        assertThrows(
                AccessDeniedException.class,
                () -> machineService.updateConnectionStatus(8L, "OFFLINE")
        );

        verifyNoInteractions(machineRepository);
        }

    @Test
    void updateConnectionStatus_ByInventoryStaff_Throws403() {
        when(currentUserService.getCurrentUser())
                .thenReturn(inventoryStaffTenant1);

        assertThrows(
                AccessDeniedException.class,
                () -> machineService.updateConnectionStatus(8L, "OFFLINE")
        );

        verifyNoInteractions(machineRepository);
    }

    @Test
    void updateConnectionStatus_ByPlatformAdmin_ToOffline_Success() {
        when(currentUserService.getCurrentUser())
            .thenReturn(platformAdmin);

        Machine machine = new Machine();
        machine.setId(8L);
        machine.setTenantId(1L);
        machine.setLocation(locationTenant1);
        machine.setConnectionStatus("ONLINE");
        machine.setOperationalStatus("ACTIVE");

        when(machineRepository.findWithLocationById(8L))
            .thenReturn(Optional.of(machine));

        when(machineRepository.save(any(Machine.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        MachineResponse result =
            machineService.updateConnectionStatus(8L, "OFFLINE");

        assertEquals("OFFLINE", result.connectionStatus());
        assertEquals("ACTIVE", result.operationalStatus());
        assertEquals(1L, result.tenantId());
        assertEquals(1L, result.locationId());

        verify(machineRepository).save(machine);
    }

    @Test
    void updateConnectionStatus_ByPlatformAdmin_ToOnline_Throws403() {
        when(currentUserService.getCurrentUser())
            .thenReturn(platformAdmin);

        Machine machine = new Machine();
        machine.setId(8L);
        machine.setTenantId(1L);
        machine.setConnectionStatus("OFFLINE");
        machine.setOperationalStatus("ACTIVE");

        when(machineRepository.findWithLocationById(8L))
            .thenReturn(Optional.of(machine));

        assertThrows(
            AccessDeniedException.class,
            () -> machineService.updateConnectionStatus(8L, "ONLINE")
        );

        assertEquals("OFFLINE", machine.getConnectionStatus());
        assertNull(machine.getLastPingAt());

        verify(machineRepository, never())
            .save(any(Machine.class));
    }
}
