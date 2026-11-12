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

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User activeUser;

    @BeforeEach
    void setUp() {
        Tenant tenant = new Tenant();
        tenant.setId(1L);
        tenant.setCode("SCENT-DEMO");

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
        assertEquals("SCENT-DEMO", response.getTenantCode());
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
        assertEquals("SCENT-DEMO", response.getTenantCode());
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
        activeUser.setTenant(null);

        LoginRequest request =
                createRequest("brandadmin", "Scent123456");

        when(userRepository.findByUsername("brandadmin"))
                .thenReturn(Optional.of(activeUser));

        when(passwordEncoder.matches(
                "Scent123456", "encoded-password"))
                .thenReturn(true);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertNull(response.getTenantCode());
    }
}
