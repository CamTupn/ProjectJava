package com.scentstation.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MockPaymentCallbackRequest(String providerReference, Long orderId,
                                         BigDecimal amount, String currency,
                                         String status, String gatewayTransactionId,
                                         LocalDateTime paidAt, String signature) { }
