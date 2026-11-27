package com.scentstation.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class DeviceEventResponse {
    private Long id;
    private Long machineId;
    private Long slotId;
    private String eventType;
    private String errorCode;
    private String severity;
    private String message;
    private Instant occurredAt;
}