package com.scentstation.tenant.service;

import com.scentstation.tenant.entity.Tenant;
import com.scentstation.tenant.dto.response.TenantResponse;
import com.scentstation.tenant.mapper.TenantMapper;
import com.scentstation.tenant.repository.TenantRepository;
import com.scentstation.common.exception.ResourceNotFoundException;
import com.scentstation.common.exception.ResourceConflictException;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final TenantMapper tenantMapper;

    public TenantService(
            TenantRepository tenantRepository,
            TenantMapper tenantMapper) {
        this.tenantRepository = tenantRepository;
        this.tenantMapper = tenantMapper;
    }

    public List<TenantResponse> getAllTenants() {
        return tenantRepository.findAll()
                .stream()
                .map(tenantMapper::toResponse)
                .toList();
    }

    // Get tenant by ID
    public TenantResponse getTenantById(Long id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Tenant not found with ID: " + id
                        )
                );
    
        return tenantMapper.toResponse(tenant);
    }
    
    // Check duplicate tenant code
    public void validateTenantCode(String code) {
        if (tenantRepository.existsByCode(code)) {
            throw new ResourceConflictException(
                    "Tenant code already exists: " + code
            );
        }
    }
}
