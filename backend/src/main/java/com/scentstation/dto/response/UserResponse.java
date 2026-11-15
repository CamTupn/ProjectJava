package com.scentstation.dto.response;

import java.time.LocalDateTime;
import java.util.Set;

public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String status;
    private Long tenantId;
    private String tenantCode;
    private Set<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserResponse(
            Long id,
            String username,
            String email,
            String fullName,
            String status,
            Long tenantId,
            String tenantCode,
            Set<String> roles,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.status = status;
        this.tenantId = tenantId;
        this.tenantCode = tenantCode;
        this.roles = roles;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getStatus() { return status; }
    public Long getTenantId() { return tenantId; }
    public String getTenantCode() { return tenantCode; }
    public Set<String> getRoles() { return roles; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
