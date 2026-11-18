package com.scentstation.service;

import com.scentstation.common.security.CurrentUserPrincipal;
import com.scentstation.common.security.CurrentUserService;
import com.scentstation.dto.request.CreateProductRequest;
import com.scentstation.dto.request.UpdateProductRequest;
import com.scentstation.dto.response.ProductResponse;
import com.scentstation.entity.Product;
import com.scentstation.entity.Tenant;
import com.scentstation.exception.ResourceConflictException;
import com.scentstation.exception.ResourceNotFoundException;
import com.scentstation.mapper.ProductMapper;
import com.scentstation.repository.ProductRepository;
import com.scentstation.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class ProductService {
    private final ProductRepository products;
    private final TenantRepository tenants;
    private final ProductMapper mapper;
    private final CurrentUserService currentUsers;

    public ProductService(ProductRepository products, TenantRepository tenants,
                          ProductMapper mapper, CurrentUserService currentUsers) {
        this.products = products; this.tenants = tenants;
        this.mapper = mapper; this.currentUsers = currentUsers;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAll(String status, String name, Long requestedTenantId) {
        CurrentUserPrincipal user = currentUsers.getCurrentUser();
        requireRole(user);
        Long tenantId = resolveReadTenant(user, requestedTenantId);
        return products.search(tenantId, normalize(status), blankToNull(name))
                .stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        CurrentUserPrincipal user = currentUsers.getCurrentUser();
        requireRole(user);
        return mapper.toResponse(findAccessible(id, user));
    }

    public ProductResponse create(CreateProductRequest request) {
        CurrentUserPrincipal user = currentUsers.getCurrentUser();
        requireRole(user);
        Long tenantId = resolveCreateTenant(user, request.tenantId());
        ensureActiveTenant(tenantId);
        String sku = request.sku().trim();
        if (products.existsByTenantIdAndSku(tenantId, sku))
            throw new ResourceConflictException("SKU already exists in this tenant: " + sku);
        Product product = new Product();
        product.setTenantId(tenantId);
        product.setSku(sku);
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setFragranceNotes(request.fragranceNotes());
        product.setImageUrl(request.imageUrl());
        product.setPrice(request.price());
        product.setVolumePerSprayMl(request.volumePerSprayMl());
        product.setStatus(request.status() == null ? "ACTIVE" : request.status());
        try {
            return mapper.toResponse(products.save(product));
        } catch (DataIntegrityViolationException ex) {
            throw new ResourceConflictException("SKU already exists in this tenant: " + sku);
        }
    }

    public ProductResponse update(Long id, UpdateProductRequest request) {
        CurrentUserPrincipal user = currentUsers.getCurrentUser();
        requireRole(user);
        Product product = findAccessible(id, user);
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setFragranceNotes(request.fragranceNotes());
        product.setImageUrl(request.imageUrl());
        product.setPrice(request.price());
        product.setVolumePerSprayMl(request.volumePerSprayMl());
        return mapper.toResponse(products.save(product));
    }

    public void delete(Long id) {
        CurrentUserPrincipal user = currentUsers.getCurrentUser();
        requireRole(user);
        Product product = findAccessible(id, user);
        if (products.isUsedByMachineSlot(id))
            throw new ResourceConflictException("Product cannot be deactivated while assigned to a Machine Slot");
        // Orders retain their product snapshot and foreign-key reference; soft-delete keeps history intact.
        product.setStatus("INACTIVE");
        products.save(product);
    }

    private Product findAccessible(Long id, CurrentUserPrincipal user) {
        if (user.isPlatformSuperAdmin())
            return products.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        if (user.tenantId() == null) throw new ResourceNotFoundException("Product not found with ID: " + id);
        return products.findByIdAndTenantId(id, user.tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
    }
    private Long resolveReadTenant(CurrentUserPrincipal user, Long requestedTenantId) {
        if (user.isPlatformSuperAdmin()) {
            if (requestedTenantId != null) ensureActiveTenant(requestedTenantId);
            return requestedTenantId;
        }
        if (user.tenantId() == null || (requestedTenantId != null && !requestedTenantId.equals(user.tenantId())))
            throw new ResourceNotFoundException("Product not found");
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
    private void requireRole(CurrentUserPrincipal user) {
        if (!user.isPlatformSuperAdmin() && !user.hasRole("BRAND_ADMIN"))
            throw new org.springframework.security.access.AccessDeniedException("Role is not permitted to manage products");
    }
    private String normalize(String value) { return value == null || value.isBlank() ? null : value.trim().toUpperCase(); }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
