package com.scentstation.payment;

import com.scentstation.contract.PaymentNotice;
import com.scentstation.dto.request.MockPaymentCallbackRequest;
import com.scentstation.entity.PaymentStatus;
import org.springframework.stereotype.Component;

@Component
public class MockPaymentProvider implements PaymentProvider {
    @Override
    public String name() {
        return "MOCK";
    }

    @Override
    public PaymentNotice verify(MockPaymentCallbackRequest request) {
        if (request == null || request.providerReference() == null
                || request.providerReference().isBlank() || request.orderId() == null
                || request.amount() == null || request.currency() == null
                || request.status() == null) {
            throw new IllegalArgumentException("Invalid mock payment callback");
        }
        if (request.signature() == null || request.signature().isBlank()) {
            throw new IllegalArgumentException("Mock payment callback signature is required");
        }
        PaymentStatus status;
        try {
            status = PaymentStatus.valueOf(request.status().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported payment status: " + request.status(), ex);
        }
        return new PaymentNotice(request.providerReference(), request.orderId(), request.amount(),
                request.currency(), status, request.gatewayTransactionId(), request.paidAt(),
                request.signature(), false);
    }
}
