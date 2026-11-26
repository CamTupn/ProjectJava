package com.scentstation.filter;

import com.scentstation.common.security.CurrentUserPrincipal;
import com.scentstation.entity.Tenant;
import com.scentstation.entity.User;
import com.scentstation.repository.UserRepository;
import com.scentstation.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
                JwtService jwtService,
                UserRepository userRepository
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

         try {
            String authorizationHeader =
                    request.getHeader("Authorization");

            if (authorizationHeader != null
                    && authorizationHeader.startsWith("Bearer ")) {

                String token = authorizationHeader.substring(7).trim();

                if (!token.isEmpty()) {
                    authenticateToken(token, request);
                }
            }

        } catch (Exception exception) {
            // Không cho request sử dụng authentication
            // nếu token hoặc tài khoản không hợp lệ.
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateToken(
            String token,
            HttpServletRequest request
    ) {
        if (SecurityContextHolder.getContext()
                .getAuthentication() != null) {
            return;
        }

        // 1. Kiểm tra JWT và chữ ký
        String username = jwtService.extractUsername(token);

        if (username == null || username.isBlank()) {
            return;
        }

        if (!jwtService.isTokenValid(token, username)) {
            return;
        }

        // 2. Kiểm tra userId trong token
        Long tokenUserId = jwtService.extractUserId(token);

        if (tokenUserId == null) {
            return;
        }

        // 3. Lấy User hiện tại từ Database
        User user = userRepository.findById(tokenUserId)
                .orElse(null);

        if (user == null) {
            return;
        }

        // 4. Kiểm tra username khớp JWT
        if (!username.equals(user.getUsername())) {
            return;
        }

        // 5. Chỉ cho tài khoản ACTIVE
        if (!"ACTIVE".equals(user.getStatus())) {
            return;
        }

        // 6. Kiểm tra Tenant
        Tenant tenant = user.getTenant();

        if (tenant != null
                && !"ACTIVE".equals(tenant.getStatus())) {
            return;
        }

        // 7. Lấy role mới nhất từ Database
        Set<String> roles = user.getRoles()
                .stream()
                .map(role -> role.getCode())
                .filter(code -> code != null && !code.isBlank())
                .collect(Collectors.toUnmodifiableSet());

        if (roles.isEmpty()) {
            return;
        }

        // 8. Platform Admin không cần Tenant.
        // Các role khác bắt buộc thuộc Tenant.
        boolean isPlatformAdmin =
                roles.contains("PLATFORM_SUPER_ADMIN");

        if (!isPlatformAdmin && tenant == null) {
            return;
        }

        // Không cho role Platform Admin
        // đồng thời gắn vào Tenant.
        if (isPlatformAdmin && tenant != null) {
            return;
        }

        // 9. Tạo authorities từ quyền hiện tại
        List<SimpleGrantedAuthority> authorities =
                roles.stream()
                        .map(role ->
                                new SimpleGrantedAuthority(
                                        "ROLE_" + role
                                )
                        )
                        .toList();

        Long tenantId = tenant != null
                ? tenant.getId()
                : null;

        String tenantCode = tenant != null
                ? tenant.getCode()
                : null;

        // 10. Tạo principal từ Database
        CurrentUserPrincipal principal =
                new CurrentUserPrincipal(
                        user.getId(),
                        user.getUsername(),
                        tenantId,
                        tenantCode,
                        roles
                );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        authorities
                );

        authentication.setDetails(
                new WebAuthenticationDetailsSource()
                        .buildDetails(request)
        );

        // 11. Lưu authentication
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }
}