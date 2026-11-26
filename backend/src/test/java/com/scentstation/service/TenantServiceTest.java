package com.scentstation.service;

import com.scentstation.common.security.CurrentUserPrincipal;
import com.scentstation.common.security.CurrentUserService;
import com.scentstation.dto.request.CreateTenantRequest;
import com.scentstation.dto.request.UpdateTenantRequest;
import com.scentstation.dto.response.TenantResponse;
import com.scentstation.entity.Tenant;
import com.scentstation.exception.ResourceConflictException;
import com.scentstation.exception.ResourceNotFoundException;
import com.scentstation.mapper.TenantMapper;
import com.scentstation.repository.TenantRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private TenantMapper tenantMapper;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private TenantService tenantService;

    @ParameterizedTest
    @ValueSource(strings = {
                    "OPERATIONS_MANAGER",
                    "TECHNICIAN",
                    "INVENTORY_STAFF"
    })
    void employeeShouldNotListTenants(String role) {

            mockCurrentUser(20L, 1L, role);

            assertThrows(
                            AccessDeniedException.class,
                            () -> tenantService.getAllTenants());

            verifyNoInteractions(tenantRepository);
            verifyNoInteractions(tenantMapper);
    }

    @ParameterizedTest
    @ValueSource(strings = {
                    "OPERATIONS_MANAGER",
                    "TECHNICIAN",
                    "INVENTORY_STAFF"
    })
    void employeeShouldNotReadTenant(String role) {

            mockCurrentUser(20L, 1L, role);

            assertThrows(
                            AccessDeniedException.class,
                            () -> tenantService.getTenantById(1L));

            verifyNoInteractions(tenantRepository);
            verifyNoInteractions(tenantMapper);
    }

    @ParameterizedTest
    @ValueSource(strings = {
                    "OPERATIONS_MANAGER",
                    "TECHNICIAN",
                    "INVENTORY_STAFF"
    })
    void employeeShouldNotUpdateTenant(String role) {

            mockCurrentUser(20L, 1L, role);

            UpdateTenantRequest request = createUpdateRequest(
                            "Unauthorized Update",
                            "BRAND-A");

            assertThrows(
                            AccessDeniedException.class,
                            () -> tenantService.updateTenant(1L, request));

            verifyNoInteractions(tenantRepository);
            verifyNoInteractions(tenantMapper);
    }

    // =========================================
    // CREATE TENANT
    // =========================================

    @Test
    void superAdminShouldCreateTenant() {

        mockCurrentUser(
                1L, null, "PLATFORM_SUPER_ADMIN"
        );

        CreateTenantRequest request =
                createRequest("New Brand", "BRAND-NEW");

        when(tenantRepository.saveAndFlush(any(Tenant.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        tenantService.createTenant(request);

        ArgumentCaptor<Tenant> captor =
                ArgumentCaptor.forClass(Tenant.class);

        verify(tenantRepository)
                .saveAndFlush(captor.capture());

        Tenant savedTenant = captor.getValue();

        assertEquals("New Brand", savedTenant.getName());
        assertEquals("BRAND-NEW", savedTenant.getCode());
        assertEquals("ACTIVE", savedTenant.getStatus());

        assertNotNull(savedTenant.getCreatedAt());
        assertNotNull(savedTenant.getUpdatedAt());

        assertEquals(
                savedTenant.getCreatedAt(),
                savedTenant.getUpdatedAt()
        );

        verify(tenantMapper).toResponse(savedTenant);
    }

    @Test
    void createTenantShouldNormalizeNameAndCode() {

        mockCurrentUser(
                1L, null, "PLATFORM_SUPER_ADMIN"
        );

        CreateTenantRequest request =
                createRequest(" New Brand ", "brand-new");

        when(tenantRepository.saveAndFlush(any(Tenant.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        tenantService.createTenant(request);

        ArgumentCaptor<Tenant> captor =
                ArgumentCaptor.forClass(Tenant.class);

        verify(tenantRepository)
                .saveAndFlush(captor.capture());

        assertEquals(
                "New Brand",
                captor.getValue().getName()
        );

        assertEquals(
                "BRAND-NEW",
                captor.getValue().getCode()
        );

        verify(tenantRepository)
                .existsByCode("BRAND-NEW");
    }

    @Test
    void createTenantShouldRejectDuplicateCode() {

        mockCurrentUser(
                1L, null, "PLATFORM_SUPER_ADMIN"
        );

        CreateTenantRequest request =
                createRequest("Duplicate Brand", "BRAND-A");

        when(tenantRepository.existsByCode("BRAND-A"))
                .thenReturn(true);

        assertThrows(
                ResourceConflictException.class,
                () -> tenantService.createTenant(request)
        );

        verify(tenantRepository, never())
                .saveAndFlush(any(Tenant.class));

        verifyNoInteractions(tenantMapper);
    }

    @Test
    void createTenantShouldHandleDatabaseConstraintViolation() {

        mockCurrentUser(
                1L, null, "PLATFORM_SUPER_ADMIN"
        );

        CreateTenantRequest request =
                createRequest("New Brand", "BRAND-NEW");

        when(tenantRepository.saveAndFlush(any(Tenant.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "Duplicate tenant code"
                ));

        assertThrows(
                ResourceConflictException.class,
                () -> tenantService.createTenant(request)
        );

        verifyNoInteractions(tenantMapper);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "BRAND_ADMIN",
            "OPERATIONS_MANAGER",
            "TECHNICIAN",
            "INVENTORY_STAFF"
    })
    void nonSuperAdminShouldNotCreateTenant(String role) {

        mockCurrentUser(20L, 1L, role);

        CreateTenantRequest request =
                createRequest("Unauthorized Brand", "BRAND-X");

        assertThrows(
                AccessDeniedException.class,
                () -> tenantService.createTenant(request)
        );

        verifyNoInteractions(tenantRepository);
        verifyNoInteractions(tenantMapper);
    }

    // =========================================
    // GET TENANTS
    // =========================================

    @Test
    void superAdminShouldGetAllTenants() {

        mockCurrentUser(
                1L, null, "PLATFORM_SUPER_ADMIN"
        );

        Tenant tenantA = createTenant(1L, "BRAND-A");
        Tenant tenantB = createTenant(2L, "BRAND-B");

        when(tenantRepository.findAll())
                .thenReturn(List.of(tenantA, tenantB));

        when(tenantMapper.toResponse(tenantA))
                .thenReturn(toResponse(tenantA));

        when(tenantMapper.toResponse(tenantB))
                .thenReturn(toResponse(tenantB));

        List<TenantResponse> result =
                tenantService.getAllTenants();

        assertEquals(2, result.size());

        verify(tenantRepository).findAll();
    }

    @Test
    void brandAdminShouldGetOnlyOwnTenant() {

        mockCurrentUser(20L, 1L, "BRAND_ADMIN");

        Tenant tenantA = createTenant(1L, "BRAND-A");

        when(tenantRepository.findById(1L))
                .thenReturn(Optional.of(tenantA));

        when(tenantMapper.toResponse(tenantA))
                .thenReturn(toResponse(tenantA));

        List<TenantResponse> result =
                tenantService.getAllTenants();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());

        verify(tenantRepository, never()).findAll();
    }

    @Test
    void brandAdminShouldGetOwnTenantDetail() {

        mockCurrentUser(20L, 1L, "BRAND_ADMIN");

        Tenant tenantA = createTenant(1L, "BRAND-A");

        when(tenantRepository.findById(1L))
                .thenReturn(Optional.of(tenantA));

        when(tenantMapper.toResponse(tenantA))
                .thenReturn(toResponse(tenantA));

        TenantResponse result =
                tenantService.getTenantById(1L);

        assertEquals(1L, result.getId());
        assertEquals("BRAND-A", result.getCode());
    }

    @Test
    void brandAdminShouldNotReadAnotherTenant() {

        mockCurrentUser(20L, 1L, "BRAND_ADMIN");

        assertThrows(
                ResourceNotFoundException.class,
                () -> tenantService.getTenantById(2L)
        );

        verifyNoInteractions(tenantRepository);
    }

    @Test
    void getTenantShouldRejectNonexistentTenant() {

        mockCurrentUser(
                1L, null, "PLATFORM_SUPER_ADMIN"
        );

        when(tenantRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> tenantService.getTenantById(999L)
        );

        verifyNoInteractions(tenantMapper);
    }

    // =========================================
    // UPDATE TENANT
    // =========================================

    @Test
    void superAdminShouldUpdateAnyTenant() {

        mockCurrentUser(
                1L, null, "PLATFORM_SUPER_ADMIN"
        );

        Tenant tenant = createTenant(2L, "BRAND-B");

        when(tenantRepository.findById(2L))
                .thenReturn(Optional.of(tenant));

        when(tenantRepository.save(tenant))
                .thenReturn(tenant);

        UpdateTenantRequest request =
                createUpdateRequest(
                        "Updated Brand",
                        "BRAND-UPDATED"
                );

        tenantService.updateTenant(2L, request);

        assertEquals("Updated Brand", tenant.getName());
        assertEquals("BRAND-UPDATED", tenant.getCode());

        verify(tenantRepository).save(tenant);
    }

    @Test
    void brandAdminShouldUpdateOwnTenant() {

        mockCurrentUser(20L, 1L, "BRAND_ADMIN");

        Tenant tenant = createTenant(1L, "BRAND-A");

        when(tenantRepository.findById(1L))
                .thenReturn(Optional.of(tenant));

        when(tenantRepository.save(tenant))
                .thenReturn(tenant);

        UpdateTenantRequest request =
                createUpdateRequest(
                        "Updated Brand A",
                        "BRAND-A"
                );

        tenantService.updateTenant(1L, request);

        assertEquals("Updated Brand A", tenant.getName());

        verify(tenantRepository).save(tenant);
    }

    @Test
    void brandAdminShouldNotUpdateAnotherTenant() {

        mockCurrentUser(20L, 1L, "BRAND_ADMIN");

        UpdateTenantRequest request =
                createUpdateRequest(
                        "Unauthorized Update",
                        "BRAND-X"
                );

        assertThrows(
                ResourceNotFoundException.class,
                () -> tenantService.updateTenant(2L, request)
        );

        verifyNoInteractions(tenantRepository);
    }

    @Test
    void updateTenantShouldRejectDuplicateCode() {

        mockCurrentUser(
                1L, null, "PLATFORM_SUPER_ADMIN"
        );

        Tenant tenant = createTenant(1L, "BRAND-A");

        when(tenantRepository.findById(1L))
                .thenReturn(Optional.of(tenant));

        when(tenantRepository.existsByCodeAndIdNot(
                "BRAND-B", 1L
        )).thenReturn(true);

        UpdateTenantRequest request =
                createUpdateRequest(
                        "Updated Brand",
                        "BRAND-B"
                );

        assertThrows(
                ResourceConflictException.class,
                () -> tenantService.updateTenant(1L, request)
        );

        verify(tenantRepository, never())
                .save(any(Tenant.class));

        assertEquals("BRAND-A", tenant.getCode());
    }

    // =========================================
    // HELPER METHODS
    // =========================================

    private void mockCurrentUser(
            Long userId,
            Long tenantId,
            String role) {

        CurrentUserPrincipal principal =
                new CurrentUserPrincipal(
                        userId,
                        "test_user",
                        tenantId,
                        tenantId == null
                                ? null
                                : "TEST-TENANT",
                        Set.of(role)
                );

        when(currentUserService.getCurrentUser())
                .thenReturn(principal);
    }

    private CreateTenantRequest createRequest(
            String name,
            String code) {

        CreateTenantRequest request =
                new CreateTenantRequest();

        request.setName(name);
        request.setCode(code);

        return request;
    }

    private UpdateTenantRequest createUpdateRequest(
            String name,
            String code) {

        UpdateTenantRequest request =
                new UpdateTenantRequest();

        request.setName(name);
        request.setCode(code);

        return request;
    }

    private Tenant createTenant(Long id, String code) {

        Tenant tenant = new Tenant();

        tenant.setId(id);
        tenant.setName(code);
        tenant.setCode(code);
        tenant.setStatus("ACTIVE");

        return tenant;
    }

    private TenantResponse toResponse(Tenant tenant) {

        return new TenantResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getCode(),
                tenant.getStatus(),
                tenant.getCreatedAt(),
                tenant.getUpdatedAt()
        );
    }
}