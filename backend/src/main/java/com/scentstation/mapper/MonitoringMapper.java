package com.scentstation.mapper;

import com.scentstation.dto.response.*;
import com.scentstation.entity.*;
import org.springframework.stereotype.Component;

@Component
public class MonitoringMapper {

    public AlertResponse toAlertResponse(Alert alert) {
        return AlertResponse.builder()
                .id(alert.getId())
                .tenantId(alert.getTenantId())
                .machineId(alert.getMachine().getId())
                .machineCode(alert.getMachine().getDeviceIdentity())
                .slotId(alert.getSlot() != null ? alert.getSlot().getId() : null)
                .slotNumber(alert.getSlot() != null ? alert.getSlot().getSlotNumber() : null)
                .type(alert.getType())
                .status(alert.getStatus())
                .message(alert.getMessage())
                .acknowledgedBy(alert.getAcknowledgedBy())
                .acknowledgedAt(alert.getAcknowledgedAt())
                .resolvedBy(alert.getResolvedBy())
                .resolvedAt(alert.getResolvedAt())
                .createdAt(alert.getCreatedAt())
                .build();
    }

    public SensorReadingResponse toSensorReadingResponse(SensorReading sr) {
        return SensorReadingResponse.builder()
                .id(sr.getId())
                .machineId(sr.getMachine().getId())
                .slotId(sr.getSlot() != null ? sr.getSlot().getId() : null)
                .measuredVolumeMl(sr.getMeasuredVolumeMl())
                .temperatureC(sr.getTemperatureC())
                .humidityPct(sr.getHumidityPct())
                .recordedAt(sr.getRecordedAt())
                .build();
    }

    public DeviceEventResponse toDeviceEventResponse(DeviceEvent event) {
        return DeviceEventResponse.builder()
                .id(event.getId())
                .machineId(event.getMachine().getId())
                .slotId(event.getSlot() != null ? event.getSlot().getId() : null)
                .eventType(event.getEventType())
                .errorCode(event.getErrorCode())
                .severity(event.getSeverity())
                .message(event.getMessage())
                .occurredAt(event.getOccurredAt())
                .build();
    }
}