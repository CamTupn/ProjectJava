package com.scentstation.controller;

import com.scentstation.dto.response.MachineSlotListResponse;
import com.scentstation.dto.response.MachineSlotResponse;
import com.scentstation.entity.MachineSlotStatus;
import com.scentstation.service.JwtService;
import com.scentstation.service.MachineSlotService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MachineSlotController.class)
@Import(MachineSlotControllerTest.MethodSecurityTestConfig.class)
class MachineSlotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MachineSlotService machineSlotService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockUser(roles = "INVENTORY_STAFF")
    void getSlotsReturnsListAndCount() throws Exception {
        when(machineSlotService.getSlotsByMachine(20L))
                .thenReturn(new MachineSlotListResponse(20L, 0, List.of()));

        mockMvc.perform(get("/api/v1/machines/20/slots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.machineId").value(20))
                .andExpect(jsonPath("$.totalSlots").value(0))
                .andExpect(jsonPath("$.slots").isArray());
    }

    @Test
    @WithMockUser(roles = "BRAND_ADMIN")
    void createSlotReturnsCreatedResponse() throws Exception {
        when(machineSlotService.createSlot(
                org.mockito.ArgumentMatchers.eq(20L),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(new MachineSlotResponse(
                40L,
                20L,
                1,
                null,
                null,
                new BigDecimal("50.00"),
                BigDecimal.ZERO,
                new BigDecimal("10.00"),
                MachineSlotStatus.EMPTY,
                null,
                null
        ));

        mockMvc.perform(post("/api/v1/machines/20/slots")
                        .contentType("application/json")
                        .content("""
                                {
                                  "slotNumber": 1,
                                  "capacityMl": 50.00,
                                  "currentVolumeMl": 0.00,
                                  "lowStockThresholdMl": 10.00
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(40))
                .andExpect(jsonPath("$.status").value("EMPTY"));
    }

    @Test
    @WithMockUser(roles = "TECHNICIAN")
    void technicianCannotCreateSlotConfiguration() throws Exception {
        mockMvc.perform(post("/api/v1/machines/20/slots")
                        .contentType("application/json")
                        .content("""
                                {
                                  "slotNumber": 1,
                                  "capacityMl": 50.00,
                                  "currentVolumeMl": 0.00,
                                  "lowStockThresholdMl": 10.00
                                }
                                """))
                .andExpect(status().isForbidden());

        verify(machineSlotService, never()).createSlot(
                org.mockito.ArgumentMatchers.eq(20L),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    @WithMockUser(roles = "BRAND_ADMIN")
    void createSlotRejectsInvalidCapacityBeforeCallingService() throws Exception {
        mockMvc.perform(post("/api/v1/machines/20/slots")
                        .contentType("application/json")
                        .content("""
                                {
                                  "slotNumber": 1,
                                  "capacityMl": 0,
                                  "currentVolumeMl": 0.00,
                                  "lowStockThresholdMl": 0.00
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(machineSlotService, never()).createSlot(
                org.mockito.ArgumentMatchers.eq(20L),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityTestConfig {
    }
}
