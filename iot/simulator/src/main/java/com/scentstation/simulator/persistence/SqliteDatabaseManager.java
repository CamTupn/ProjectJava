package com.scentstation.simulator.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SqliteDatabaseManager {
    private final String jdbcUrl;
    private final Path dbPath;

    public SqliteDatabaseManager(Path dbPath) {
        this.dbPath = dbPath;
        if (dbPath != null && dbPath.getParent() != null) {
            try {
                Files.createDirectories(dbPath.getParent());
            } catch (IOException e) {
                throw new IllegalStateException("Cannot create SQLite directory: " + dbPath.getParent(), e);
            }
        }
        this.jdbcUrl = dbPath == null
                ? "jdbc:sqlite::memory:"
                : "jdbc:sqlite:" + dbPath.toAbsolutePath();
        initializeSchema();
    }

    public Connection openConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(jdbcUrl);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA journal_mode=WAL;");
            stmt.execute("PRAGMA busy_timeout=5000;");
            stmt.execute("PRAGMA foreign_keys=ON;");
        }
        return conn;
    }

    private void initializeSchema() {
        try (Connection conn = openConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS slot_state (
                    slot_id TEXT NOT NULL,
                    machine_id TEXT NOT NULL,
                    fragrance_name TEXT NOT NULL,
                    remaining_ml TEXT NOT NULL,
                    capacity_ml TEXT NOT NULL,
                    status TEXT NOT NULL,
                    updated_at TEXT NOT NULL,
                    PRIMARY KEY (machine_id, slot_id)
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS processed_commands (
                    command_id TEXT PRIMARY KEY,
                    machine_id TEXT NOT NULL,
                    device_identity TEXT NOT NULL,
                    slot_id TEXT NOT NULL,
                    order_id TEXT,
                    volume_ml TEXT NOT NULL,
                    payload_fingerprint TEXT NOT NULL,
                    status TEXT NOT NULL,
                    result TEXT,
                    error_code TEXT,
                    volume_dispensed_ml TEXT,
                    created_at TEXT NOT NULL,
                    updated_at TEXT NOT NULL
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS simulator_meta (
                    meta_key TEXT PRIMARY KEY,
                    meta_value TEXT NOT NULL
                );
            """);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize SQLite schema", e);
        }
    }

    public Path getDbPath() {
        return dbPath;
    }
}
