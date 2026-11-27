package com.scentstation.service;

import com.scentstation.contract.*;
import com.scentstation.dto.response.*;
import com.scentstation.entity.*;
import com.scentstation.mapper.MonitoringMapper;
import com.scentstation.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitoringService {

    private final MonitoringLockRepository lockRepository;
    private final MachineSlotRepository machineSlotRepository;
    private final SensorReadingRepository sensorReadingRepository;
    private final DeviceEventRepository deviceEventRepository;
    private final AlertRepository alertRepository;
    private final AlertService alertService;
    private final MonitoringMapper mapper;

    // TODO: Đối chiếu ngưỡng lệch tồn kho trong Issue #53
    @Value("${monitoring.inventory-mismatch-tolerance-ml:15}")
    private int inventoryMismatchToleranceMl;

    @Transactional
    public void processHeartbeat(Heartbeat hb) {
        // TODO: Đối chiếu tên getter trong contract/Heartbeat.java (vd: getMachineId(), getDeviceIdentity(), getTimestamp())
        Machine machine = lockRepository.findByIdForUpdate(hb.getMachineId())
                .orElseThrow(() -> new IllegalArgumentException("Machine không tồn tại: " + hb.getMachineId()));

        if (!machine.getDeviceIdentity().equals(hb.getDeviceIdentity())) {
            log.warn("Heartbeat sai deviceIdentity: machineId={}, expected={}, actual={}",
                    machine.getId(), machine.getDeviceIdentity(), hb.getDeviceIdentity());
            return;
        }

        Instant eventTs = hb.getTimestamp() != null ? hb.getTimestamp() : Instant.now();
        LocalDateTime eventLocal = LocalDateTime.ofInstant(eventTs, ZoneOffset.UTC);
        if (machine.getLastPingAt() != null && eventLocal.isBefore(machine.getLastPingAt())) {
            log.debug("Bỏ qua Heartbeat cũ hơn lastPingAt: machineId={}", machine.getId());
            return;
        }

        machine.setLastPingAt(eventLocal);
        machine.setConnectionStatus("ONLINE");
        // Tuyệt đối KHÔNG sửa operationalStatus
        lockRepository.save(machine);

        alertService.resolveByType(machine, AlertType.MACHINE_OFFLINE);
    }

    @Transactional
    public void processTelemetry(Telemetry telemetry) {
        // TODO: Đối chiếu tên getter trong contract/Telemetry.java
        Machine machine = lockRepository.findById(telemetry.getMachineId())
                .orElseThrow(() -> new IllegalArgumentException("Machine không tồn tại: " + telemetry.getMachineId()));

        MachineSlot slot = null;
        if (telemetry.getSlotId() != null) {
            slot = machineSlotRepository.findById(telemetry.getSlotId())
                    .orElseThrow(() -> new IllegalArgumentException("Slot không tồn tại: " + telemetry.getSlotId()));

            if (!slot.getMachine().getId().equals(machine.getId())) {
                log.warn("Telemetry từ chối do slot {} không thuộc machine {}", slot.getId(), machine.getId());
                return;
            }
        }

        SensorReading reading = SensorReading.builder()
                .tenantId(machine.getTenantId())
                .machine(machine)
                .slot(slot)
                .measuredVolumeMl(telemetry.getMeasuredVolumeMl())
                .temperatureC(telemetry.getTemperatureC())
                .humidityPct(telemetry.getHumidityPct())
                .recordedAt(telemetry.getTimestamp() != null ? telemetry.getTimestamp() : Instant.now())
                .build();

        // Lưu cảm biến, tuyệt đối KHÔNG ghi vào slot.setCurrentVolumeMl(...)
        sensorReadingRepository.save(reading);

        if (slot != null && telemetry.getMeasuredVolumeMl() != null && slot.getCurrentVolumeMl() != null) {
            java.math.BigDecimal measured = java.math.BigDecimal.valueOf(telemetry.getMeasuredVolumeMl());
            java.math.BigDecimal diff = slot.getCurrentVolumeMl().subtract(measured).abs();
            if (diff.compareTo(java.math.BigDecimal.valueOf(inventoryMismatchToleranceMl)) > 0) {
                alertService.createIfAbsent(
                        machine,
                        slot,
                        AlertType.INVENTORY_MISMATCH,
                        String.format("Lệch tồn kho tại slot %d: hệ thống=%sml, cảm biến=%dml",
                                slot.getSlotNumber(), slot.getCurrentVolumeMl().stripTrailingZeros().toPlainString(), telemetry.getMeasuredVolumeMl())
                );
            }
        }
    }

    @Transactional
    public void processDeviceFault(DeviceFault fault) {
        // TODO: Đối chiếu tên getter trong contract/DeviceFault.java
        Machine machine = lockRepository.findById(fault.getMachineId())
                .orElseThrow(() -> new IllegalArgumentException("Machine không tồn tại: " + fault.getMachineId()));

        MachineSlot slot = (fault.getSlotId() != null)
                ? machineSlotRepository.findById(fault.getSlotId()).orElse(null)
                : null;

        DeviceEvent event = DeviceEvent.builder()
                .tenantId(machine.getTenantId())
                .machine(machine)
                .slot(slot)
                .eventType("FAULT")
                .errorCode(fault.getErrorCode())
                .severity("ERROR")
                .message(fault.getMessage())
                .occurredAt(fault.getTimestamp() != null ? fault.getTimestamp() : Instant.now())
                .build();
        deviceEventRepository.save(event);

        alertService.createIfAbsent(
                machine,
                slot,
                AlertType.DEVICE_ERROR,
                "Lỗi thiết bị [" + fault.getErrorCode() + "]: " + fault.getMessage()
        );
    }

    @Transactional
    public void processDeviceAck(DeviceAck ack) {
        // TODO: Đối chiếu getter trong contract/DeviceAck.java
        Machine machine = lockRepository.findById(ack.getMachineId())
                .orElseThrow(() -> new IllegalArgumentException("Machine không tồn tại: " + ack.getMachineId()));

        DeviceEvent event = DeviceEvent.builder()
                .tenantId(machine.getTenantId())
                .machine(machine)
                .eventType("ACK")
                .message("Command acknowledged: " + ack.getCommandId())
                .occurredAt(ack.getTimestamp() != null ? ack.getTimestamp() : Instant.now())
                .build();
        deviceEventRepository.save(event);
    }

    @Transactional
    public void processDeviceResult(DeviceResult result) {
        // TODO: Đối chiếu getter trong contract/DeviceResult.java
        Machine machine = lockRepository.findById(result.getMachineId())
                .orElseThrow(() -> new IllegalArgumentException("Machine không tồn tại: " + result.getMachineId()));

        DeviceEvent event = DeviceEvent.builder()
                .tenantId(machine.getTenantId())
                .machine(machine)
                .eventType("RESULT")
                .message("Command result [" + result.getCommandId() + "]: " + result.getStatus())
                .occurredAt(result.getTimestamp() != null ? result.getTimestamp() : Instant.now())
                .build();
        deviceEventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public MonitoringOverviewResponse getOverview(Long tenantId) {
        List<Machine> machines = lockRepository.findByTenantId(tenantId);
        long total = machines.size();
        long online = machines.stream()
                .filter(m -> "ONLINE".equals(m.getConnectionStatus()))
                .count();
        long offline = total - online;

        long openAlerts = alertRepository.countByTenantIdAndStatusIn(tenantId, EnumSet.of(AlertStatus.OPEN));
        long ackAlerts = alertRepository.countByTenantIdAndStatusIn(tenantId, EnumSet.of(AlertStatus.ACKNOWLEDGED));

        return MonitoringOverviewResponse.builder()
                .totalMachines(total)
                .onlineMachines(online)
                .offlineMachines(offline)
                .activeAlerts(openAlerts + ackAlerts)
                .openAlerts(openAlerts)
                .acknowledgedAlerts(ackAlerts)
                .build();
    }

    @Transactional(readOnly = true)
    public MachineMonitoringResponse getMachineMonitoring(Long machineId, Long tenantId) {
        Machine machine = lockRepository.findByIdAndTenantId(machineId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy Machine"));

        long activeAlerts = alertRepository
                .findByTenantIdAndMachineIdOrderByCreatedAtDesc(tenantId, machineId, Pageable.unpaged())
                .stream()
                .filter(a -> AlertService.ACTIVE_STATUSES.contains(a.getStatus()))
                .count();

        return MachineMonitoringResponse.builder()
                .machineId(machine.getId())
                .machineCode(machine.getDeviceIdentity())
                .deviceIdentity(machine.getDeviceIdentity())
                .connectionStatus(machine.getConnectionStatus())
                .operationalStatus(machine.getOperationalStatus())
                .lastPingAt(machine.getLastPingAt())
                .activeAlertCount(activeAlerts)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<SensorReadingResponse> getSensorReadings(Long machineId, Long tenantId, Pageable pageable) {
        lockRepository.findByIdAndTenantId(machineId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy Machine"));
        return sensorReadingRepository
                .findByTenantIdAndMachineIdOrderByRecordedAtDesc(tenantId, machineId, pageable)
                .map(mapper::toSensorReadingResponse);
    }

    @Transactional(readOnly = true)
    public Page<DeviceEventResponse> getDeviceEvents(Long machineId, Long tenantId, Pageable pageable) {
        lockRepository.findByIdAndTenantId(machineId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy Machine"));
        return deviceEventRepository
                .findByTenantIdAndMachineIdOrderByOccurredAtDesc(tenantId, machineId, pageable)
                .map(mapper::toDeviceEventResponse);
    }
}