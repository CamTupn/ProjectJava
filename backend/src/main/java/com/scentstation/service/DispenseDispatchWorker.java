package com.scentstation.service;

import com.scentstation.contract.DispensePublisher;
import com.scentstation.entity.DispenseCommand;
import com.scentstation.entity.DispenseCommandStatus;
import com.scentstation.repository.DispenseCommandRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DispenseDispatchWorker {
    private final DispenseCommandRepository commandRepository;
    private final DispenseService dispenseService;
    private final ObjectProvider<DispensePublisher> publisherProvider;

    public DispenseDispatchWorker(DispenseCommandRepository commandRepository,
                                  DispenseService dispenseService,
                                  ObjectProvider<DispensePublisher> publisherProvider) {
        this.commandRepository = commandRepository;
        this.dispenseService = dispenseService;
        this.publisherProvider = publisherProvider;
    }

    @Scheduled(fixedDelayString = "${dispense.dispatch-interval-ms:1000}")
    public void dispatchPendingCommands() {
        DispensePublisher publisher = publisherProvider.getIfAvailable();
        if (publisher == null) return;
        commandRepository.findByStatusIn(java.util.List.of(DispenseCommandStatus.PENDING))
                .forEach(command -> dispatch(command, publisher));
    }

    public void dispatch(DispenseCommand command, DispensePublisher publisher) {
        if (command.getStatus() != DispenseCommandStatus.PENDING) return;
        if (command.getOrder() != null && !"DISPENSING".equals(command.getOrder().getStatus())) return;
        dispenseService.markSent(command);
        publisher.publish(command);
    }
}
