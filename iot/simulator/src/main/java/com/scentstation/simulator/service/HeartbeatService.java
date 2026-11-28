package com.scentstation.simulator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.scentstation.simulator.config.SimulatorConfig;
import com.scentstation.simulator.model.SimulationScenario;
import com.scentstation.simulator.mqtt.SimulatorMqttPublisher;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class HeartbeatService {
    private final SimulatorConfig config;
    private final SimulatorMqttPublisher publisher;
    private final Supplier<SimulationScenario> scenarioSupplier;
    private final Consumer<String> logger;
    private final ObjectMapper objectMapper;

    private ScheduledFuture<?> scheduledTask;

    public HeartbeatService(
            SimulatorConfig config,
            SimulatorMqttPublisher publisher,
            Supplier<SimulationScenario> scenarioSupplier,
            Consumer<String> logger
    ) {
        this.config = config;
        this.publisher = publisher;
        this.scenarioSupplier = scenarioSupplier;
        this.logger = logger != null ? logger : msg -> {};
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public synchronized void start(ScheduledExecutorService scheduler) {
        stop();
        int interval = Math.max(1, config.getHeartbeatIntervalSeconds());
        scheduledTask = scheduler.scheduleAtFixedRate(
                this::publishHeartbeatOnce,
                0,
                interval,
                TimeUnit.SECONDS
        );
    }

    public synchronized void stop() {
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            scheduledTask = null;
        }
    }

    public boolean publishHeartbeatOnce() {
        SimulationScenario scenario = scenarioSupplier.get();
        if (scenario == SimulationScenario.OFFLINE) {
            logger.accept("[HEARTBEAT] Suppressed because scenario is OFFLINE.");
            return false;
        }
        if (!publisher.isConnected()) {
            return false;
        }
        try {
            String status = (scenario == SimulationScenario.DEVICE_ERROR) ? "FAULT" : "ONLINE";
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("type", "HEARTBEAT");
            payload.put("machineId", config.getMachineId());
            payload.put("deviceIdentity", config.getDeviceIdentity());
            payload.put("status", status);
            payload.put("firmwareVersion", config.getFirmwareVersion());
            payload.put("sentAt", Instant.now().toString());

            String json = objectMapper.writeValueAsString(payload);
            publisher.publishStatus(json);
            return true;
        } catch (Exception e) {
            logger.accept("[HEARTBEAT] Publish failed: " + e.getMessage());
            return false;
        }
    }
}
