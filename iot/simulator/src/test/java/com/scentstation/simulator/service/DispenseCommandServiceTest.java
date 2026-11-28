package com.scentstation.simulator.service;

import com.scentstation.simulator.config.SimulatorConfig;
import com.scentstation.simulator.model.*;
import com.scentstation.simulator.mqtt.SimulatorMqttPublisher;
import com.scentstation.simulator.persistence.ProcessedCommandRepository;
import com.scentstation.simulator.persistence.SlotStateRepository;
import com.scentstation.simulator.persistence.SqliteDatabaseManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class DispenseCommandServiceTest {

    @TempDir
    Path tempDir;

    private SimulatorConfig config;
    private SlotStateRepository slotRepository;
    private ProcessedCommandRepository commandRepository;
    private AtomicReference<SimulationScenario> scenario;
    private List<String> publishedStatus;
    private List<String> publishedEvents;
    private DispenseCommandService service;

    @BeforeEach
    void setUp() {
        config = new SimulatorConfig();
        config.setMachineId("machine-01");
        config.setDeviceIdentity("esp32-01");
        config.setDispenseDurationMs(0L);

        SqliteDatabaseManager dbManager = new SqliteDatabaseManager(tempDir.resolve("service-test.db"));
        slotRepository = new SlotStateRepository(dbManager);
        commandRepository = new ProcessedCommandRepository(dbManager, slotRepository);
        scenario = new AtomicReference<>(SimulationScenario.NORMAL);

        publishedStatus = new ArrayList<>();
        publishedEvents = new ArrayList<>();

        SimulatorMqttPublisher fakePublisher = new SimulatorMqttPublisher() {
            @Override
            public void publishStatus(String jsonPayload) {
                publishedStatus.add(jsonPayload);
            }

            @Override
            public void publishEvent(String jsonPayload) {
                publishedEvents.add(jsonPayload);
            }

            @Override
            public boolean isConnected() {
                return true;
            }
        };

        slotRepository.save(new SlotState(
                "SLOT-1", "machine-01", "Bleu",
                new BigDecimal("50.00"), new BigDecimal("100.00"), "READY", Instant.now()
        ));

        service = new DispenseCommandService(
                config,
                fakePublisher,
                slotRepository,
                commandRepository,
                scenario::get,
                msg -> {},
                null
        );
    }

    private DispenseCommand buildCmd(String cmdId, String slotId, String vol) {
        return new DispenseCommand(
                cmdId, "machine-01", "esp32-01", "DISPENSE",
                slotId, "ORD-1", new BigDecimal(vol), 0, Instant.now().plusSeconds(60)
        );
    }

    @Test
    void normalScenarioShouldReturnSuccessAndDeductVolumeOnce() {
        DispenseCommand cmd = buildCmd("cmd-normal-1", "SLOT-1", "3.00");

        ProcessedCommand first = service.processCommand(cmd);
        assertEquals(CommandStatus.DONE, first.status());
        assertEquals("SUCCESS", first.result());
        assertEquals(0, new BigDecimal("3.00").compareTo(first.volumeDispensedMl()));
        assertEquals(2, publishedEvents.size());
        assertTrue(publishedEvents.get(0).contains("\"ACK\""));
        assertTrue(publishedEvents.get(1).contains("\"SUCCESS\""));

        SlotState slotAfterFirst = slotRepository.findByMachineIdAndSlotId("machine-01", "SLOT-1").orElseThrow();
        assertEquals(0, new BigDecimal("47.00").compareTo(slotAfterFirst.getRemainingMl()));

        // Duplicate command must NOT dispense or deduct fluid a second time
        ProcessedCommand duplicate = service.processCommand(cmd);
        assertEquals("SUCCESS", duplicate.result());
        SlotState slotAfterDuplicate = slotRepository.findByMachineIdAndSlotId("machine-01", "SLOT-1").orElseThrow();
        assertEquals(0, new BigDecimal("47.00").compareTo(slotAfterDuplicate.getRemainingMl()));
    }

    @Test
    void deviceErrorScenarioShouldReturnFailedWithoutDeductingVolume() {
        scenario.set(SimulationScenario.DEVICE_ERROR);
        ProcessedCommand res = service.processCommand(buildCmd("cmd-err-1", "SLOT-1", "2.00"));

        assertEquals(CommandStatus.DONE, res.status());
        assertEquals("FAILED", res.result());
        assertEquals("DEVICE_ERROR", res.errorCode());
        assertTrue(publishedEvents.stream().anyMatch(e -> e.contains("\"FAULT\"")));
        assertTrue(publishedEvents.stream().anyMatch(e -> e.contains("\"DEVICE_ERROR\"")));

        SlotState slot = slotRepository.findByMachineIdAndSlotId("machine-01", "SLOT-1").orElseThrow();
        assertEquals(0, new BigDecimal("50.00").compareTo(slot.getRemainingMl()));
    }

    @Test
    void emptySlotScenarioShouldReturnFailed() {
        scenario.set(SimulationScenario.EMPTY_SLOT);
        ProcessedCommand res = service.processCommand(buildCmd("cmd-empty-1", "SLOT-1", "2.00"));

        assertEquals(CommandStatus.DONE, res.status());
        assertEquals("FAILED", res.result());
        assertEquals("EMPTY_SLOT", res.errorCode());
    }

    @Test
    void nonExistentSlotShouldReturnFailed() {
        ProcessedCommand res = service.processCommand(buildCmd("cmd-noslot-1", "SLOT-99", "2.00"));
        assertEquals("FAILED", res.result());
        assertEquals("SLOT_NOT_FOUND", res.errorCode());
    }

    @Test
    void timeoutScenarioShouldSendAckButNoResultAndKeepInProgress() {
        scenario.set(SimulationScenario.TIMEOUT);
        ProcessedCommand res = service.processCommand(buildCmd("cmd-timeout-1", "SLOT-1", "2.00"));

        assertEquals(CommandStatus.IN_PROGRESS, res.status());
        assertEquals(1, publishedEvents.size());
        assertTrue(publishedEvents.get(0).contains("\"ACK\""));
    }

    @Test
    void offlineScenarioShouldStopHeartbeatAndIgnoreCommands() {
        scenario.set(SimulationScenario.OFFLINE);
        HeartbeatService hb = new HeartbeatService(config, new SimulatorMqttPublisher() {
            @Override public void publishStatus(String jsonPayload) { publishedStatus.add(jsonPayload); }
            @Override public void publishEvent(String jsonPayload) { publishedEvents.add(jsonPayload); }
            @Override public boolean isConnected() { return true; }
        }, scenario::get, msg -> {});

        assertFalse(hb.publishHeartbeatOnce());
        assertTrue(publishedStatus.isEmpty());
        assertNull(service.processCommand(buildCmd("cmd-offline-1", "SLOT-1", "2.00")));
    }

    @Test
    void duplicateCommandIdWithDifferentPayloadShouldBeRejected() {
        service.processCommand(buildCmd("cmd-conflict-1", "SLOT-1", "2.00"));
        DispenseCommand modifiedPayload = buildCmd("cmd-conflict-1", "SLOT-1", "9.00");

        service.processCommand(modifiedPayload);
        assertTrue(publishedEvents.get(publishedEvents.size() - 1).contains("PAYLOAD_CONFLICT"));

        SlotState slot = slotRepository.findByMachineIdAndSlotId("machine-01", "SLOT-1").orElseThrow();
        assertEquals(0, new BigDecimal("48.00").compareTo(slot.getRemainingMl()));
    }
}
