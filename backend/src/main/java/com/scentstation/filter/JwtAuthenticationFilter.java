package com.scentstation.filter;

import com.scentstation.common.security.CurrentUserPrincipal;
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

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String authorizationHeader =
                request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // 1. Read Bearer Token
        if (authorizationHeader != null
                && authorizationHeader.startsWith("Bearer ")) {

            jwt = authorizationHeader.substring(7);

            try {
                username = jwtService.extractUsername(jwt);
            } catch (Exception exception) {
                // Invalid or expired token.
            }
        }

        // 2. Validate token
        if (username != null
                && SecurityContextHolder.getContext()
                        .getAuthentication() == null
                && jwtService.isTokenValid(jwt, username)) {

            try {
                // 3. Extract user information
                Long userId = jwtService.extractUserId(jwt);
                Long tenantId = jwtService.extractTenantId(jwt);
                String tenantCode = jwtService.extractTenantCode(jwt);

                Set<String> roles = Set.copyOf(
                        jwtService.extractRoles(jwt)
                );

                // 4. Convert roles to Spring Security authorities
                List<SimpleGrantedAuthority> authorities =
                        roles.stream()
                                .map(role -> new SimpleGrantedAuthority(
                                        "ROLE_" + role
                                ))
                                .toList();

                // 5. Create current user principal
                CurrentUserPrincipal principal =
                        new CurrentUserPrincipal(
                                userId,
                                username,
                                tenantId,
                                tenantCode,
                                roles
                        );

                // 6. Create authentication
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

                // 7. Save authentication to Security Context
                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);

            } catch (Exception exception) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
