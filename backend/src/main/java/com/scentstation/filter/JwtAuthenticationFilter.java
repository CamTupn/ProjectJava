package com.scentstation.filter;

import com.scentstation.common.security.CurrentUserPrincipal;
import com.scentstation.common.response.ApiResponse;
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

import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository) {

        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        User authenticatedUser = null;

        try {

            String authorizationHeader =
                    request.getHeader("Authorization");

            if (authorizationHeader != null
                    && authorizationHeader.startsWith("Bearer ")) {

                String token =
                        authorizationHeader.substring(7).trim();

                if (!token.isEmpty()) {
                    authenticatedUser =
                            authenticateToken(token, request);
                }
            }

        } catch (Exception exception) {

            SecurityContextHolder.clearContext();
        }

        // Force password change before accessing other APIs
        if (authenticatedUser != null
                && authenticatedUser.isMustChangePassword()
                && !isAllowedDuringPasswordChange(request)) {

            SecurityContextHolder.clearContext();

            response.setStatus(
                    HttpServletResponse.SC_FORBIDDEN
            );

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            ApiResponse<Void> body = ApiResponse.error(
                    "Password change is required",
                    Map.of()
            );

            response.getWriter().write(
                    new ObjectMapper().writeValueAsString(body)
            );

            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAllowedDuringPasswordChange(
            HttpServletRequest request) {

        String path = request.getRequestURI()
                .substring(request.getContextPath().length());

        String method = request.getMethod();

        if ("OPTIONS".equals(method)) {
            return true;
        }

        if ("POST".equals(method)
                && "/api/v1/auth/login".equals(path)) {
            return true;
        }

        if ("GET".equals(method)
                && "/api/v1/auth/me".equals(path)) {
            return true;
        }

        if ("POST".equals(method)
                && "/api/v1/auth/change-password".equals(path)) {
            return true;
        }

        return false;
    }

    private User authenticateToken(
            String token,
            HttpServletRequest request) {

        if (SecurityContextHolder.getContext()
                .getAuthentication() != null) {
            return null;
        }

        // 1. Validate JWT
        String username =
                jwtService.extractUsername(token);

        if (username == null || username.isBlank()) {
            return null;
        }

        if (!jwtService.isTokenValid(token, username)) {
            return null;
        }

        // 2. Extract user ID and token version
        Long tokenUserId =
                jwtService.extractUserId(token);

        Long tokenVersion =
                jwtService.extractTokenVersion(token);

        if (tokenUserId == null
                || tokenVersion == null) {
            return null;
        }

        // 3. Load current user from database
        User user = userRepository.findById(tokenUserId)
                .orElse(null);

        if (user == null) {
            return null;
        }

        // 4. Validate username
        if (!username.equals(user.getUsername())) {
            return null;
        }

        // 5. Reject old tokens
        if (tokenVersion.longValue()
                != user.getTokenVersion()) {
            return null;
        }

        // 6. Validate account status
        if (!"ACTIVE".equals(user.getStatus())) {
            return null;
        }

        // 7. Validate tenant
        Tenant tenant = user.getTenant();

        if (tenant != null
                && !"ACTIVE".equals(tenant.getStatus())) {
            return null;
        }

        // 8. Load current roles from database
        Set<String> roles = user.getRoles()
                .stream()
                .map(role -> role.getCode())
                .filter(code ->
                        code != null && !code.isBlank())
                .collect(Collectors.toUnmodifiableSet());

        if (roles.isEmpty()) {
            return null;
        }

        boolean isPlatformAdmin =
                roles.contains("PLATFORM_SUPER_ADMIN");

        if (!isPlatformAdmin && tenant == null) {
            return null;
        }

        if (isPlatformAdmin && tenant != null) {
            return null;
        }

        // 9. Create Spring Security authorities
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

        // 10. Create authenticated principal
        CurrentUserPrincipal principal =
                new CurrentUserPrincipal(
                        user.getId(),
                        user.getUsername(),
                        tenantId,
                        tenantCode,
                        roles,
                        tokenVersion.longValue()
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

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        return user;
    }
}
