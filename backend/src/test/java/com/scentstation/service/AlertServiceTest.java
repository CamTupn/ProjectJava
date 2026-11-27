package com.scentstation.service;

import com.scentstation.entity.*;
import com.scentstation.mapper.MonitoringMapper;
import com.scentstation.repository.AlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @Spy
    private MonitoringMapper mapper = new MonitoringMapper();

    @InjectMocks
    private AlertService alertService;

    private Machine machine;

    @BeforeEach
    void setUp() {
        machine = new Machine();
        machine.setId(1L);
        machine.setTenantId(10L);
        machine.setDeviceIdentity("DEV-001");
    }

    @Test
    void createIfAbsent_shouldCreateWhenNoActiveAlert() {
        when(alertRepository.existsByMachineIdAndTypeAndSlotIdAndStatusIn(
                eq(1L), eq(AlertType.MACHINE_OFFLINE), isNull(), anySet()
        )).thenReturn(false);
        when(alertRepository.save(any(Alert.class))).thenAnswer(i -> i.getArgument(0));

        Alert created = alertService.createIfAbsent(machine, null, AlertType.MACHINE_OFFLINE, "Offline");

        assertNotNull(created);
        assertEquals(AlertStatus.OPEN, created.getStatus());
        verify(alertRepository).save(any(Alert.class));
    }

    @Test
    void createIfAbsent_shouldNotDuplicateWhenActiveAlertExists() {
        when(alertRepository.existsByMachineIdAndTypeAndSlotIdAndStatusIn(
                eq(1L), eq(AlertType.MACHINE_OFFLINE), isNull(), anySet()
        )).thenReturn(true);

        Alert result = alertService.createIfAbsent(machine, null, AlertType.MACHINE_OFFLINE, "Offline");

        assertNull(result);
        verify(alertRepository, never()).save(any());
    }

    @Test
    void acknowledge_fromOpen_shouldSucceed() {
        Alert alert = Alert.builder()
                .id(5L).tenantId(10L).machine(machine)
                .type(AlertType.DEVICE_ERROR).status(AlertStatus.OPEN).message("Err")
                .build();
        when(alertRepository.findByIdAndTenantId(5L, 10L)).thenReturn(Optional.of(alert));
        when(alertRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var res = alertService.acknowledge(5L, 10L, 99L);

        assertEquals(AlertStatus.ACKNOWLEDGED, res.getStatus());
        assertEquals(99L, res.getAcknowledgedBy());
    }

    @Test
    void acknowledge_fromResolved_shouldThrowBadRequest() {
        Alert alert = Alert.builder()
                .id(5L).tenantId(10L).machine(machine)
                .type(AlertType.DEVICE_ERROR).status(AlertStatus.RESOLVED).message("Err")
                .build();
        when(alertRepository.findByIdAndTenantId(5L, 10L)).thenReturn(Optional.of(alert));

        assertThrows(ResponseStatusException.class, () -> alertService.acknowledge(5L, 10L, 99L));
    }
}