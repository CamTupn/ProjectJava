package com.scentstation.dto.response;

import java.util.Set;

public record ProfileResponse(

        Long userId,
        String username,
        String email,
        String fullName,

        Long tenantId,
        String tenantCode,
        String tenantName,

        Set<String> roles,
        String status,

        boolean mustChangePassword

) {
}
