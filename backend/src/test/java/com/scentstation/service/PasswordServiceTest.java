package com.scentstation.service;

import com.scentstation.common.security.CurrentUserPrincipal;
import com.scentstation.common.security.CurrentUserService;
import com.scentstation.dto.request.ChangePasswordRequest;
import com.scentstation.dto.request.ResetPasswordRequest;
import com.scentstation.entity.Role;
import com.scentstation.entity.Tenant;
import com.scentstation.entity.User;
import com.scentstation.exception.ResourceNotFoundException;
import com.scentstation.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private PasswordService passwordService;

    private static final String OLD_PASSWORD = "OldPassword123";
    private static final String NEW_PASSWORD = "NewPassword456";
    private static final String TEMP_PASSWORD = "Temporary123";

    private static final String OLD_HASH = "old-password-hash";
    private static final String NEW_HASH = "new-password-hash";
    private static final String TEMP_HASH = "temporary-password-hash";

    private Tenant tenantA;
    private Tenant tenantB;

    private User employeeA;
    private User employeeB;
    private User brandAdmin;
    private User superAdmin;

    @BeforeEach
    void setUp() {

        tenantA = createTenant(
                1L,
                "SCENT-DEMO",
                "ScentStation Demo"
        );

        tenantB = createTenant(
                2L,
                "AROMA-DEMO",
                "Aroma House"
        );

        employeeA = createUser(
                10L,
                "employee_a",
                tenantA,
                "TECHNICIAN"
        );

        employeeB = createUser(
                30L,
                "employee_b",
                tenantB,
                "INVENTORY_STAFF"
        );

        brandAdmin = createUser(
                20L,
                "brand_admin",
                tenantA,
                "BRAND_ADMIN"
        );

        superAdmin = createUser(
                1L,
                "platform_admin",
                null,
                "PLATFORM_SUPER_ADMIN"
        );
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    private Tenant createTenant(
            Long id,
            String code,
            String name) {

        Tenant tenant = new Tenant();

        tenant.setId(id);
        tenant.setCode(code);
        tenant.setName(name);
        tenant.setStatus("ACTIVE");

        return tenant;
    }

    private Role createRole(String code) {

        Role role = new Role();

        role.setCode(code);
        role.setName(code);

        return role;
    }

    private User createUser(
            Long id,
            String username,
            Tenant tenant,
            String roleCode) {

        User user = new User();

        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setFullName(username);
        user.setPasswordHash(OLD_HASH);
        user.setStatus("ACTIVE");
        user.setTenant(tenant);
        user.setMustChangePassword(false);
        user.setTokenVersion(0L);

        user.setRoles(
                new HashSet<>(
                        Set.of(createRole(roleCode))
                )
        );

        return user;
    }

    private void mockCurrentUser(User user) {

        mockCurrentUser(user, user.getTokenVersion());
    }

    private void mockCurrentUser(
            User user,
            long tokenVersion) {

        Tenant tenant = user.getTenant();

        Set<String> roles = new HashSet<>();

        for (Role role : user.getRoles()) {
            roles.add(role.getCode());
        }

        CurrentUserPrincipal principal =
                new CurrentUserPrincipal(
                        user.getId(),
                        user.getUsername(),
                        tenant != null ? tenant.getId() : null,
                        tenant != null ? tenant.getCode() : null,
                        roles,
                        tokenVersion
                );

        when(currentUserService.getCurrentUser())
                .thenReturn(principal);
    }

    private void mockLockedUser(User user) {

        when(userRepository.findByIdForUpdate(user.getId()))
                .thenReturn(Optional.of(user));
    }

    private void prepareChangePassword(User user) {

        mockCurrentUser(user);
        mockLockedUser(user);
    }

    private void verifyNoPasswordUpdate() {

        verify(userRepository, never())
                .save(any(User.class));

        verify(passwordEncoder, never())
                .encode(anyString());
    }

    // ==========================================
    // CHANGE PASSWORD TESTS
    // ==========================================

    // Test 1: Change password successfully
    @Test
    void shouldChangePasswordSuccessfully() {

        employeeA.setTokenVersion(3L);

        prepareChangePassword(employeeA);

        when(passwordEncoder.matches(
                OLD_PASSWORD, OLD_HASH))
                .thenReturn(true);

        when(passwordEncoder.matches(
                NEW_PASSWORD, OLD_HASH))
                .thenReturn(false);

        when(passwordEncoder.encode(NEW_PASSWORD))
                .thenReturn(NEW_HASH);

        passwordService.changePassword(
                new ChangePasswordRequest(
                        OLD_PASSWORD,
                        NEW_PASSWORD
                )
        );

        assertEquals(
                NEW_HASH,
                employeeA.getPasswordHash()
        );

        assertFalse(employeeA.isMustChangePassword());

        assertEquals(
                4L,
                employeeA.getTokenVersion()
        );

        verify(userRepository)
                .findByIdForUpdate(10L);

        verify(userRepository)
                .save(employeeA);
    }

    // Test 2: Clear forced password change flag
    @Test
    void shouldClearMustChangePasswordAfterChange() {

        employeeA.setMustChangePassword(true);
        employeeA.setTokenVersion(5L);

        prepareChangePassword(employeeA);

        when(passwordEncoder.matches(
                OLD_PASSWORD, OLD_HASH))
                .thenReturn(true);

        when(passwordEncoder.matches(
                NEW_PASSWORD, OLD_HASH))
                .thenReturn(false);

        when(passwordEncoder.encode(NEW_PASSWORD))
                .thenReturn(NEW_HASH);

        passwordService.changePassword(
                new ChangePasswordRequest(
                        OLD_PASSWORD,
                        NEW_PASSWORD
                )
        );

        assertFalse(employeeA.isMustChangePassword());
        assertEquals(6L, employeeA.getTokenVersion());
        assertEquals(NEW_HASH, employeeA.getPasswordHash());

        verify(userRepository).save(employeeA);
    }

    // Test 3: Reject incorrect current password
    @Test
    void shouldRejectIncorrectCurrentPassword() {

        prepareChangePassword(employeeA);

        when(passwordEncoder.matches(
                "WrongPassword123", OLD_HASH))
                .thenReturn(false);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> passwordService.changePassword(
                                new ChangePasswordRequest(
                                        "WrongPassword123",
                                        NEW_PASSWORD
                                )
                        )
                );

        assertEquals(
                "Current password is incorrect",
                exception.getMessage()
        );

        assertEquals(OLD_HASH, employeeA.getPasswordHash());
        assertEquals(0L, employeeA.getTokenVersion());

        verifyNoPasswordUpdate();
    }

    // Test 4: Reject reuse of current password
    @Test
    void shouldRejectSamePassword() {

        prepareChangePassword(employeeA);

        when(passwordEncoder.matches(
                OLD_PASSWORD, OLD_HASH))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> passwordService.changePassword(
                                new ChangePasswordRequest(
                                        OLD_PASSWORD,
                                        OLD_PASSWORD
                                )
                        )
                );

        assertEquals(
                "New password must be different from current password",
                exception.getMessage()
        );

        assertEquals(0L, employeeA.getTokenVersion());

        verifyNoPasswordUpdate();
    }

    // Test 5: Reject short password
    @Test
    void shouldRejectShortNewPassword() {

        prepareChangePassword(employeeA);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> passwordService.changePassword(
                                new ChangePasswordRequest(
                                        OLD_PASSWORD,
                                        "1234567"
                                )
                        )
                );

        assertEquals(
                "New password must contain at least 8 characters",
                exception.getMessage()
        );

        verifyNoPasswordUpdate();
    }

    // Test 6: Reject password exceeding 72 UTF-8 bytes
    @Test
    void shouldRejectNewPasswordExceeding72Bytes() {

        prepareChangePassword(employeeA);

        String longPassword = "ệ".repeat(25);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> passwordService.changePassword(
                                new ChangePasswordRequest(
                                        OLD_PASSWORD,
                                        longPassword
                                )
                        )
                );

        assertEquals(
                "New password must not exceed 72 bytes",
                exception.getMessage()
        );

        verifyNoPasswordUpdate();
    }

    // Test 7: Reject blank current password
    @Test
    void shouldRejectBlankCurrentPassword() {

        prepareChangePassword(employeeA);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> passwordService.changePassword(
                                new ChangePasswordRequest(
                                        "   ",
                                        NEW_PASSWORD
                                )
                        )
                );

        assertEquals(
                "Current password is required",
                exception.getMessage()
        );

        verifyNoPasswordUpdate();
    }

    // Test 8: Reject blank new password
    @Test
    void shouldRejectBlankNewPassword() {

        prepareChangePassword(employeeA);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> passwordService.changePassword(
                                new ChangePasswordRequest(
                                        OLD_PASSWORD,
                                        "   "
                                )
                        )
                );

        assertEquals(
                "New password is required",
                exception.getMessage()
        );

        verifyNoPasswordUpdate();
    }

    // Test 9: Reject locked account
    @Test
    void shouldRejectLockedAccount() {

        employeeA.setStatus("LOCKED");

        prepareChangePassword(employeeA);

        assertThrows(
                AccessDeniedException.class,
                () -> passwordService.changePassword(
                        new ChangePasswordRequest(
                                OLD_PASSWORD,
                                NEW_PASSWORD
                        )
                )
        );

        verifyNoPasswordUpdate();
    }

    // Test 10: Reject inactive account
    @Test
    void shouldRejectInactiveAccount() {

        employeeA.setStatus("INACTIVE");

        prepareChangePassword(employeeA);

        assertThrows(
                AccessDeniedException.class,
                () -> passwordService.changePassword(
                        new ChangePasswordRequest(
                                OLD_PASSWORD,
                                NEW_PASSWORD
                        )
                )
        );

        verifyNoPasswordUpdate();
    }

    // Test 11: Reject inactive tenant
    @Test
    void shouldRejectInactiveTenant() {

        tenantA.setStatus("INACTIVE");

        prepareChangePassword(employeeA);

        assertThrows(
                AccessDeniedException.class,
                () -> passwordService.changePassword(
                        new ChangePasswordRequest(
                                OLD_PASSWORD,
                                NEW_PASSWORD
                        )
                )
        );

        verifyNoPasswordUpdate();
    }

    // Test 12: Reject nonexistent account
    @Test
    void shouldRejectNonexistentAccount() {

        mockCurrentUser(employeeA);

        when(userRepository.findByIdForUpdate(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                AccessDeniedException.class,
                () -> passwordService.changePassword(
                        new ChangePasswordRequest(
                                OLD_PASSWORD,
                                NEW_PASSWORD
                        )
                )
        );

        verifyNoPasswordUpdate();
    }

    // Test 13: Reject revoked session after acquiring lock
    @Test
    void shouldRejectRevokedSession() {

        employeeA.setTokenVersion(5L);

        mockCurrentUser(employeeA, 4L);
        mockLockedUser(employeeA);

        AccessDeniedException exception =
                assertThrows(
                        AccessDeniedException.class,
                        () -> passwordService.changePassword(
                                new ChangePasswordRequest(
                                        OLD_PASSWORD,
                                        NEW_PASSWORD
                                )
                        )
                );

        assertEquals(
                "Session is no longer valid. Please log in again",
                exception.getMessage()
        );

        assertEquals(5L, employeeA.getTokenVersion());
        assertEquals(OLD_HASH, employeeA.getPasswordHash());

        verifyNoInteractions(passwordEncoder);

        verify(userRepository, never())
                .save(any(User.class));
    }

    // ==========================================
    // RESET PASSWORD TESTS
    // ==========================================

    // Test 14: Platform Super Admin resets Brand Admin
    @Test
    void superAdminShouldResetBrandAdminPassword() {

        brandAdmin.setTokenVersion(2L);

        mockCurrentUser(superAdmin);
        mockLockedUser(brandAdmin);

        when(passwordEncoder.matches(
                TEMP_PASSWORD, OLD_HASH))
                .thenReturn(false);

        when(passwordEncoder.encode(TEMP_PASSWORD))
                .thenReturn(TEMP_HASH);

        passwordService.resetPassword(
                20L,
                new ResetPasswordRequest(TEMP_PASSWORD)
        );

        assertEquals(
                TEMP_HASH,
                brandAdmin.getPasswordHash()
        );

        assertTrue(brandAdmin.isMustChangePassword());

        assertEquals(
                3L,
                brandAdmin.getTokenVersion()
        );

        verify(userRepository)
                .save(brandAdmin);
    }

    // Test 15: Brand Admin resets own employee
    @Test
    void brandAdminShouldResetOwnEmployeePassword() {

        mockCurrentUser(brandAdmin);
        mockLockedUser(employeeA);

        when(passwordEncoder.matches(
                TEMP_PASSWORD, OLD_HASH))
                .thenReturn(false);

        when(passwordEncoder.encode(TEMP_PASSWORD))
                .thenReturn(TEMP_HASH);

        passwordService.resetPassword(
                10L,
                new ResetPasswordRequest(TEMP_PASSWORD)
        );

        assertEquals(
                TEMP_HASH,
                employeeA.getPasswordHash()
        );

        assertTrue(employeeA.isMustChangePassword());
        assertEquals(1L, employeeA.getTokenVersion());

        verify(userRepository)
                .save(employeeA);
    }

    // Test 16: Reject cross-tenant reset
    @Test
    void brandAdminShouldNotResetOtherTenantEmployee() {

        mockCurrentUser(brandAdmin);
        mockLockedUser(employeeB);

        assertThrows(
                ResourceNotFoundException.class,
                () -> passwordService.resetPassword(
                        30L,
                        new ResetPasswordRequest(TEMP_PASSWORD)
                )
        );

        assertEquals(OLD_HASH, employeeB.getPasswordHash());
        assertEquals(0L, employeeB.getTokenVersion());

        verifyNoPasswordUpdate();
    }

    // Test 17: Brand Admin cannot reset another Brand Admin
    @Test
    void brandAdminShouldNotResetAnotherBrandAdmin() {

        User anotherBrandAdmin = createUser(
                21L,
                "another_admin",
                tenantA,
                "BRAND_ADMIN"
        );

        mockCurrentUser(brandAdmin);
        mockLockedUser(anotherBrandAdmin);

        assertThrows(
                AccessDeniedException.class,
                () -> passwordService.resetPassword(
                        21L,
                        new ResetPasswordRequest(TEMP_PASSWORD)
                )
        );

        verifyNoPasswordUpdate();
    }

    // Test 18: Protect Platform Super Admin accounts
    @Test
    void superAdminShouldNotResetAnotherSuperAdmin() {

        User anotherSuperAdmin = createUser(
                2L,
                "another_platform_admin",
                null,
                "PLATFORM_SUPER_ADMIN"
        );

        mockCurrentUser(superAdmin);
        mockLockedUser(anotherSuperAdmin);

        assertThrows(
                AccessDeniedException.class,
                () -> passwordService.resetPassword(
                        2L,
                        new ResetPasswordRequest(TEMP_PASSWORD)
                )
        );

        verifyNoPasswordUpdate();
    }

    // Test 19: Prevent self-reset
    @Test
    void adminShouldNotResetOwnPassword() {

        mockCurrentUser(brandAdmin);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> passwordService.resetPassword(
                                20L,
                                new ResetPasswordRequest(TEMP_PASSWORD)
                        )
                );

        assertEquals(
                "Use change password to update your own password",
                exception.getMessage()
        );

        verifyNoInteractions(
                userRepository,
                passwordEncoder
        );
    }

    // Test 20: Employee cannot reset passwords
    @Test
    void employeeShouldNotResetPassword() {

        mockCurrentUser(employeeA);

        assertThrows(
                AccessDeniedException.class,
                () -> passwordService.resetPassword(
                        30L,
                        new ResetPasswordRequest(TEMP_PASSWORD)
                )
        );

        verifyNoInteractions(
                userRepository,
                passwordEncoder
        );
    }

    // Test 21: Reject nonexistent target account
    @Test
    void shouldRejectResetForNonexistentUser() {

        mockCurrentUser(superAdmin);

        when(userRepository.findByIdForUpdate(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> passwordService.resetPassword(
                        999L,
                        new ResetPasswordRequest(TEMP_PASSWORD)
                )
        );

        verifyNoPasswordUpdate();
    }

    // Test 22: Reject short temporary password
    @Test
    void shouldRejectShortTemporaryPassword() {

        mockCurrentUser(brandAdmin);
        mockLockedUser(employeeA);

        assertThrows(
                IllegalArgumentException.class,
                () -> passwordService.resetPassword(
                        10L,
                        new ResetPasswordRequest("1234567")
                )
        );

        assertEquals(0L, employeeA.getTokenVersion());
        assertFalse(employeeA.isMustChangePassword());

        verifyNoPasswordUpdate();
    }

    // Test 23: Reject temporary password exceeding 72 bytes
    @Test
    void shouldRejectTemporaryPasswordExceeding72Bytes() {

        mockCurrentUser(brandAdmin);
        mockLockedUser(employeeA);

        String longPassword = "ệ".repeat(25);

        assertThrows(
                IllegalArgumentException.class,
                () -> passwordService.resetPassword(
                        10L,
                        new ResetPasswordRequest(longPassword)
                )
        );

        verifyNoPasswordUpdate();
    }

    // Test 24: Reject reuse of current password during reset
    @Test
    void shouldRejectTemporaryPasswordMatchingCurrentPassword() {

        mockCurrentUser(brandAdmin);
        mockLockedUser(employeeA);

        when(passwordEncoder.matches(
                OLD_PASSWORD, OLD_HASH))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> passwordService.resetPassword(
                                10L,
                                new ResetPasswordRequest(OLD_PASSWORD)
                        )
                );

        assertEquals(
                "Temporary password must be different from current password",
                exception.getMessage()
        );

        assertEquals(0L, employeeA.getTokenVersion());

        verifyNoPasswordUpdate();
    }

    // Test 25: Reject target with mixed employee/admin roles
    @Test
    void brandAdminShouldNotResetUserWithMixedRoles() {

        employeeA.getRoles().add(
                createRole("BRAND_ADMIN")
        );

        mockCurrentUser(brandAdmin);
        mockLockedUser(employeeA);

        assertThrows(
                AccessDeniedException.class,
                () -> passwordService.resetPassword(
                        10L,
                        new ResetPasswordRequest(TEMP_PASSWORD)
                )
        );

        assertEquals(OLD_HASH, employeeA.getPasswordHash());
        assertEquals(0L, employeeA.getTokenVersion());

        verifyNoPasswordUpdate();
    }
}