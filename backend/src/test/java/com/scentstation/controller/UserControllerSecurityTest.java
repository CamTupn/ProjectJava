package com.scentstation.controller;

import com.scentstation.config.SecurityConfig;
import com.scentstation.repository.UserRepository;
import com.scentstation.service.JwtService;
import com.scentstation.service.UserService;
import com.scentstation.dto.request.CreateUserRequest;
import com.scentstation.dto.response.UserResponse;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

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

    // =====================================================
    // CREATE USER SECURITY TESTS
    // =====================================================

    private static final String CREATE_USER_BODY = """
            {
                "username": "new_employee",
                "email": "employee@example.com",
                "password": "TestPassword123!",
                "fullName": "New Employee",
                "tenantId": 1,
                "roleCode": "TECHNICIAN"
            }
            """;

    @ParameterizedTest
    @ValueSource(strings = {
            "PLATFORM_SUPER_ADMIN",
            "BRAND_ADMIN"
    })
    void authorizedAdminShouldCreateUser(String role)
            throws Exception {

        UserResponse response = new UserResponse(
                100L,
                "new_employee",
                "employee@example.com",
                "New Employee",
                "ACTIVE",
                1L,
                "BRAND-A",
                Set.of("TECHNICIAN"),
                null,
                null);

        when(userService.createUser(any(CreateUserRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                post("/api/v1/users")
                        .with(user("admin").roles(role))
                        .contentType("application/json")
                        .content(CREATE_USER_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.username").value("new_employee"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.password").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.passwordHash").doesNotHaveJsonPath());

        verify(userService)
                .createUser(any(CreateUserRequest.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "OPERATIONS_MANAGER",
            "TECHNICIAN",
            "INVENTORY_STAFF"
    })
    void employeeShouldNotCreateUser(String role)
            throws Exception {

        mockMvc.perform(
                post("/api/v1/users")
                        .with(user("employee").roles(role))
                        .contentType("application/json")
                        .content(CREATE_USER_BODY))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    void unauthenticatedUserShouldNotCreateUser()
            throws Exception {

        mockMvc.perform(
                post("/api/v1/users")
                        .contentType("application/json")
                        .content(CREATE_USER_BODY))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userService);
    }

    @Test
    void createUserShouldRejectInvalidRequest()
            throws Exception {

        mockMvc.perform(
                post("/api/v1/users")
                        .with(user("admin")
                                .roles("PLATFORM_SUPER_ADMIN"))
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }
}
