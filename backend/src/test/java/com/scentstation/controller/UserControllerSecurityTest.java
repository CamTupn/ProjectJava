package com.scentstation.controller;

import com.scentstation.config.SecurityConfig;
import com.scentstation.repository.UserRepository;
import com.scentstation.service.JwtService;
import com.scentstation.service.UserService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    // =====================================================
    // 1. PLATFORM SUPER ADMIN
    // =====================================================

    @Test
    void superAdminShouldAccessUserList() throws Exception {

        when(userService.getAllUsers())
                .thenReturn(List.of());

        mockMvc.perform(
                get("/api/v1/users")
                    .with(user("platformadmin")
                        .roles("PLATFORM_SUPER_ADMIN"))
        )
        .andExpect(status().isOk());

        verify(userService).getAllUsers();
    }

    // =====================================================
    // 2. BRAND ADMIN
    // =====================================================

    @Test
    void brandAdminShouldAccessUserList() throws Exception {

        when(userService.getAllUsers())
                .thenReturn(List.of());

        mockMvc.perform(
                get("/api/v1/users")
                    .with(user("brandadmin")
                        .roles("BRAND_ADMIN"))
        )
        .andExpect(status().isOk());

        verify(userService).getAllUsers();
    }

    // =====================================================
    // 3. OPERATIONS MANAGER
    // =====================================================

    @Test
    void operationsManagerShouldNotAccessUserList()
            throws Exception {

        mockMvc.perform(
                get("/api/v1/users")
                    .with(user("operations")
                        .roles("OPERATIONS_MANAGER"))
        )
        .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    // =====================================================
    // 4. TECHNICIAN
    // =====================================================

    @Test
    void technicianShouldNotAccessUserList()
            throws Exception {

        mockMvc.perform(
                get("/api/v1/users")
                    .with(user("technician")
                        .roles("TECHNICIAN"))
        )
        .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    // =====================================================
    // 5. INVENTORY STAFF
    // =====================================================

    @Test
    void inventoryStaffShouldNotAccessUserList()
            throws Exception {

        mockMvc.perform(
                get("/api/v1/users")
                    .with(user("inventory")
                        .roles("INVENTORY_STAFF"))
        )
        .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    // =====================================================
    // 6. OPERATIONS MANAGER - USER DETAIL
    // =====================================================

    @Test
    void operationsManagerShouldNotAccessUserDetail()
            throws Exception {

        mockMvc.perform(
                get("/api/v1/users/10")
                    .with(user("operations")
                        .roles("OPERATIONS_MANAGER"))
        )
        .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    // =====================================================
    // 7. TECHNICIAN - UPDATE USER
    // =====================================================

    @Test
    void technicianShouldNotUpdateUser()
            throws Exception {

        String requestBody = """
                {
                    "fullName": "Updated Employee",
                    "email": "updated@example.com"
                }
                """;

        mockMvc.perform(
                put("/api/v1/users/10")
                    .with(user("technician")
                        .roles("TECHNICIAN"))
                    .with(csrf())
                    .contentType("application/json")
                    .content(requestBody)
        )
        .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    // =====================================================
    // 8. INVENTORY STAFF - LOCK USER
    // =====================================================

    @Test
    void inventoryStaffShouldNotLockUser()
            throws Exception {

        mockMvc.perform(
                patch("/api/v1/users/10/status")
                    .with(user("inventory")
                        .roles("INVENTORY_STAFF"))
                    .with(csrf())
                    .param("status", "LOCKED")
        )
        .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    // =====================================================
    // 9. OPERATIONS MANAGER - LOCK USER
    // =====================================================

    @Test
    void operationsManagerShouldNotLockUser()
            throws Exception {

        mockMvc.perform(
                patch("/api/v1/users/10/status")
                    .with(user("operations")
                        .roles("OPERATIONS_MANAGER"))
                    .with(csrf())
                    .param("status", "LOCKED")
        )
        .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    // =====================================================
    // 10. BRAND ADMIN - UPDATE USER STATUS
    // =====================================================

    @Test
    void brandAdminShouldAccessUpdateStatusEndpoint()
            throws Exception {

        mockMvc.perform(
                patch("/api/v1/users/10/status")
                    .with(user("brandadmin")
                        .roles("BRAND_ADMIN"))
                    .with(csrf())
                    .param("status", "LOCKED")
        )
        .andExpect(status().isOk());

        verify(userService)
                .updateUserStatus(10L, "LOCKED");
    }

    // =====================================================
    // 11. UNAUTHENTICATED USER
    // =====================================================

    @Test
    void unauthenticatedUserShouldNotAccessUserList()
            throws Exception {

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userService);
    }
}
