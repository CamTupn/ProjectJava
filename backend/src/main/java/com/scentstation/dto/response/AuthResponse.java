package com.scentstation.dto.response;

import java.util.Set;

public class AuthResponse {
    
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private String tenantCode;
    private Set<String> roles;

    public AuthResponse() {}

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId){
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }   
    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getTenantCode() {
        return tenantCode;
    }
    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public Set<String> getRoles() {
        return roles;
    }
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
