package com.scentstation.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Heartbeat(String machineId, String deviceIdentity, String status,
                        String firmwareVersion, Instant sentAt) { }
