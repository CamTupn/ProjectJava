package com.scentstation.simulator.persistence;

import com.scentstation.simulator.model.CommandStatus;
import com.scentstation.simulator.model.DispenseCommand;
import com.scentstation.simulator.model.ProcessedCommand;
import com.scentstation.simulator.model.SlotState;

import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProcessedCommandRepository {
    private final SqliteDatabaseManager dbManager;
    private final SlotStateRepository slotRepository;

    public ProcessedCommandRepository(SqliteDatabaseManager dbManager, SlotStateRepository slotRepository) {
        this.dbManager = dbManager;
        this.slotRepository = slotRepository;
    }

    public sealed interface ClaimOutcome permits ClaimOutcome.Acquired, ClaimOutcome.Duplicate, ClaimOutcome.PayloadConflict {
        record Acquired(ProcessedCommand record) implements ClaimOutcome {}
        record Duplicate(ProcessedCommand existing) implements ClaimOutcome {}
        record PayloadConflict(ProcessedCommand existing) implements ClaimOutcome {}
    }

    /**
     * Atomically checks idempotency and inserts IN_PROGRESS if this is a brand-new commandId.
     */
    public synchronized ClaimOutcome claimOrFetch(DispenseCommand command) {
        String fingerprint = command.businessFingerprint();
        try (Connection conn = dbManager.openConnection()) {
            conn.setAutoCommit(false);
            try {
                Optional<ProcessedCommand> existingOpt = findById(conn, command.commandId());
                if (existingOpt.isPresent()) {
                    ProcessedCommand existing = existingOpt.get();
                    conn.commit();
                    if (!existing.payloadFingerprint().equals(fingerprint)) {
                        return new ClaimOutcome.PayloadConflict(existing);
                    }
                    return new ClaimOutcome.Duplicate(existing);
                }

                Instant now = Instant.now();
                ProcessedCommand fresh = new ProcessedCommand(
                        command.commandId(),
                        command.machineId(),
                        command.deviceIdentity(),
                        command.slotId(),
                        command.orderId(),
                        command.volumeMl(),
                        fingerprint,
                        CommandStatus.IN_PROGRESS,
                        null,
                        null,
                        null,
                        now,
                        now
                );

                String insertSql = """
                    INSERT INTO processed_commands (
                        command_id, machine_id, device_identity, slot_id, order_id,
                        volume_ml, payload_fingerprint, status, result, error_code,
                        volume_dispensed_ml, created_at, updated_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NULL, NULL, NULL, ?, ?)
                """;
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setString(1, fresh.commandId());
                    ps.setString(2, fresh.machineId());
                    ps.setString(3, fresh.deviceIdentity());
                    ps.setString(4, fresh.slotId());
                    ps.setString(5, fresh.orderId());
                    ps.setString(6, fresh.volumeMl().toPlainString());
                    ps.setString(7, fresh.payloadFingerprint());
                    ps.setString(8, fresh.status().name());
                    ps.setString(9, fresh.createdAt().toString());
                    ps.setString(10, fresh.updatedAt().toString());
                    ps.executeUpdate();
                }

                conn.commit();
                return new ClaimOutcome.Acquired(fresh);
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("SQLite error claiming commandId=" + command.commandId(), e);
        }
    }

    /**
     * Atomically updates slot remaining volume (when SUCCESS) and marks command DONE in one transaction.
     */
    public synchronized ProcessedCommand completeCommandAndDeductSlot(
            String commandId,
            String result,
            String errorCode,
            BigDecimal volumeDispensedMl,
            SlotState updatedSlotOrNull
    ) {
        try (Connection conn = dbManager.openConnection()) {
            conn.setAutoCommit(false);
            try {
                if (updatedSlotOrNull != null && slotRepository != null) {
                    slotRepository.upsert(conn, updatedSlotOrNull);
                }

                Instant now = Instant.now();
                String updateSql = """
                    UPDATE processed_commands
                    SET status = ?, result = ?, error_code = ?, volume_dispensed_ml = ?, updated_at = ?
                    WHERE command_id = ?
                """;
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setString(1, CommandStatus.DONE.name());
                    ps.setString(2, result);
                    ps.setString(3, errorCode);
                    ps.setString(4, volumeDispensedMl == null ? null : volumeDispensedMl.toPlainString());
                    ps.setString(5, now.toString());
                    ps.setString(6, commandId);
                    ps.executeUpdate();
                }

                ProcessedCommand updated = findById(conn, commandId)
                        .orElseThrow(() -> new IllegalStateException("Command disappeared: " + commandId));
                conn.commit();
                return updated;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to complete command=" + commandId, e);
        }
    }

    /**
     * Rule 9 & 11: Any IN_PROGRESS command surviving a simulator restart must be marked UNCERTAIN
     * with volumeDispensedMl = null and never re-executed automatically.
     */
    public synchronized int markInterruptedCommandsUncertain() {
        String sql = """
            UPDATE processed_commands
            SET status = ?, result = ?, error_code = ?, volume_dispensed_ml = NULL, updated_at = ?
            WHERE status = ?
        """;
        try (Connection conn = dbManager.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, CommandStatus.UNCERTAIN.name());
            ps.setString(2, "UNCERTAIN");
            ps.setString(3, "RESTART_INTERRUPTED");
            ps.setString(4, Instant.now().toString());
            ps.setString(5, CommandStatus.IN_PROGRESS.name());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to reconcile interrupted commands on startup", e);
        }
    }

    public synchronized Optional<ProcessedCommand> findByCommandId(String commandId) {
        try (Connection conn = dbManager.openConnection()) {
            return findById(conn, commandId);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to query commandId=" + commandId, e);
        }
    }

    public synchronized List<ProcessedCommand> findRecent(int limit) {
        String sql = "SELECT * FROM processed_commands ORDER BY created_at DESC LIMIT ?";
        List<ProcessedCommand> list = new ArrayList<>();
        try (Connection conn = dbManager.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load recent processed commands", e);
        }
        return list;
    }

    private Optional<ProcessedCommand> findById(Connection conn, String commandId) throws SQLException {
        String sql = "SELECT * FROM processed_commands WHERE command_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, commandId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    private ProcessedCommand mapRow(ResultSet rs) throws SQLException {
        String dispensedStr = rs.getString("volume_dispensed_ml");
        return new ProcessedCommand(
                rs.getString("command_id"),
                rs.getString("machine_id"),
                rs.getString("device_identity"),
                rs.getString("slot_id"),
                rs.getString("order_id"),
                new BigDecimal(rs.getString("volume_ml")),
                rs.getString("payload_fingerprint"),
                CommandStatus.valueOf(rs.getString("status")),
                rs.getString("result"),
                rs.getString("error_code"),
                dispensedStr == null ? null : new BigDecimal(dispensedStr),
                Instant.parse(rs.getString("created_at")),
                Instant.parse(rs.getString("updated_at"))
        );
    }
}
