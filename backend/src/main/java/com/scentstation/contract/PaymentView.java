package com.scentstation.contract;

import com.scentstation.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentView(Long id, Long orderId, String paymentRef, String provider,
                         BigDecimal amount, String currency, PaymentStatus status,
                         String gatewayTransactionId, LocalDateTime paidAt,
                         LocalDateTime createdAt) { }
