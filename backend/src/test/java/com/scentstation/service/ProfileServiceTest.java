package com.scentstation.service;

import com.scentstation.common.security.CurrentUserService;
import com.scentstation.dto.response.ProfileResponse;
import com.scentstation.entity.Role;
import com.scentstation.entity.Tenant;
import com.scentstation.entity.User;
import com.scentstation.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.access.AccessDeniedException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private ProfileService profileService;

    private User activeUser;
    private Tenant tenant;

    @BeforeEach
    void setUp() {

        tenant = new Tenant();
        tenant.setId(3L);
        tenant.setCode("LUXURY-PER");
        tenant.setName("Luxury Perfume");
        tenant.setStatus("ACTIVE");

        activeUser = new User();
        activeUser.setId(7L);
        activeUser.setUsername("luxuryadmin");
        activeUser.setEmail("admin@luxury.example");
        activeUser.setFullName("Luxury Brand Admin");
        activeUser.setStatus("ACTIVE");
        activeUser.setTenant(tenant);
        activeUser.setMustChangePassword(false);
        activeUser.setTokenVersion(3L);

        activeUser.setRoles(
                new HashSet<>(
                        Set.of(createRole("BRAND_ADMIN"))
                )
        );
    }

    private Role createRole(String code) {

        Role role = new Role();

        role.setCode(code);
        role.setName(code);

        return role;
    }

    private void mockCurrentUser() {

        when(currentUserService.getCurrentUserId())
                .thenReturn(activeUser.getId());

        when(userRepository.findById(activeUser.getId()))
                .thenReturn(Optional.of(activeUser));
    }

    // Test 1: Brand Admin profile
    @Test
    void shouldReturnBrandAdminProfile() {

        mockCurrentUser();

        ProfileResponse response =
                profileService.getMyProfile();

        assertNotNull(response);

        assertEquals(7L, response.userId());
        assertEquals("luxuryadmin", response.username());
        assertEquals(
                "admin@luxury.example",
                response.email()
        );
        assertEquals(
                "Luxury Brand Admin",
                response.fullName()
        );

        assertEquals(3L, response.tenantId());
        assertEquals("LUXURY-PER", response.tenantCode());
        assertEquals("Luxury Perfume", response.tenantName());

        assertEquals(
                Set.of("BRAND_ADMIN"),
                response.roles()
        );

        assertEquals("ACTIVE", response.status());
        assertFalse(response.mustChangePassword());

        verify(userRepository).findById(7L);
    }

    // Test 2: Platform Super Admin has no tenant
    @Test
    void shouldReturnPlatformAdminWithoutTenant() {

        activeUser.setId(2L);
        activeUser.setUsername("platformadmin");
        activeUser.setEmail("platform@example.com");
        activeUser.setFullName("Platform Administrator");
        activeUser.setTenant(null);

        activeUser.setRoles(
                Set.of(createRole("PLATFORM_SUPER_ADMIN"))
        );

        mockCurrentUser();

        ProfileResponse response =
                profileService.getMyProfile();

        assertEquals(2L, response.userId());
        assertEquals("platformadmin", response.username());

        assertNull(response.tenantId());
        assertNull(response.tenantCode());
        assertNull(response.tenantName());

        assertEquals(
                Set.of("PLATFORM_SUPER_ADMIN"),
                response.roles()
        );

        assertEquals("ACTIVE", response.status());
        assertFalse(response.mustChangePassword());
    }

    // Test 3: Operations Manager profile
    @Test
    void shouldReturnOperationsManagerProfile() {

        activeUser.setUsername("operations");
        activeUser.setRoles(
                Set.of(createRole("OPERATIONS_MANAGER"))
        );

        mockCurrentUser();

        ProfileResponse response =
                profileService.getMyProfile();

        assertEquals("operations", response.username());
        assertEquals(3L, response.tenantId());
        assertEquals("Luxury Perfume", response.tenantName());

        assertEquals(
                Set.of("OPERATIONS_MANAGER"),
                response.roles()
        );
    }

    // Test 4: Technician profile
    @Test
    void shouldReturnTechnicianProfile() {

        activeUser.setUsername("technician");
        activeUser.setRoles(
                Set.of(createRole("TECHNICIAN"))
        );

        mockCurrentUser();

        ProfileResponse response =
                profileService.getMyProfile();

        assertEquals("technician", response.username());
        assertEquals(3L, response.tenantId());

        assertEquals(
                Set.of("TECHNICIAN"),
                response.roles()
        );
    }

    // Test 5: Inventory Staff profile
    @Test
    void shouldReturnInventoryStaffProfile() {

        activeUser.setUsername("inventory");
        activeUser.setRoles(
                Set.of(createRole("INVENTORY_STAFF"))
        );

        mockCurrentUser();

        ProfileResponse response =
                profileService.getMyProfile();

        assertEquals("inventory", response.username());
        assertEquals(3L, response.tenantId());

        assertEquals(
                Set.of("INVENTORY_STAFF"),
                response.roles()
        );
    }

    // Test 6: User has multiple roles
    @Test
    void shouldReturnAllUserRoles() {

        activeUser.setRoles(
                Set.of(
                        createRole("TECHNICIAN"),
                        createRole("INVENTORY_STAFF")
                )
        );

        mockCurrentUser();

        ProfileResponse response =
                profileService.getMyProfile();

        assertEquals(
                Set.of(
                        "TECHNICIAN",
                        "INVENTORY_STAFF"
                ),
                response.roles()
        );
    }

    // Test 7: User must change password
    @Test
    void shouldReturnMustChangePasswordTrue() {

        activeUser.setMustChangePassword(true);

        mockCurrentUser();

        ProfileResponse response =
                profileService.getMyProfile();

        assertTrue(response.mustChangePassword());
    }

    // Test 8: User does not need to change password
    @Test
    void shouldReturnMustChangePasswordFalse() {

        activeUser.setMustChangePassword(false);

        mockCurrentUser();

        ProfileResponse response =
                profileService.getMyProfile();

        assertFalse(response.mustChangePassword());
    }

    // Test 9: User does not exist
    @Test
    void shouldRejectNonexistentUser() {

        when(currentUserService.getCurrentUserId())
                .thenReturn(999L);

        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> profileService.getMyProfile()
        );

        assertEquals(
                "Account is unavailable",
                exception.getMessage()
        );
    }

    // Test 10: Locked account
    @Test
    void shouldRejectLockedAccount() {

        activeUser.setStatus("LOCKED");

        mockCurrentUser();

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> profileService.getMyProfile()
        );

        assertEquals(
                "Account is not active",
                exception.getMessage()
        );
    }

    // Test 11: Inactive account
    @Test
    void shouldRejectInactiveAccount() {

        activeUser.setStatus("INACTIVE");

        mockCurrentUser();

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> profileService.getMyProfile()
        );

        assertEquals(
                "Account is not active",
                exception.getMessage()
        );
    }

    // Test 12: Inactive tenant
    @Test
    void shouldRejectInactiveTenant() {

        tenant.setStatus("INACTIVE");

        mockCurrentUser();

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> profileService.getMyProfile()
        );

        assertEquals(
                "Tenant is not active",
                exception.getMessage()
        );
    }

    // Test 13: No authenticated user
    @Test
    void shouldRejectUnauthenticatedUser() {

        when(currentUserService.getCurrentUserId())
                .thenThrow(
                        new IllegalStateException(
                                "No authenticated user found"
                        )
                );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> profileService.getMyProfile()
        );

        assertEquals(
                "No authenticated user found",
                exception.getMessage()
        );

        verifyNoInteractions(userRepository);
    }

    // Test 14: Return updated tenant identity from database
    @Test
    void shouldReturnUpdatedTenantIdentity() {

        tenant.setCode("LUXURY-NEW");
        tenant.setName("Luxury Perfume Updated");

        mockCurrentUser();

        ProfileResponse response =
                profileService.getMyProfile();

        assertEquals(
                "LUXURY-NEW",
                response.tenantCode()
        );

        assertEquals(
                "Luxury Perfume Updated",
                response.tenantName()
        );
    }
}
