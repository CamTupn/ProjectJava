package com.scentstation.integration;

import com.jayway.jsonpath.JsonPath;

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

import java.math.BigDecimal;
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
class BusinessFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String PASSWORD = "IntegrationTest@123";
    private static final String NEW_PASSWORD = "IntegrationChanged@456";

    private String suffix;
    private String platformToken;

    private record TenantContext(
            Long tenantId,
            String token
    ) {}

    private record BusinessContext(
            Long locationId,
            Long productId,
            Long machineId
    ) {}

    // =====================================================
    // TEST DATA SETUP
    // =====================================================

    @BeforeEach
    void setUp() throws Exception {

        String database = jdbcTemplate.queryForObject(
                "SELECT DATABASE()",
                String.class
        );

        assertEquals("scentstation_test", database);

        suffix = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12);

        String username = "bf_admin_" + suffix;

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
                username,
                username + "@example.com",
                passwordEncoder.encode(PASSWORD),
                "Business Flow Platform Admin"
        );

        Long userId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE username = ?",
                Long.class,
                username
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
                userId,
                roleId
        );

        platformToken = login(username);
    }

    // =====================================================
    // 1. COMPLETE BUSINESS FLOW
    // =====================================================

    @Test
    void shouldCompleteBusinessConfigurationFlow()
            throws Exception {

        TenantContext tenant = createTenantContext("A");

        BusinessContext business =
                createBusinessContext(tenant, "A");

        Long slotId = createSlot(
                tenant.token(),
                business.machineId(),
                business.productId(),
                1,
                20
        );

        mockMvc.perform(
                get("/api/v1/locations/{id}",
                        business.locationId())
                        .header("Authorization",
                                bearer(tenant.token()))
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                get("/api/v1/products/{id}",
                        business.productId())
                        .header("Authorization",
                                bearer(tenant.token()))
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                get("/api/v1/machines/{id}",
                        business.machineId())
                        .header("Authorization",
                                bearer(tenant.token()))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.connectionStatus")
                .value("OFFLINE"))
        .andExpect(jsonPath("$.operationalStatus")
                .value("ACTIVE"));

        mockMvc.perform(
                get("/api/v1/machine-slots/{id}", slotId)
                        .header("Authorization",
                                bearer(tenant.token()))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.slotNumber").value(1))
        .andExpect(jsonPath("$.status").value("AVAILABLE"));

        mockMvc.perform(
                get("/api/v1/machines/{id}/slots",
                        business.machineId())
                        .header("Authorization",
                                bearer(tenant.token()))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalSlots").value(1))
        .andExpect(jsonPath("$.slots").isArray());

        // Verify actual database relationships

        assertEquals(
                tenant.tenantId(),
                jdbcTemplate.queryForObject(
                        "SELECT tenant_id FROM locations WHERE id = ?",
                        Long.class,
                        business.locationId()
                )
        );

        assertEquals(
                tenant.tenantId(),
                jdbcTemplate.queryForObject(
                        "SELECT tenant_id FROM products WHERE id = ?",
                        Long.class,
                        business.productId()
                )
        );

        assertEquals(
                tenant.tenantId(),
                jdbcTemplate.queryForObject(
                        "SELECT tenant_id FROM machines WHERE id = ?",
                        Long.class,
                        business.machineId()
                )
        );

        assertEquals(
                business.locationId(),
                jdbcTemplate.queryForObject(
                        "SELECT location_id FROM machines WHERE id = ?",
                        Long.class,
                        business.machineId()
                )
        );

        assertEquals(
                business.machineId(),
                jdbcTemplate.queryForObject(
                        "SELECT machine_id FROM machine_slots WHERE id = ?",
                        Long.class,
                        slotId
                )
        );

        assertEquals(
                business.productId(),
                jdbcTemplate.queryForObject(
                        "SELECT product_id FROM machine_slots WHERE id = ?",
                        Long.class,
                        slotId
                )
        );

        BigDecimal currentVolume =
                jdbcTemplate.queryForObject(
                        """
                        SELECT current_volume_ml
                        FROM machine_slots
                        WHERE id = ?
                        """,
                        BigDecimal.class,
                        slotId
                );

        assertNotNull(currentVolume);

        assertEquals(
                0,
                new BigDecimal("20.00")
                        .compareTo(currentVolume)
        );
    }

    // =====================================================
    // 2. INITIAL INVENTORY TRANSACTION
    // =====================================================

    @Test
    void shouldRecordInitialInventoryTransaction()
            throws Exception {

        TenantContext tenant = createTenantContext("A");

        BusinessContext business = createBusinessContext(tenant, "A");

        Long slotId = createSlot(
                tenant.token(),
                business.machineId(),
                business.productId(),
                1,
                20);

        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM inventory_transactions
                        WHERE slot_id = ?
                        AND transaction_type = 'ADJUSTMENT'
                        """,
                Integer.class,
                slotId);

        assertEquals(1, count);

        BigDecimal volumeChange = jdbcTemplate.queryForObject(
                """
                        SELECT volume_change_ml
                        FROM inventory_transactions
                        WHERE slot_id = ?
                        AND transaction_type = 'ADJUSTMENT'
                        """,
                BigDecimal.class,
                slotId);

        assertNotNull(volumeChange);

        assertEquals(
                0,
                new BigDecimal("20.00")
                        .compareTo(volumeChange));
    }

    // =====================================================
    // 3. CROSS-TENANT LOCATION ACCESS
    // =====================================================

    @Test
    void shouldRejectCrossTenantLocationAccess()
            throws Exception {

        TenantContext tenantA = createTenantContext("A");
        TenantContext tenantB = createTenantContext("B");

        Long locationId = createLocation(
                tenantB.token(), "B"
        );

        mockMvc.perform(
                get("/api/v1/locations/{id}", locationId)
                        .header("Authorization",
                                bearer(tenantA.token()))
        )
        .andExpect(status().isNotFound());
    }

    // =====================================================
    // 4. CROSS-TENANT PRODUCT ACCESS
    // =====================================================

    @Test
    void shouldRejectCrossTenantProductAccess()
            throws Exception {

        TenantContext tenantA = createTenantContext("A");
        TenantContext tenantB = createTenantContext("B");

        Long productId = createProduct(
                tenantB.token(), "B"
        );

        mockMvc.perform(
                get("/api/v1/products/{id}", productId)
                        .header("Authorization",
                                bearer(tenantA.token()))
        )
        .andExpect(status().isNotFound());
    }

    // =====================================================
    // 5. CROSS-TENANT MACHINE ACCESS
    // =====================================================

    @Test
    void shouldRejectCrossTenantMachineAccess()
            throws Exception {

        TenantContext tenantA = createTenantContext("A");
        TenantContext tenantB = createTenantContext("B");

        Long locationId = createLocation(
                tenantB.token(), "B"
        );

        Long machineId = createMachine(
                tenantB.token(), locationId, "B"
        );

        mockMvc.perform(
                get("/api/v1/machines/{id}", machineId)
                        .header("Authorization",
                                bearer(tenantA.token()))
        )
        .andExpect(status().isNotFound());
    }

    // =====================================================
    // 6. CROSS-TENANT MACHINE SLOT ACCESS
    // =====================================================

    @Test
    void shouldRejectCrossTenantSlotAccess()
            throws Exception {

        TenantContext tenantA = createTenantContext("A");
        TenantContext tenantB = createTenantContext("B");

        BusinessContext business =
                createBusinessContext(tenantB, "B");

        Long slotId = createSlot(
                tenantB.token(),
                business.machineId(),
                business.productId(),
                1,
                0
        );

        mockMvc.perform(
                get("/api/v1/machine-slots/{id}", slotId)
                        .header("Authorization",
                                bearer(tenantA.token()))
        )
        .andExpect(status().isNotFound());

        mockMvc.perform(
                get("/api/v1/machines/{id}/slots",
                        business.machineId())
                        .header("Authorization",
                                bearer(tenantA.token()))
        )
        .andExpect(status().isNotFound());
    }

    // =====================================================
    // 7. CROSS-TENANT PRODUCT ASSIGNMENT
    // =====================================================

    @Test
    void shouldRejectProductAssignmentFromAnotherTenant()
            throws Exception {

        TenantContext tenantA = createTenantContext("A");
        TenantContext tenantB = createTenantContext("B");

        BusinessContext business =
                createBusinessContext(tenantA, "A");

        Long foreignProductId = createProduct(
                tenantB.token(), "B"
        );

        mockMvc.perform(
                post("/api/v1/machines/{id}/slots",
                        business.machineId())
                        .header("Authorization",
                                bearer(tenantA.token()))
                        .contentType("application/json")
                        .content(slotBody(
                                foreignProductId, 1, 0
                        ))
        )
        .andExpect(status().isNotFound());

        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM machine_slots
                WHERE machine_id = ?
                """,
                Integer.class,
                business.machineId()
        );

        assertEquals(0, count);
    }

    // =====================================================
    // 8. DUPLICATE SLOT NUMBER
    // =====================================================

    @Test
    void shouldRejectDuplicateSlotNumber()
            throws Exception {

        TenantContext tenant = createTenantContext("A");

        BusinessContext business =
                createBusinessContext(tenant, "A");

        createSlot(
                tenant.token(),
                business.machineId(),
                business.productId(),
                1,
                0
        );

        mockMvc.perform(
                post("/api/v1/machines/{id}/slots",
                        business.machineId())
                        .header("Authorization",
                                bearer(tenant.token()))
                        .contentType("application/json")
                        .content(slotBody(
                                business.productId(), 1, 0
                        ))
        )
        .andExpect(status().isConflict());

        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM machine_slots
                WHERE machine_id = ?
                AND slot_number = 1
                """,
                Integer.class,
                business.machineId()
        );

        assertEquals(1, count);
    }

    // =====================================================
    // 9. TECHNICIAN RBAC
    // =====================================================

    @Test
    void technicianShouldReadButNotCreateMachineSlot()
            throws Exception {

        TenantContext tenant = createTenantContext("A");

        BusinessContext business =
                createBusinessContext(tenant, "A");

        Long slotId = createSlot(
                tenant.token(),
                business.machineId(),
                business.productId(),
                1,
                0
        );

        String username = "bf_tech_" + suffix;

        createUser(
                tenant.token(),
                tenant.tenantId(),
                username,
                "TECHNICIAN"
        );

        String technicianToken = loginAfterPasswordChange(username);

        mockMvc.perform(
                get("/api/v1/machine-slots/{id}", slotId)
                        .header("Authorization",
                                bearer(technicianToken))
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                post("/api/v1/machines/{id}/slots",
                        business.machineId())
                        .header("Authorization",
                                bearer(technicianToken))
                        .contentType("application/json")
                        .content(slotBody(
                                business.productId(), 2, 0
                        ))
        )
        .andExpect(status().isForbidden());

        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM machine_slots
                WHERE machine_id = ?
                """,
                Integer.class,
                business.machineId()
        );

        assertEquals(1, count);
    }

    // =====================================================
    // 10. INVENTORY STAFF RBAC
    // =====================================================

    @Test
    void inventoryStaffShouldCreateSlotButNotDisableIt()
            throws Exception {

        TenantContext tenant = createTenantContext("A");

        BusinessContext business =
                createBusinessContext(tenant, "A");

        String username = "bf_inventory_" + suffix;

        createUser(
                tenant.token(),
                tenant.tenantId(),
                username,
                "INVENTORY_STAFF"
        );

        String inventoryToken = loginAfterPasswordChange(username);

        Long slotId = createSlot(
                inventoryToken,
                business.machineId(),
                business.productId(),
                1,
                0
        );

        mockMvc.perform(
                delete("/api/v1/machine-slots/{id}", slotId)
                        .header("Authorization",
                                bearer(inventoryToken))
        )
        .andExpect(status().isForbidden());

        String actualStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM machine_slots WHERE id = ?",
                String.class,
                slotId
        );

        assertEquals("EMPTY", actualStatus);
    }

    // =====================================================
    // 11. DISABLE SLOT WITH REMAINING INVENTORY
    // =====================================================

    @Test
    void shouldRejectDisablingSlotWithRemainingInventory()
            throws Exception {

        TenantContext tenant = createTenantContext("A");

        BusinessContext business =
                createBusinessContext(tenant, "A");

        Long slotId = createSlot(
                tenant.token(),
                business.machineId(),
                business.productId(),
                1,
                20
        );

        mockMvc.perform(
                delete("/api/v1/machine-slots/{id}", slotId)
                        .header("Authorization",
                                bearer(tenant.token()))
        )
        .andExpect(status().isConflict());

        String actualStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM machine_slots WHERE id = ?",
                String.class,
                slotId
        );

        assertEquals("AVAILABLE", actualStatus);
    }

    // =====================================================
    // 12. SOFT DELETE EMPTY SLOT
    // =====================================================

    @Test
    void shouldSoftDisableEmptySlot()
            throws Exception {

        TenantContext tenant = createTenantContext("A");

        BusinessContext business =
                createBusinessContext(tenant, "A");

        Long slotId = createSlot(
                tenant.token(),
                business.machineId(),
                business.productId(),
                1,
                0
        );

        mockMvc.perform(
                delete("/api/v1/machine-slots/{id}", slotId)
                        .header("Authorization",
                                bearer(tenant.token()))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("DISABLED"));

        String actualStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM machine_slots WHERE id = ?",
                String.class,
                slotId
        );

        assertEquals("DISABLED", actualStatus);
    }

    // =====================================================
    // 13. PLATFORM ADMIN GLOBAL ACCESS
    // =====================================================

    @Test
    void platformAdminShouldReadResourcesAcrossTenants()
            throws Exception {

        TenantContext tenant = createTenantContext("A");

        BusinessContext business =
                createBusinessContext(tenant, "A");

        Long slotId = createSlot(
                tenant.token(),
                business.machineId(),
                business.productId(),
                1,
                0
        );

        mockMvc.perform(
                get("/api/v1/locations/{id}",
                        business.locationId())
                        .header("Authorization",
                                bearer(platformToken))
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                get("/api/v1/products/{id}",
                        business.productId())
                        .header("Authorization",
                                bearer(platformToken))
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                get("/api/v1/machines/{id}",
                        business.machineId())
                        .header("Authorization",
                                bearer(platformToken))
        )
        .andExpect(status().isOk());

        mockMvc.perform(
                get("/api/v1/machine-slots/{id}", slotId)
                        .header("Authorization",
                                bearer(platformToken))
        )
        .andExpect(status().isOk());
    }

    // =====================================================
    // 14. MISSING JWT
    // =====================================================

    @Test
    void protectedBusinessEndpointsShouldRequireJwt()
            throws Exception {

        mockMvc.perform(get("/api/v1/locations"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/machines"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/machines/1/slots"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/machine-slots/1"))
                .andExpect(status().isUnauthorized());
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================

    private String login(String username) throws Exception {
            return login(username, PASSWORD);
    }
    private String login(
                    String username,
                    String password) throws Exception {
            MvcResult result = mockMvc.perform(
                post("/api/v1/auth/login")
                                .contentType("application/json")
                                .content("""
                                        {"usernameOrEmail": "%s","password": "%s"}""".formatted(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andReturn();

            return JsonPath.read(
                result.getResponse().getContentAsString(),
                "$.data.accessToken");
    }

    private String loginAfterPasswordChange( String username) throws Exception {
            // 1. Login with temporary password
            String temporaryToken = login(username, PASSWORD);
            // 2. Verify forced password change flag
            mockMvc.perform(get("/api/v1/auth/me").header("Authorization",bearer(temporaryToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mustChangePassword").value(true));
            // 3. Change temporary password
            mockMvc.perform(post("/api/v1/auth/change-password")
                 .header("Authorization",bearer(temporaryToken)).contentType("application/json")
                        .content("""
                                {"currentPassword": "%s", "newPassword": "%s"}
                                """.formatted(PASSWORD, NEW_PASSWORD)))
                        .andExpect(status().isOk());
            // 4. Verify old JWT is revoked
            mockMvc.perform(get("/api/v1/auth/me").header("Authorization",bearer(temporaryToken)))
                        .andExpect(status().isUnauthorized());
            // 5. Login again with new password
            String newToken = login(username, NEW_PASSWORD);
            // 6. Verify password change is completed
            mockMvc.perform(get("/api/v1/auth/me").header("Authorization",bearer(newToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mustChangePassword").value(false));
            return newToken;
    }

        private TenantContext createTenantContext(
                String label
        ) throws Exception {

        String code = ("BF_" + label + "_" + suffix)
                .toUpperCase(java.util.Locale.ROOT);

        Long tenantId = postAndGetId(
                "/api/v1/tenants",
                platformToken,
                """
                {
                        "name": "Business Tenant %s %s",
                        "code": "%s"
                }
                """.formatted(label, suffix, code)
        );

        String username =
                "bf_brand_" + label + "_" + suffix;

        createUser(
                platformToken,
                tenantId,
                username,
                "BRAND_ADMIN"
        );

        // Complete mandatory password change
        String brandToken = loginAfterPasswordChange(username);

        return new TenantContext(
                tenantId,
                brandToken
        );
        }

    private Long createUser(
            String token,
            Long tenantId,
            String username,
            String roleCode
    ) throws Exception {

        return postAndGetId(
                "/api/v1/users",
                token,
                """
                {
                    "username": "%s",
                    "email": "%s@example.com",
                    "password": "%s",
                    "fullName": "Business Flow Test User",
                    "tenantId": %d,
                    "roleCode": "%s"
                }
                """.formatted(
                        username,
                        username,
                        PASSWORD,
                        tenantId,
                        roleCode
                )
        );
    }

    private BusinessContext createBusinessContext(
            TenantContext tenant,
            String label
    ) throws Exception {

        Long locationId = createLocation(
                tenant.token(), label
        );

        Long productId = createProduct(
                tenant.token(), label
        );

        Long machineId = createMachine(
                tenant.token(), locationId, label
        );

        return new BusinessContext(
                locationId,
                productId,
                machineId
        );
    }

    private Long createLocation(
            String token,
            String label
    ) throws Exception {

        return postAndGetId(
                "/api/v1/locations",
                token,
                """
                {
                    "name": "Business Location %s %s",
                    "address": "123 Integration Test Street",
                    "description": "Business flow integration test",
                    "status": "ACTIVE"
                }
                """.formatted(label, suffix)
        );
    }

    private Long createProduct(
            String token,
            String label
    ) throws Exception {

        return postAndGetId(
                "/api/v1/products",
                token,
                """
                {
                    "sku": "BF-SKU-%s-%s",
                    "name": "Integration Fragrance %s",
                    "description": "Test fragrance",
                    "fragranceNotes": "Citrus and wood",
                    "price": 15000.00,
                    "volumePerSprayMl": 0.10,
                    "status": "ACTIVE"
                }
                """.formatted(label, suffix, label)
        );
    }

    private Long createMachine(
            String token,
            Long locationId,
            String label
    ) throws Exception {

        return postAndGetId(
                "/api/v1/machines",
                token,
                """
                {
                    "machineCode": "BF-M-%s-%s",
                    "serialNumber": "BF-SN-%s-%s",
                    "deviceIdentity": "BF-DEVICE-%s-%s",
                    "locationId": %d,
                    "firmwareVersion": "1.0.0",
                    "connectionStatus": "OFFLINE",
                    "operationalStatus": "ACTIVE"
                }
                """.formatted(
                        label, suffix,
                        label, suffix,
                        label, suffix,
                        locationId
                )
        );
    }

    private Long createSlot(
            String token,
            Long machineId,
            Long productId,
            int slotNumber,
            int currentVolume
    ) throws Exception {

        return postAndGetId(
                "/api/v1/machines/" + machineId + "/slots",
                token,
                slotBody(productId, slotNumber, currentVolume)
        );
    }

    private String slotBody(
            Long productId,
            int slotNumber,
            int currentVolume
    ) {

        return """
                {
                    "slotNumber": %d,
                    "productId": %d,
                    "capacityMl": 50.00,
                    "currentVolumeMl": %d,
                    "lowStockThresholdMl": 10.00
                }
                """.formatted(
                        slotNumber,
                        productId,
                        currentVolume
                );
    }

    private Long postAndGetId(
            String path,
            String token,
            String body
    ) throws Exception {

        MvcResult result = mockMvc.perform(
                post(path)
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(body)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNumber())
        .andReturn();

        Number id = JsonPath.read(
                result.getResponse().getContentAsString(),
                "$.id"
        );

        return id.longValue();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}