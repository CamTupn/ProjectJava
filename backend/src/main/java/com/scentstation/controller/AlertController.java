package com.scentstation.controller;

import com.scentstation.common.security.CurrentUserService;
import com.scentstation.dto.response.AlertResponse;
import com.scentstation.entity.AlertStatus;
import com.scentstation.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;
    private final CurrentUserService currentUserService;

    @GetMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'OPERATOR', 'SYSTEM_ADMIN')")
    public ResponseEntity<Page<AlertResponse>> listAlerts(
            @RequestParam(required = false) AlertStatus status,
            @RequestParam(required = false) Long machineId,
            Pageable pageable
    ) {
        Long tenantId = currentUserService.getCurrentTenantId();
        return ResponseEntity.ok(alertService.getAlerts(tenantId, status, machineId, pageable));
    }

    @PostMapping("/{id}/acknowledge")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'OPERATOR', 'SYSTEM_ADMIN')")
    public ResponseEntity<AlertResponse> acknowledge(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.acknowledge(id, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'OPERATOR', 'SYSTEM_ADMIN')")
    public ResponseEntity<AlertResponse> resolve(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.resolve(id, getCurrentTenantId(), getCurrentUserId()));
    }

    @PostMapping("/{id}/dismiss")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'OPERATOR', 'SYSTEM_ADMIN')")
    public ResponseEntity<AlertResponse> dismiss(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.dismiss(id, getCurrentTenantId(), getCurrentUserId()));
    }

    private Long getCurrentTenantId() {
        return currentUserService.getCurrentTenantId();
    }

    private Long getCurrentUserId() {
        return currentUserService.getCurrentUserId();
    }
}