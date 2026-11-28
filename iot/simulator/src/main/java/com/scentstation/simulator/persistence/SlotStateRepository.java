package com.scentstation.simulator.persistence;

import com.scentstation.simulator.model.SlotState;

import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SlotStateRepository {
    private final SqliteDatabaseManager dbManager;

    public SlotStateRepository(SqliteDatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public synchronized List<SlotState> findByMachineId(String machineId) {
        String sql = "SELECT slot_id, machine_id, fragrance_name, remaining_ml, capacity_ml, status, updated_at " +
                     "FROM slot_state WHERE machine_id = ? ORDER BY slot_id ASC";
        List<SlotState> results = new ArrayList<>();
        try (Connection conn = dbManager.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, machineId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load slots for machineId=" + machineId, e);
        }
        return results;
    }

    public synchronized Optional<SlotState> findByMachineIdAndSlotId(String machineId, String slotId) {
        String sql = "SELECT slot_id, machine_id, fragrance_name, remaining_ml, capacity_ml, status, updated_at " +
                     "FROM slot_state WHERE machine_id = ? AND slot_id = ?";
        try (Connection conn = dbManager.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, machineId);
            ps.setString(2, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find slot=" + slotId, e);
        }
        return Optional.empty();
    }

    public synchronized void save(SlotState slot) {
        try (Connection conn = dbManager.openConnection()) {
            upsert(conn, slot);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save slot=" + slot.getSlotId(), e);
        }
    }

    public void upsert(Connection conn, SlotState slot) throws SQLException {
        String sql = """
            INSERT INTO slot_state (slot_id, machine_id, fragrance_name, remaining_ml, capacity_ml, status, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(machine_id, slot_id) DO UPDATE SET
                fragrance_name = excluded.fragrance_name,
                remaining_ml = excluded.remaining_ml,
                capacity_ml = excluded.capacity_ml,
                status = excluded.status,
                updated_at = excluded.updated_at
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, slot.getSlotId());
            ps.setString(2, slot.getMachineId());
            ps.setString(3, slot.getFragranceName());
            ps.setString(4, slot.getRemainingMl().toPlainString());
            ps.setString(5, slot.getCapacityMl().toPlainString());
            ps.setString(6, slot.getStatus());
            ps.setString(7, slot.getUpdatedAt().toString());
            ps.executeUpdate();
        }
    }

    public synchronized void deleteSlot(String machineId, String slotId) {
        String sql = "DELETE FROM slot_state WHERE machine_id = ? AND slot_id = ?";
        try (Connection conn = dbManager.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, machineId);
            ps.setString(2, slotId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete slot=" + slotId, e);
        }
    }

    public synchronized List<SlotState> ensureDefaultSlots(String machineId) {
        List<SlotState> existing = findByMachineId(machineId);
        if (!existing.isEmpty()) {
            return existing;
        }
        Instant now = Instant.now();
        List<SlotState> defaults = List.of(
                new SlotState("SLOT-1", machineId, "Bleu de Chanel", new BigDecimal("100.00"), new BigDecimal("100.00"), "READY", now),
                new SlotState("SLOT-2", machineId, "Dior Sauvage", new BigDecimal("100.00"), new BigDecimal("100.00"), "READY", now),
                new SlotState("SLOT-3", machineId, "Creed Aventus", new BigDecimal("85.00"), new BigDecimal("100.00"), "READY", now),
                new SlotState("SLOT-4", machineId, "YSL Libre", new BigDecimal("12.00"), new BigDecimal("100.00"), "LOW", now)
        );
        for (SlotState slot : defaults) {
            save(slot);
        }
        return findByMachineId(machineId);
    }

    private SlotState mapRow(ResultSet rs) throws SQLException {
        return new SlotState(
                rs.getString("slot_id"),
                rs.getString("machine_id"),
                rs.getString("fragrance_name"),
                new BigDecimal(rs.getString("remaining_ml")),
                new BigDecimal(rs.getString("capacity_ml")),
                rs.getString("status"),
                Instant.parse(rs.getString("updated_at"))
        );
    }
}
