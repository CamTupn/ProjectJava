package com.scentstation.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DeviceResult(String machineId, String deviceIdentity, String eventId,
                           String commandId, String slotId, String result,
                           String errorCode, Instant occurredAt) { }
