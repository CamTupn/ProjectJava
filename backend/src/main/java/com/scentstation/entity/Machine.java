package com.scentstation.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "machines",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_machines_code", columnNames = "machine_code"),
        @UniqueConstraint(name = "uq_machines_serial", columnNames = "serial_number"),
        @UniqueConstraint(name = "uq_machines_device_identity", columnNames = "device_identity")
    }
)
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(name = "machine_code", nullable = false, unique = true, length = 100)
    private String machineCode;

    @Column(name = "serial_number", nullable = false, unique = true, length = 100)
    private String serialNumber;

    @Column(name = "device_identity", nullable = false, unique = true, length = 150)
    private String deviceIdentity;

    @Column(name = "connection_status", nullable = false, length = 20)
    private String connectionStatus = "OFFLINE";

    @Column(name = "operational_status", nullable = false, length = 20)
    private String operationalStatus = "ACTIVE";

    @Column(name = "firmware_version", length = 100)
    private String firmwareVersion;

    @Column(name = "last_ping_at")
    private LocalDateTime lastPingAt;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public Machine() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }

    public String getMachineCode() { return machineCode; }
    public void setMachineCode(String machineCode) { this.machineCode = machineCode; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public String getDeviceIdentity() { return deviceIdentity; }
    public void setDeviceIdentity(String deviceIdentity) { this.deviceIdentity = deviceIdentity; }

    public String getConnectionStatus() { return connectionStatus; }
    public void setConnectionStatus(String connectionStatus) { this.connectionStatus = connectionStatus; }

    public String getOperationalStatus() { return operationalStatus; }
    public void setOperationalStatus(String operationalStatus) { this.operationalStatus = operationalStatus; }

    public String getFirmwareVersion() { return firmwareVersion; }
    public void setFirmwareVersion(String firmwareVersion) { this.firmwareVersion = firmwareVersion; }

    public LocalDateTime getLastPingAt() { return lastPingAt; }
    public void setLastPingAt(LocalDateTime lastPingAt) { this.lastPingAt = lastPingAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
