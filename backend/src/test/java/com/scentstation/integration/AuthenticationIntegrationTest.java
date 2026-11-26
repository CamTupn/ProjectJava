package com.scentstation.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@EnabledIfEnvironmentVariable(
        named = "RUN_INTEGRATION_TESTS",
        matches = "true"
)
@Transactional
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String PASSWORD = "IntegrationTest@123";

    private String suffix;
    private String adminUsername;
    private String adminEmail;

    // =====================================================
    // TEST DATA SETUP
    // =====================================================

    @BeforeEach
    void setUp() {

        // Không cho phép test ghi dữ liệu vào database chính
        String database = jdbcTemplate.queryForObject(
                "SELECT DATABASE()",
                String.class
        );

        assertEquals("scentstation_test", database);

        suffix = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12);

        adminUsername = "it_admin_" + suffix;
        adminEmail = adminUsername + "@example.com";

        // Tạo tài khoản Platform Super Admin dành riêng cho test
        jdbcTemplate.update(
                """
                INSERT INTO users (
                    tenant_id,
                    username,
                    email,
                    password_hash,
                    full_name,
                    status
                )
                VALUES (NULL, ?, ?, ?, ?, 'ACTIVE')
                """,
                adminUsername,
                adminEmail,
                passwordEncoder.encode(PASSWORD),
                "Integration Platform Admin"
        );

        Long adminId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE username = ?",
                Long.class,
                adminUsername
        );

        Long roleId = jdbcTemplate.queryForObject(
                "SELECT id FROM roles WHERE code = ?",
                Long.class,
                "PLATFORM_SUPER_ADMIN"
        );

        jdbcTemplate.update(
                """
                INSERT INTO user_roles (user_id, role_id)
                VALUES (?, ?)
                """,
                adminId,
                roleId
        );
    }

    // =====================================================
    // 1. LOGIN BY USERNAME
    // =====================================================

    @Test
    void shouldLoginWithUsername() throws Exception {

        mockMvc.perform(
                post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(loginBody(adminUsername, PASSWORD))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.username").value(adminUsername))
        .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
        .andExpect(jsonPath("$.data.roles[0]")
                .value("PLATFORM_SUPER_ADMIN"));
    }

    // =====================================================
    // 2. LOGIN BY EMAIL
    // =====================================================

    @Test
    void shouldLoginWithEmail() throws Exception {

        mockMvc.perform(
                post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(loginBody(adminEmail, PASSWORD))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.username").value(adminUsername))
        .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    // =====================================================
    // 3. WRONG PASSWORD
    // =====================================================

    @Test
    void shouldRejectIncorrectPassword() throws Exception {

        mockMvc.perform(
                post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(loginBody(adminUsername, "WrongPassword"))
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));
    }

    // =====================================================
    // 4. UNKNOWN USER
    // =====================================================

    @Test
    void shouldRejectUnknownUser() throws Exception {

        mockMvc.perform(
                post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(loginBody(
                                "unknown_" + suffix,
                                PASSWORD
                        ))
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));
    }

    // =====================================================
    // 5. INVALID LOGIN REQUEST
    // =====================================================

    @Test
    void shouldRejectInvalidLoginRequest() throws Exception {

        mockMvc.perform(
                post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("{}")
        )
        .andExpect(status().isBadRequest());
    }

    // =====================================================
    // 6. VALID JWT
    // =====================================================

    @Test
    void validJwtShouldAccessProtectedApi() throws Exception {

        String token = login(adminUsername, PASSWORD);

        mockMvc.perform(
                get("/api/v1/users")
                        .header("Authorization", bearer(token))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
    }

    // =====================================================
    // 7. MISSING JWT
    // =====================================================

    @Test
    void missingJwtShouldReturn401() throws Exception {

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }

    // =====================================================
    // 8. INVALID JWT
    // =====================================================

    @Test
    void invalidJwtShouldReturn401() throws Exception {

        mockMvc.perform(
                get("/api/v1/users")
                        .header(
                                "Authorization",
                                "Bearer invalid.jwt.token"
                        )
        )
        .andExpect(status().isUnauthorized());
    }

    // =====================================================
    // 9. TAMPERED JWT SIGNATURE
    // =====================================================

    @Test
    void tamperedJwtShouldReturn401() throws Exception {

        String token = login(adminUsername, PASSWORD);

        String[] parts = token.split("\\.");

        assertEquals(3, parts.length);

        String signature = parts[2];

        char replacement =
                signature.charAt(0) == 'A' ? 'B' : 'A';

        String tamperedToken =
                parts[0] + "." +
                parts[1] + "." +
                replacement + signature.substring(1);

        mockMvc.perform(
                get("/api/v1/users")
                        .header(
                                "Authorization",
                                bearer(tamperedToken)
                        )
        )
        .andExpect(status().isUnauthorized());
    }

    // =====================================================
    // 10. CREATE TENANT AND BRAND ADMIN
    // =====================================================

    @Test
    void shouldCreateTenantAndBrandAdminThenLogin()
            throws Exception {

        String adminToken = login(adminUsername, PASSWORD);

        Long tenantId = createTenant(adminToken, "A");

        String username = "it_brand_" + suffix;

        Long userId = createUser(
                adminToken,
                username,
                username + "@example.com",
                tenantId,
                "BRAND_ADMIN"
        );

        mockMvc.perform(
                post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(loginBody(username, PASSWORD))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.userId").value(userId.intValue()))
        .andExpect(jsonPath("$.data.username").value(username))
        .andExpect(jsonPath("$.data.tenantCode")
                .value(tenantCode("A")))
        .andExpect(jsonPath("$.data.roles[0]").value("BRAND_ADMIN"))
        .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    // =====================================================
    // 11. PASSWORD MUST BE HASHED
    // =====================================================

    @Test
    void createdUserPasswordShouldBeHashedWithBCrypt()
            throws Exception {

        String token = login(adminUsername, PASSWORD);

        Long tenantId = createTenant(token, "A");

        String username = "it_hash_" + suffix;

        Long userId = createUser(
                token,
                username,
                username + "@example.com",
                tenantId,
                "BRAND_ADMIN"
        );

        String storedHash = jdbcTemplate.queryForObject(
                "SELECT password_hash FROM users WHERE id = ?",
                String.class,
                userId
        );

        assertNotNull(storedHash);
        assertNotEquals(PASSWORD, storedHash);
        assertTrue(storedHash.startsWith("$2"));
        assertTrue(passwordEncoder.matches(PASSWORD, storedHash));
    }

    // =====================================================
    // 12. BRAND ADMIN CREATES EMPLOYEE
    // =====================================================

    @Test
    void brandAdminShouldCreateEmployeeInOwnTenant()
            throws Exception {

        String platformToken = login(adminUsername, PASSWORD);

        Long tenantId = createTenant(platformToken, "A");

        String brandUsername = "it_brand_" + suffix;

        createUser(
                platformToken,
                brandUsername,
                brandUsername + "@example.com",
                tenantId,
                "BRAND_ADMIN"
        );

        String brandToken = login(brandUsername, PASSWORD);

        String employeeUsername = "it_employee_" + suffix;

        Long employeeId = createUser(
                brandToken,
                employeeUsername,
                employeeUsername + "@example.com",
                tenantId,
                "TECHNICIAN"
        );

        Long actualTenantId = jdbcTemplate.queryForObject(
                "SELECT tenant_id FROM users WHERE id = ?",
                Long.class,
                employeeId
        );

        assertEquals(tenantId, actualTenantId);

        String employeeToken = login(employeeUsername, PASSWORD);

        assertNotNull(employeeToken);
        assertFalse(employeeToken.isBlank());
    }

    // =====================================================
    // 13. DUPLICATE EMAIL
    // =====================================================

    @Test
    void shouldRejectDuplicateEmail() throws Exception {

        String token = login(adminUsername, PASSWORD);

        Long tenantId = createTenant(token, "A");

        String email = "duplicate_" + suffix + "@example.com";

        createUser(
                token,
                "it_first_" + suffix,
                email,
                tenantId,
                "BRAND_ADMIN"
        );

        mockMvc.perform(
                post("/api/v1/users")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(userBody(
                                "it_second_" + suffix,
                                email,
                                tenantId,
                                "BRAND_ADMIN"
                        ))
        )
        .andExpect(status().isConflict());
    }

    // =====================================================
    // 14. DUPLICATE USERNAME
    // =====================================================

    @Test
    void shouldRejectDuplicateUsername() throws Exception {

        String token = login(adminUsername, PASSWORD);

        Long tenantId = createTenant(token, "A");

        String username = "it_duplicate_" + suffix;

        createUser(
                token,
                username,
                "first_" + suffix + "@example.com",
                tenantId,
                "BRAND_ADMIN"
        );

        mockMvc.perform(
                post("/api/v1/users")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(userBody(
                                username,
                                "second_" + suffix + "@example.com",
                                tenantId,
                                "BRAND_ADMIN"
                        ))
        )
        .andExpect(status().isConflict());
    }

    // =====================================================
    // 15. RBAC - TECHNICIAN CANNOT MANAGE USERS
    // =====================================================

    @Test
    void technicianShouldNotAccessUserManagement()
            throws Exception {

        String token = login(adminUsername, PASSWORD);

        Long tenantId = createTenant(token, "A");

        String username = "it_tech_" + suffix;

        createUser(
                token,
                username,
                username + "@example.com",
                tenantId,
                "TECHNICIAN"
        );

        String technicianToken = login(username, PASSWORD);

        mockMvc.perform(
                get("/api/v1/users")
                        .header(
                                "Authorization",
                                bearer(technicianToken)
                        )
        )
        .andExpect(status().isForbidden());
    }

    // =====================================================
    // 16. BRAND ADMIN CANNOT CREATE PLATFORM ADMIN
    // =====================================================

    @Test
    void brandAdminShouldNotCreatePlatformSuperAdmin()
            throws Exception {

        String platformToken = login(adminUsername, PASSWORD);

        Long tenantId = createTenant(platformToken, "A");

        String username = "it_brand_" + suffix;

        createUser(
                platformToken,
                username,
                username + "@example.com",
                tenantId,
                "BRAND_ADMIN"
        );

        String brandToken = login(username, PASSWORD);

        mockMvc.perform(
                post("/api/v1/users")
                        .header("Authorization", bearer(brandToken))
                        .contentType("application/json")
                        .content(userBody(
                                "it_escalation_" + suffix,
                                "escalation_" + suffix + "@example.com",
                                tenantId,
                                "PLATFORM_SUPER_ADMIN"
                        ))
        )
        .andExpect(status().isForbidden());

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE username = ?",
                Integer.class,
                "it_escalation_" + suffix
        );

        assertEquals(0, count);
    }

    // =====================================================
    // 17. TENANT ISOLATION
    // =====================================================

    @Test
    void brandAdminShouldNotReadAnotherTenantUser()
            throws Exception {

        String platformToken = login(adminUsername, PASSWORD);

        Long tenantA = createTenant(platformToken, "A");
        Long tenantB = createTenant(platformToken, "B");

        String brandUsername = "it_brand_a_" + suffix;

        createUser(
                platformToken,
                brandUsername,
                brandUsername + "@example.com",
                tenantA,
                "BRAND_ADMIN"
        );

        String otherUsername = "it_brand_b_" + suffix;

        Long otherUserId = createUser(
                platformToken,
                otherUsername,
                otherUsername + "@example.com",
                tenantB,
                "BRAND_ADMIN"
        );

        String brandToken = login(brandUsername, PASSWORD);

        mockMvc.perform(
                get("/api/v1/users/{id}", otherUserId)
                        .header("Authorization", bearer(brandToken))
        )
        .andExpect(status().isNotFound());
    }

    // =====================================================
    // 18. LOCKED USER CANNOT LOGIN
    // =====================================================

    @Test
    void lockedUserShouldNotLogin() throws Exception {

        String platformToken = login(adminUsername, PASSWORD);

        Long tenantId = createTenant(platformToken, "A");

        String username = "it_locked_" + suffix;

        Long userId = createUser(
                platformToken,
                username,
                username + "@example.com",
                tenantId,
                "TECHNICIAN"
        );

        mockMvc.perform(
                patch("/api/v1/users/{id}/status", userId)
                        .header("Authorization", bearer(platformToken))
                        .param("status", "LOCKED")
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(loginBody(username, PASSWORD))
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));
    }

    // =====================================================
    // 19. EXISTING JWT OF LOCKED USER MUST BE REJECTED
    // =====================================================

    @Test
    void lockedUserExistingJwtShouldBeRejected()
            throws Exception {

        String platformToken = login(adminUsername, PASSWORD);

        Long tenantId = createTenant(platformToken, "A");

        String username = "it_revoked_" + suffix;

        Long userId = createUser(
                platformToken,
                username,
                username + "@example.com",
                tenantId,
                "BRAND_ADMIN"
        );

        String brandToken = login(username, PASSWORD);

        mockMvc.perform(
                get("/api/v1/users")
                        .header("Authorization", bearer(brandToken))
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                patch("/api/v1/users/{id}/status", userId)
                        .header("Authorization", bearer(platformToken))
                        .param("status", "LOCKED")
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                get("/api/v1/users")
                        .header("Authorization", bearer(brandToken))
        )
        .andExpect(status().isUnauthorized());
    }

    // =====================================================
    // 20. BRAND ADMIN CANNOT CREATE TENANT
    // =====================================================

    @Test
    void brandAdminShouldNotCreateTenant() throws Exception {

        String platformToken = login(adminUsername, PASSWORD);

        Long tenantId = createTenant(platformToken, "A");

        String username = "it_brand_" + suffix;

        createUser(
                platformToken,
                username,
                username + "@example.com",
                tenantId,
                "BRAND_ADMIN"
        );

        String brandToken = login(username, PASSWORD);

        mockMvc.perform(
                post("/api/v1/tenants")
                        .header("Authorization", bearer(brandToken))
                        .contentType("application/json")
                        .content(tenantBody("B"))
        )
        .andExpect(status().isForbidden());
    }

    // =====================================================
    // 21. PUBLIC REGISTER IS DISABLED
    // =====================================================

    @Test
    void publicRegistrationShouldNotBeAllowed()
            throws Exception {

        mockMvc.perform(
                post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content("{}")
        )
        .andExpect(status().isUnauthorized());
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================

    private String login(
            String usernameOrEmail,
            String password
    ) throws Exception {

        MvcResult result = mockMvc.perform(
                post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(loginBody(usernameOrEmail, password))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
        .andReturn();

        return JsonPath.read(
                result.getResponse().getContentAsString(),
                "$.data.accessToken"
        );
    }

    private Long createTenant(
            String token,
            String label
    ) throws Exception {

        MvcResult result = mockMvc.perform(
                post("/api/v1/tenants")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(tenantBody(label))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.status").value("ACTIVE"))
        .andReturn();

        Number id = JsonPath.read(
                result.getResponse().getContentAsString(),
                "$.id"
        );

        return id.longValue();
    }

    private Long createUser(
            String token,
            String username,
            String email,
            Long tenantId,
            String roleCode
    ) throws Exception {

        MvcResult result = mockMvc.perform(
                post("/api/v1/users")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(userBody(
                                username,
                                email,
                                tenantId,
                                roleCode
                        ))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.username").value(username))
        .andExpect(jsonPath("$.status").value("ACTIVE"))
        .andExpect(jsonPath("$.passwordHash").doesNotExist())
        .andExpect(jsonPath("$.password").doesNotExist())
        .andReturn();

        Number id = JsonPath.read(
                result.getResponse().getContentAsString(),
                "$.id"
        );

        return id.longValue();
    }

    private String loginBody(
            String usernameOrEmail,
            String password
    ) {

        return """
                {
                    "usernameOrEmail": "%s",
                    "password": "%s"
                }
                """.formatted(usernameOrEmail, password);
    }

    private String tenantBody(String label) {

        return """
                {
                    "name": "Integration Tenant %s %s",
                    "code": "%s"
                }
                """.formatted(label, suffix, tenantCode(label));
    }

    private String tenantCode(String label) {

        return ("IT_" + label + "_" + suffix)
                .toUpperCase(java.util.Locale.ROOT);
    }

    private String userBody(
            String username,
            String email,
            Long tenantId,
            String roleCode
    ) {

        return """
                {
                    "username": "%s",
                    "email": "%s",
                    "password": "%s",
                    "fullName": "Integration Test User",
                    "tenantId": %d,
                    "roleCode": "%s"
                }
                """.formatted(
                        username,
                        email,
                        PASSWORD,
                        tenantId,
                        roleCode
                );
    }

    private String bearer(String token) {

        return "Bearer " + token;
    }
}