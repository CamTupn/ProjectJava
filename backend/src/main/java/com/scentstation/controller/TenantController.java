package com.scentstation.controller;

import com.scentstation.dto.response.TenantResponse;
import com.scentstation.service.TenantService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping
    public List<TenantResponse> getAllTenants() {
        return tenantService.getAllTenants();
    }

    @GetMapping("/{id}")
    public TenantResponse getTenantById(@PathVariable Long id) {
        return tenantService.getTenantById(id);
    }
}
