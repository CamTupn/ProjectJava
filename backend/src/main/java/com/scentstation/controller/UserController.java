package com.scentstation.controller;

import com.scentstation.dto.request.UpdateUserRequest;
import com.scentstation.dto.response.UserResponse;
import com.scentstation.service.UserService;
import com.scentstation.dto.request.CreateUserRequest;
import com.scentstation.common.response.ApiResponse;
import com.scentstation.dto.request.ResetPasswordRequest;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Xem danh sách người dùng
    @GetMapping
    @PreAuthorize(
        "hasAnyRole('PLATFORM_SUPER_ADMIN', 'BRAND_ADMIN')"
    )
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // Xem chi tiết người dùng
    @GetMapping("/{id}")
    @PreAuthorize(
        "hasAnyRole('PLATFORM_SUPER_ADMIN', 'BRAND_ADMIN')"
    )
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                userService.getUserById(id)
        );
    }

    // Cập nhật thông tin người dùng
    @PutMapping("/{id}")
    @PreAuthorize(
        "hasAnyRole('PLATFORM_SUPER_ADMIN', 'BRAND_ADMIN')"
    )
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {

        return ResponseEntity.ok(
                userService.updateUser(id, request)
        );
    }

    // Khóa, mở khóa hoặc vô hiệu hóa tài khoản
    @PatchMapping("/{id}/status")
    @PreAuthorize(
        "hasAnyRole('PLATFORM_SUPER_ADMIN', 'BRAND_ADMIN')"
    )
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        return ResponseEntity.ok(
                userService.updateUserStatus(id, status)
        );
    }

    // Tạo tài khoản người dùng
    @PostMapping
    @PreAuthorize("hasAnyRole('PLATFORM_SUPER_ADMIN', 'BRAND_ADMIN')")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.createUser(request));
    }


    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAnyRole('PLATFORM_SUPER_ADMIN', 'BRAND_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody ResetPasswordRequest request) {

        userService.resetPassword(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Password reset successfully",
                        null));
    }

}
