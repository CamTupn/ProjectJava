package com.scentstation.service;

import com.scentstation.common.security.CurrentUserPrincipal;
import com.scentstation.common.security.CurrentUserService;
import com.scentstation.dto.request.UpdateUserRequest;
import com.scentstation.entity.Role;
import com.scentstation.entity.Tenant;
import com.scentstation.entity.User;
import com.scentstation.exception.ResourceNotFoundException;
import com.scentstation.mapper.UserMapper;
import com.scentstation.repository.UserRepository;
import com.scentstation.dto.request.CreateUserRequest;
import com.scentstation.exception.ResourceConflictException;
import com.scentstation.repository.TenantRepository;
import com.scentstation.repository.RoleRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordService passwordService;

    @InjectMocks
    private UserService userService;

    private Tenant tenantA;
    private Tenant tenantB;

    private User employeeA;
    private User brandAdminA;
    private User brandAdminB;
    private User employeeB;

    @BeforeEach
    void setUp() {

        tenantA = createTenant(1L, "BRAND-A");
        tenantB = createTenant(2L, "BRAND-B");

        employeeA = createUser(
                10L,
                tenantA,
                "employee_a",
                "INVENTORY_STAFF"
        );

        brandAdminA = createUser(
                20L,
                tenantA,
                "admin_a",
                "BRAND_ADMIN"
        );

        brandAdminB = createUser(
                21L,
                tenantA,
                "admin_b",
                "BRAND_ADMIN"
        );

        employeeB = createUser(
                30L,
                tenantB,
                "employee_b",
                "TECHNICIAN"
        );
    }

    // =========================================
    // TEST 1: SUPER ADMIN
    // =========================================

    @Test
    void superAdminShouldUpdateAnyUser() {

        mockCurrentUser(
                1L,
                null,
                "PLATFORM_SUPER_ADMIN"
        );

        when(userRepository.findById(30L))
                .thenReturn(Optional.of(employeeB));

        when(userRepository.existsByEmailAndIdNot(
                "updated@example.com", 30L
        )).thenReturn(false);

        when(userRepository.save(employeeB))
                .thenReturn(employeeB);

        UpdateUserRequest request = createUpdateRequest(
                "Updated Employee",
                "updated@example.com"
        );

        userService.updateUser(30L, request);

        assertEquals(
                "Updated Employee",
                employeeB.getFullName()
        );

        assertEquals(
                "updated@example.com",
                employeeB.getEmail()
        );

        verify(userRepository).save(employeeB);
    }

    // =========================================
    // TEST 2: BRAND ADMIN - SAME TENANT
    // =========================================

    @Test
    void brandAdminShouldUpdateEmployeeInSameTenant() {

        mockCurrentUser(
                20L,
                1L,
                "BRAND_ADMIN"
        );

        when(userRepository.findByIdAndTenant_Id(
                10L, 1L
        )).thenReturn(Optional.of(employeeA));

        when(userRepository.existsByEmailAndIdNot(
                "new@example.com", 10L
        )).thenReturn(false);

        when(userRepository.save(employeeA))
                .thenReturn(employeeA);

        UpdateUserRequest request = createUpdateRequest(
                "New Employee Name",
                "new@example.com"
        );

        userService.updateUser(10L, request);

        assertEquals(
                "New Employee Name",
                employeeA.getFullName()
        );

        verify(userRepository).save(employeeA);
    }

    // =========================================
    // TEST 3: BRAND ADMIN - OTHER ADMIN
    // =========================================

    @Test
    void brandAdminShouldNotUpdateAnotherBrandAdmin() {

        mockCurrentUser(
                20L,
                1L,
                "BRAND_ADMIN"
        );

        when(userRepository.findByIdAndTenant_Id(
                21L, 1L
        )).thenReturn(Optional.of(brandAdminB));

        UpdateUserRequest request = createUpdateRequest(
                "Changed Admin",
                "changed@example.com"
        );

        assertThrows(
                AccessDeniedException.class,
                () -> userService.updateUser(21L, request)
        );

        verify(userRepository, never())
                .save(any(User.class));
    }

    // =========================================
    // TEST 4: BRAND ADMIN - SELF UPDATE
    // =========================================

    @Test
    void brandAdminShouldNotUpdateOwnAdminAccount() {

        mockCurrentUser(
                20L,
                1L,
                "BRAND_ADMIN"
        );

        when(userRepository.findByIdAndTenant_Id(
                20L, 1L
        )).thenReturn(Optional.of(brandAdminA));

        UpdateUserRequest request = createUpdateRequest(
                "Changed Name",
                "self@example.com"
        );

        assertThrows(
                AccessDeniedException.class,
                () -> userService.updateUser(20L, request)
        );

        verify(userRepository, never())
                .save(any(User.class));
    }

    // =========================================
    // TEST 5: CROSS-TENANT
    // =========================================

    @Test
    void brandAdminShouldNotUpdateOtherTenantUser() {

        mockCurrentUser(
                20L,
                1L,
                "BRAND_ADMIN"
        );

        when(userRepository.findByIdAndTenant_Id(
                30L, 1L
        )).thenReturn(Optional.empty());

        UpdateUserRequest request = createUpdateRequest(
                "Hacked Name",
                "hacked@example.com"
        );

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.updateUser(30L, request)
        );

        verify(userRepository, never())
                .save(any(User.class));
    }

    // =========================================
    // TEST 6: LOCK EMPLOYEE
    // =========================================

    @Test
    void brandAdminShouldLockEmployeeInSameTenant() {

            mockCurrentUser(
                            20L,
                            1L,
                            "BRAND_ADMIN");

            when(userRepository.findByIdForUpdate(10L))
                            .thenReturn(Optional.of(employeeA));

            when(userRepository.save(employeeA))
                            .thenReturn(employeeA);

            userService.updateUserStatus(10L, "LOCKED");

            assertEquals("LOCKED", employeeA.getStatus());
            assertEquals(1L, employeeA.getTokenVersion());

            verify(userRepository).findByIdForUpdate(10L);
            verify(userRepository).save(employeeA);
    }

    // =========================================
    // TEST 7: LOCK ANOTHER ADMIN
    // =========================================

    @Test
    void brandAdminShouldNotLockAnotherBrandAdmin() {

            mockCurrentUser(
                            20L,
                            1L,
                            "BRAND_ADMIN");

            when(userRepository.findByIdForUpdate(21L))
                            .thenReturn(Optional.of(brandAdminB));

            assertThrows(
                            AccessDeniedException.class,
                            () -> userService.updateUserStatus(
                                            21L,
                                            "LOCKED"));

            assertEquals(0L, brandAdminB.getTokenVersion());

            verify(userRepository, never())
                            .save(any(User.class));
    }

    // =========================================
    // TEST 8: SUPER ADMIN SELF LOCK
    // =========================================

    @Test
    void superAdminShouldNotLockOwnAccount() {

            mockCurrentUser(
                            1L,
                            null,
                            "PLATFORM_SUPER_ADMIN");

            User superAdmin = createUser(
                            1L,
                            null,
                            "platform_admin",
                            "PLATFORM_SUPER_ADMIN");

            when(userRepository.findByIdForUpdate(1L))
                            .thenReturn(Optional.of(superAdmin));

            assertThrows(
                            IllegalArgumentException.class,
                            () -> userService.updateUserStatus(
                                            1L,
                                            "LOCKED"));

            assertEquals(0L, superAdmin.getTokenVersion());

            verify(userRepository, never())
                            .save(any(User.class));
    }

    // =========================================
    // TEST 9: INVALID STATUS
    // =========================================

    @Test
    void invalidUserStatusShouldBeRejected() {

            mockCurrentUser(
                            1L,
                            null,
                            "PLATFORM_SUPER_ADMIN");

            when(userRepository.findByIdForUpdate(10L))
                            .thenReturn(Optional.of(employeeA));

            assertThrows(
                            IllegalArgumentException.class,
                            () -> userService.updateUserStatus(
                                            10L,
                                            "DELETED"));

            assertEquals("ACTIVE", employeeA.getStatus());
            assertEquals(0L, employeeA.getTokenVersion());

            verify(userRepository, never())
                            .save(any(User.class));
    }
    // =========================================
    // TEST 10: EMPLOYEE CANNOT MANAGE USERS
    // =========================================

    @Test
    void technicianShouldNotUpdateUsers() {

        mockCurrentUser(
                30L,
                2L,
                "TECHNICIAN"
        );

        when(userRepository.findByIdAndTenant_Id(
                30L, 2L
        )).thenReturn(Optional.of(employeeB));

        UpdateUserRequest request = createUpdateRequest(
                "Changed",
                "changed@example.com"
        );

        assertThrows(
                AccessDeniedException.class,
                () -> userService.updateUser(30L, request)
        );

        verify(userRepository, never())
                .save(any(User.class));
    }

    // =========================================
    // HELPER METHODS
    // =========================================

    private void mockCurrentUser(
            Long userId,
            Long tenantId,
            String role
    ) {

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

    private Tenant createTenant(
            Long id,
            String code
    ) {

        Tenant tenant = new Tenant();

        tenant.setId(id);
        tenant.setCode(code);
        tenant.setName(code);
        tenant.setStatus("ACTIVE");

        return tenant;
    }

    private User createUser(
            Long id,
            Tenant tenant,
            String username,
            String roleCode
    ) {

        User user = new User();

        user.setId(id);
        user.setTenant(tenant);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setFullName(username);
        user.setStatus("ACTIVE");

        Role role = new Role();
        role.setCode(roleCode);

        user.setRoles(Set.of(role));

        return user;
    }

    private UpdateUserRequest createUpdateRequest(
            String fullName,
            String email
    ) {

        UpdateUserRequest request =
                new UpdateUserRequest();

        request.setFullName(fullName);
        request.setEmail(email);

        return request;
    }

    // =========================================
    // CREATE USER TESTS
    // =========================================

    @Test
    void superAdminShouldCreateBrandAdmin() {

            mockCurrentUser(1L, null, "PLATFORM_SUPER_ADMIN");

            CreateUserRequest request = createUserRequest(1L, "BRAND_ADMIN");

            mockSuccessfulUserCreation(tenantA, "BRAND_ADMIN");

            userService.createUser(request);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

            verify(userRepository).saveAndFlush(captor.capture());

            User savedUser = captor.getValue();

            assertEquals("new_user", savedUser.getUsername());
            assertEquals("new@example.com", savedUser.getEmail());
            assertEquals("New User", savedUser.getFullName());
            assertEquals("ACTIVE", savedUser.getStatus());
            assertEquals(tenantA, savedUser.getTenant());
            assertEquals("encoded-password", savedUser.getPasswordHash());
            assertTrue(savedUser.isMustChangePassword());
            assertEquals(0L, savedUser.getTokenVersion());

            assertEquals(
                            Set.of("BRAND_ADMIN"),
                            savedUser.getRoles().stream()
                                            .map(Role::getCode)
                                            .collect(java.util.stream.Collectors.toSet()));

            assertNotEquals(
                            request.getPassword(),
                            savedUser.getPasswordHash());

            verify(passwordEncoder).encode(request.getPassword());
    }

    @Test
    void brandAdminShouldCreateEmployeeInOwnTenant() {

            mockCurrentUser(20L, 1L, "BRAND_ADMIN");

            CreateUserRequest request = createUserRequest(null, "TECHNICIAN");

            mockSuccessfulUserCreation(tenantA, "TECHNICIAN");

            userService.createUser(request);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

            verify(userRepository).saveAndFlush(captor.capture());

            assertEquals(
                            1L,
                            captor.getValue().getTenant().getId());
            assertTrue(captor.getValue().isMustChangePassword());
            assertEquals(0L, captor.getValue().getTokenVersion());
    }

    @Test
    void brandAdminShouldNotCreateUserInAnotherTenant() {

            mockCurrentUser(20L, 1L, "BRAND_ADMIN");

            CreateUserRequest request = createUserRequest(2L, "TECHNICIAN");

            assertThrows(
                            AccessDeniedException.class,
                            () -> userService.createUser(request));

            verifyNoInteractions(tenantRepository);
            verifyNoInteractions(passwordEncoder);
            verifyNoInteractions(userRepository);
    }

    @Test
    void brandAdminShouldNotCreateAnotherBrandAdmin() {

            mockCurrentUser(20L, 1L, "BRAND_ADMIN");

            CreateUserRequest request = createUserRequest(1L, "BRAND_ADMIN");

            assertThrows(
                            AccessDeniedException.class,
                            () -> userService.createUser(request));

            verifyNoInteractions(userRepository);
            verifyNoInteractions(passwordEncoder);
    }

    @Test
    void superAdminShouldNotCreatePlatformSuperAdmin() {

            mockCurrentUser(1L, null, "PLATFORM_SUPER_ADMIN");

            CreateUserRequest request = createUserRequest(null, "PLATFORM_SUPER_ADMIN");

            assertThrows(
                            AccessDeniedException.class,
                            () -> userService.createUser(request));

            verifyNoInteractions(userRepository);
            verifyNoInteractions(passwordEncoder);
    }

    @Test
    void technicianShouldNotCreateUser() {

            mockCurrentUser(30L, 2L, "TECHNICIAN");

            CreateUserRequest request = createUserRequest(2L, "INVENTORY_STAFF");

            assertThrows(
                            AccessDeniedException.class,
                            () -> userService.createUser(request));

            verifyNoInteractions(userRepository);
            verifyNoInteractions(passwordEncoder);
    }

    @Test
    void createUserShouldRejectDuplicateUsername() {

            mockCurrentUser(1L, null, "PLATFORM_SUPER_ADMIN");

            CreateUserRequest request = createUserRequest(1L, "TECHNICIAN");

            when(tenantRepository.findById(1L))
                            .thenReturn(Optional.of(tenantA));

            when(userRepository.existsByUsername("new_user"))
                            .thenReturn(true);

            assertThrows(
                            ResourceConflictException.class,
                            () -> userService.createUser(request));

            verify(userRepository, never())
                            .saveAndFlush(any(User.class));

            verifyNoInteractions(passwordEncoder);
    }

    @Test
    void createUserShouldRejectDuplicateEmail() {

            mockCurrentUser(1L, null, "PLATFORM_SUPER_ADMIN");

            CreateUserRequest request = createUserRequest(1L, "TECHNICIAN");

            when(tenantRepository.findById(1L))
                            .thenReturn(Optional.of(tenantA));

            when(userRepository.existsByEmail("new@example.com"))
                            .thenReturn(true);

            assertThrows(
                            ResourceConflictException.class,
                            () -> userService.createUser(request));

            verify(userRepository, never())
                            .saveAndFlush(any(User.class));

            verifyNoInteractions(passwordEncoder);
    }

    @Test
    void createUserShouldRejectNonexistentTenant() {

            mockCurrentUser(1L, null, "PLATFORM_SUPER_ADMIN");

            CreateUserRequest request = createUserRequest(999L, "TECHNICIAN");

            when(tenantRepository.findById(999L))
                            .thenReturn(Optional.empty());

            assertThrows(
                            ResourceNotFoundException.class,
                            () -> userService.createUser(request));

            verifyNoInteractions(userRepository);
            verifyNoInteractions(passwordEncoder);
    }

    @Test
    void createUserShouldRejectInactiveTenant() {

            mockCurrentUser(1L, null, "PLATFORM_SUPER_ADMIN");

            tenantA.setStatus("INACTIVE");

            CreateUserRequest request = createUserRequest(1L, "TECHNICIAN");

            when(tenantRepository.findById(1L))
                            .thenReturn(Optional.of(tenantA));

            assertThrows(
                            ResourceConflictException.class,
                            () -> userService.createUser(request));

            verifyNoInteractions(userRepository);
            verifyNoInteractions(passwordEncoder);
    }

    @Test
    void createUserShouldRejectInvalidRole() {

            mockCurrentUser(1L, null, "PLATFORM_SUPER_ADMIN");

            CreateUserRequest request = createUserRequest(1L, "UNKNOWN_ROLE");

            assertThrows(
                            IllegalArgumentException.class,
                            () -> userService.createUser(request));

            verifyNoInteractions(userRepository);
            verifyNoInteractions(passwordEncoder);
    }

    @Test
    void superAdminShouldProvideTenantId() {

            mockCurrentUser(1L, null, "PLATFORM_SUPER_ADMIN");

            CreateUserRequest request = createUserRequest(null, "TECHNICIAN");

            assertThrows(
                            IllegalArgumentException.class,
                            () -> userService.createUser(request));

            verifyNoInteractions(tenantRepository);
            verifyNoInteractions(userRepository);
    }

    @Test
    void createUserShouldRejectMissingRoleInDatabase() {

            mockCurrentUser(1L, null, "PLATFORM_SUPER_ADMIN");

            CreateUserRequest request = createUserRequest(1L, "TECHNICIAN");

            when(tenantRepository.findById(1L))
                            .thenReturn(Optional.of(tenantA));

            when(roleRepository.findByCode("TECHNICIAN"))
                            .thenReturn(Optional.empty());

            assertThrows(
                            ResourceNotFoundException.class,
                            () -> userService.createUser(request));

            verify(userRepository, never())
                            .saveAndFlush(any(User.class));

            verifyNoInteractions(passwordEncoder);
    }

    @Test
    void createUserShouldRejectPasswordExceedingBCryptLimit() {

            mockCurrentUser(1L, null, "PLATFORM_SUPER_ADMIN");

            CreateUserRequest request = createUserRequest(1L, "TECHNICIAN");

            request.setPassword("a".repeat(73));

            Role role = new Role();
            role.setCode("TECHNICIAN");

            when(tenantRepository.findById(1L))
                            .thenReturn(Optional.of(tenantA));

            when(roleRepository.findByCode("TECHNICIAN"))
                            .thenReturn(Optional.of(role));

            assertThrows(
                            IllegalArgumentException.class,
                            () -> userService.createUser(request));

            verify(userRepository, never())
                            .saveAndFlush(any(User.class));

            verifyNoInteractions(passwordEncoder);
    }

    @Test
    void createUserShouldNormalizeUsernameAndEmail() {

            mockCurrentUser(1L, null, "PLATFORM_SUPER_ADMIN");

            CreateUserRequest request = createUserRequest(1L, "technician");

            request.setUsername(" NEW_USER ");
            request.setEmail(" NEW@EXAMPLE.COM ");
            request.setFullName(" New User ");

            mockSuccessfulUserCreation(tenantA, "TECHNICIAN");

            userService.createUser(request);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

            verify(userRepository).saveAndFlush(captor.capture());

            assertEquals(
                            "new_user",
                            captor.getValue().getUsername());

            assertEquals(
                            "new@example.com",
                            captor.getValue().getEmail());

            assertEquals(
                            "New User",
                            captor.getValue().getFullName());

            verify(userRepository).existsByUsername("new_user");
            verify(userRepository).existsByEmail("new@example.com");
    }

    private CreateUserRequest createUserRequest(
                    Long tenantId,
                    String roleCode) {

            CreateUserRequest request = new CreateUserRequest();

            request.setUsername("new_user");
            request.setEmail("new@example.com");
            request.setPassword("TestPassword123!");
            request.setFullName("New User");
            request.setTenantId(tenantId);
            request.setRoleCode(roleCode);

            return request;
    }

    private void mockSuccessfulUserCreation(
                    Tenant tenant,
                    String roleCode) {

            Role role = new Role();
            role.setCode(roleCode);

            when(tenantRepository.findById(tenant.getId()))
                            .thenReturn(Optional.of(tenant));

            when(roleRepository.findByCode(roleCode))
                            .thenReturn(Optional.of(role));

            when(passwordEncoder.encode("TestPassword123!"))
                            .thenReturn("encoded-password");

            when(userRepository.saveAndFlush(any(User.class)))
                            .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void unlockingUserShouldIncrementTokenVersion() {

            mockCurrentUser(
                            20L,
                            1L,
                            "BRAND_ADMIN");

            employeeA.setStatus("LOCKED");
            employeeA.setTokenVersion(3L);

            when(userRepository.findByIdForUpdate(10L))
                            .thenReturn(Optional.of(employeeA));

            when(userRepository.save(employeeA))
                            .thenReturn(employeeA);

            userService.updateUserStatus(10L, "ACTIVE");

            assertEquals("ACTIVE", employeeA.getStatus());
            assertEquals(4L, employeeA.getTokenVersion());

            verify(userRepository).save(employeeA);
    }

    @Test
    void unchangedStatusShouldNotIncrementTokenVersion() {

            mockCurrentUser(
                            20L,
                            1L,
                            "BRAND_ADMIN");

            employeeA.setStatus("ACTIVE");
            employeeA.setTokenVersion(5L);

            when(userRepository.findByIdForUpdate(10L))
                            .thenReturn(Optional.of(employeeA));

            userService.updateUserStatus(10L, "ACTIVE");

            assertEquals("ACTIVE", employeeA.getStatus());
            assertEquals(5L, employeeA.getTokenVersion());

            verify(userRepository, never())
                            .save(any(User.class));
    }

    @Test
    void brandAdminShouldNotChangeOtherTenantUserStatus() {

            mockCurrentUser(
                            20L,
                            1L,
                            "BRAND_ADMIN");

            when(userRepository.findByIdForUpdate(30L))
                            .thenReturn(Optional.of(employeeB));

            assertThrows(
                            ResourceNotFoundException.class,
                            () -> userService.updateUserStatus(
                                            30L,
                                            "LOCKED"));

            assertEquals("ACTIVE", employeeB.getStatus());
            assertEquals(0L, employeeB.getTokenVersion());

            verify(userRepository, never())
                            .save(any(User.class));
    }

    @Test
    void deactivatingUserShouldIncrementTokenVersion() {

            mockCurrentUser(
                            1L,
                            null,
                            "PLATFORM_SUPER_ADMIN");

            employeeA.setTokenVersion(2L);

            when(userRepository.findByIdForUpdate(10L))
                            .thenReturn(Optional.of(employeeA));

            when(userRepository.save(employeeA))
                            .thenReturn(employeeA);

            userService.updateUserStatus(10L, "INACTIVE");

            assertEquals("INACTIVE", employeeA.getStatus());
            assertEquals(3L, employeeA.getTokenVersion());

            verify(userRepository).save(employeeA);
    }

    @Test
    void updateStatusShouldRejectNonexistentUser() {

            mockCurrentUser(
                            1L,
                            null,
                            "PLATFORM_SUPER_ADMIN");

            when(userRepository.findByIdForUpdate(999L))
                            .thenReturn(Optional.empty());

            assertThrows(
                            ResourceNotFoundException.class,
                            () -> userService.updateUserStatus(
                                            999L,
                                            "LOCKED"));

            verify(userRepository, never())
                            .save(any(User.class));
    }
}
