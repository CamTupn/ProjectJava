package com.scentstation.service;

import com.scentstation.contract.Heartbeat;
import com.scentstation.entity.AlertType;
import com.scentstation.entity.Machine;
import com.scentstation.mapper.MonitoringMapper;
import com.scentstation.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonitoringServiceTest {

    @Mock private MonitoringLockRepository lockRepository;
    @Mock private MachineSlotRepository machineSlotRepository;
    @Mock private SensorReadingRepository sensorReadingRepository;
    @Mock private DeviceEventRepository deviceEventRepository;
    @Mock private AlertRepository alertRepository;
    @Mock private AlertService alertService;
    @Spy private MonitoringMapper mapper = new MonitoringMapper();

    @InjectMocks
    private MonitoringService monitoringService;

    @Test
    void processHeartbeat_validReconnect_shouldSetOnlineAndResolveOfflineAlert() {
        Machine m = new Machine();
        m.setId(1L);
        m.setDeviceIdentity("KIOSK-01");
        m.setConnectionStatus("OFFLINE");
        m.setOperationalStatus("ACTIVE");

        Heartbeat hb = mock(Heartbeat.class);
        when(hb.getMachineId()).thenReturn(1L);
        when(hb.getDeviceIdentity()).thenReturn("KIOSK-01");
        when(hb.getTimestamp()).thenReturn(Instant.now());

        when(lockRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(m));

        monitoringService.processHeartbeat(hb);

        assertEquals("ONLINE", m.getConnectionStatus());
        assertEquals("ACTIVE", m.getOperationalStatus());
        verify(alertService).resolveByType(m, AlertType.MACHINE_OFFLINE);
    }

    @Test
    void processHeartbeat_wrongDeviceIdentity_shouldIgnore() {
        Machine m = new Machine();
        m.setId(1L);
        m.setDeviceIdentity("KIOSK-01");
        m.setConnectionStatus("OFFLINE");

        Heartbeat hb = mock(Heartbeat.class);
        when(hb.getMachineId()).thenReturn(1L);
        when(hb.getDeviceIdentity()).thenReturn("FAKE-DEVICE");

        when(lockRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(m));

        monitoringService.processHeartbeat(hb);

        assertEquals("OFFLINE", m.getConnectionStatus());
        verify(alertService, never()).resolveByType(any(), any());
    }
}