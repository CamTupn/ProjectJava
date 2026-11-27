package com.scentstation.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.scentstation.config.SecurityConfig;
import com.scentstation.repository.UserRepository;
import com.scentstation.service.InventoryService;
import com.scentstation.service.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InventoryController.class)
@Import(SecurityConfig.class)
class InventoryControllerSecurityTest {

    private static final String INVENTORY_URL = "/api/v1/inventory";
    private static final String SLOT_URL = "/api/v1/inventory/slots/40";
    private static final String TRANSACTIONS_URL =
            "/api/v1/inventory/slots/40/transactions";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventoryService inventoryService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @ParameterizedTest
    @CsvSource({
            "PLATFORM_SUPER_ADMIN, 200",
            "BRAND_ADMIN, 200",
            "OPERATIONS_MANAGER, 200",
            "TECHNICIAN, 200",
            "INVENTORY_STAFF, 200",
            "PUBLIC_KIOSK, 403",
            "KIOSK_DEVICE, 403"
    })
    void listInventoryEnforcesRolePermissions(String role, int expectedStatus)
            throws Exception {
        mockMvc.perform(get(INVENTORY_URL).with(user("u").roles(role)))
                .andExpect(status().is(expectedStatus));

        if (expectedStatus == 200) {
            verify(inventoryService).getInventory(null);
        } else {
            verifyNoInteractions(inventoryService);
        }
    }

    @Test
    void listInventoryPassesMachineFilter() throws Exception {
        mockMvc.perform(get(INVENTORY_URL).param("machineId", "20")
                        .with(user("u").roles("INVENTORY_STAFF")))
                .andExpect(status().isOk());

        verify(inventoryService).getInventory(20L);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "PLATFORM_SUPER_ADMIN", "BRAND_ADMIN", "OPERATIONS_MANAGER",
            "TECHNICIAN", "INVENTORY_STAFF"
    })
    void authorizedRolesCanReadSlotInventoryAndTransactions(String role)
            throws Exception {
        mockMvc.perform(get(SLOT_URL).with(user("u").roles(role)))
                .andExpect(status().isOk());
        mockMvc.perform(get(TRANSACTIONS_URL).param("limit", "10")
                        .with(user("u").roles(role)))
                .andExpect(status().isOk());

        verify(inventoryService).getSlotInventory(40L);
        verify(inventoryService).getTransactions(40L, 10);
    }

    @Test
    void unauthorizedRoleCannotReadSlotInventoryOrTransactions() throws Exception {
        mockMvc.perform(get(SLOT_URL).with(user("u").roles("PUBLIC_KIOSK")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(TRANSACTIONS_URL).with(user("u").roles("PUBLIC_KIOSK")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(inventoryService);
    }

    @Test
    void anonymousRequestsAreRejectedWith401() throws Exception {
        mockMvc.perform(get(INVENTORY_URL)).andExpect(status().isUnauthorized());
        mockMvc.perform(get(SLOT_URL)).andExpect(status().isUnauthorized());
        mockMvc.perform(get(TRANSACTIONS_URL)).andExpect(status().isUnauthorized());

        verifyNoInteractions(inventoryService);
    }

    @Test
    void nonNumericPathVariableReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/inventory/slots/abc")
                        .with(user("u").roles("BRAND_ADMIN")))
                .andExpect(status().isBadRequest());
    }
}
