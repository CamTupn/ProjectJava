package com.scentstation.service;

import com.scentstation.dto.response.AlertResponse;
import com.scentstation.entity.*;
import com.scentstation.mapper.MonitoringMapper;
import com.scentstation.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    public static final Set<AlertStatus> ACTIVE_STATUSES =
            EnumSet.of(AlertStatus.OPEN, AlertStatus.ACKNOWLEDGED);

    private final AlertRepository alertRepository;
    private final MonitoringMapper mapper;

    @Transactional
    public Alert createIfAbsent(Machine machine, MachineSlot slot, AlertType type, String message) {
        Long slotId = (slot != null) ? slot.getId() : null;
        boolean exists = alertRepository.existsByMachineIdAndTypeAndSlotIdAndStatusIn(
                machine.getId(), type, slotId, ACTIVE_STATUSES
        );
        if (exists) {
            log.debug("Bỏ qua alert trùng: machineId={}, slotId={}, type={}", machine.getId(), slotId, type);
            return null;
        }

        Alert alert = Alert.builder()
                .tenantId(machine.getTenantId())
                .machine(machine)
                .slot(slot)
                .type(type)
                .status(AlertStatus.OPEN)
                .message(message)
                .build();

        return alertRepository.save(alert);
    }

    @Transactional
    public void resolveByType(Machine machine, AlertType type) {
        List<Alert> activeAlerts = alertRepository.findByMachineIdAndTypeAndStatusIn(
                machine.getId(), type, ACTIVE_STATUSES
        );
        Instant now = Instant.now();
        for (Alert alert : activeAlerts) {
            alert.setStatus(AlertStatus.RESOLVED);
            alert.setResolvedAt(now);
        }
        if (!activeAlerts.isEmpty()) {
            alertRepository.saveAll(activeAlerts);
        }
    }

    @Transactional(readOnly = true)
    public Page<AlertResponse> getAlerts(Long tenantId, AlertStatus status, Long machineId, Pageable pageable) {
        Page<Alert> page;
        if (status != null) {
            page = alertRepository.findByTenantIdAndStatusOrderByCreatedAtDesc(tenantId, status, pageable);
        } else if (machineId != null) {
            page = alertRepository.findByTenantIdAndMachineIdOrderByCreatedAtDesc(tenantId, machineId, pageable);
        } else {
            page = alertRepository.findByTenantIdOrderByCreatedAtDesc(tenantId, pageable);
        }
        return page.map(mapper::toAlertResponse);
    }

    @Transactional
    public AlertResponse acknowledge(Long alertId, Long tenantId, Long userId) {
        Alert alert = getAlertForTenant(alertId, tenantId);
        if (alert.getStatus() != AlertStatus.OPEN) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Chỉ có thể Acknowledge alert đang ở trạng thái OPEN. Trạng thái hiện tại: " + alert.getStatus()
            );
        }
        alert.setStatus(AlertStatus.ACKNOWLEDGED);
        alert.setAcknowledgedBy(userId);
        alert.setAcknowledgedAt(Instant.now());
        return mapper.toAlertResponse(alertRepository.save(alert));
    }

    @Transactional
    public AlertResponse resolve(Long alertId, Long tenantId, Long userId) {
        Alert alert = getAlertForTenant(alertId, tenantId);
        if (!ACTIVE_STATUSES.contains(alert.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Không thể Resolve alert đã ở trạng thái: " + alert.getStatus()
            );
        }
        alert.setStatus(AlertStatus.RESOLVED);
        alert.setResolvedBy(userId);
        alert.setResolvedAt(Instant.now());
        return mapper.toAlertResponse(alertRepository.save(alert));
    }

    @Transactional
    public AlertResponse dismiss(Long alertId, Long tenantId, Long userId) {
        Alert alert = getAlertForTenant(alertId, tenantId);
        if (!ACTIVE_STATUSES.contains(alert.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Không thể Dismiss alert đã ở trạng thái: " + alert.getStatus()
            );
        }
        alert.setStatus(AlertStatus.DISMISSED);
        alert.setResolvedBy(userId);
        alert.setResolvedAt(Instant.now());
        return mapper.toAlertResponse(alertRepository.save(alert));
    }

    private Alert getAlertForTenant(Long alertId, Long tenantId) {
        return alertRepository.findByIdAndTenantId(alertId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy Alert"));
    }
}