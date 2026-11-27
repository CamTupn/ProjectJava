package com.scentstation.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.scentstation.config.SecurityConfig;
import com.scentstation.dto.response.OrderResponse;
import com.scentstation.repository.UserRepository;
import com.scentstation.service.JwtService;
import com.scentstation.service.OrderService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
class OrderControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @ParameterizedTest
    @ValueSource(strings = {
            "PLATFORM_SUPER_ADMIN",
            "BRAND_ADMIN",
            "OPERATIONS_MANAGER"
    })
    void authorizedRolesCanListAndViewOrders(String role) throws Exception {
        OrderResponse response = new OrderResponse(
                100L, 1L, 10L, 20L, 30L, "Ocean Mist",
                new BigDecimal("15000.00"), "VND", "PAID",
                LocalDateTime.now().plusMinutes(5), LocalDateTime.now(), LocalDateTime.now()
        );
        when(orderService.getOrders(10L, "PAID")).thenReturn(List.of(response));
        when(orderService.getOrder(100L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/orders")
                        .param("machineId", "10")
                        .param("status", "PAID")
                        .with(user("tester").roles(role)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/orders/100")
                        .with(user("tester").roles(role)))
                .andExpect(status().isOk());

        verify(orderService).getOrders(10L, "PAID");
        verify(orderService).getOrder(100L);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "TECHNICIAN",
            "INVENTORY_STAFF"
    })
    void unauthorizedRolesCannotAccessOrders(String role) throws Exception {
        mockMvc.perform(get("/api/v1/orders")
                        .with(user("tester").roles(role)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/orders/100")
                        .with(user("tester").roles(role)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(orderService);
    }

    @Test
    void unauthenticatedUserIsRejected() throws Exception {
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(orderService);
    }

    @Test
    void crossTenantOrderAccessReturnsForbidden() throws Exception {
        when(orderService.getOrder(100L))
                .thenThrow(new AccessDeniedException("You do not have permission to access this order"));

        mockMvc.perform(get("/api/v1/orders/100")
                        .with(user("brand2").roles("BRAND_ADMIN")))
                .andExpect(status().isForbidden());
    }

    @Test
    void clientCannotUpdateOrderStatusDirectly() throws Exception {
        int status = mockMvc.perform(patch("/api/v1/orders/100/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DISPENSED\"}")
                        .with(user("admin").roles("PLATFORM_SUPER_ADMIN")))
                .andReturn()
                .getResponse()
                .getStatus();

        org.junit.jupiter.api.Assertions.assertTrue(status >= 400, "Expected non-2xx error status, got " + status);
        verifyNoInteractions(orderService);
    }
}
