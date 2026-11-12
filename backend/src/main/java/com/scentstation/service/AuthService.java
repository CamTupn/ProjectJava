package com.scentstation.service;

import com.scentstation.dto.request.LoginRequest;
import com.scentstation.dto.response.AuthResponse;
import com.scentstation.entity.User;
import com.scentstation.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        // 1. Lấy username hoặc email
        String identifier = request.getUsernameOrEmail().trim();
        // 2. Tìm tài khoản bằng username hoặc email
        User user = userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid username/email or password"
                        )
                );
        // 3. Kiểm tra trạng thái tài khoản
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new IllegalArgumentException(
                    "Account is not active"
            );
        }
        // 4. Kiểm tra mật khẩu bằng BCrypt
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash())) {

            throw new IllegalArgumentException(
                    "Invalid username/email or password"
            );
        }
        // 5. Lấy danh sách Role
        Set<String> roles = user.getRoles()
                .stream()
                .map(role -> role.getCode())
                .collect(Collectors.toSet());

        // 6. Tạo response
        AuthResponse response = new AuthResponse();

        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());

        // Platform Super Admin có thể không thuộc Tenant
        if (user.getTenant() != null) {
            response.setTenantCode(
                    user.getTenant().getCode()
            );
        }

        response.setRoles(roles);
        // Generate JWT claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("tenantCode",
                user.getTenant() != null
                        ? user.getTenant().getCode()
                        : null);
        claims.put("roles", roles);

        // Generate access token
        String accessToken = jwtService.generateToken(
                user.getUsername(),
                claims
        );

        response.setAccessToken(accessToken);
        return response;
    }
}
