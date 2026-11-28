package com.scentstation.filter;

import com.scentstation.common.security.CurrentUserPrincipal;
import com.scentstation.entity.Role;
import com.scentstation.entity.Tenant;
import com.scentstation.entity.User;
import com.scentstation.repository.UserRepository;
import com.scentstation.service.JwtService;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String SECRET =
            Base64.getEncoder()
                    .encodeToString(new byte[32]);

    @Mock
    private UserRepository userRepository;

    @Mock
    private FilterChain filterChain;

    private JwtService jwtService;

    private JwtAuthenticationFilter jwtFilter;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    private User user;

    private Tenant tenant;

    @BeforeEach
    void setUp() {

        SecurityContextHolder.clearContext();

        jwtService = new JwtService(SECRET, 60000);

        jwtFilter = new JwtAuthenticationFilter(
                jwtService,
                userRepository
        );

        request = new MockHttpServletRequest(
                "GET",
                "/api/v1/users"
        );

        response = new MockHttpServletResponse();

        tenant = new Tenant();
        tenant.setId(3L);
        tenant.setCode("LUXURY-PER");
        tenant.setName("Luxury Perfume");
        tenant.setStatus("ACTIVE");

        user = new User();
        user.setId(7L);
        user.setUsername("luxuryadmin");
        user.setEmail("admin@luxury.example");
        user.setFullName("Luxury Brand Admin");
        user.setStatus("ACTIVE");
        user.setTenant(tenant);
        user.setTokenVersion(3L);
        user.setMustChangePassword(false);

        user.setRoles(
                Set.of(createRole("BRAND_ADMIN"))
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Role createRole(String code) {

        Role role = new Role();

        role.setCode(code);
        role.setName(code);

        return role;
    }

    private String createToken(
            String username,
            Long userId,
            Long tokenVersion) {

        Map<String, Object> claims = new HashMap<>();

        if (userId != null) {
            claims.put("userId", userId);
        }

        if (tokenVersion != null) {
            claims.put("tokenVersion", tokenVersion);
        }

        return jwtService.generateToken(
                username,
                claims
        );
    }

    private void setBearerToken(String token) {

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );
    }

    private void mockUser() {

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
    }

    private void prepareValidToken() {

        mockUser();

        setBearerToken(
                createToken(
                        user.getUsername(),
                        user.getId(),
                        user.getTokenVersion()
                )
        );
    }

    private void executeFilter() throws Exception {

        jwtFilter.doFilter(
                request,
                response,
                filterChain
        );
    }

    private void assertUnauthenticated() throws Exception {

        assertNull(
                SecurityContextHolder.getContext()
                        .getAuthentication()
        );

        verify(filterChain)
                .doFilter(request, response);
    }

    private CurrentUserPrincipal getPrincipal() {

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());

        return assertInstanceOf(
                CurrentUserPrincipal.class,
                authentication.getPrincipal()
        );
    }

    private void assertPasswordChangeRequired()
            throws Exception {

        assertEquals(403, response.getStatus());

        assertTrue(
                response.getContentAsString()
                        .contains("Password change is required")
        );

        assertNull(
                SecurityContextHolder.getContext()
                        .getAuthentication()
        );

        verifyNoInteractions(filterChain);
    }

    // Test 1: Valid JWT
    @Test
    void shouldAuthenticateValidToken() throws Exception {

        prepareValidToken();

        executeFilter();

        CurrentUserPrincipal principal = getPrincipal();

        assertEquals(7L, principal.userId());
        assertEquals("luxuryadmin", principal.username());
        assertEquals(3L, principal.tenantId());
        assertEquals("LUXURY-PER", principal.tenantCode());
        assertEquals(Set.of("BRAND_ADMIN"), principal.roles());
        assertEquals(3L, principal.tokenVersion());

        verify(filterChain)
                .doFilter(request, response);
    }

    // Test 2: Missing Authorization header
    @Test
    void shouldNotAuthenticateWithoutToken() throws Exception {

        executeFilter();

        assertUnauthenticated();

        verifyNoInteractions(userRepository);
    }

    // Test 3: Invalid Authorization scheme
    @Test
    void shouldIgnoreNonBearerAuthorization()
            throws Exception {

        request.addHeader(
                "Authorization",
                "Basic invalid"
        );

        executeFilter();

        assertUnauthenticated();

        verifyNoInteractions(userRepository);
    }

    // Test 4: Empty Bearer token
    @Test
    void shouldRejectEmptyBearerToken() throws Exception {

        request.addHeader(
                "Authorization",
                "Bearer "
        );

        executeFilter();

        assertUnauthenticated();

        verifyNoInteractions(userRepository);
    }

    // Test 5: Invalid JWT
    @Test
    void shouldRejectInvalidToken() throws Exception {

        setBearerToken("invalid-jwt-token");

        executeFilter();

        assertUnauthenticated();

        verifyNoInteractions(userRepository);
    }

    // Test 6: Expired JWT
    @Test
    void shouldRejectExpiredToken() throws Exception {

        JwtService expiredJwtService =
                new JwtService(SECRET, -60000);

        String expiredToken =
                expiredJwtService.generateToken(
                        "luxuryadmin",
                        Map.of(
                                "userId", 7L,
                                "tokenVersion", 3L
                        )
                );

        setBearerToken(expiredToken);

        executeFilter();

        assertUnauthenticated();

        verifyNoInteractions(userRepository);
    }

    // Test 7: JWT without userId
    @Test
    void shouldRejectTokenWithoutUserId() throws Exception {

        setBearerToken(
                createToken("luxuryadmin", null, 3L)
        );

        executeFilter();

        assertUnauthenticated();

        verifyNoInteractions(userRepository);
    }

    // Test 8: Legacy JWT without tokenVersion
    @Test
    void shouldRejectTokenWithoutVersion() throws Exception {

        setBearerToken(
                createToken("luxuryadmin", 7L, null)
        );

        executeFilter();

        assertUnauthenticated();

        verifyNoInteractions(userRepository);
    }

    // Test 9: Old token after password change
    @Test
    void shouldRejectOldTokenAfterPasswordChange()
            throws Exception {

        user.setTokenVersion(4L);

        mockUser();

        setBearerToken(
                createToken("luxuryadmin", 7L, 3L)
        );

        executeFilter();

        assertUnauthenticated();
    }

    // Test 10: Old token after administrator reset
    @Test
    void shouldRejectOldTokenAfterPasswordReset()
            throws Exception {

        user.setTokenVersion(4L);
        user.setMustChangePassword(true);

        mockUser();

        setBearerToken(
                createToken("luxuryadmin", 7L, 3L)
        );

        executeFilter();

        assertUnauthenticated();
    }

    // Test 11: Incorrect future token version
    @Test
    void shouldRejectFutureTokenVersion()
            throws Exception {

        mockUser();

        setBearerToken(
                createToken("luxuryadmin", 7L, 99L)
        );

        executeFilter();

        assertUnauthenticated();
    }

    // Test 12: User no longer exists
    @Test
    void shouldRejectNonexistentUser() throws Exception {

        when(userRepository.findById(7L))
                .thenReturn(Optional.empty());

        setBearerToken(
                createToken("luxuryadmin", 7L, 3L)
        );

        executeFilter();

        assertUnauthenticated();
    }

    // Test 13: JWT username does not match database
    @Test
    void shouldRejectUsernameMismatch() throws Exception {

        mockUser();

        setBearerToken(
                createToken("anotheruser", 7L, 3L)
        );

        executeFilter();

        assertUnauthenticated();
    }

    // Test 14: Locked account
    @Test
    void shouldRejectLockedAccount() throws Exception {

        user.setStatus("LOCKED");

        prepareValidToken();

        executeFilter();

        assertUnauthenticated();
    }

    // Test 15: Inactive account
    @Test
    void shouldRejectInactiveAccount() throws Exception {

        user.setStatus("INACTIVE");

        prepareValidToken();

        executeFilter();

        assertUnauthenticated();
    }

    // Test 16: Inactive tenant
    @Test
    void shouldRejectInactiveTenant() throws Exception {

        tenant.setStatus("INACTIVE");

        prepareValidToken();

        executeFilter();

        assertUnauthenticated();
    }

    // Test 17: User without roles
    @Test
    void shouldRejectUserWithoutRoles() throws Exception {

        user.setRoles(Set.of());

        prepareValidToken();

        executeFilter();

        assertUnauthenticated();
    }

    // Test 18: Brand Admin without tenant
    @Test
    void shouldRejectBrandAdminWithoutTenant()
            throws Exception {

        user.setTenant(null);

        prepareValidToken();

        executeFilter();

        assertUnauthenticated();
    }

    // Test 19: Platform Super Admin without tenant
    @Test
    void shouldAuthenticatePlatformAdminWithoutTenant()
            throws Exception {

        user.setUsername("platformadmin");
        user.setTenant(null);

        user.setRoles(
                Set.of(createRole("PLATFORM_SUPER_ADMIN"))
        );

        prepareValidToken();

        executeFilter();

        CurrentUserPrincipal principal = getPrincipal();

        assertTrue(principal.isPlatformSuperAdmin());

        assertNull(principal.tenantId());
        assertNull(principal.tenantCode());

        assertEquals(3L, principal.tokenVersion());

        verify(filterChain)
                .doFilter(request, response);
    }

    // Test 20: Invalid Platform Super Admin tenant
    @Test
    void shouldRejectPlatformAdminWithTenant()
            throws Exception {

        user.setRoles(
                Set.of(createRole("PLATFORM_SUPER_ADMIN"))
        );

        prepareValidToken();

        executeFilter();

        assertUnauthenticated();
    }

    // Test 21: Use current database roles instead of JWT roles
    @Test
    void shouldUseRolesFromDatabase() throws Exception {

        user.setRoles(
                Set.of(createRole("TECHNICIAN"))
        );

        mockUser();

        String token = jwtService.generateToken(
                "luxuryadmin",
                Map.of(
                        "userId", 7L,
                        "tokenVersion", 3L,
                        "roles", Set.of("PLATFORM_SUPER_ADMIN")
                )
        );

        setBearerToken(token);

        executeFilter();

        CurrentUserPrincipal principal = getPrincipal();

        assertEquals(
                Set.of("TECHNICIAN"),
                principal.roles()
        );

        assertFalse(principal.isPlatformSuperAdmin());

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        assertTrue(
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                "ROLE_TECHNICIAN".equals(
                                        authority.getAuthority()
                                )
                        )
        );

        assertFalse(
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                "ROLE_PLATFORM_SUPER_ADMIN".equals(
                                        authority.getAuthority()
                                )
                        )
        );

        verify(filterChain)
                .doFilter(request, response);
    }

    // Test 22: Force password change blocks business API
    @Test
    void shouldBlockBusinessApiWhenPasswordChangeRequired()
            throws Exception {

        user.setMustChangePassword(true);

        prepareValidToken();

        executeFilter();

        assertPasswordChangeRequired();
    }

    // Test 23: Allow GET /auth/me
    @Test
    void shouldAllowProfileWhenPasswordChangeRequired()
            throws Exception {

        user.setMustChangePassword(true);

        request.setMethod("GET");
        request.setRequestURI("/api/v1/auth/me");

        prepareValidToken();

        executeFilter();

        getPrincipal();

        verify(filterChain)
                .doFilter(request, response);
    }

    // Test 24: Allow POST /auth/change-password
    @Test
    void shouldAllowChangePasswordWhenRequired()
            throws Exception {

        user.setMustChangePassword(true);

        request.setMethod("POST");

        request.setRequestURI(
                "/api/v1/auth/change-password"
        );

        prepareValidToken();

        executeFilter();

        getPrincipal();

        verify(filterChain)
                .doFilter(request, response);
    }

    // Test 25: Allow POST /auth/login
    @Test
    void shouldAllowLoginWhenPasswordChangeRequired()
            throws Exception {

        user.setMustChangePassword(true);

        request.setMethod("POST");
        request.setRequestURI("/api/v1/auth/login");

        prepareValidToken();

        executeFilter();

        verify(filterChain)
                .doFilter(request, response);
    }

    // Test 26: Allow OPTIONS requests
    @Test
    void shouldAllowOptionsWhenPasswordChangeRequired()
            throws Exception {

        user.setMustChangePassword(true);

        request.setMethod("OPTIONS");

        prepareValidToken();

        executeFilter();

        verify(filterChain)
                .doFilter(request, response);
    }

    // Test 27: Reject incorrect HTTP method on profile
    @Test
    void shouldBlockPostProfileWhenPasswordChangeRequired()
            throws Exception {

        user.setMustChangePassword(true);

        request.setMethod("POST");
        request.setRequestURI("/api/v1/auth/me");

        prepareValidToken();

        executeFilter();

        assertPasswordChangeRequired();
    }

    // Test 28: Reject incorrect method on change password
    @Test
    void shouldBlockGetChangePasswordWhenRequired()
            throws Exception {

        user.setMustChangePassword(true);

        request.setMethod("GET");

        request.setRequestURI(
                "/api/v1/auth/change-password"
        );

        prepareValidToken();

        executeFilter();

        assertPasswordChangeRequired();
    }

    // Test 29: Prevent password reset before own password change
    @Test
    void shouldBlockResetPasswordWhenPasswordChangeRequired()
            throws Exception {

        user.setMustChangePassword(true);

        request.setMethod("POST");

        request.setRequestURI(
                "/api/v1/users/5/reset-password"
        );

        prepareValidToken();

        executeFilter();

        assertPasswordChangeRequired();
    }

    // Test 30: New token after successful password change
    @Test
    void shouldAuthenticateNewTokenAfterPasswordChange()
            throws Exception {

        user.setTokenVersion(4L);
        user.setMustChangePassword(false);

        prepareValidToken();

        executeFilter();

        CurrentUserPrincipal principal = getPrincipal();

        assertEquals(4L, principal.tokenVersion());

        verify(filterChain)
                .doFilter(request, response);
    }
}
