package com.scentstation.scheduler;

import com.scentstation.entity.AlertType;
import com.scentstation.entity.Machine;
import com.scentstation.repository.MonitoringLockRepository;
import com.scentstation.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class HeartbeatMonitor {

    private final MonitoringLockRepository lockRepository;
    private final AlertService alertService;

    // TODO: Đối chiếu ngưỡng offline (giây) trong Issue #53
    @Value("${monitoring.heartbeat.offline-threshold-seconds:90}")
    private long offlineThresholdSeconds;

    @Scheduled(fixedDelayString = "${monitoring.heartbeat.check-interval-ms:30000}")
    @Transactional
    public void checkOfflineMachines() {
        Instant cutoff = Instant.now().minusSeconds(offlineThresholdSeconds);
        LocalDateTime cutoffLocal = LocalDateTime.ofInstant(cutoff, ZoneOffset.UTC);
        List<Machine> timedOutMachines = lockRepository.findOnlineMachinesTimedOut(cutoffLocal);

        for (Machine m : timedOutMachines) {
            Machine locked = lockRepository.findByIdForUpdate(m.getId()).orElse(null);
            if (locked != null
                    && "ONLINE".equals(locked.getConnectionStatus())
                    && locked.getLastPingAt() != null
                    && locked.getLastPingAt().isBefore(cutoffLocal)) {

                locked.setConnectionStatus("OFFLINE");
                // KHÔNG sửa operationalStatus
                lockRepository.save(locked);

                alertService.createIfAbsent(
                        locked,
                        null,
                        AlertType.MACHINE_OFFLINE,
                        "Máy " + locked.getDeviceIdentity() + " mất kết nối quá " + offlineThresholdSeconds + " giây"
                );
                log.info("Đánh dấu OFFLINE và tạo alert MACHINE_OFFLINE cho machineId={}", locked.getId());
            }
        }
    }
}