package com.scentstation.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class SensorReadingResponse {
    private Long id;
    private Long machineId;
    private Long slotId;
    private Integer measuredVolumeMl;
    private BigDecimal temperatureC;
    private BigDecimal humidityPct;
    private Instant recordedAt;
}