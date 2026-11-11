package com.scentstation.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.scentstation.dto.response.TenantResponse;
import com.scentstation.exception.ResourceNotFoundException;
import com.scentstation.service.TenantService;

@WebMvcTest(TenantController.class)
class TenantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TenantService tenantService;

    private final LocalDateTime now = LocalDateTime.of(
            2026, 11, 6, 10, 0, 0
    );

    // Test GET /api/v1/tenants
    @Test
    void getAllTenants_ShouldReturnTenantList() throws Exception {

        TenantResponse tenant = new TenantResponse(
                1L,
                "ScentStation Demo",
                "SCENT-DEMO",
                "ACTIVE",
                now,
                now
        );

        when(tenantService.getAllTenants())
                .thenReturn(List.of(tenant));

        mockMvc.perform(get("/api/v1/tenants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name")
                        .value("ScentStation Demo"))
                .andExpect(jsonPath("$[0].code")
                        .value("SCENT-DEMO"))
                .andExpect(jsonPath("$[0].status")
                        .value("ACTIVE"));
    }

    // Test GET /api/v1/tenants/{id}
    @Test
    void getTenantById_ShouldReturnTenant() throws Exception {

        TenantResponse tenant = new TenantResponse(
                1L,
                "ScentStation Demo",
                "SCENT-DEMO",
                "ACTIVE",
                now,
                now
        );

        when(tenantService.getTenantById(1L))
                .thenReturn(tenant);

        mockMvc.perform(get("/api/v1/tenants/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name")
                        .value("ScentStation Demo"))
                .andExpect(jsonPath("$.code")
                        .value("SCENT-DEMO"))
                .andExpect(jsonPath("$.status")
                        .value("ACTIVE"));
    }

    // Test 404 - Tenant does not exist
    @Test
    void getTenantById_WhenNotFound_ShouldReturn404() throws Exception {

        when(tenantService.getTenantById(999L))
                .thenThrow(new ResourceNotFoundException(
                        "Tenant not found with ID: 999"
                ));

        mockMvc.perform(get("/api/v1/tenants/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message")
                        .value("Tenant not found with ID: 999"));
    }

    // Test 400 - Invalid ID type
    @Test
    void getTenantById_WhenIdIsInvalid_ShouldReturn400() throws Exception {

        mockMvc.perform(get("/api/v1/tenants/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message")
                        .value("Invalid value for parameter: id"))
                .andExpect(jsonPath("$.errors.id")
                        .value("Expected type: Long"));
    }
}
