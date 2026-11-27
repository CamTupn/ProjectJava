package com.scentstation.dto.response;

import com.scentstation.entity.Payment;
import com.scentstation.entity.PaymentStatus;
import com.scentstation.contract.PaymentView;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(Long id, Long orderId, String paymentRef, String provider,
                             BigDecimal amount, String currency, PaymentStatus status,
                             String gatewayTransactionId, LocalDateTime paidAt,
                             LocalDateTime createdAt) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(payment.getId(), payment.getOrder().getId(),
                payment.getPaymentRef(), payment.getProvider(), payment.getAmount(),
                payment.getCurrency(), payment.getStatus(), payment.getGatewayTransactionId(),
                payment.getPaidAt(), payment.getCreatedAt());
    }

    public static PaymentResponse from(PaymentView payment) {
        return new PaymentResponse(payment.id(), payment.orderId(), payment.paymentRef(),
                payment.provider(), payment.amount(), payment.currency(), payment.status(),
                payment.gatewayTransactionId(), payment.paidAt(), payment.createdAt());
    }
}
