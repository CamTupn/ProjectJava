package com.scentstation.service;

import com.scentstation.dto.request.LoginRequest;
import com.scentstation.dto.response.AuthResponse;
import com.scentstation.entity.Role;
import com.scentstation.entity.Tenant;
import com.scentstation.entity.User;
import com.scentstation.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User activeUser;

    @BeforeEach
    void setUp() {
        Tenant tenant = new Tenant();
        tenant.setId(1L);
        tenant.setName("ScentStation Demo");
        tenant.setCode("SCENT-DEMO");
        tenant.setStatus("ACTIVE");

        Role role = new Role();
        role.setId(2L);
        role.setCode("BRAND_ADMIN");
        role.setName("Brand Admin");

        activeUser = new User();
        activeUser.setId(1L);
        activeUser.setUsername("brandadmin");
        activeUser.setEmail("admin@scentstation.com");
        activeUser.setPasswordHash("encoded-password");
        activeUser.setFullName("Brand Administrator");
        activeUser.setStatus("ACTIVE");
        activeUser.setTenant(tenant);
        activeUser.setRoles(new HashSet<>(Set.of(role)));
        lenient().when(jwtService.generateToken(anyString(), anyMap()))
                .thenReturn("mock-jwt-token");
    }

    private LoginRequest createRequest(String identifier, String password) {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail(identifier);
        request.setPassword(password);
        return request;
    }

    // 1. Login thành công bằng username
    @Test
    void loginWithUsernameShouldReturnAuthResponse() {
        LoginRequest request =
                createRequest("brandadmin", "Scent123456");

        when(userRepository.findByUsername("brandadmin"))
                .thenReturn(Optional.of(activeUser));

        when(passwordEncoder.matches(
                "Scent123456", "encoded-password"))
                .thenReturn(true);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals("brandadmin", response.getUsername());
        assertEquals("admin@scentstation.com", response.getEmail());
        assertEquals("Brand Administrator", response.getFullName());
        assertEquals(1L, response.getTenantId());
        assertEquals("SCENT-DEMO", response.getTenantCode());
        assertEquals("ScentStation Demo", response.getTenantName());
        assertTrue(response.getRoles().contains("BRAND_ADMIN"));

        verify(userRepository).findByUsername("brandadmin");
        verify(passwordEncoder).matches(
                "Scent123456", "encoded-password");
    }

    // 2. Login thành công bằng email
    @Test
    void loginWithEmailShouldReturnAuthResponse() {
        LoginRequest request =
                createRequest("admin@scentstation.com", "Scent123456");

        when(userRepository.findByUsername("admin@scentstation.com"))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmail("admin@scentstation.com"))
                .thenReturn(Optional.of(activeUser));

        when(passwordEncoder.matches(
                "Scent123456", "encoded-password"))
                .thenReturn(true);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("brandadmin", response.getUsername());
        assertEquals(1L, response.getTenantId());
        assertEquals("SCENT-DEMO", response.getTenantCode());
        assertEquals("ScentStation Demo", response.getTenantName());
    }

    // 3. Username hoặc email không tồn tại
    @Test
    void loginWithUnknownAccountShouldThrowException() {
        LoginRequest request =
                createRequest("unknownuser", "Scent123456");

        when(userRepository.findByUsername("unknownuser"))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmail("unknownuser"))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(request)
        );

        assertEquals(
                "Invalid username/email or password",
                exception.getMessage()
        );

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    // 4. Sai mật khẩu
    @Test
    void loginWithWrongPasswordShouldThrowException() {
        LoginRequest request =
                createRequest("brandadmin", "WrongPassword");

        when(userRepository.findByUsername("brandadmin"))
                .thenReturn(Optional.of(activeUser));

        when(passwordEncoder.matches(
                "WrongPassword", "encoded-password"))
                .thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(request)
        );

        assertEquals(
                "Invalid username/email or password",
                exception.getMessage()
        );
    }

    // 5. Tài khoản LOCKED không được đăng nhập
    @Test
    void loginWithLockedAccountShouldThrowException() {
        activeUser.setStatus("LOCKED");

        LoginRequest request =
                createRequest("brandadmin", "Scent123456");

        when(userRepository.findByUsername("brandadmin"))
                .thenReturn(Optional.of(activeUser));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(request)
        );

        assertEquals("Account is not active", exception.getMessage());

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    // 6. Tài khoản INACTIVE không được đăng nhập
    @Test
    void loginWithInactiveAccountShouldThrowException() {
        activeUser.setStatus("INACTIVE");

        LoginRequest request =
                createRequest("brandadmin", "Scent123456");

        when(userRepository.findByUsername("brandadmin"))
                .thenReturn(Optional.of(activeUser));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(request)
        );

        assertEquals("Account is not active", exception.getMessage());

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    // 7. Platform Super Admin không thuộc Tenant
    @Test
    void loginWithoutTenantShouldReturnNullTenantCode() {
        activeUser.setUsername("platformadmin");
        activeUser.setEmail("platform@scentstation.com");
        activeUser.setFullName("Platform Administrator");
        activeUser.setTenant(null);

        Role superAdminRole = new Role();
        superAdminRole.setId(1L);
        superAdminRole.setCode("PLATFORM_SUPER_ADMIN");
        superAdminRole.setName("Platform Super Admin");

        activeUser.setRoles(new HashSet<>(Set.of(superAdminRole)));

        LoginRequest request =
            createRequest("platformadmin", "Scent123456");

        when(userRepository.findByUsername("platformadmin"))
                .thenReturn(Optional.of(activeUser));

        when(passwordEncoder.matches(
                "Scent123456", "encoded-password"))
                .thenReturn(true);
        
        AuthResponse response = authService.login(request);
        
        assertNotNull(response);
        assertEquals("platformadmin", response.getUsername());
        assertNull(response.getTenantId());
        assertNull(response.getTenantCode());
        assertNull(response.getTenantName());
        assertTrue(
            response.getRoles().contains("PLATFORM_SUPER_ADMIN")
        );
    }

    // 8. Login should include token version in JWT
    @Test
    void loginShouldIncludeTokenVersionInJwt() {

            activeUser.setTokenVersion(5L);

            LoginRequest request = createRequest("brandadmin", "Scent123456");

            when(userRepository.findByUsername("brandadmin"))
                            .thenReturn(Optional.of(activeUser));

            when(passwordEncoder.matches(
                            "Scent123456", "encoded-password"))
                            .thenReturn(true);

            authService.login(request);

            verify(jwtService).generateToken(
                            eq("brandadmin"),
                            argThat(claims -> Long.valueOf(5L).equals(
                                            claims.get("tokenVersion"))));
    }

    // 9. Login with inactive tenant should be rejected
    @Test
    void loginWithInactiveTenantShouldThrowException() {

            activeUser.getTenant().setStatus("INACTIVE");

            LoginRequest request = createRequest("brandadmin", "Scent123456");

            when(userRepository.findByUsername("brandadmin"))
                            .thenReturn(Optional.of(activeUser));

            IllegalArgumentException exception = assertThrows(
                            IllegalArgumentException.class,
                            () -> authService.login(request));

            assertEquals(
                            "Account is not active",
                            exception.getMessage());

            verify(passwordEncoder, never())
                            .matches(anyString(), anyString());

            verify(jwtService, never())
                            .generateToken(anyString(), anyMap());
    }
}
