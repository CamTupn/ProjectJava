package com.scentstation.controller;

import com.scentstation.config.SecurityConfig;
import com.scentstation.payment.MockPaymentProvider;
import com.scentstation.repository.UserRepository;
import com.scentstation.service.JwtService;
import com.scentstation.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MockPaymentController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("demo")
class MockPaymentControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private PaymentService paymentService;
    @MockitoBean private MockPaymentProvider provider;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserRepository userRepository;

    @Test
    void technicianCannotInvokeMockCallback() throws Exception {
        mockMvc.perform(post("/api/v1/payments/callback")
                        .with(user("tech").roles("TECHNICIAN"))
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedMockCallbackIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/payments/callback")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
