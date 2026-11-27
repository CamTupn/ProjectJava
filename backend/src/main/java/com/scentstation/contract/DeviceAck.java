package com.scentstation.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DeviceAck(String machineId, String deviceIdentity, String eventId,
                        String commandId, String status, Instant occurredAt) { }
