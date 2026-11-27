package com.scentstation.dto.response;

import com.scentstation.entity.AlertStatus;
import com.scentstation.entity.AlertType;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class AlertResponse {
    private Long id;
    private Long tenantId;
    private Long machineId;
    private String machineCode;
    private Long slotId;
    private Integer slotNumber;
    private AlertType type;
    private AlertStatus status;
    private String message;
    private Long acknowledgedBy;
    private Instant acknowledgedAt;
    private Long resolvedBy;
    private Instant resolvedAt;
    private Instant createdAt;
}