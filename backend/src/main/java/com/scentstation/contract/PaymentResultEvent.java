package com.scentstation.contract;

import java.math.BigDecimal;

/**
 * Event published when a payment attempt reaches a terminal status.
 */
public record PaymentResultEvent(
        Long orderId,
        String paymentRef,
        String status,
        BigDecimal amount,
        String currency
) {
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_EXPIRED = "EXPIRED";
    public static final String STATUS_REFUNDED = "REFUNDED";
}
