package com.scentstation.service;

import com.scentstation.entity.DispenseCommand;
import com.scentstation.entity.DispenseCommandStatus;
import com.scentstation.repository.DispenseCommandRepository;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

class DispenseTimeoutJobTest {
    @Test
    void marksSentCommandUncertainAfterTimeout() {
        DispenseCommandRepository repository = mock(DispenseCommandRepository.class);
        DispenseService service = mock(DispenseService.class);
        DispenseCommand command = new DispenseCommand();
        command.setStatus(DispenseCommandStatus.SENT);
        command.setSentAt(LocalDateTime.now().minusSeconds(10));
        when(repository.findByStatusIn(List.of(DispenseCommandStatus.SENT,
                DispenseCommandStatus.ACKNOWLEDGED))).thenReturn(List.of(command));

        MockEnvironment environment = new MockEnvironment()
                .withProperty("dispense.timeout-ms", "100");
        DispenseTimeoutJob job = new DispenseTimeoutJob(repository, service, environment);
        job.markTimedOutCommands();

        verify(service).markUncertain(command);
    }

    @Test
    void leavesRecentCommandUntouched() {
        DispenseCommandRepository repository = mock(DispenseCommandRepository.class);
        DispenseService service = mock(DispenseService.class);
        DispenseCommand command = new DispenseCommand();
        command.setStatus(DispenseCommandStatus.ACKNOWLEDGED);
        command.setSentAt(LocalDateTime.now());
        when(repository.findByStatusIn(List.of(DispenseCommandStatus.SENT,
                DispenseCommandStatus.ACKNOWLEDGED))).thenReturn(List.of(command));

        DispenseTimeoutJob job = new DispenseTimeoutJob(repository, service,
                new MockEnvironment().withProperty("dispense.timeout-ms", "30000"));
        job.markTimedOutCommands();

        verifyNoInteractions(service);
    }
}
