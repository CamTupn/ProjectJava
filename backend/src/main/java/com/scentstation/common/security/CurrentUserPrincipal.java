package com.scentstation.common.security;

import java.util.Set;

public record CurrentUserPrincipal(
        Long userId,
        String username,
        Long tenantId,
        String tenantCode,
        Set<String> roles
) {
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    public boolean isPlatformSuperAdmin() {
        return hasRole("PLATFORM_SUPER_ADMIN");
    }
}
