package com.scentstation.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.scentstation.common.security.CurrentUserPrincipal;
import com.scentstation.contract.InventoryChanged;
import com.scentstation.dto.request.CreateRefillRequest;
import com.scentstation.exception.ResourceConflictException;
import com.scentstation.service.InventoryService;
import com.scentstation.service.RefillService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

/**
 * Rollback and concurrency checks against the separate MySQL test database
 * (TASK-026). Opt-in like the other integration tests: RUN_INTEGRATION_TESTS=true.
 *
 * <p>These tests are intentionally NOT {@code @Transactional}: they need real
 * commits so that two threads contend on the same slot row. Everything they
 * create is deleted in {@link #cleanUp()}.
 */
@SpringBootTest
@ActiveProfiles("integration")
@EnabledIfEnvironmentVariable(named = "RUN_INTEGRATION_TESTS", matches = "true")
class InventoryIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private RefillService refillService;

    private long tenantId;
    private long productId;
    private long machineId;
    private long slotId;
    private final List<Long> executionIds = new ArrayList<>();
    private final List<Long> commandIds = new ArrayList<>();

    @BeforeEach
    void createFixture() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        tenantId = insert("tenants", "name", "Inv IT " + suffix, "code", "INV" + suffix);
        productId = insert("products",
                "tenant_id", tenantId, "sku", "SKU" + suffix, "name", "Inv IT scent",
                "price", new BigDecimal("100.00"), "volume_per_spray_ml", new BigDecimal("1.00"));
        machineId = insert("machines",
                "tenant_id", tenantId, "machine_code", "M" + suffix,
                "serial_number", "S" + suffix, "device_identity", "D" + suffix);
        slotId = insert("machine_slots",
                "machine_id", machineId, "slot_number", 1, "product_id", productId,
                "capacity_ml", new BigDecimal("50.00"),
                "current_volume_ml", new BigDecimal("30.00"),
                "low_stock_threshold_ml", new BigDecimal("10.00"),
                "status", "AVAILABLE");
    }

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
        jdbc.update("DELETE FROM inventory_transactions WHERE slot_id = ?", slotId);
        jdbc.update("DELETE FROM refills WHERE slot_id = ?", slotId);
        executionIds.forEach(id -> jdbc.update("DELETE FROM dispense_executions WHERE id = ?", id));
        commandIds.forEach(id -> jdbc.update("DELETE FROM dispense_commands WHERE id = ?", id));
        jdbc.update("DELETE FROM machine_slots WHERE id = ?", slotId);
        jdbc.update("DELETE FROM machines WHERE id = ?", machineId);
        jdbc.update("DELETE FROM products WHERE id = ?", productId);
        jdbc.update("DELETE FROM tenants WHERE id = ?", tenantId);
    }

    // ------------------------------------------------------------------

    @Test
    void sameExecutionDeliveredConcurrentlyIsDeductedExactlyOnce() throws Exception {
        long executionId = createExecution("SUCCESS", "SUCCESS", new BigDecimal("5.00"));

        List<Optional<InventoryChanged>> results = runConcurrently(
                () -> inventoryService.recordSuccessfulDispense(executionId),
                () -> inventoryService.recordSuccessfulDispense(executionId),
                () -> inventoryService.recordSuccessfulDispense(executionId)
        );

        long applied = results.stream().filter(Optional::isPresent).count();
        assertEquals(1, applied, "exactly one delivery may apply the deduction");
        assertEquals(new BigDecimal("25.00"), currentVolume());
        assertEquals(1, count("inventory_transactions WHERE execution_id = ?", executionId));
    }

    @Test
    void concurrentRefillsNeverExceedCapacity() throws Exception {
        // 30 + 15 = 45 fits, 45 + 15 = 60 does not: exactly one of the two may win.
        Callable<Boolean> refill = () -> {
            authenticateAsInventoryStaff();
            try {
                CreateRefillRequest request = new CreateRefillRequest();
                request.setSlotId(slotId);
                request.setVolumeMl(new BigDecimal("15.00"));
                refillService.createRefill(request);
                return true;
            } catch (ResourceConflictException ex) {
                return false;
            } finally {
                SecurityContextHolder.clearContext();
            }
        };

        List<Boolean> outcomes = runConcurrently(refill, refill);

        assertEquals(1, outcomes.stream().filter(b -> b).count());
        assertEquals(new BigDecimal("45.00"), currentVolume());
        assertEquals(1, count("refills WHERE slot_id = ?", slotId));
        assertEquals(1, count("inventory_transactions WHERE slot_id = ? AND transaction_type = 'REFILL'", slotId));
    }

    @Test
    void rejectedDeductionRollsBackEverything() {
        jdbc.update("UPDATE machine_slots SET current_volume_ml = 3.00 WHERE id = ?", slotId);
        long executionId = createExecution("SUCCESS", "SUCCESS", new BigDecimal("5.00"));

        assertThrows(ResourceConflictException.class,
                () -> inventoryService.recordSuccessfulDispense(executionId));

        assertEquals(new BigDecimal("3.00"), currentVolume());
        assertEquals(0, count("inventory_transactions WHERE execution_id = ?", executionId));
    }

    @Test
    void refillIsRejectedWhileACommandIsUnreconciledAndLeavesNoRows() {
        createExecution("UNCERTAIN", "UNCERTAIN", new BigDecimal("5.00"));
        authenticateAsInventoryStaff();

        CreateRefillRequest request = new CreateRefillRequest();
        request.setSlotId(slotId);
        request.setVolumeMl(new BigDecimal("5.00"));

        assertThrows(ResourceConflictException.class, () -> refillService.createRefill(request));

        assertTrue(inventoryService.hasUnreconciledCommands(slotId));
        assertEquals(new BigDecimal("30.00"), currentVolume());
        assertEquals(0, count("refills WHERE slot_id = ?", slotId));
        assertEquals(0, count("inventory_transactions WHERE slot_id = ?", slotId));
    }

    // ------------------------------------------------------------------

    private void authenticateAsInventoryStaff() {
        CurrentUserPrincipal principal = new CurrentUserPrincipal(
                null, "it-inventory", tenantId, "IT", Set.of("INVENTORY_STAFF"));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null,
                        List.of(new SimpleGrantedAuthority("ROLE_INVENTORY_STAFF"))));
    }

    @SafeVarargs
    private <T> List<T> runConcurrently(Callable<T>... tasks) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(tasks.length);
        CountDownLatch ready = new CountDownLatch(tasks.length);
        CountDownLatch go = new CountDownLatch(1);
        try {
            List<Future<T>> futures = new ArrayList<>();
            for (Callable<T> task : tasks) {
                futures.add(pool.submit(() -> {
                    ready.countDown();
                    go.await();
                    return task.call();
                }));
            }
            ready.await();
            go.countDown();
            List<T> results = new ArrayList<>();
            for (Future<T> future : futures) {
                results.add(future.get());
            }
            return results;
        } finally {
            pool.shutdownNow();
        }
    }

    private long createExecution(String commandStatus, String executionStatus, BigDecimal volume) {
        long commandId = insert("dispense_commands",
                "command_token", "IT-" + UUID.randomUUID(),
                "machine_id", machineId, "slot_id", slotId,
                "command_type", "DIAGNOSTIC", "volume_ml", volume, "status", commandStatus);
        commandIds.add(commandId);
        long executionId = insert("dispense_executions",
                "command_id", commandId, "execution_status", executionStatus);
        executionIds.add(executionId);
        return executionId;
    }

    private BigDecimal currentVolume() {
        return jdbc.queryForObject(
                "SELECT current_volume_ml FROM machine_slots WHERE id = ?",
                BigDecimal.class, slotId);
    }

    private int count(String fromAndWhere, Object... args) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM " + fromAndWhere, Integer.class, args);
        return n == null ? 0 : n;
    }

    private long insert(String table, Object... columnsAndValues) {
        java.util.Map<String, Object> values = new java.util.LinkedHashMap<>();
        for (int i = 0; i < columnsAndValues.length; i += 2) {
            values.put((String) columnsAndValues[i], columnsAndValues[i + 1]);
        }
        return new SimpleJdbcInsert(jdbc)
                .withTableName(table)
                .usingGeneratedKeyColumns("id")
                .executeAndReturnKey(values)
                .longValue();
    }
}
