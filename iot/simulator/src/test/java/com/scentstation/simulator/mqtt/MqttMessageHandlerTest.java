package com.scentstation.simulator.mqtt;

import com.scentstation.simulator.config.SimulatorConfig;
import com.scentstation.simulator.model.DispenseCommand;
import com.scentstation.simulator.service.DispenseCommandService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MqttMessageHandlerTest {

    private SimulatorConfig config;
    private DispenseCommandService commandService;
    private MqttMessageHandler handler;

    @BeforeEach
    void setUp() {
        config = new SimulatorConfig();
        config.setMachineId("machine-01");
        config.setDeviceIdentity("esp32-01");
        commandService = mock(DispenseCommandService.class);
        handler = new MqttMessageHandler(config, commandService, null, msg -> {});
    }

    @Test
    void shouldParseValidCommandAndDispatch() {
        String json = """
            {
              "commandId": "cmd-100",
              "machineId": "machine-01",
              "deviceIdentity": "esp32-01",
              "commandType": "DISPENSE",
              "slotId": "SLOT-1",
              "orderId": "ORD-99",
              "parameters": {
                "volumeMl": 2.5,
                "durationMs": 800
              }
            }
        """;

        Optional<DispenseCommand> parsed = handler.handleIncomingPayload("scentstation/machine-01/command", json);
        assertTrue(parsed.isPresent());
        assertEquals("cmd-100", parsed.get().commandId());
        assertEquals("SLOT-1", parsed.get().slotId());
        assertEquals(0, new BigDecimal("2.5").compareTo(parsed.get().volumeMl()));
        verify(commandService, times(1)).processCommand(any(DispenseCommand.class));
    }

    @Test
    void shouldRejectMalformedJson() {
        Optional<DispenseCommand> parsed = handler.handleIncomingPayload("scentstation/machine-01/command", "{bad-json");
        assertTrue(parsed.isEmpty());
        verifyNoInteractions(commandService);
    }

    @Test
    void shouldRejectMismatchedMachineId() {
        String json = """
            {
              "commandId": "cmd-101",
              "machineId": "machine-OTHER",
              "deviceIdentity": "esp32-01",
              "slotId": "SLOT-1",
              "volumeMl": 2.0
            }
        """;
        Optional<DispenseCommand> parsed = handler.handleIncomingPayload("scentstation/machine-01/command", json);
        assertTrue(parsed.isEmpty());
        verifyNoInteractions(commandService);
    }

    @Test
    void shouldRejectMismatchedDeviceIdentity() {
        String json = """
            {
              "commandId": "cmd-102",
              "machineId": "machine-01",
              "deviceIdentity": "wrong-device",
              "slotId": "SLOT-1",
              "volumeMl": 2.0
            }
        """;
        Optional<DispenseCommand> parsed = handler.handleIncomingPayload("scentstation/machine-01/command", json);
        assertTrue(parsed.isEmpty());
        verifyNoInteractions(commandService);
    }

    @Test
    void shouldRejectZeroOrNegativeVolume() {
        String zeroVol = """
            {"commandId":"cmd-103","machineId":"machine-01","deviceIdentity":"esp32-01","slotId":"SLOT-1","volumeMl":0}
        """;
        String negVol = """
            {"commandId":"cmd-104","machineId":"machine-01","deviceIdentity":"esp32-01","slotId":"SLOT-1","volumeMl":-1.5}
        """;
        assertTrue(handler.handleIncomingPayload("scentstation/machine-01/command", zeroVol).isEmpty());
        assertTrue(handler.handleIncomingPayload("scentstation/machine-01/command", negVol).isEmpty());
        verifyNoInteractions(commandService);
    }
}
