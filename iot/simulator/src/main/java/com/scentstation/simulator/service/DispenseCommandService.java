package com.scentstation.simulator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.scentstation.simulator.config.SimulatorConfig;
import com.scentstation.simulator.model.*;
import com.scentstation.simulator.mqtt.SimulatorMqttPublisher;
import com.scentstation.simulator.persistence.ProcessedCommandRepository;
import com.scentstation.simulator.persistence.ProcessedCommandRepository.ClaimOutcome;
import com.scentstation.simulator.persistence.SlotStateRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DispenseCommandService {
    public interface Listener {
        default void onCommandReceived(DispenseCommand command) {}
        default void onPumpStateChanged(PumpState state, String activeSlotId, long durationMs) {}
        default void onCommandCompleted(ProcessedCommand record) {}
        default void onSlotsUpdated() {}
    }

    private final SimulatorConfig config;
    private final SimulatorMqttPublisher publisher;
    private final SlotStateRepository slotRepository;
    private final ProcessedCommandRepository commandRepository;
    private final Supplier<SimulationScenario> scenarioSupplier;
    private final Consumer<String> logger;
    private final Listener listener;
    private final ObjectMapper objectMapper;

    public DispenseCommandService(
            SimulatorConfig config,
            SimulatorMqttPublisher publisher,
            SlotStateRepository slotRepository,
            ProcessedCommandRepository commandRepository,
            Supplier<SimulationScenario> scenarioSupplier,
            Consumer<String> logger,
            Listener listener
    ) {
        this.config = config;
        this.publisher = publisher;
        this.slotRepository = slotRepository;
        this.commandRepository = commandRepository;
        this.scenarioSupplier = scenarioSupplier;
        this.logger = logger != null ? logger : msg -> {};
        this.listener = listener != null ? listener : new Listener() {};
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public ProcessedCommand processCommand(DispenseCommand command) {
        listener.onCommandReceived(command);

        SimulationScenario currentScenario = scenarioSupplier.get();
        if (currentScenario == SimulationScenario.OFFLINE) {
            logger.accept("[COMMAND] Ignored commandId=" + command.commandId() + " because simulator is OFFLINE.");
            return null;
        }

        ClaimOutcome claim = commandRepository.claimOrFetch(command);

        if (claim instanceof ClaimOutcome.PayloadConflict conflict) {
            logger.accept("[IDEMPOTENCY] REJECTED commandId=" + command.commandId()
                    + " due to payload mismatch against existing record.");
            publishResultEvent(
                    command.commandId(),
                    command.slotId(),
                    command.orderId(),
                    "FAILED",
                    "PAYLOAD_CONFLICT",
                    BigDecimal.ZERO
            );
            return conflict.existing();
        }

        if (claim instanceof ClaimOutcome.Duplicate dup) {
            ProcessedCommand existing = dup.existing();
            if (existing.status() == CommandStatus.DONE) {
                logger.accept("[IDEMPOTENCY] Duplicate DONE commandId=" + command.commandId()
                        + " -> returning cached result=" + existing.result() + " without re-dispensing.");
                publishAckEvent(command.commandId(), "ACCEPTED");
                publishResultEvent(
                        existing.commandId(),
                        existing.slotId(),
                        existing.orderId(),
                        existing.result(),
                        existing.errorCode(),
                        existing.volumeDispensedMl()
                );
            } else {
                logger.accept("[IDEMPOTENCY] Duplicate commandId=" + command.commandId()
                        + " is currently " + existing.status() + " -> skipping duplicate execution.");
            }
            return existing;
        }

        // Brand-new command claimed as IN_PROGRESS
        publishAckEvent(command.commandId(), "ACCEPTED");

        if (command.expiresAt() != null && Instant.now().isAfter(command.expiresAt())) {
            logger.accept("[COMMAND] Expired commandId=" + command.commandId());
            ProcessedCommand done = commandRepository.completeCommandAndDeductSlot(
                    command.commandId(), "FAILED", "COMMAND_EXPIRED", BigDecimal.ZERO, null
            );
            publishResultEvent(command.commandId(), command.slotId(), command.orderId(),
                    "FAILED", "COMMAND_EXPIRED", BigDecimal.ZERO);
            listener.onCommandCompleted(done);
            return done;
        }

        Optional<SlotState> slotOpt = slotRepository.findByMachineIdAndSlotId(
                config.getMachineId(), command.slotId());
        if (slotOpt.isEmpty()) {
            logger.accept("[COMMAND] Slot not found: " + command.slotId());
            ProcessedCommand done = commandRepository.completeCommandAndDeductSlot(
                    command.commandId(), "FAILED", "SLOT_NOT_FOUND", BigDecimal.ZERO, null
            );
            publishResultEvent(command.commandId(), command.slotId(), command.orderId(),
                    "FAILED", "SLOT_NOT_FOUND", BigDecimal.ZERO);
            listener.onCommandCompleted(done);
            return done;
        }

        SlotState slot = slotOpt.get();
        long durationMs = command.durationMs() != null && command.durationMs() > 0
                ? command.durationMs()
                : config.getDispenseDurationMs();

        // Scenario: EMPTY_SLOT or actual insufficient remaining volume
        if (currentScenario == SimulationScenario.EMPTY_SLOT
                || slot.getRemainingMl().compareTo(command.volumeMl()) < 0
                || "EMPTY".equalsIgnoreCase(slot.getStatus())) {
            logger.accept("[SCENARIO] EMPTY_SLOT / insufficient fluid on " + command.slotId());
            ProcessedCommand done = commandRepository.completeCommandAndDeductSlot(
                    command.commandId(), "FAILED", "EMPTY_SLOT", BigDecimal.ZERO, null
            );
            publishResultEvent(command.commandId(), command.slotId(), command.orderId(),
                    "FAILED", "EMPTY_SLOT", BigDecimal.ZERO);
            listener.onCommandCompleted(done);
            return done;
        }

        // Scenario: DEVICE_ERROR
        if (currentScenario == SimulationScenario.DEVICE_ERROR) {
            logger.accept("[SCENARIO] DEVICE_ERROR triggered during commandId=" + command.commandId());
            listener.onPumpStateChanged(PumpState.ERROR, command.slotId(), durationMs);
            publishFaultEvent("PUMP_HARDWARE_FAULT", "Simulated pump hardware failure on " + command.slotId());
            ProcessedCommand done = commandRepository.completeCommandAndDeductSlot(
                    command.commandId(), "FAILED", "DEVICE_ERROR", BigDecimal.ZERO, null
            );
            publishResultEvent(command.commandId(), command.slotId(), command.orderId(),
                    "FAILED", "DEVICE_ERROR", BigDecimal.ZERO);
            listener.onCommandCompleted(done);
            return done;
        }

        // Simulate pump actuation (NORMAL or TIMEOUT)
        listener.onPumpStateChanged(PumpState.DISPENSING, command.slotId(), durationMs);
        simulatePumpActuation(durationMs);

        // Scenario: TIMEOUT -> ACK was sent, pump ran, but result is never published
        if (currentScenario == SimulationScenario.TIMEOUT) {
            logger.accept("[SCENARIO] TIMEOUT simulated for commandId=" + command.commandId()
                    + " (ACK sent, no RESULT published).");
            listener.onPumpStateChanged(PumpState.IDLE, command.slotId(), 0);
            return commandRepository.findByCommandId(command.commandId()).orElse(null);
        }

        // Scenario: NORMAL -> deduct fluid & mark DONE atomically in SQLite
        BigDecimal newRemaining = slot.getRemainingMl().subtract(command.volumeMl());
        if (newRemaining.compareTo(BigDecimal.ZERO) < 0) {
            newRemaining = BigDecimal.ZERO;
        }
        slot.setRemainingMl(newRemaining);

        ProcessedCommand done = commandRepository.completeCommandAndDeductSlot(
                command.commandId(),
                "SUCCESS",
                null,
                command.volumeMl(),
                slot
        );

        listener.onPumpStateChanged(PumpState.IDLE, command.slotId(), 0);
        listener.onSlotsUpdated();
        publishResultEvent(
                command.commandId(),
                command.slotId(),
                command.orderId(),
                "SUCCESS",
                null,
                command.volumeMl()
        );
        listener.onCommandCompleted(done);
        return done;
    }

    protected void simulatePumpActuation(long durationMs) {
        if (durationMs <= 0) {
            return;
        }
        try {
            Thread.sleep(durationMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void publishAckEvent(String commandId, String status) {
        if (!publisher.isConnected()) {
            return;
        }
        try {
            Map<String, Object> ack = new LinkedHashMap<>();
            ack.put("type", "ACK");
            ack.put("machineId", config.getMachineId());
            ack.put("deviceIdentity", config.getDeviceIdentity());
            ack.put("eventId", "evt-" + UUID.randomUUID());
            ack.put("commandId", commandId);
            ack.put("status", status);
            ack.put("occurredAt", Instant.now().toString());
            publisher.publishEvent(objectMapper.writeValueAsString(ack));
        } catch (Exception e) {
            logger.accept("[EVENT] Failed to publish ACK: " + e.getMessage());
        }
    }

    private void publishResultEvent(
            String commandId,
            String slotId,
            String orderId,
            String result,
            String errorCode,
            BigDecimal volumeDispensedMl
    ) {
        if (!publisher.isConnected()) {
            return;
        }
        try {
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("type", "RESULT");
            res.put("machineId", config.getMachineId());
            res.put("deviceIdentity", config.getDeviceIdentity());
            res.put("eventId", "evt-" + UUID.randomUUID());
            res.put("commandId", commandId);
            res.put("slotId", slotId);
            if (orderId != null) {
                res.put("orderId", orderId);
            }
            res.put("result", result);
            res.put("errorCode", errorCode);
            res.put("volumeDispensedMl", volumeDispensedMl);
            res.put("occurredAt", Instant.now().toString());
            publisher.publishEvent(objectMapper.writeValueAsString(res));
        } catch (Exception e) {
            logger.accept("[EVENT] Failed to publish RESULT: " + e.getMessage());
        }
    }

    private void publishFaultEvent(String faultCode, String message) {
        if (!publisher.isConnected()) {
            return;
        }
        try {
            Map<String, Object> fault = new LinkedHashMap<>();
            fault.put("type", "FAULT");
            fault.put("machineId", config.getMachineId());
            fault.put("deviceIdentity", config.getDeviceIdentity());
            fault.put("faultCode", faultCode);
            fault.put("message", message);
            fault.put("severity", "HIGH");
            fault.put("occurredAt", Instant.now().toString());
            publisher.publishEvent(objectMapper.writeValueAsString(fault));
        } catch (Exception e) {
            logger.accept("[EVENT] Failed to publish FAULT: " + e.getMessage());
        }
    }
}
