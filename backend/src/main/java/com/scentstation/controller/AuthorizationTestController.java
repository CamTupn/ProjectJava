package com.scentstation.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test/authorization")
public class AuthorizationTestController {

    @GetMapping("/brand-admin")
    @PreAuthorize("hasRole('BRAND_ADMIN')")
    public String brandAdminOnly() {
        return "Access granted: BRAND_ADMIN";
    }

    @GetMapping("/technician")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public String technicianOnly() {
        return "Access granted: TECHNICIAN";
    }

    @GetMapping("/platform-admin")
    @PreAuthorize("hasRole('PLATFORM_SUPER_ADMIN')")
    public String platformAdminOnly() {
        return "Access granted: PLATFORM_SUPER_ADMIN";
    }
}
