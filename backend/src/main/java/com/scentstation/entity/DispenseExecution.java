package com.scentstation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "dispense_executions")
public class DispenseExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "command_id", nullable = false, unique = true)
    private DispenseCommand command;
    @Enumerated(EnumType.STRING)
    @Column(name = "execution_status", nullable = false, length = 20)
    private DispenseExecutionStatus executionStatus;
    @Column(name = "error_code", length = 100)
    private String errorCode;
    @Column(name = "hardware_telemetry", columnDefinition = "json")
    private String hardwareTelemetry;
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    public Long getId() { return id; }
    public DispenseCommand getCommand() { return command; }
    public DispenseExecutionStatus getExecutionStatus() { return executionStatus; }
    public String getErrorCode() { return errorCode; }
    public String getHardwareTelemetry() { return hardwareTelemetry; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setCommand(DispenseCommand value) { command = value; }
    public void setExecutionStatus(DispenseExecutionStatus value) { executionStatus = value; }
    public void setErrorCode(String value) { errorCode = value; }
    public void setHardwareTelemetry(String value) { hardwareTelemetry = value; }
    public void setRecordedAt(LocalDateTime value) { recordedAt = value; }
}
