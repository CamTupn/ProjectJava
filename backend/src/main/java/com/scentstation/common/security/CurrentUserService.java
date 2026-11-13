package com.scentstation.common.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    public CurrentUserPrincipal getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || !(authentication.getPrincipal()
                        instanceof CurrentUserPrincipal principal)) {

            throw new IllegalStateException(
                    "No authenticated user found"
            );
        }

        return principal;
    }

    public Long getCurrentUserId() {
        return getCurrentUser().userId();
    }

    public Long getCurrentTenantId() {
        return getCurrentUser().tenantId();
    }

    public boolean hasRole(String role) {
        return getCurrentUser().hasRole(role);
    }

    public boolean isPlatformAdmin() {
        return hasRole("PLATFORM_SUPER_ADMIN");
    }
}
