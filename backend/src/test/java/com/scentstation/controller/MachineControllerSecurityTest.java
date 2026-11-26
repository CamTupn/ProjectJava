package com.scentstation.controller;

import com.scentstation.config.SecurityConfig;
import com.scentstation.dto.request.CreateMachineRequest;
import com.scentstation.dto.request.UpdateMachineRequest;
import com.scentstation.exception.ResourceNotFoundException;
import com.scentstation.repository.UserRepository;
import com.scentstation.service.JwtService;
import com.scentstation.service.MachineService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MachineController.class)
@Import(SecurityConfig.class)
class MachineControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MachineService machineService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private static final String BASE_URL = "/api/v1/machines";

    private static final Long MACHINE_ID = 8L;

    private static final String CREATE_BODY = """
            {
                "tenantId": 1,
                "locationId": 4,
                "machineCode": "TEST-019-M03",
                "serialNumber": "TEST-019-SN03",
                "deviceIdentity": "TEST-019-DEVICE03",
                "connectionStatus": "OFFLINE",
                "operationalStatus": "ACTIVE",
                "firmwareVersion": "1.0.0"
            }
            """;

    private static final String UPDATE_BODY = """
            {
                "machineCode": "TEST-019-M01-UPDATED",
                "firmwareVersion": "2.0.0"
            }
            """;

    // =====================================================
    // 1. GET ALL MACHINES
    // =====================================================

    @ParameterizedTest
    @ValueSource(strings = {
            "PLATFORM_SUPER_ADMIN",
            "BRAND_ADMIN",
            "OPERATIONS_MANAGER",
            "TECHNICIAN"
    })
    void authorizedRolesShouldAccessMachineList(String role)
            throws Exception {

        when(machineService.getAll(null, null, null, null))
                .thenReturn(List.of());

        mockMvc.perform(
                get(BASE_URL)
                        .with(user("testuser").roles(role))
        )
                .andExpect(status().isOk());

        verify(machineService)
                .getAll(null, null, null, null);
    }

    @Test
    void inventoryStaffShouldNotAccessMachineList()
            throws Exception {

        mockMvc.perform(
                get(BASE_URL)
                        .with(user("inventory")
                                .roles("INVENTORY_STAFF"))
        )
                .andExpect(status().isForbidden());

        verifyNoInteractions(machineService);
    }

    // =====================================================
    // 2. GET MACHINE DETAIL
    // =====================================================

    @ParameterizedTest
    @ValueSource(strings = {
            "PLATFORM_SUPER_ADMIN",
            "BRAND_ADMIN",
            "OPERATIONS_MANAGER",
            "TECHNICIAN"
    })
    void authorizedRolesShouldAccessMachineDetail(String role)
            throws Exception {

        mockMvc.perform(
                get(BASE_URL + "/{id}", MACHINE_ID)
                        .with(user("testuser").roles(role))
        )
                .andExpect(status().isOk());

        verify(machineService).getById(MACHINE_ID);
    }

    @Test
    void inventoryStaffShouldNotAccessMachineDetail()
            throws Exception {

        mockMvc.perform(
                get(BASE_URL + "/{id}", MACHINE_ID)
                        .with(user("inventory")
                                .roles("INVENTORY_STAFF"))
        )
                .andExpect(status().isForbidden());

        verifyNoInteractions(machineService);
    }

    // =====================================================
    // 3. CREATE MACHINE
    // =====================================================

    @ParameterizedTest
    @ValueSource(strings = {
            "PLATFORM_SUPER_ADMIN",
            "BRAND_ADMIN"
    })
    void adminRolesShouldCreateMachine(String role)
            throws Exception {

        mockMvc.perform(
                post(BASE_URL)
                        .with(user("testuser").roles(role))
                        .with(csrf())
                        .contentType("application/json")
                        .content(CREATE_BODY)
        )
                .andExpect(status().isCreated());

        verify(machineService)
                .create(any(CreateMachineRequest.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "OPERATIONS_MANAGER",
            "TECHNICIAN",
            "INVENTORY_STAFF"
    })
    void nonAdminRolesShouldNotCreateMachine(String role)
            throws Exception {

        mockMvc.perform(
                post(BASE_URL)
                        .with(user("testuser").roles(role))
                        .with(csrf())
                        .contentType("application/json")
                        .content(CREATE_BODY)
        )
                .andExpect(status().isForbidden());

        verifyNoInteractions(machineService);
    }

    // =====================================================
    // 4. UPDATE MACHINE
    // =====================================================

    @ParameterizedTest
    @ValueSource(strings = {
            "PLATFORM_SUPER_ADMIN",
            "BRAND_ADMIN"
    })
    void adminRolesShouldUpdateMachine(String role)
            throws Exception {

        mockMvc.perform(
                put(BASE_URL + "/{id}", MACHINE_ID)
                        .with(user("testuser").roles(role))
                        .with(csrf())
                        .contentType("application/json")
                        .content(UPDATE_BODY)
        )
                .andExpect(status().isOk());

        verify(machineService)
                .update(
                        eq(MACHINE_ID),
                        any(UpdateMachineRequest.class)
                );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "OPERATIONS_MANAGER",
            "TECHNICIAN",
            "INVENTORY_STAFF"
    })
    void nonAdminRolesShouldNotUpdateMachine(String role)
            throws Exception {

        mockMvc.perform(
                put(BASE_URL + "/{id}", MACHINE_ID)
                        .with(user("testuser").roles(role))
                        .with(csrf())
                        .contentType("application/json")
                        .content(UPDATE_BODY)
        )
                .andExpect(status().isForbidden());

        verifyNoInteractions(machineService);
    }

    // =====================================================
    // 5. DELETE MACHINE
    // =====================================================

    @ParameterizedTest
    @ValueSource(strings = {
            "PLATFORM_SUPER_ADMIN",
            "BRAND_ADMIN"
    })
    void adminRolesShouldDisableMachine(String role)
            throws Exception {

        mockMvc.perform(
                delete(BASE_URL + "/{id}", MACHINE_ID)
                        .with(user("testuser").roles(role))
                        .with(csrf())
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("Machine disabled successfully"));

        verify(machineService).delete(MACHINE_ID);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "OPERATIONS_MANAGER",
            "TECHNICIAN",
            "INVENTORY_STAFF"
    })
    void nonAdminRolesShouldNotDeleteMachine(String role)
            throws Exception {

        mockMvc.perform(
                delete(BASE_URL + "/{id}", MACHINE_ID)
                        .with(user("testuser").roles(role))
                        .with(csrf())
        )
                .andExpect(status().isForbidden());

        verifyNoInteractions(machineService);
    }

    // =====================================================
    // 6. UPDATE OPERATIONAL STATUS
    // =====================================================

    @ParameterizedTest
    @ValueSource(strings = {
            "PLATFORM_SUPER_ADMIN",
            "BRAND_ADMIN",
            "OPERATIONS_MANAGER",
            "TECHNICIAN"
    })
    void authorizedRolesShouldUpdateOperationalStatus(
            String role
    ) throws Exception {

        mockMvc.perform(
                patch(BASE_URL + "/{id}/status", MACHINE_ID)
                        .with(user("testuser").roles(role))
                        .with(csrf())
                        .param("status", "MAINTENANCE")
        )
                .andExpect(status().isOk());

        verify(machineService)
                .updateOperationalStatus(
                        MACHINE_ID,
                        "MAINTENANCE"
                );
    }

    @Test
    void inventoryStaffShouldNotUpdateOperationalStatus()
            throws Exception {

        mockMvc.perform(
                patch(BASE_URL + "/{id}/status", MACHINE_ID)
                        .with(user("inventory")
                                .roles("INVENTORY_STAFF"))
                        .with(csrf())
                        .param("status", "MAINTENANCE")
        )
                .andExpect(status().isForbidden());

        verifyNoInteractions(machineService);
    }

    // =====================================================
    // 7. UPDATE CONNECTION STATUS
    // =====================================================

    @ParameterizedTest
    @ValueSource(strings = {
            "PLATFORM_SUPER_ADMIN",
            "TECHNICIAN"
    })
    void authorizedRolesShouldUpdateConnectionStatus(
            String role
    ) throws Exception {

        mockMvc.perform(
                patch(
                        BASE_URL + "/{id}/connection-status",
                        MACHINE_ID
                )
                        .with(user("testuser").roles(role))
                        .with(csrf())
                        .param("status", "OFFLINE")
        )
                .andExpect(status().isOk());

        verify(machineService)
                .updateConnectionStatus(
                        MACHINE_ID,
                        "OFFLINE"
                );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "BRAND_ADMIN",
            "OPERATIONS_MANAGER",
            "INVENTORY_STAFF"
    })
    void unauthorizedRolesShouldNotUpdateConnectionStatus(
            String role
    ) throws Exception {

        mockMvc.perform(
                patch(
                        BASE_URL + "/{id}/connection-status",
                        MACHINE_ID
                )
                        .with(user("testuser").roles(role))
                        .with(csrf())
                        .param("status", "OFFLINE")
        )
                .andExpect(status().isForbidden());

        verifyNoInteractions(machineService);
    }

    // =====================================================
    // 8. SERVICE-LEVEL SECURITY EXCEPTION MAPPING
    // =====================================================

    @ParameterizedTest
    @ValueSource(strings = {
            "PLATFORM_SUPER_ADMIN",
            "TECHNICIAN"
    })
    void manualOnlineStatusShouldReturn403(String role)
            throws Exception {

        when(machineService.updateConnectionStatus(
                MACHINE_ID, "ONLINE"
        ))
                .thenThrow(new AccessDeniedException(
                        "ONLINE status must be confirmed by the device"
                ));

        mockMvc.perform(
                patch(
                        BASE_URL + "/{id}/connection-status",
                        MACHINE_ID
                )
                        .with(user("testuser").roles(role))
                        .with(csrf())
                        .param("status", "ONLINE")
        )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(machineService)
                .updateConnectionStatus(MACHINE_ID, "ONLINE");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "OPERATIONS_MANAGER",
            "TECHNICIAN"
    })
    void nonAdminShouldNotSetDisabledStatus(String role)
            throws Exception {

        when(machineService.updateOperationalStatus(
                MACHINE_ID, "DISABLED"
        ))
                .thenThrow(new AccessDeniedException(
                        "Only administrators can disable machines"
                ));

        mockMvc.perform(
                patch(BASE_URL + "/{id}/status", MACHINE_ID)
                        .with(user("testuser").roles(role))
                        .with(csrf())
                        .param("status", "DISABLED")
        )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));

        verify(machineService)
                .updateOperationalStatus(
                        MACHINE_ID,
                        "DISABLED"
                );
    }

    @Test
    void inaccessibleMachineShouldReturn404()
            throws Exception {

        when(machineService.getById(7L))
                .thenThrow(new ResourceNotFoundException(
                        "Machine not found with ID: 7"
                ));

        mockMvc.perform(
                get(BASE_URL + "/7")
                        .with(user("brandadmin")
                                .roles("BRAND_ADMIN"))
        )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message")
                        .value("Machine not found with ID: 7"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(machineService).getById(7L);
    }

    @Test
    void inaccessibleMachineConnectionUpdateShouldReturn404()
            throws Exception {

        when(machineService.updateConnectionStatus(
                7L, "OFFLINE"
        ))
                .thenThrow(new ResourceNotFoundException(
                        "Machine not found with ID: 7"
                ));

        mockMvc.perform(
                patch(BASE_URL + "/7/connection-status")
                        .with(user("technician")
                                .roles("TECHNICIAN"))
                        .with(csrf())
                        .param("status", "OFFLINE")
        )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(machineService)
                .updateConnectionStatus(7L, "OFFLINE");
    }

    // =====================================================
    // 9. UNAUTHENTICATED REQUESTS
    // =====================================================

    @Test
    void unauthenticatedUserShouldNotAccessMachineList()
            throws Exception {

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(machineService);
    }

    @Test
    void unauthenticatedUserShouldNotAccessMachineDetail()
            throws Exception {

        mockMvc.perform(get(BASE_URL + "/8"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(machineService);
    }

    @Test
    void unauthenticatedUserShouldNotCreateMachine()
            throws Exception {

        mockMvc.perform(
                post(BASE_URL)
                        .with(csrf())
                        .contentType("application/json")
                        .content(CREATE_BODY)
        )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(machineService);
    }

    @Test
    void unauthenticatedUserShouldNotUpdateMachine()
            throws Exception {

        mockMvc.perform(
                put(BASE_URL + "/8")
                        .with(csrf())
                        .contentType("application/json")
                        .content(UPDATE_BODY)
        )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(machineService);
    }

    @Test
    void unauthenticatedUserShouldNotDeleteMachine()
            throws Exception {

        mockMvc.perform(
                delete(BASE_URL + "/8")
                        .with(csrf())
        )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(machineService);
    }

    @Test
    void unauthenticatedUserShouldNotUpdateOperationalStatus()
            throws Exception {

        mockMvc.perform(
                patch(BASE_URL + "/8/status")
                        .with(csrf())
                        .param("status", "MAINTENANCE")
        )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(machineService);
    }

    @Test
    void unauthenticatedUserShouldNotUpdateConnectionStatus()
            throws Exception {

        mockMvc.perform(
                patch(BASE_URL + "/8/connection-status")
                        .with(csrf())
                        .param("status", "OFFLINE")
        )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(machineService);
    }
}