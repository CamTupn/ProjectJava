package com.scentstation.service;

import com.scentstation.common.security.CurrentUserService;
import com.scentstation.dto.response.ProfileResponse;
import com.scentstation.entity.User;
import com.scentstation.entity.Tenant;
import com.scentstation.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public ProfileService(
            UserRepository userRepository,
            CurrentUserService currentUserService) {

        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public ProfileResponse getMyProfile() {

        // Get authenticated user ID
        Long userId = currentUserService.getCurrentUserId();

        // Load current user information from database
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Account is unavailable"
                        )
                );

        // Validate account status
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new AccessDeniedException(
                    "Account is not active"
            );
        }

        Tenant tenant = user.getTenant();

        // Validate tenant status
        if (tenant != null
                && !"ACTIVE".equals(tenant.getStatus())) {

            throw new AccessDeniedException(
                    "Tenant is not active"
            );
        }

        // Get current roles
        Set<String> roles = user.getRoles()
                .stream()
                .map(role -> role.getCode())
                .collect(Collectors.toSet());

        // Build profile response
        return new ProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                tenant != null ? tenant.getId() : null,
                tenant != null ? tenant.getCode() : null,
                tenant != null ? tenant.getName() : null,
                roles,
                user.getStatus(),
                user.isMustChangePassword()
        );
    }
}