package com.scentstation.controller;

import com.scentstation.config.SecurityConfig;
import com.scentstation.repository.UserRepository;
import com.scentstation.security.KioskTokenService;
import com.scentstation.service.JwtService;
import com.scentstation.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@Import(SecurityConfig.class)
class PaymentControllerSecurityTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private PaymentService paymentService;
    @MockitoBean private KioskTokenService kioskTokenService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserRepository userRepository;

    @Test
    void operationsManagerCanReadPayments() throws Exception {
        when(paymentService.findPayments(null, null)).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/v1/payments")
                        .with(user("ops").roles("OPERATIONS_MANAGER")))
                .andExpect(status().isOk());
        verify(paymentService).findPayments(null, null);
    }

    @Test
    void unauthorizedRoleCannotReadPayments() throws Exception {
        mockMvc.perform(get("/api/v1/payments")
                        .with(user("tech").roles("TECHNICIAN")))
                .andExpect(status().isForbidden());
        verify(paymentService, never()).findPayments(null, null);
    }

    @Test
    void invalidKioskTokenReturnsNotFound() throws Exception {
        when(kioskTokenService.verify("bad", 10L)).thenReturn(false);

        mockMvc.perform(post("/api/v1/orders/10/payments")
                        .header("X-Order-Token", "bad")
                        .with(user("kiosk").roles("BRAND_ADMIN")))
                .andExpect(status().isNotFound());
        verify(paymentService, never()).createPayment(10L);
    }
}
