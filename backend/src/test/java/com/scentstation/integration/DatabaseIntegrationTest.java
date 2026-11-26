package com.scentstation.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("integration")
@EnabledIfEnvironmentVariable(
        named = "RUN_INTEGRATION_TESTS",
        matches = "true"
)
class DatabaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void applicationContextShouldLoad() {
        assertNotNull(jdbcTemplate);
    }

    @Test
    void shouldConnectToTestDatabase() {

        String database = jdbcTemplate.queryForObject(
                "SELECT DATABASE()",
                String.class
        );

        assertEquals("scentstation_test", database);
    }

    @Test
    void shouldContainAllRequiredTables() {

        Integer totalTables = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.TABLES
                WHERE TABLE_SCHEMA = DATABASE()
                AND TABLE_TYPE = 'BASE TABLE'
                """,
                Integer.class
        );

        assertEquals(21, totalTables);
    }

    @Test
    void shouldConnectToMySqlServer() {

        Integer result = jdbcTemplate.queryForObject(
                "SELECT 1",
                Integer.class
        );

        assertEquals(1, result);
    }
}