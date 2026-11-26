package com.scentstation.controller;

import com.scentstation.dto.response.MachineResponse;
import com.scentstation.exception.ResourceNotFoundException;
import com.scentstation.service.JwtService;
import com.scentstation.service.MachineService;
import com.scentstation.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MachineController.class)
@AutoConfigureMockMvc(addFilters = false)
class MachineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MachineService machineService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private final LocalDateTime now = LocalDateTime.of(2026, 11, 20, 9, 0, 0);

    @Test
    void getAllMachines_ShouldReturnMachineList() throws Exception {
        MachineResponse machine = new MachineResponse(
                1L,
                1L,
                1L,
                new MachineResponse.LocationInfo(1L, "Scent Demo - District 1", "123 Nguyen Hue", "ACTIVE"),
                "SCENT-M001",
                "SCENT-SN-0001",
                "scent-device-0001",
                "ONLINE",
                "ACTIVE",
                "1.0.0",
                now,
                now,
                now
        );

        when(machineService.getAll(null, null, null, null))
                .thenReturn(List.of(machine));

        mockMvc.perform(get("/api/v1/machines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].machineCode").value("SCENT-M001"))
                .andExpect(jsonPath("$[0].connectionStatus").value("ONLINE"))
                .andExpect(jsonPath("$[0].operationalStatus").value("ACTIVE"))
                .andExpect(jsonPath("$[0].location.name").value("Scent Demo - District 1"));
    }

    @Test
    void getMachineById_WhenNotFound_ShouldReturn404() throws Exception {
        when(machineService.getById(999L))
                .thenThrow(new ResourceNotFoundException("Machine not found with ID: 999"));

        mockMvc.perform(get("/api/v1/machines/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Machine not found with ID: 999"));
    }
}
