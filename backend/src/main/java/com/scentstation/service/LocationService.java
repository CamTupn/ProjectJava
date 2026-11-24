package com.scentstation.service;

import com.scentstation.common.security.CurrentUserPrincipal;
import com.scentstation.common.security.CurrentUserService;
import com.scentstation.dto.request.CreateLocationRequest;
import com.scentstation.dto.request.UpdateLocationRequest;
import com.scentstation.dto.response.LocationResponse;
import com.scentstation.entity.Location;
import com.scentstation.entity.Tenant;
import com.scentstation.exception.ResourceConflictException;
import com.scentstation.exception.ResourceNotFoundException;
import com.scentstation.mapper.LocationMapper;
import com.scentstation.repository.LocationRepository;
import com.scentstation.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class LocationService {
    private final LocationRepository locations;
    private final TenantRepository tenants;
    private final LocationMapper mapper;
    private final CurrentUserService currentUsers;

    public LocationService(LocationRepository locations, TenantRepository tenants,
                           LocationMapper mapper, CurrentUserService currentUsers) {
        this.locations = locations; this.tenants = tenants;
        this.mapper = mapper; this.currentUsers = currentUsers;
    }

    @Transactional(readOnly = true)
    public List<LocationResponse> getAll(String status, String name, Long requestedTenantId) {
        CurrentUserPrincipal user = currentUsers.getCurrentUser();
        requireReadRole(user);
        Long tenantId = resolveReadTenant(user, requestedTenantId);
        return locations.search(tenantId, normalize(status), blankToNull(name))
                .stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public LocationResponse getById(Long id) {
        CurrentUserPrincipal user = currentUsers.getCurrentUser();
        requireReadRole(user);
        return mapper.toResponse(findAccessible(id, user));
    }

    public LocationResponse create(CreateLocationRequest request) {
        CurrentUserPrincipal user = currentUsers.getCurrentUser();
        requireManageRole(user);
        Long tenantId = resolveCreateTenant(user, request.tenantId());
        ensureActiveTenant(tenantId);
        Location location = new Location();
        location.setTenantId(tenantId);
        location.setName(request.name().trim());
        location.setAddress(request.address().trim());
        location.setDescription(request.description());
        location.setStatus(request.status() == null ? "ACTIVE" : request.status());
        return mapper.toResponse(locations.save(location));
    }

    public LocationResponse update(Long id, UpdateLocationRequest request) {
        CurrentUserPrincipal user = currentUsers.getCurrentUser();
        requireManageRole(user);
        Location location = findAccessible(id, user);
        location.setName(request.name().trim());
        location.setAddress(request.address().trim());
        location.setDescription(request.description());
        return mapper.toResponse(locations.save(location));
    }

    public void delete(Long id) {
        CurrentUserPrincipal user = currentUsers.getCurrentUser();
        requireManageRole(user);
        Location location = findAccessible(id, user);
        if (locations.countMachinesByLocationId(id) > 0) {
            throw new ResourceConflictException("Location cannot be deactivated while a Machine is assigned to it");
        }
        location.setStatus("INACTIVE");
        locations.save(location);
    }

    private Location findAccessible(Long id, CurrentUserPrincipal user) {
        if (user.isPlatformSuperAdmin()) {
            return locations.findById(id).orElseThrow(() -> new ResourceNotFoundException("Location not found with ID: " + id));
        }
        if (user.tenantId() == null) throw new ResourceNotFoundException("Location not found with ID: " + id);
        return locations.findByIdAndTenantId(id, user.tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with ID: " + id));
    }

    private Long resolveReadTenant(CurrentUserPrincipal user, Long requestedTenantId) {
        if (user.isPlatformSuperAdmin()) {
            if (requestedTenantId != null) ensureActiveTenant(requestedTenantId);
            return requestedTenantId;
        }
        if (user.tenantId() == null || (requestedTenantId != null && !requestedTenantId.equals(user.tenantId())))
            throw new ResourceNotFoundException("Location not found");
        return user.tenantId();
    }

    private Long resolveCreateTenant(CurrentUserPrincipal user, Long requestedTenantId) {
        if (user.isPlatformSuperAdmin()) {
            if (requestedTenantId == null) throw new IllegalArgumentException("tenantId is required for PLATFORM_SUPER_ADMIN");
            return requestedTenantId;
        }
        if (user.tenantId() == null) throw new IllegalArgumentException("Authenticated user has no tenant");
        return user.tenantId();
    }

    private void ensureActiveTenant(Long tenantId) {
        Tenant tenant = tenants.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with ID: " + tenantId));
        if (!"ACTIVE".equals(tenant.getStatus())) throw new ResourceConflictException("Cannot manage resources for an inactive tenant");
    }
    private void requireReadRole(CurrentUserPrincipal user) {
        if (!user.isPlatformSuperAdmin() && !user.hasRole("BRAND_ADMIN") && !user.hasRole("OPERATIONS_MANAGER"))
            throw new org.springframework.security.access.AccessDeniedException("Role is not permitted to read locations");
    }
    private void requireManageRole(CurrentUserPrincipal user) {
        if (!user.isPlatformSuperAdmin() && !user.hasRole("BRAND_ADMIN"))
            throw new org.springframework.security.access.AccessDeniedException("Role is not permitted to manage locations");
    }
    private String normalize(String value) { return value == null || value.isBlank() ? null : value.trim().toUpperCase(); }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
