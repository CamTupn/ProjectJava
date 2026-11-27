package com.scentstation.controller;

import com.scentstation.config.SecurityConfig;
import com.scentstation.repository.UserRepository;
import com.scentstation.service.DispenseService;
import com.scentstation.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DispenseController.class)
@Import(SecurityConfig.class)
class DispenseControllerSecurityTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private DispenseService dispenseService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserRepository userRepository;

    @Test
    void technicianCanReadDispenseCommands() throws Exception {
        when(dispenseService.findCommands(null, null)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/dispense/commands")
                        .with(user("technician").roles("TECHNICIAN")))
                .andExpect(status().isOk());

        verify(dispenseService).findCommands(null, null);
    }

    @Test
    void inventoryStaffCannotReadDispenseCommands() throws Exception {
        mockMvc.perform(get("/api/v1/dispense/commands")
                        .with(user("inventory").roles("INVENTORY_STAFF")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(dispenseService);
    }

    @Test
    void anonymousUserCannotReadDispenseCommands() throws Exception {
        mockMvc.perform(get("/api/v1/dispense/commands"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(dispenseService);
    }
}
