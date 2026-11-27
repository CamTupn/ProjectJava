package com.scentstation.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MonitoringOverviewResponse {
    private long totalMachines;
    private long onlineMachines;
    private long offlineMachines;
    private long activeAlerts;
    private long openAlerts;
    private long acknowledgedAlerts;
}