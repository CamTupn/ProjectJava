package com.scentstation.contract;

import com.scentstation.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentNotice(String paymentRef, Long orderId, BigDecimal amount,
                            String currency, PaymentStatus status,
                            String gatewayTransactionId, LocalDateTime paidAt,
                            String signature, boolean idempotent) {
    public PaymentNotice withIdempotent(boolean value) {
        return new PaymentNotice(paymentRef, orderId, amount, currency, status,
                gatewayTransactionId, paidAt, signature, value);
    }
}
