
package com.scentstation.controller;

import com.scentstation.config.SecurityConfig;
import com.scentstation.dto.request.CreateMachineSlotRequest;
import com.scentstation.dto.request.UpdateMachineSlotRequest;
import com.scentstation.exception.ResourceNotFoundException;
import com.scentstation.exception.ResourceConflictException;
import com.scentstation.repository.UserRepository;
import com.scentstation.service.JwtService;
import com.scentstation.service.MachineSlotService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MachineSlotController.class)
@Import(SecurityConfig.class)
class MachineSlotControllerSecurityTest {

    private static final String MACHINE_SLOTS_URL =
            "/api/v1/machines/20/slots";

    private static final String SLOT_URL =
            "/api/v1/machine-slots/40";

    private static final String SLOT_STATUS_URL =
            "/api/v1/machine-slots/40/status";

    private static final String CREATE_BODY = """
            {
                "slotNumber": 1,
                "productId": 30,
                "capacityMl": 50.00,
                "currentVolumeMl": 0.00,
                "lowStockThresholdMl": 10.00
            }
            """;

    private static final String UPDATE_BODY = """
            {
                "slotNumber": 1,
                "productId": 30,
                "capacityMl": 60.00,
                "lowStockThresholdMl": 10.00
            }
            """;

    private static final String STATUS_BODY = """
            {
                "status": "ERROR"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MachineSlotService machineSlotService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    // =====================================================
    // 1. GET LIST - RBAC
    // =====================================================

    @ParameterizedTest
    @ValueSource(strings = {
            "PLATFORM_SUPER_ADMIN",
            "BRAND_ADMIN",
            "OPERATIONS_MANAGER",
            "TECHNICIAN",
            "INVENTORY_STAFF"
    })
    void authorizedRolesCanListSlots(String role) throws Exception {

        mockMvc.perform(get(MACHINE_SLOTS_URL)
                        .with(user("test-user").roles(role)))
                .andExpect(status().isOk());

        verify(machineSlotService).getSlotsByMachine(20L);
    }

    // =====================================================
    // 2. GET DETAIL - RBAC
    // =====================================================

    @ParameterizedTest
    @ValueSource(strings = {
            "PLATFORM_SUPER_ADMIN",
            "BRAND_ADMIN",
            "OPERATIONS_MANAGER",
            "TECHNICIAN",
            "INVENTORY_STAFF"
    })
    void authorizedRolesCanGetSlotDetail(String role) throws Exception {

        mockMvc.perform(get(SLOT_URL)
                        .with(user("test-user").roles(role)))
                .andExpect(status().isOk());

        verify(machineSlotService).getSlot(40L);
    }

    // =====================================================
    // 3. CREATE SLOT - RBAC
    // =====================================================

    @ParameterizedTest
    @CsvSource({
            "PLATFORM_SUPER_ADMIN, 201",
            "BRAND_ADMIN, 201",
            "OPERATIONS_MANAGER, 201",
            "TECHNICIAN, 403",
            "INVENTORY_STAFF, 201"
    })
    void createSlotEnforcesRolePermissions(
            String role,
            int expectedStatus
    ) throws Exception {

        mockMvc.perform(post(MACHINE_SLOTS_URL)
                        .with(user("test-user").roles(role))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_BODY))
                .andExpect(status().is(expectedStatus));

        if (expectedStatus == 201) {
            verify(machineSlotService).createSlot(
                    eq(20L),
                    any(CreateMachineSlotRequest.class)
            );
        } else {
            verifyNoInteractions(machineSlotService);
        }
    }

    // =====================================================
    // 4. UPDATE SLOT CONFIGURATION - RBAC
    // =====================================================

    @ParameterizedTest
    @CsvSource({
            "PLATFORM_SUPER_ADMIN, 200",
            "BRAND_ADMIN, 200",
            "OPERATIONS_MANAGER, 200",
            "TECHNICIAN, 403",
            "INVENTORY_STAFF, 200"
    })
    void updateSlotEnforcesRolePermissions(
            String role,
            int expectedStatus
    ) throws Exception {

        mockMvc.perform(put(SLOT_URL)
                        .with(user("test-user").roles(role))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_BODY))
                .andExpect(status().is(expectedStatus));

        if (expectedStatus == 200) {
            verify(machineSlotService).updateSlot(
                    eq(40L),
                    any(UpdateMachineSlotRequest.class)
            );
        } else {
            verifyNoInteractions(machineSlotService);
        }
    }

    // =====================================================
    // 5. UPDATE SLOT STATUS - RBAC
    // =====================================================

    @ParameterizedTest
    @CsvSource({
            "PLATFORM_SUPER_ADMIN, 200",
            "BRAND_ADMIN, 200",
            "OPERATIONS_MANAGER, 200",
            "TECHNICIAN, 200",
            "INVENTORY_STAFF, 403"
    })
    void updateSlotStatusEnforcesRolePermissions(
            String role,
            int expectedStatus
    ) throws Exception {

        mockMvc.perform(patch(SLOT_STATUS_URL)
                        .with(user("test-user").roles(role))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(STATUS_BODY))
                .andExpect(status().is(expectedStatus));

        if (expectedStatus == 200) {
            verify(machineSlotService)
                    .updateSlotStatus(40L, "ERROR");
        } else {
            verifyNoInteractions(machineSlotService);
        }
    }

    // =====================================================
    // 6. DISABLE SLOT - RBAC
    // =====================================================

    @ParameterizedTest
    @CsvSource({
            "PLATFORM_SUPER_ADMIN, 200",
            "BRAND_ADMIN, 200",
            "OPERATIONS_MANAGER, 200",
            "TECHNICIAN, 403",
            "INVENTORY_STAFF, 403"
    })
    void disableSlotEnforcesRolePermissions(
            String role,
            int expectedStatus
    ) throws Exception {

        mockMvc.perform(delete(SLOT_URL)
                        .with(user("test-user").roles(role)))
                .andExpect(status().is(expectedStatus));

        if (expectedStatus == 200) {
            verify(machineSlotService).disableSlot(40L);
        } else {
            verifyNoInteractions(machineSlotService);
        }
    }

    // =====================================================
    // 7. UNAUTHENTICATED REQUESTS - HTTP 401
    // =====================================================

    @Test
    void unauthenticatedUserCannotListSlots() throws Exception {

        mockMvc.perform(get(MACHINE_SLOTS_URL))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(machineSlotService);
    }

    @Test
    void unauthenticatedUserCannotGetSlotDetail() throws Exception {

        mockMvc.perform(get(SLOT_URL))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(machineSlotService);
    }

    @Test
    void unauthenticatedUserCannotCreateSlot() throws Exception {

        mockMvc.perform(post(MACHINE_SLOTS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_BODY))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(machineSlotService);
    }

    @Test
    void unauthenticatedUserCannotUpdateSlot() throws Exception {

        mockMvc.perform(put(SLOT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_BODY))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(machineSlotService);
    }

    @Test
    void unauthenticatedUserCannotUpdateSlotStatus() throws Exception {

        mockMvc.perform(patch(SLOT_STATUS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(STATUS_BODY))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(machineSlotService);
    }

    @Test
    void unauthenticatedUserCannotDisableSlot() throws Exception {

        mockMvc.perform(delete(SLOT_URL))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(machineSlotService);
    }

    // =====================================================
    // 8. TENANT ISOLATION - HTTP 404
    // =====================================================

    @Test
    void inaccessibleMachineReturnsNotFoundWhenListingSlots()
            throws Exception {

        when(machineSlotService.getSlotsByMachine(20L))
                .thenThrow(new ResourceNotFoundException(
                        "Machine not found"
                ));

        mockMvc.perform(get(MACHINE_SLOTS_URL)
                        .with(user("brand-admin").roles("BRAND_ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void inaccessibleSlotReturnsNotFoundWhenReadingDetail()
            throws Exception {

        when(machineSlotService.getSlot(40L))
                .thenThrow(new ResourceNotFoundException(
                        "Machine slot not found"
                ));

        mockMvc.perform(get(SLOT_URL)
                        .with(user("brand-admin").roles("BRAND_ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void inaccessibleMachineReturnsNotFoundWhenCreatingSlot()
            throws Exception {

        when(machineSlotService.createSlot(
                eq(20L),
                any(CreateMachineSlotRequest.class)
        )).thenThrow(new ResourceNotFoundException(
                "Machine not found"
        ));

        mockMvc.perform(post(MACHINE_SLOTS_URL)
                        .with(user("brand-admin").roles("BRAND_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_BODY))
                .andExpect(status().isNotFound());
    }

    @Test
    void inaccessibleSlotReturnsNotFoundWhenUpdatingConfiguration()
            throws Exception {

        when(machineSlotService.updateSlot(
                eq(40L),
                any(UpdateMachineSlotRequest.class)
        )).thenThrow(new ResourceNotFoundException(
                "Machine slot not found"
        ));

        mockMvc.perform(put(SLOT_URL)
                        .with(user("brand-admin").roles("BRAND_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_BODY))
                .andExpect(status().isNotFound());
    }

    @Test
    void inaccessibleSlotReturnsNotFoundWhenUpdatingStatus()
            throws Exception {

        when(machineSlotService.updateSlotStatus(40L, "ERROR"))
                .thenThrow(new ResourceNotFoundException(
                        "Machine slot not found"
                ));

        mockMvc.perform(patch(SLOT_STATUS_URL)
                        .with(user("technician").roles("TECHNICIAN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(STATUS_BODY))
                .andExpect(status().isNotFound());
    }

    @Test
    void inaccessibleSlotReturnsNotFoundWhenDisabling()
            throws Exception {

        when(machineSlotService.disableSlot(40L))
                .thenThrow(new ResourceNotFoundException(
                        "Machine slot not found"
                ));

        mockMvc.perform(delete(SLOT_URL)
                        .with(user("brand-admin").roles("BRAND_ADMIN")))
                .andExpect(status().isNotFound());
    }

    // =====================================================
    // 9. BUSINESS RULES - HTTP 409
    // =====================================================

    @Test
    void duplicateSlotNumberReturnsConflict() throws Exception {

        when(machineSlotService.createSlot(
                eq(20L),
                any(CreateMachineSlotRequest.class)
        )).thenThrow(new ResourceConflictException(
                "Slot number already exists"
        ));

        mockMvc.perform(post(MACHINE_SLOTS_URL)
                        .with(user("brand-admin").roles("BRAND_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_BODY))
                .andExpect(status().isConflict());
    }

    @Test
    void disablingSlotWithRemainingInventoryReturnsConflict()
            throws Exception {

        when(machineSlotService.disableSlot(40L))
                .thenThrow(new ResourceConflictException(
                        "Slot still contains inventory"
                ));

        mockMvc.perform(delete(SLOT_URL)
                        .with(user("brand-admin").roles("BRAND_ADMIN")))
                .andExpect(status().isConflict());
    }

    @Test
    void updatingDisabledSlotReturnsConflict() throws Exception {

        when(machineSlotService.updateSlot(
                eq(40L),
                any(UpdateMachineSlotRequest.class)
        )).thenThrow(new ResourceConflictException(
                "Machine slot is disabled"
        ));

        mockMvc.perform(put(SLOT_URL)
                        .with(user("brand-admin").roles("BRAND_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_BODY))
                .andExpect(status().isConflict());
    }

    // =====================================================
    // 10. BUSINESS VALIDATION - HTTP 400
    // =====================================================

    @Test
    void invalidSlotStatusReturnsBadRequest() throws Exception {

        when(machineSlotService.updateSlotStatus(40L, "INVALID"))
                .thenThrow(new IllegalArgumentException(
                        "Invalid slot status"
                ));

        mockMvc.perform(patch(SLOT_STATUS_URL)
                        .with(user("technician").roles("TECHNICIAN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "status": "INVALID"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidCapacityReturnsBadRequest() throws Exception {

        mockMvc.perform(post(MACHINE_SLOTS_URL)
                        .with(user("brand-admin").roles("BRAND_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "slotNumber": 1,
                                    "capacityMl": 0,
                                    "currentVolumeMl": 0,
                                    "lowStockThresholdMl": 0
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(machineSlotService);
    }

    // =====================================================
    // 11. SERVICE ACCESS DENIED - HTTP 403
    // =====================================================

    @Test
    void serviceAccessDeniedReturnsForbidden() throws Exception {

        when(machineSlotService.updateSlotStatus(40L, "ERROR"))
                .thenThrow(new AccessDeniedException(
                        "Access denied"
                ));

        mockMvc.perform(patch(SLOT_STATUS_URL)
                        .with(user("technician").roles("TECHNICIAN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(STATUS_BODY))
                .andExpect(status().isForbidden());
    }
}
