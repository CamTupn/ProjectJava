
package com.scentstation.tenant.service;

import com.scentstation.tenant.dto.response.TenantResponse;
import com.scentstation.tenant.mapper.TenantMapper;
import com.scentstation.tenant.repository.TenantRepository;

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
}
