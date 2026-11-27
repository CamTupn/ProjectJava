package com.scentstation.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Telemetry(String machineId, String deviceIdentity, String sensorType,
                        JsonNode readings, Instant occurredAt) { }
