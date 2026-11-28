package com.scentstation.service;

import com.scentstation.common.security.CurrentUserService;
import com.scentstation.dto.request.ChangePasswordRequest;
import com.scentstation.entity.User;
import com.scentstation.repository.UserRepository;
import com.scentstation.common.security.CurrentUserPrincipal;
import com.scentstation.dto.request.ResetPasswordRequest;
import com.scentstation.exception.ResourceNotFoundException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Set;

@Service
public class PasswordService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;

    public PasswordService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            CurrentUserService currentUserService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {

        // 1. Get authenticated user ID
        CurrentUserPrincipal current = currentUserService.getCurrentUser();

        Long userId = current.userId();

        // 2. Find and lock user
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Account is not available"
                        )
                );
        // Revalidate token version after acquiring the lock
        if (current.tokenVersion() != user.getTokenVersion()) {
            throw new AccessDeniedException(
                    "Session is no longer valid. Please log in again");
        }

        // 3. Validate account status
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new AccessDeniedException(
                    "Account is not active"
            );
        }

        // 4. Validate tenant status
        if (user.getTenant() != null
                && !"ACTIVE".equals(user.getTenant().getStatus())) {

            throw new AccessDeniedException(
                    "Account is not active"
            );
        }

        // 5. Validate password input
        String currentPassword = request.currentPassword();
        String newPassword = request.newPassword();

        if (currentPassword == null
                || currentPassword.isBlank()) {

            throw new IllegalArgumentException(
                    "Current password is required"
            );
        }

        if (currentPassword.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException(
                    "Invalid current password"
            );
        }

        validateNewPassword(newPassword);

        // 6. Verify current password
        if (!passwordEncoder.matches(
                currentPassword,
                user.getPasswordHash())) {

            throw new IllegalArgumentException(
                    "Current password is incorrect"
            );
        }

        // 7. Prevent reusing current password
        if (passwordEncoder.matches(
                newPassword,
                user.getPasswordHash())) {

            throw new IllegalArgumentException(
                    "New password must be different from current password"
            );
        }

        // 8. Encode and update password
        user.setPasswordHash(
                passwordEncoder.encode(newPassword)
        );

        // 9. Remove forced password change flag
        user.setMustChangePassword(false);

        // 10. Increment token version
        user.setTokenVersion(
                user.getTokenVersion() + 1
        );

        // 11. Persist changes
        userRepository.save(user);
    }

    private void validateNewPassword(String password) {

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "New password is required"
            );
        }

        if (password.length() < 8) {
            throw new IllegalArgumentException(
                    "New password must contain at least 8 characters"
            );
        }

        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException(
                    "New password must not exceed 72 bytes"
            );
        }
    }

    @Transactional
    public void resetPassword(
            Long userId,
            ResetPasswordRequest request) {

        CurrentUserPrincipal current = currentUserService.getCurrentUser();

        boolean isSuperAdmin = current.isPlatformSuperAdmin();

        boolean isBrandAdmin = current.hasRole("BRAND_ADMIN");

        // 1. Validate administrator permission
        if (!isSuperAdmin && !isBrandAdmin) {
            throw new AccessDeniedException(
                    "You do not have permission to reset passwords");
        }

        // 2. Prevent self-reset
        if (userId.equals(current.userId())) {
            throw new IllegalArgumentException(
                    "Use change password to update your own password");
        }

        // 3. Find and lock target user
        User targetUser = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found"));

        // 4. Validate tenant isolation
        if (!isSuperAdmin) {

            if (current.tenantId() == null
                    || targetUser.getTenant() == null
                    || !current.tenantId().equals(
                            targetUser.getTenant().getId())) {

                throw new ResourceNotFoundException(
                        "User not found");
            }
        }

        // 5. Protect administrator accounts
        boolean targetIsSuperAdmin = targetUser.getRoles()
                .stream()
                .anyMatch(role -> "PLATFORM_SUPER_ADMIN".equals(role.getCode()));

        if (targetIsSuperAdmin) {
            throw new AccessDeniedException(
                    "Platform Super Admin password cannot be reset");
        }

        Set<String> employeeRoles = Set.of(
                "OPERATIONS_MANAGER",
                "TECHNICIAN",
                "INVENTORY_STAFF");

        if (!isSuperAdmin) {

            boolean employeeOnly = !targetUser.getRoles().isEmpty()
                    && targetUser.getRoles()
                            .stream()
                            .allMatch(role -> employeeRoles.contains(role.getCode()));

            if (!employeeOnly) {
                throw new AccessDeniedException(
                        "Brand Admin can only reset employee passwords");
            }
        }

        // 6. Validate temporary password
        String temporaryPassword = request.temporaryPassword();

        validateNewPassword(temporaryPassword);

        if (passwordEncoder.matches(
                temporaryPassword,
                targetUser.getPasswordHash())) {

            throw new IllegalArgumentException(
                    "Temporary password must be different from current password");
        }

        // 7. Update password security information
        targetUser.setPasswordHash(
                passwordEncoder.encode(temporaryPassword));

        targetUser.setMustChangePassword(true);

        targetUser.setTokenVersion(
                targetUser.getTokenVersion() + 1);

        // 8. Save changes
        userRepository.save(targetUser);
    }

}
