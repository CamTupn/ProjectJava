package com.scentstation.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scentstation.contract.DeviceAck;
import com.scentstation.contract.Heartbeat;
import com.scentstation.contract.Telemetry;
import com.scentstation.contract.DeviceResult;
import com.scentstation.contract.DeviceFault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import static org.mockito.Mockito.*;

class MqttMessageRouterTest {
    private ApplicationEventPublisher publisher;
    private MqttMessageRouter router;

    @BeforeEach void setUp() {
        publisher = mock(ApplicationEventPublisher.class);
        MqttProperties properties = new MqttProperties();
        properties.setDevices(Map.of("machine-1", "identity-1"));
        router = new MqttMessageRouter(new ObjectMapper().findAndRegisterModules(), publisher, properties);
    }

    @Test void routesHeartbeat() {
        String json = """
            {"type":"HEARTBEAT","machineId":"machine-1","deviceIdentity":"identity-1","status":"ONLINE","sentAt":"2026-11-27T10:00:00Z"}
            """;
        router.route("scentstation/machine-1/status", json.getBytes(StandardCharsets.UTF_8));
        verify(publisher).publishEvent(any(Heartbeat.class));
    }

    @Test void routesAck() {
        String json = """
            {"type":"ACK","machineId":"machine-1","deviceIdentity":"identity-1","commandId":"cmd-1","status":"ACCEPTED"}
            """;
        router.route("scentstation/machine-1/event", json.getBytes(StandardCharsets.UTF_8));
        verify(publisher).publishEvent(any(DeviceAck.class));
    }


    @Test void routesTelemetry() {
        String json = """
            {"type":"TELEMETRY","machineId":"machine-1","deviceIdentity":"identity-1","sensorType":"TEMPERATURE","readings":{"celsius":25.2}}
            """;
        router.route("scentstation/machine-1/status", json.getBytes(StandardCharsets.UTF_8));
        verify(publisher).publishEvent(any(Telemetry.class));
    }

    @Test void routesResult() {
        String json = """
            {"type":"RESULT","machineId":"machine-1","deviceIdentity":"identity-1","commandId":"cmd-1","result":"SUCCESS"}
            """;
        router.route("scentstation/machine-1/event", json.getBytes(StandardCharsets.UTF_8));
        verify(publisher).publishEvent(any(DeviceResult.class));
    }

    @Test void routesFault() {
        String json = """
            {"type":"FAULT","machineId":"machine-1","deviceIdentity":"identity-1","faultCode":"LOW_FLUID","severity":"WARNING"}
            """;
        router.route("scentstation/machine-1/event", json.getBytes(StandardCharsets.UTF_8));
        verify(publisher).publishEvent(any(DeviceFault.class));
    }

    @Test void rejectsMachineIdMismatch() {
        String json = """
            {"type":"HEARTBEAT","machineId":"other-machine","deviceIdentity":"identity-1","status":"ONLINE"}
            """;
        router.route("scentstation/machine-1/status", json.getBytes(StandardCharsets.UTF_8));
        verifyNoInteractions(publisher);
    }

    @Test void rejectsUntrustedDeviceIdentity() {
        String json = """
            {"type":"HEARTBEAT","machineId":"machine-1","deviceIdentity":"forged","status":"ONLINE"}
            """;
        router.route("scentstation/machine-1/status", json.getBytes(StandardCharsets.UTF_8));
        verifyNoInteractions(publisher);
    }

    @Test void rejectsInvalidJsonWithoutThrowing() {
        router.route("scentstation/machine-1/status", "not-json".getBytes(StandardCharsets.UTF_8));
        verifyNoInteractions(publisher);
    }
}
