package com.scentstation.contract;

/**
 * Event published when a dispense execution reports its outcome.
 */
public record DispenseResultEvent(
        Long orderId,
        Long commandId,
        String executionStatus,
        String errorCode
) {
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_UNCERTAIN = "UNCERTAIN";
}
