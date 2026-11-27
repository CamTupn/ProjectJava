package com.scentstation.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DeviceFault(String machineId, String deviceIdentity, String faultCode,
                          String message, String severity, Instant occurredAt) { }
