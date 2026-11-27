package com.scentstation.service;

import com.scentstation.entity.DispenseCommandStatus;
import com.scentstation.repository.DispenseCommandRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DispenseTimeoutJob {
    private final DispenseCommandRepository commandRepository;
    private final DispenseService dispenseService;
    private final Duration timeout;

    public DispenseTimeoutJob(DispenseCommandRepository commandRepository,
                              DispenseService dispenseService,
                              org.springframework.core.env.Environment environment) {
        this.commandRepository = commandRepository;
        this.dispenseService = dispenseService;
        long timeoutMs = environment.getProperty("dispense.timeout-ms", Long.class, 30_000L);
        this.timeout = Duration.ofMillis(timeoutMs);
    }

    @Scheduled(fixedDelayString = "${dispense.timeout-scan-interval-ms:5000}")
    public void markTimedOutCommands() {
        LocalDateTime cutoff = LocalDateTime.now().minus(timeout);
        commandRepository.findByStatusIn(List.of(DispenseCommandStatus.SENT,
                        DispenseCommandStatus.ACKNOWLEDGED))
                .stream()
                .filter(command -> command.getSentAt() != null && command.getSentAt().isBefore(cutoff))
                .forEach(dispenseService::markUncertain);
    }
}
