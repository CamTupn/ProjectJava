package com.scentstation.service;

import com.scentstation.dto.response.TenantResponse;
import com.scentstation.entity.Tenant;
import com.scentstation.exception.ResourceConflictException;
import com.scentstation.exception.ResourceNotFoundException;
import com.scentstation.mapper.TenantMapper;
import com.scentstation.repository.TenantRepository;

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
