package com.scentstation.controller;

import com.scentstation.common.response.ApiResponse;
import com.scentstation.dto.request.LoginRequest;
import com.scentstation.dto.request.ChangePasswordRequest;
import com.scentstation.dto.response.AuthResponse;
import com.scentstation.dto.response.ProfileResponse;
import com.scentstation.service.AuthService;
import com.scentstation.service.ProfileService;
import com.scentstation.service.PasswordService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final ProfileService profileService;
    private final PasswordService passwordService;

    public AuthController(
            AuthService authService,
            ProfileService profileService,
            PasswordService passwordService) {

        this.authService = authService;
        this.profileService = profileService;
        this.passwordService = passwordService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Login successful",
                        response
                )
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> getMyProfile() {

        ProfileResponse response =
                profileService.getMyProfile();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Profile retrieved successfully",
                        response
                )
        );
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        passwordService.changePassword(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Password changed successfully",
                        null
                )
        );
    }
}
