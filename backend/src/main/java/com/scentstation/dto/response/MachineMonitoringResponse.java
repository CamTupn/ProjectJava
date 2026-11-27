package com.scentstation.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class MachineMonitoringResponse {
    private Long machineId;
    private String machineCode;
    private String deviceIdentity;
    private String connectionStatus;
    private String operationalStatus;
    private LocalDateTime lastPingAt;
    private long activeAlertCount;
}