package com.scentstation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateTenantRequest {

    @NotBlank(message = "Tenant name is required")
    @Size(max = 150)
    private String name;

    @NotBlank(message = "Tenant code is required")
    @Size(max = 50)
    @Pattern(
        regexp = "^[a-zA-Z0-9_-]+$",
        message = "Tenant code contains invalid characters"
    )
    private String code;

    public CreateTenantRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}