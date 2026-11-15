package com.scentstation.service;

import com.scentstation.common.security.CurrentUserPrincipal;
import com.scentstation.common.security.CurrentUserService;
import com.scentstation.dto.request.UpdateTenantRequest;
import com.scentstation.dto.response.TenantResponse;
import com.scentstation.entity.Tenant;
import com.scentstation.exception.ResourceConflictException;
import com.scentstation.exception.ResourceNotFoundException;
import com.scentstation.mapper.TenantMapper;
import com.scentstation.repository.TenantRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class TenantService {

    private final TenantRepository tenantRepository;
    private final TenantMapper tenantMapper;
    private final CurrentUserService currentUserService;

    public TenantService(
            TenantRepository tenantRepository,
            TenantMapper tenantMapper,
            CurrentUserService currentUserService) {

        this.tenantRepository = tenantRepository;
        this.tenantMapper = tenantMapper;
        this.currentUserService = currentUserService;
    }

    // 1. Xem danh sách tenant theo quyền truy cập
    public List<TenantResponse> getAllTenants() {

        CurrentUserPrincipal current =
                currentUserService.getCurrentUser();

        if (current.isPlatformSuperAdmin()) {
            return tenantRepository.findAll()
                    .stream()
                    .map(tenantMapper::toResponse)
                    .toList();
        }

        Tenant tenant = findAccessibleTenant(current.tenantId());

        return List.of(tenantMapper.toResponse(tenant));
    }

    // 2. Xem chi tiết tenant
    public TenantResponse getTenantById(Long id) {

        Tenant tenant = findAccessibleTenant(id);

        return tenantMapper.toResponse(tenant);
    }

    // 3. Cập nhật thông tin tenant
    public TenantResponse updateTenant(
            Long id,
            UpdateTenantRequest request) {

        Tenant tenant = findAccessibleTenant(id);

        String name = request.getName().trim();
        String code = request.getCode().trim().toUpperCase();

        // Kiểm tra mã tenant trùng
        if (tenantRepository.existsByCodeAndIdNot(code, id)) {
            throw new ResourceConflictException(
                    "Tenant code already exists: " + code
            );
        }

        tenant.setName(name);
        tenant.setCode(code);

        Tenant savedTenant = tenantRepository.save(tenant);

        return tenantMapper.toResponse(savedTenant);
    }

    // 4. Kiểm tra mã tenant khi tạo mới
    public void validateTenantCode(String code) {

        if (tenantRepository.existsByCode(code)) {
            throw new ResourceConflictException(
                    "Tenant code already exists: " + code
            );
        }
    }

    // 5. Kiểm tra quyền truy cập tenant
    private Tenant findAccessibleTenant(Long tenantId) {

        CurrentUserPrincipal current =
                currentUserService.getCurrentUser();

        if (current.isPlatformSuperAdmin()) {
            return tenantRepository.findById(tenantId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Tenant not found with ID: " + tenantId
                            )
                    );
        }

        if (current.tenantId() == null
                || !current.tenantId().equals(tenantId)) {

            throw new ResourceNotFoundException(
                    "Tenant not found with ID: " + tenantId
            );
        }

        return tenantRepository.findById(tenantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Tenant not found with ID: " + tenantId
                        )
                );
    }
}
