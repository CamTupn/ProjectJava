package com.scentstation.controller;

import com.scentstation.dto.request.UpdateTenantRequest;
import com.scentstation.dto.response.TenantResponse;
import com.scentstation.service.TenantService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    // 1. Xem danh sách tenant
    @GetMapping
    @PreAuthorize("hasAnyRole('PLATFORM_SUPER_ADMIN', 'BRAND_ADMIN')")
    public ResponseEntity<List<TenantResponse>> getAllTenants() {
        return ResponseEntity.ok(tenantService.getAllTenants());
    }

    // 2. Xem chi tiết tenant
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PLATFORM_SUPER_ADMIN', 'BRAND_ADMIN')")
    public ResponseEntity<TenantResponse> getTenantById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                tenantService.getTenantById(id)
        );
    }

    // 3. Cập nhật thông tin tenant
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PLATFORM_SUPER_ADMIN', 'BRAND_ADMIN')")
    public ResponseEntity<TenantResponse> updateTenant(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTenantRequest request) {

        return ResponseEntity.ok(
                tenantService.updateTenant(id, request)
        );
    }
}
