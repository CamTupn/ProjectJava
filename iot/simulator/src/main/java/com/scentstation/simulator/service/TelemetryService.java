package com.scentstation.simulator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.scentstation.simulator.config.SimulatorConfig;
import com.scentstation.simulator.model.SimulationScenario;
import com.scentstation.simulator.model.SlotState;
import com.scentstation.simulator.mqtt.SimulatorMqttPublisher;
import com.scentstation.simulator.persistence.SlotStateRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TelemetryService {
    private final SimulatorConfig config;
    private final SimulatorMqttPublisher publisher;
    private final SlotStateRepository slotRepository;
    private final Supplier<SimulationScenario> scenarioSupplier;
    private final Consumer<String> logger;
    private final ObjectMapper objectMapper;

    private ScheduledFuture<?> scheduledTask;

    public TelemetryService(
            SimulatorConfig config,
            SimulatorMqttPublisher publisher,
            SlotStateRepository slotRepository,
            Supplier<SimulationScenario> scenarioSupplier,
            Consumer<String> logger
    ) {
        this.config = config;
        this.publisher = publisher;
        this.slotRepository = slotRepository;
        this.scenarioSupplier = scenarioSupplier;
        this.logger = logger != null ? logger : msg -> {};
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public synchronized void start(ScheduledExecutorService scheduler) {
        stop();
        int interval = Math.max(1, config.getTelemetryIntervalSeconds());
        scheduledTask = scheduler.scheduleAtFixedRate(
                this::publishTelemetryOnce,
                2,
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

    public boolean publishTelemetryOnce() {
        SimulationScenario scenario = scenarioSupplier.get();
        if (scenario == SimulationScenario.OFFLINE) {
            return false;
        }
        if (!publisher.isConnected()) {
            return false;
        }
        try {
            List<SlotState> slots = slotRepository.findByMachineId(config.getMachineId());
            List<Map<String, Object>> slotReadings = new ArrayList<>();
            for (SlotState slot : slots) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("slotId", slot.getSlotId());
                item.put("fragranceName", slot.getFragranceName());
                item.put("remainingMl", slot.getRemainingMl());
                item.put("capacityMl", slot.getCapacityMl());
                item.put("status", slot.getStatus());
                slotReadings.add(item);
            }

            Map<String, Object> readings = new LinkedHashMap<>();
            readings.put("scenario", scenario.name());
            readings.put("temperatureCelsius", 24.5);
            readings.put("humidityPercent", 52.0);
            readings.put("slots", slotReadings);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("type", "TELEMETRY");
            payload.put("machineId", config.getMachineId());
            payload.put("deviceIdentity", config.getDeviceIdentity());
            payload.put("sensorType", "FLUID_LEVEL_AND_ENV");
            payload.put("readings", readings);
            payload.put("occurredAt", Instant.now().toString());

            String json = objectMapper.writeValueAsString(payload);
            publisher.publishStatus(json);
            return true;
        } catch (Exception e) {
            logger.accept("[TELEMETRY] Publish failed: " + e.getMessage());
            return false;
        }
    }
}
