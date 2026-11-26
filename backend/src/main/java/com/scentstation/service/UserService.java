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
import com.scentstation.dto.request.CreateUserRequest;
import com.scentstation.entity.Role;
import com.scentstation.entity.Tenant;
import com.scentstation.repository.RoleRepository;
import com.scentstation.repository.TenantRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Locale;
import java.nio.charset.StandardCharsets;

@Service
@Transactional
public class UserService {

        private final UserRepository userRepository;
        private final UserMapper userMapper;
        private final CurrentUserService currentUserService;
        private final TenantRepository tenantRepository;
        private final RoleRepository roleRepository;
        private final PasswordEncoder passwordEncoder;

        public UserService(
                UserRepository userRepository,
                UserMapper userMapper,
                CurrentUserService currentUserService,
                TenantRepository tenantRepository,
                RoleRepository roleRepository,
                PasswordEncoder passwordEncoder) {

            this.userRepository = userRepository;
            this.userMapper = userMapper;
            this.currentUserService = currentUserService;
            this.tenantRepository = tenantRepository;
            this.roleRepository = roleRepository;
            this.passwordEncoder = passwordEncoder;
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

        validateUserManagementPermission(user);

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

        validateUserManagementPermission(user);

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

    private void validateUserManagementPermission(User targetUser) {
        CurrentUserPrincipal current =
            currentUserService.getCurrentUser();
        // Platform Super Admin được quản lý User
        // trên toàn hệ thống.
        if (current.isPlatformSuperAdmin()) {
                return;
        }
        // Chỉ Brand Admin mới có quyền quản lý User.
        if (!current.hasRole("BRAND_ADMIN")) {
                throw new AccessDeniedException(
                        "You do not have permission to manage users"
                );
        }
        // Brand Admin chỉ được quản lý User cùng tenant.
        if (current.tenantId() == null
                || targetUser.getTenant() == null
                || !current.tenantId().equals(
                            targetUser.getTenant().getId()
                )) {
                throw new AccessDeniedException(
                "You cannot manage users from another tenant"
                );
        }
        // Không cho Brand Admin quản lý
        // Platform Super Admin hoặc Brand Admin khác.
        boolean targetIsPrivileged = targetUser.getRoles()
            .stream()
            .anyMatch(role ->
                    "PLATFORM_SUPER_ADMIN".equals(role.getCode())
                    || "BRAND_ADMIN".equals(role.getCode())
            );

        if (targetIsPrivileged) {
                throw new AccessDeniedException(
                        "Brand Admin cannot manage administrator accounts"
                );
        }
   }

   // Tạo tài khoản người dùng có kiểm soát phân quyền
   public UserResponse createUser(CreateUserRequest request) {

           CurrentUserPrincipal current = currentUserService.getCurrentUser();

           boolean isSuperAdmin = current.isPlatformSuperAdmin();
           boolean isBrandAdmin = current.hasRole("BRAND_ADMIN");

           // 1. Kiểm tra quyền tạo tài khoản
           if (!isSuperAdmin && !isBrandAdmin) {
                   throw new AccessDeniedException(
                                   "You do not have permission to create users");
           }

           // 2. Chuẩn hóa dữ liệu
           String username = request.getUsername()
                           .trim()
                           .toLowerCase(Locale.ROOT);

           String email = request.getEmail()
                           .trim()
                           .toLowerCase(Locale.ROOT);

           String fullName = request.getFullName().trim();

           String roleCode = request.getRoleCode()
                           .trim()
                           .toUpperCase(Locale.ROOT);

           // 3. Kiểm tra role hợp lệ
           Set<String> employeeRoles = Set.of(
                           "OPERATIONS_MANAGER",
                           "TECHNICIAN",
                           "INVENTORY_STAFF");

           Set<String> allowedRoles = Set.of(
                           "BRAND_ADMIN",
                           "OPERATIONS_MANAGER",
                           "TECHNICIAN",
                           "INVENTORY_STAFF");

           // Không cho tạo Platform Super Admin qua API
           if ("PLATFORM_SUPER_ADMIN".equals(roleCode)) {
                   throw new AccessDeniedException(
                                   "Platform Super Admin accounts cannot be created via this API");
           }

           if (!allowedRoles.contains(roleCode)) {
                   throw new IllegalArgumentException(
                                   "Invalid role code: " + roleCode);
           }

           // 4. Brand Admin chỉ được tạo tài khoản nhân viên
           if (!isSuperAdmin && !employeeRoles.contains(roleCode)) {
                   throw new AccessDeniedException(
                                   "Brand Admin can only create employee accounts");
           }

           // 5. Xác định tenant
           Long tenantId;

           if (isSuperAdmin) {

                   tenantId = request.getTenantId();

                   if (tenantId == null) {
                           throw new IllegalArgumentException(
                                           "Tenant ID is required");
                   }

           } else {

                   tenantId = current.tenantId();

                   if (tenantId == null) {
                           throw new AccessDeniedException(
                                           "Current user does not belong to a tenant");
                   }

                   if (request.getTenantId() != null
                                   && !tenantId.equals(request.getTenantId())) {

                           throw new AccessDeniedException(
                                           "You cannot create users for another tenant");
                   }
           }

           // 6. Kiểm tra tenant tồn tại
           Tenant tenant = tenantRepository.findById(tenantId)
                           .orElseThrow(() -> new ResourceNotFoundException(
                                           "Tenant not found"));

           if (!"ACTIVE".equals(tenant.getStatus())) {
                   throw new ResourceConflictException(
                                   "Cannot create users for an inactive tenant");
           }

           // 7. Kiểm tra username và email trùng
           if (userRepository.existsByUsername(username)) {
                   throw new ResourceConflictException(
                                   "Username already exists");
           }

           if (userRepository.existsByEmail(email)) {
                   throw new ResourceConflictException(
                                   "Email already exists");
           }

           // 8. Lấy role từ database
           Role role = roleRepository.findByCode(roleCode)
                           .orElseThrow(() -> new ResourceNotFoundException(
                                           "Role not found: " + roleCode));

           // 9. Kiểm tra giới hạn BCrypt
           if (request.getPassword()
                           .getBytes(StandardCharsets.UTF_8).length > 72) {

                   throw new IllegalArgumentException(
                                   "Password must not exceed 72 bytes");
           }

           // 10. Tạo tài khoản
           User user = new User();

           user.setUsername(username);
           user.setEmail(email);
           user.setFullName(fullName);

           user.setPasswordHash(
                           passwordEncoder.encode(request.getPassword()));

           user.setTenant(tenant);
           user.setStatus("ACTIVE");

           Set<Role> roles = new HashSet<>();
           roles.add(role);

           user.setRoles(roles);

           // 11. Lưu tài khoản vào database
           try {

                   User savedUser = userRepository.saveAndFlush(user);

                   return userMapper.toResponse(savedUser);

           } catch (DataIntegrityViolationException ex) {

                   throw new ResourceConflictException(
                                   "User could not be created due to a data constraint");
           }
   }
}