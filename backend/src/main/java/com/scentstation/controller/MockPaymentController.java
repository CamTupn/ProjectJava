package com.scentstation.controller;

import com.scentstation.contract.PaymentNotice;
import com.scentstation.dto.request.MockPaymentCallbackRequest;
import com.scentstation.dto.response.PaymentResponse;
import com.scentstation.payment.MockPaymentProvider;
import com.scentstation.service.PaymentService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("demo")
@RestController
@RequestMapping("/api/v1/payments")
@PreAuthorize("hasAnyRole('PLATFORM_SUPER_ADMIN', 'BRAND_ADMIN')")
public class MockPaymentController {
    private final PaymentService paymentService;
    private final MockPaymentProvider provider;

    public MockPaymentController(PaymentService paymentService, MockPaymentProvider provider) {
        this.paymentService = paymentService;
        this.provider = provider;
    }

    @PostMapping("/callback")
    public ResponseEntity<PaymentResponse> callback(@RequestBody MockPaymentCallbackRequest request) {
        PaymentNotice notice = provider.verify(request);
        return ResponseEntity.ok(PaymentResponse.from(paymentService.processManagementCallback(notice)));
    }
}
