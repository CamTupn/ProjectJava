package com.scentstation.controller;

import com.scentstation.common.security.CurrentUserService;
import com.scentstation.dto.response.*;
import com.scentstation.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// TODO: Đối chiếu base path & @PreAuthorize role với docs/api/api-specification.md
@RestController
@RequestMapping("/api/v1/monitoring")
@RequiredArgsConstructor
public class MonitoringController {

    private final MonitoringService monitoringService;
    private final CurrentUserService currentUserService;

    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'OPERATOR', 'SYSTEM_ADMIN')")
    public ResponseEntity<MonitoringOverviewResponse> getOverview() {
        return ResponseEntity.ok(monitoringService.getOverview(getCurrentTenantId()));
    }

    @GetMapping("/machines/{machineId}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'OPERATOR', 'SYSTEM_ADMIN')")
    public ResponseEntity<MachineMonitoringResponse> getMachineMonitoring(@PathVariable Long machineId) {
        return ResponseEntity.ok(monitoringService.getMachineMonitoring(machineId, getCurrentTenantId()));
    }

    @GetMapping("/machines/{machineId}/sensor-readings")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'OPERATOR', 'SYSTEM_ADMIN')")
    public ResponseEntity<Page<SensorReadingResponse>> getSensorReadings(
            @PathVariable Long machineId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(monitoringService.getSensorReadings(machineId, getCurrentTenantId(), pageable));
    }

    @GetMapping("/machines/{machineId}/device-events")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'OPERATOR', 'SYSTEM_ADMIN')")
    public ResponseEntity<Page<DeviceEventResponse>> getDeviceEvents(
            @PathVariable Long machineId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(monitoringService.getDeviceEvents(machineId, getCurrentTenantId(), pageable));
    }

    private Long getCurrentTenantId() {
        return currentUserService.getCurrentTenantId();
    }
}