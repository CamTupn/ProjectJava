package com.scentstation.controller;

import com.scentstation.dto.response.PaymentResponse;
import com.scentstation.entity.PaymentStatus;
import com.scentstation.security.KioskTokenService;
import com.scentstation.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class PaymentController {
    private final PaymentService paymentService;
    private final KioskTokenService kioskTokenService;

    public PaymentController(PaymentService paymentService, KioskTokenService kioskTokenService) {
        this.paymentService = paymentService;
        this.kioskTokenService = kioskTokenService;
    }

    @PostMapping({"/orders/{orderId}/payments", "/orders/{orderId}/payment-sessions"})
    public ResponseEntity<PaymentResponse> createKioskPayment(
            @PathVariable Long orderId,
            @RequestHeader(value = "X-Order-Token", required = false) String orderToken) {
        verifyKioskToken(orderToken, orderId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PaymentResponse.from(paymentService.createPayment(orderId)));
    }

    @GetMapping({"/orders/{orderId}/payments", "/orders/{orderId}/payment"})
    public ResponseEntity<List<PaymentResponse>> getKioskPayments(
            @PathVariable Long orderId,
            @RequestHeader(value = "X-Order-Token", required = false) String orderToken,
            @RequestParam(required = false) PaymentStatus status) {
        verifyKioskToken(orderToken, orderId);
        return ResponseEntity.ok(paymentService.findPaymentsForKiosk(orderId, status)
                .stream().map(PaymentResponse::from).toList());
    }

    @GetMapping("/payments")
    @PreAuthorize("hasAnyRole('PLATFORM_SUPER_ADMIN', 'BRAND_ADMIN', 'OPERATIONS_MANAGER')")
    public ResponseEntity<List<PaymentResponse>> getPayments(
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) PaymentStatus status) {
        return ResponseEntity.ok(paymentService.findPayments(orderId, status)
                .stream().map(PaymentResponse::from).toList());
    }

    @GetMapping("/payments/{id}")
    @PreAuthorize("hasAnyRole('PLATFORM_SUPER_ADMIN', 'BRAND_ADMIN', 'OPERATIONS_MANAGER')")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long id) {
        return ResponseEntity.ok(PaymentResponse.from(paymentService.getPayment(id)));
    }

    private void verifyKioskToken(String token, Long orderId) {
        if (!kioskTokenService.verify(token, orderId)) {
            throw new com.scentstation.exception.ResourceNotFoundException("Order not found");
        }
    }
}
