package com.scentstation.controller;

import com.scentstation.service.JwtService;

import org.junit.jupiter.api.Test;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(AuthorizationTestController.class)
@Import(AuthorizationTestControllerTest.MethodSecurityTestConfig.class)
class AuthorizationTestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    // Enable method-level security for testing @PreAuthorize
    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityTestConfig {
    }

    // BRAND_ADMIN is allowed
    @Test
    @WithMockUser(roles = "BRAND_ADMIN")
    void brandAdminShouldAccessBrandAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/test/authorization/brand-admin"))
                .andExpect(status().isOk())
                .andExpect(content().string("Access granted: BRAND_ADMIN"));
    }

    // TECHNICIAN cannot access BRAND_ADMIN endpoint
    @Test
    @WithMockUser(roles = "TECHNICIAN")
    void technicianShouldNotAccessBrandAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/test/authorization/brand-admin"))
                .andExpect(status().isForbidden());
    }

    // TECHNICIAN is allowed
    @Test
    @WithMockUser(roles = "TECHNICIAN")
    void technicianShouldAccessTechnicianEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/test/authorization/technician"))
                .andExpect(status().isOk())
                .andExpect(content().string("Access granted: TECHNICIAN"));
    }

    // BRAND_ADMIN cannot access TECHNICIAN endpoint
    @Test
    @WithMockUser(roles = "BRAND_ADMIN")
    void brandAdminShouldNotAccessTechnicianEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/test/authorization/technician"))
                .andExpect(status().isForbidden());
    }

    // PLATFORM_SUPER_ADMIN is allowed
    @Test
    @WithMockUser(roles = "PLATFORM_SUPER_ADMIN")
    void platformAdminShouldAccessPlatformAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/test/authorization/platform-admin"))
                .andExpect(status().isOk())
                .andExpect(content().string("Access granted: PLATFORM_SUPER_ADMIN"));
    }

    // BRAND_ADMIN cannot access PLATFORM_SUPER_ADMIN endpoint
    @Test
    @WithMockUser(roles = "BRAND_ADMIN")
    void brandAdminShouldNotAccessPlatformAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/test/authorization/platform-admin"))
                .andExpect(status().isForbidden());
    }
}
