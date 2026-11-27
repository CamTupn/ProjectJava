package com.scentstation.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.scentstation.config.SecurityConfig;
import com.scentstation.dto.request.CreateRefillRequest;
import com.scentstation.repository.UserRepository;
import com.scentstation.service.JwtService;
import com.scentstation.service.RefillService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RefillController.class)
@Import(SecurityConfig.class)
class RefillControllerSecurityTest {

    private static final String REFILLS_URL = "/api/v1/inventory/refills";

    private static final String VALID_BODY = """
            {"slotId": 40, "volumeMl": 12.50, "notes": "weekly"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RefillService refillService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @ParameterizedTest
    @CsvSource({
            "PLATFORM_SUPER_ADMIN, 201",
            "BRAND_ADMIN, 201",
            "INVENTORY_STAFF, 201",
            "OPERATIONS_MANAGER, 403",
            "TECHNICIAN, 403",
            "PUBLIC_KIOSK, 403"
    })
    void createRefillEnforcesRolePermissions(String role, int expectedStatus)
            throws Exception {
        mockMvc.perform(post(REFILLS_URL)
                        .with(user("u").roles(role))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().is(expectedStatus));

        if (expectedStatus == 201) {
            ArgumentCaptor<CreateRefillRequest> captor =
                    ArgumentCaptor.forClass(CreateRefillRequest.class);
            verify(refillService).createRefill(captor.capture());
            assertEquals(40L, captor.getValue().getSlotId());
            assertEquals(0, new BigDecimal("12.50")
                    .compareTo(captor.getValue().getVolumeMl()));
        } else {
            verifyNoInteractions(refillService);
        }
    }

    @Test
    void anonymousCreateIsRejectedWith401() throws Exception {
        mockMvc.perform(post(REFILLS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(refillService);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"slotId\": 40, \"volumeMl\": 0}",
            "{\"slotId\": 40, \"volumeMl\": 0.00}",
            "{\"slotId\": 40, \"volumeMl\": -5}",
            "{\"slotId\": 40, \"volumeMl\": 1.234}",
            "{\"slotId\": 40}",
            "{\"volumeMl\": 5}"
    })
    void invalidBodyIsRejectedWith400(String body) throws Exception {
        mockMvc.perform(post(REFILLS_URL)
                        .with(user("u").roles("INVENTORY_STAFF"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(refillService);
    }

    @Test
    void requestContractHasNoTenantOrProductField() {
        // The tenant is derived from the slot's machine and the product is the
        // one assigned to the slot, so the client cannot supply either.
        assertEquals(
                java.util.List.of("notes", "slotId", "volumeMl"),
                java.util.Arrays.stream(CreateRefillRequest.class.getDeclaredFields())
                        .map(java.lang.reflect.Field::getName)
                        .sorted()
                        .toList()
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "PLATFORM_SUPER_ADMIN", "BRAND_ADMIN", "OPERATIONS_MANAGER",
            "TECHNICIAN", "INVENTORY_STAFF"
    })
    void readRolesCanListAndGetRefills(String role) throws Exception {
        mockMvc.perform(get(REFILLS_URL).param("slotId", "40").param("limit", "5")
                        .with(user("u").roles(role)))
                .andExpect(status().isOk());
        mockMvc.perform(get(REFILLS_URL + "/300").with(user("u").roles(role)))
                .andExpect(status().isOk());

        verify(refillService).getRefillsBySlot(40L, 5);
        verify(refillService).getRefill(300L);
    }

    @Test
    void unauthorizedRoleCannotReadRefills() throws Exception {
        mockMvc.perform(get(REFILLS_URL).param("slotId", "40")
                        .with(user("u").roles("PUBLIC_KIOSK")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(REFILLS_URL + "/300").with(user("u").roles("PUBLIC_KIOSK")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(refillService);
    }

    @Test
    void anonymousReadIsRejectedWith401() throws Exception {
        mockMvc.perform(get(REFILLS_URL).param("slotId", "40"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get(REFILLS_URL + "/300"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(refillService);
    }

    @Test
    void listWithoutSlotIdReachesServiceWhichReturns400() throws Exception {
        org.mockito.Mockito.when(refillService.getRefillsBySlot(any(), any()))
                .thenThrow(new IllegalArgumentException("slotId is required"));

        mockMvc.perform(get(REFILLS_URL).with(user("u").roles("BRAND_ADMIN")))
                .andExpect(status().isBadRequest());
    }
}
