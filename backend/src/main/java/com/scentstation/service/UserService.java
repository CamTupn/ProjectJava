package com.scentstation.service;

import com.scentstation.common.security.CurrentUserPrincipal;
import com.scentstation.common.security.CurrentUserService;
import com.scentstation.dto.request.UpdateUserRequest;
import com.scentstation.dto.response.UserResponse;
import com.scentstation.entity.User;
import com.scentstation.exception.ResourceConflictException;
import com.scentstation.exception.ResourceNotFoundException;
import com.scentstation.mapper.UserMapper;
import com.scentstation.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CurrentUserService currentUserService;

    public UserService(
            UserRepository userRepository,
            UserMapper userMapper,
            CurrentUserService currentUserService) {

        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.currentUserService = currentUserService;
    }

    // 1. Xem danh sách người dùng
    public List<UserResponse> getAllUsers() {

        CurrentUserPrincipal current =
                currentUserService.getCurrentUser();

        List<User> users;

        if (current.isPlatformSuperAdmin()) {
            users = userRepository.findAll();
        } else {
            if (current.tenantId() == null) {
                throw new IllegalStateException(
                        "Current user does not belong to a tenant"
                );
            }

            users = userRepository.findAllByTenant_Id(
                    current.tenantId()
            );
        }

        return users.stream()
                .map(userMapper::toResponse)
                .toList();
    }

    // 2. Xem chi tiết người dùng
    public UserResponse getUserById(Long userId) {

        User user = findAccessibleUser(userId);

        return userMapper.toResponse(user);
    }

    // 3. Cập nhật thông tin người dùng
    public UserResponse updateUser(
            Long userId,
            UpdateUserRequest request) {

        User user = findAccessibleUser(userId);

        String email = request.getEmail().trim();

        if (userRepository.existsByEmailAndIdNot(
                email, userId)) {

            throw new ResourceConflictException(
                    "Email already exists: " + email
            );
        }

        user.setFullName(request.getFullName().trim());
        user.setEmail(email);

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    // 4. Quản lý trạng thái tài khoản
    public UserResponse updateUserStatus(
            Long userId,
            String status) {

        User user = findAccessibleUser(userId);

        String normalizedStatus =
                status.trim().toUpperCase();

        if (!List.of("ACTIVE", "LOCKED", "INACTIVE")
                .contains(normalizedStatus)) {

            throw new IllegalArgumentException(
                    "Invalid user status. Allowed: ACTIVE, LOCKED, INACTIVE"
            );
        }

        CurrentUserPrincipal current =
                currentUserService.getCurrentUser();

        // Không cho phép tự khóa hoặc vô hiệu hóa tài khoản
        if (user.getId().equals(current.userId())
                && !"ACTIVE".equals(normalizedStatus)) {

            throw new IllegalArgumentException(
                    "You cannot lock or deactivate your own account"
            );
        }

        user.setStatus(normalizedStatus);

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    // Kiểm tra quyền truy cập theo tenant
    private User findAccessibleUser(Long userId) {

        CurrentUserPrincipal current =
                currentUserService.getCurrentUser();

        if (current.isPlatformSuperAdmin()) {
            return userRepository.findById(userId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "User not found with ID: " + userId
                            )
                    );
        }

        if (current.tenantId() == null) {
            throw new IllegalStateException(
                    "Current user does not belong to a tenant"
            );
        }

        return userRepository.findByIdAndTenant_Id(
                userId,
                current.tenantId()
        ).orElseThrow(() ->
                new ResourceNotFoundException(
                        "User not found with ID: " + userId
                )
        );
    }
}