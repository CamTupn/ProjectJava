package com.scentstation.service;

import com.scentstation.contract.DispensePublisher;
import com.scentstation.entity.DispenseCommand;
import com.scentstation.entity.DispenseCommandStatus;
import com.scentstation.entity.Order;
import com.scentstation.repository.DispenseCommandRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;

import static org.mockito.Mockito.*;

class DispenseDispatchWorkerTest {
    @Test
    void dispatchesPendingCommandOnlyWhenOrderIsDispensing() {
        DispenseCommandRepository repository = mock(DispenseCommandRepository.class);
        DispenseService service = mock(DispenseService.class);
        ObjectProvider<DispensePublisher> provider = mock(ObjectProvider.class);
        DispensePublisher publisher = mock(DispensePublisher.class);
        when(provider.getIfAvailable()).thenReturn(publisher);

        Order order = new Order();
        order.setStatus("DISPENSING");
        DispenseCommand command = new DispenseCommand();
        command.setOrder(order);
        command.setStatus(DispenseCommandStatus.PENDING);
        when(repository.findByStatusIn(List.of(DispenseCommandStatus.PENDING)))
                .thenReturn(List.of(command));

        DispenseDispatchWorker worker = new DispenseDispatchWorker(repository, service, provider);
        worker.dispatchPendingCommands();

        verify(service).markSent(command);
        verify(publisher).publish(command);
    }

    @Test
    void doesNotPublishPaidOrderBeforeItEntersDispensing() {
        DispenseCommandRepository repository = mock(DispenseCommandRepository.class);
        DispenseService service = mock(DispenseService.class);
        ObjectProvider<DispensePublisher> provider = mock(ObjectProvider.class);
        DispensePublisher publisher = mock(DispensePublisher.class);
        when(provider.getIfAvailable()).thenReturn(publisher);

        Order order = new Order();
        order.setStatus("PAID");
        DispenseCommand command = new DispenseCommand();
        command.setOrder(order);
        command.setStatus(DispenseCommandStatus.PENDING);
        when(repository.findByStatusIn(List.of(DispenseCommandStatus.PENDING)))
                .thenReturn(List.of(command));

        new DispenseDispatchWorker(repository, service, provider).dispatchPendingCommands();

        verifyNoInteractions(service, publisher);
    }
}
