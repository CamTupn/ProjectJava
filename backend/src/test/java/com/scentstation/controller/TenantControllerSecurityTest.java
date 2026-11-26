package com.scentstation.controller;

import com.scentstation.config.SecurityConfig;
import com.scentstation.dto.request.CreateTenantRequest;
import com.scentstation.dto.request.UpdateTenantRequest;
import com.scentstation.dto.response.TenantResponse;
import com.scentstation.exception.ResourceConflictException;
import com.scentstation.exception.ResourceNotFoundException;
import com.scentstation.repository.UserRepository;
import com.scentstation.service.JwtService;
import com.scentstation.service.TenantService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

import static org.springframework.security.test.web.servlet
        .request.SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet
        .request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet
        .result.MockMvcResultMatchers.*;

@WebMvcTest(TenantController.class)
@Import(SecurityConfig.class)
class TenantControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TenantService tenantService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private static final String TENANTS_URL =
            "/api/v1/tenants";

    private static final String VALID_BODY = """
            {
                "name": "New Brand",
                "code": "BRAND-NEW"
            }
            """;

    private final LocalDateTime now =
            LocalDateTime.of(2026, 11, 27, 10, 0);

    // =========================================
    // CREATE TENANT
    // =========================================

    @Test
    void superAdminShouldCreateTenant() throws Exception {

        TenantResponse response = new TenantResponse(
                100L,
                "New Brand",
                "BRAND-NEW",
                "ACTIVE",
                now,
                now
        );

        when(tenantService.createTenant(
                any(CreateTenantRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                post(TENANTS_URL)
                        .with(user("admin")
                                .roles("PLATFORM_SUPER_ADMIN"))
                        .contentType("application/json")
                        .content(VALID_BODY)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(100))
        .andExpect(jsonPath("$.name").value("New Brand"))
        .andExpect(jsonPath("$.code").value("BRAND-NEW"))
        .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(tenantService)
                .createTenant(any(CreateTenantRequest.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "BRAND_ADMIN",
            "OPERATIONS_MANAGER",
            "TECHNICIAN",
            "INVENTORY_STAFF"
    })
    void nonSuperAdminShouldNotCreateTenant(String role)
            throws Exception {

        mockMvc.perform(
                post(TENANTS_URL)
                        .with(user("test_user").roles(role))
                        .contentType("application/json")
                        .content(VALID_BODY)
        )
        .andExpect(status().isForbidden());

        verifyNoInteractions(tenantService);
    }

    @Test
    void unauthenticatedUserShouldNotCreateTenant()
            throws Exception {

        mockMvc.perform(
                post(TENANTS_URL)
                        .contentType("application/json")
                        .content(VALID_BODY)
        )
        .andExpect(status().isUnauthorized());

        verifyNoInteractions(tenantService);
    }

    @Test
    void createTenantShouldRejectDuplicateCode()
            throws Exception {

        when(tenantService.createTenant(
                any(CreateTenantRequest.class)
        )).thenThrow(new ResourceConflictException(
                "Tenant code already exists: BRAND-NEW"
        ));

        mockMvc.perform(
                post(TENANTS_URL)
                        .with(user("admin")
                                .roles("PLATFORM_SUPER_ADMIN"))
                        .contentType("application/json")
                        .content(VALID_BODY)
        )
        .andExpect(status().isConflict());

        verify(tenantService)
                .createTenant(any(CreateTenantRequest.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",
            "{\"name\":\"\",\"code\":\"BRAND-A\"}",
            "{\"name\":\"Brand A\",\"code\":\"\"}",
            "{\"name\":\"Brand A\",\"code\":\"INVALID CODE\"}",
            "{\"name\":\"Brand A\",\"code\":\"BRAND@A\"}"
    })
    void createTenantShouldRejectInvalidRequest(String body)
            throws Exception {

        mockMvc.perform(
                post(TENANTS_URL)
                        .with(user("admin")
                                .roles("PLATFORM_SUPER_ADMIN"))
                        .contentType("application/json")
                        .content(body)
        )
        .andExpect(status().isBadRequest());

        verifyNoInteractions(tenantService);
    }

    // =========================================
    // GET TENANTS
    // =========================================

    @ParameterizedTest
    @ValueSource(strings = {
            "PLATFORM_SUPER_ADMIN",
            "BRAND_ADMIN"
    })
    void adminShouldAccessTenantList(String role)
            throws Exception {

        when(tenantService.getAllTenants())
                .thenReturn(List.of());

        mockMvc.perform(
                get(TENANTS_URL)
                        .with(user("admin").roles(role))
        )
        .andExpect(status().isOk());

        verify(tenantService).getAllTenants();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "OPERATIONS_MANAGER",
            "TECHNICIAN",
            "INVENTORY_STAFF"
    })
    void employeeShouldNotAccessTenantList(String role)
            throws Exception {

        mockMvc.perform(
                get(TENANTS_URL)
                        .with(user("employee").roles(role))
        )
        .andExpect(status().isForbidden());

        verifyNoInteractions(tenantService);
    }

    @Test
    void unauthenticatedUserShouldNotAccessTenantList()
            throws Exception {

        mockMvc.perform(get(TENANTS_URL))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(tenantService);
    }

    @Test
    void crossTenantAccessShouldReturn404()
            throws Exception {

        when(tenantService.getTenantById(2L))
                .thenThrow(new ResourceNotFoundException(
                        "Tenant not found with ID: 2"
                ));

        mockMvc.perform(
                get(TENANTS_URL + "/2")
                        .with(user("brandadmin")
                                .roles("BRAND_ADMIN"))
        )
        .andExpect(status().isNotFound());

        verify(tenantService).getTenantById(2L);
    }

    // =========================================
    // UPDATE TENANT
    // =========================================

    @ParameterizedTest
    @ValueSource(strings = {
            "PLATFORM_SUPER_ADMIN",
            "BRAND_ADMIN"
    })
    void adminShouldAccessUpdateTenantEndpoint(String role)
            throws Exception {

        mockMvc.perform(
                put(TENANTS_URL + "/1")
                        .with(user("admin").roles(role))
                        .contentType("application/json")
                        .content(VALID_BODY)
        )
        .andExpect(status().isOk());

        verify(tenantService)
                .updateTenant(
                        eq(1L),
                        any(UpdateTenantRequest.class)
                );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "OPERATIONS_MANAGER",
            "TECHNICIAN",
            "INVENTORY_STAFF"
    })
    void employeeShouldNotUpdateTenant(String role)
            throws Exception {

        mockMvc.perform(
                put(TENANTS_URL + "/1")
                        .with(user("employee").roles(role))
                        .contentType("application/json")
                        .content(VALID_BODY)
        )
        .andExpect(status().isForbidden());

        verifyNoInteractions(tenantService);
    }

    @Test
    void crossTenantUpdateShouldReturn404()
            throws Exception {

        when(tenantService.updateTenant(
                eq(2L),
                any(UpdateTenantRequest.class)
        )).thenThrow(new ResourceNotFoundException(
                "Tenant not found with ID: 2"
        ));

        mockMvc.perform(
                put(TENANTS_URL + "/2")
                        .with(user("brandadmin")
                                .roles("BRAND_ADMIN"))
                        .contentType("application/json")
                        .content(VALID_BODY)
        )
        .andExpect(status().isNotFound());
    }
}