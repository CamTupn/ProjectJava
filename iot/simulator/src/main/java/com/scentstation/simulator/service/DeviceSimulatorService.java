package com.scentstation.simulator.service;

import com.scentstation.simulator.config.SimulatorConfig;
import com.scentstation.simulator.model.*;
import com.scentstation.simulator.mqtt.MqttClientService;
import com.scentstation.simulator.mqtt.MqttMessageHandler;
import com.scentstation.simulator.persistence.ProcessedCommandRepository;
import com.scentstation.simulator.persistence.SlotStateRepository;
import com.scentstation.simulator.persistence.SqliteDatabaseManager;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DeviceSimulatorService {
    private final SimulatorConfig config;
    private final Consumer<String> activityLogger;
    private final BiConsumer<Boolean, String> connectionListener;
    private final DispenseCommandService.Listener commandListener;

    private final AtomicReference<SimulationScenario> scenario = new AtomicReference<>(SimulationScenario.NORMAL);

    private SqliteDatabaseManager dbManager;
    private SlotStateRepository slotRepository;
    private ProcessedCommandRepository commandRepository;
    private MqttClientService mqttClientService;
    private DispenseCommandService dispenseCommandService;
    private MqttMessageHandler messageHandler;
    private HeartbeatService heartbeatService;
    private TelemetryService telemetryService;

    private ExecutorService connectExecutor;
    private ExecutorService commandExecutor;
    private ScheduledExecutorService telemetryScheduler;

    public DeviceSimulatorService(
            SimulatorConfig config,
            Consumer<String> activityLogger,
            BiConsumer<Boolean, String> connectionListener,
            DispenseCommandService.Listener commandListener
    ) {
        this.config = config;
        this.activityLogger = activityLogger != null ? activityLogger : msg -> {};
        this.connectionListener = connectionListener != null ? connectionListener : (c, r) -> {};
        this.commandListener = commandListener != null ? commandListener : new DispenseCommandService.Listener() {};
        initializeStorageAndWorkers();
    }

    private synchronized void initializeStorageAndWorkers() {
        shutdownExecutors();
        this.dbManager = new SqliteDatabaseManager(config.resolveDatabasePath());
        this.slotRepository = new SlotStateRepository(dbManager);
        this.commandRepository = new ProcessedCommandRepository(dbManager, slotRepository);

        int uncertainCount = commandRepository.markInterruptedCommandsUncertain();
        if (uncertainCount > 0) {
            activityLogger.accept("[RECOVERY] Marked " + uncertainCount
                    + " interrupted IN_PROGRESS command(s) as UNCERTAIN after restart.");
        }
        slotRepository.ensureDefaultSlots(config.getMachineId());

        this.connectExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "sim-mqtt-connect");
            t.setDaemon(true);
            return t;
        });
        this.commandExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "sim-command-worker");
            t.setDaemon(true);
            return t;
        });
        this.telemetryScheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "sim-scheduler");
            t.setDaemon(true);
            return t;
        });

        this.mqttClientService = new MqttClientService(config, activityLogger, (connected, reason) -> {
            if (!connected) {
                stopSchedulers();
            }
            connectionListener.accept(connected, reason);
        });

        this.dispenseCommandService = new DispenseCommandService(
                config,
                mqttClientService,
                slotRepository,
                commandRepository,
                scenario::get,
                activityLogger,
                commandListener
        );

        this.messageHandler = new MqttMessageHandler(
                config,
                dispenseCommandService,
                commandExecutor,
                activityLogger
        );
        this.mqttClientService.setMessageConsumer(messageHandler::handleIncomingPayload);

        this.heartbeatService = new HeartbeatService(config, mqttClientService, scenario::get, activityLogger);
        this.telemetryService = new TelemetryService(
                config, mqttClientService, slotRepository, scenario::get, activityLogger);
    }

    public void reloadDeviceStorageIfNeeded() {
        initializeStorageAndWorkers();
    }

    public void connectAsync() {
        connectExecutor.submit(() -> {
            try {
                if (scenario.get() == SimulationScenario.OFFLINE) {
                    activityLogger.accept("[SIM] Switching scenario from OFFLINE to NORMAL on connect.");
                    scenario.set(SimulationScenario.NORMAL);
                }
                mqttClientService.connect();
                heartbeatService.start(telemetryScheduler);
                telemetryService.start(telemetryScheduler);
            } catch (Exception e) {
                activityLogger.accept("[ERROR] Failed to connect MQTT: " + e.getMessage());
                connectionListener.accept(false, e.getMessage());
            }
        });
    }

    public void disconnectAsync() {
        connectExecutor.submit(() -> {
            stopSchedulers();
            mqttClientService.disconnect();
        });
    }

    public void setScenario(SimulationScenario newScenario) {
        SimulationScenario prev = scenario.getAndSet(newScenario);
        activityLogger.accept("[SCENARIO] Changed from " + prev.name() + " to " + newScenario.name());
        if (newScenario == SimulationScenario.OFFLINE) {
            stopSchedulers();
            if (mqttClientService != null && mqttClientService.isConnected()) {
                connectExecutor.submit(() -> mqttClientService.disconnect());
            }
        } else if (prev == SimulationScenario.OFFLINE && mqttClientService != null && mqttClientService.isConnected()) {
            heartbeatService.start(telemetryScheduler);
            telemetryService.start(telemetryScheduler);
        }
    }

    public SimulationScenario getScenario() {
        return scenario.get();
    }

    public List<SlotState> getSlots() {
        return slotRepository.findByMachineId(config.getMachineId());
    }

    public void addOrUpdateSlot(String slotId, String fragranceName, BigDecimal remainingMl, BigDecimal capacityMl) {
        SlotState slot = new SlotState(
                slotId.trim(),
                config.getMachineId(),
                fragranceName.trim(),
                remainingMl,
                capacityMl,
                SlotState.computeStatus(remainingMl, capacityMl),
                Instant.now()
        );
        slotRepository.save(slot);
        commandListener.onSlotsUpdated();
        activityLogger.accept("[SLOT] Saved " + slot.getSlotId() + " (" + slot.getFragranceName()
                + ": " + slot.getRemainingMl() + "/" + slot.getCapacityMl() + " ml)");
    }

    public void refillAllSlots() {
        List<SlotState> slots = getSlots();
        for (SlotState slot : slots) {
            slot.setRemainingMl(slot.getCapacityMl());
            slotRepository.save(slot);
        }
        commandListener.onSlotsUpdated();
        activityLogger.accept("[SLOT] Refilled all slots to 100% capacity.");
    }

    public void deleteSlot(String slotId) {
        slotRepository.deleteSlot(config.getMachineId(), slotId);
        commandListener.onSlotsUpdated();
        activityLogger.accept("[SLOT] Deleted " + slotId);
    }

    public void triggerLocalTestDispense(String slotId, BigDecimal volumeMl) {
        DispenseCommand cmd = new DispenseCommand(
                "local-test-" + System.currentTimeMillis(),
                config.getMachineId(),
                config.getDeviceIdentity(),
                "DISPENSE",
                slotId,
                "LOCAL-DEMO",
                volumeMl,
                (int) config.getDispenseDurationMs(),
                Instant.now().plusSeconds(60)
        );
        commandExecutor.submit(() -> dispenseCommandService.processCommand(cmd));
    }

    public List<ProcessedCommand> getRecentCommands() {
        return commandRepository.findRecent(20);
    }

    public boolean isConnected() {
        return mqttClientService != null && mqttClientService.isConnected();
    }

    private void stopSchedulers() {
        if (heartbeatService != null) heartbeatService.stop();
        if (telemetryService != null) telemetryService.stop();
    }

    private void shutdownExecutors() {
        stopSchedulers();
        if (connectExecutor != null) connectExecutor.shutdownNow();
        if (commandExecutor != null) commandExecutor.shutdownNow();
        if (telemetryScheduler != null) telemetryScheduler.shutdownNow();
    }

    public void shutdown() {
        stopSchedulers();
        if (mqttClientService != null) {
            mqttClientService.disconnect();
        }
        shutdownExecutors();
    }
}
