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
import com.scentstation.dto.request.CreateTenantRequest;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

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

        validateTenantManagementPermission();

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

        validateTenantManagementPermission();

        Tenant tenant = findAccessibleTenant(id);

        return tenantMapper.toResponse(tenant);
    }

    // 3. Cập nhật thông tin tenant
    public TenantResponse updateTenant(
            Long id,
            UpdateTenantRequest request) {

        validateTenantManagementPermission();

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

    // Tạo tenant mới
    public TenantResponse createTenant(CreateTenantRequest request) {

            CurrentUserPrincipal current = currentUserService.getCurrentUser();

            if (!current.isPlatformSuperAdmin()) {
                    throw new AccessDeniedException(
                                    "Only Platform Super Admin can create tenants");
            }

            String name = request.getName().trim();

            String code = request.getCode()
                            .trim()
                            .toUpperCase(Locale.ROOT);

            validateTenantCode(code);

            LocalDateTime now = LocalDateTime.now();

            Tenant tenant = new Tenant();

            tenant.setName(name);
            tenant.setCode(code);
            tenant.setStatus("ACTIVE");
            tenant.setCreatedAt(now);
            tenant.setUpdatedAt(now);

            try {
                    Tenant savedTenant = tenantRepository.saveAndFlush(tenant);

                    return tenantMapper.toResponse(savedTenant);

            } catch (DataIntegrityViolationException ex) {
                    throw new ResourceConflictException(
                                    "Tenant could not be created due to a data constraint");
            }
    }

    private void validateTenantManagementPermission() {

            CurrentUserPrincipal current = currentUserService.getCurrentUser();

            if (!current.isPlatformSuperAdmin()
                            && !current.hasRole("BRAND_ADMIN")) {

                    throw new AccessDeniedException(
                                    "You do not have permission to manage tenants");
            }
    }
}
