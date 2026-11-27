package com.scentstation.service;

import com.scentstation.entity.DispenseCommand;
import com.scentstation.entity.DispenseCommandStatus;
import com.scentstation.entity.DispenseCommandType;
import com.scentstation.entity.DispenseExecution;
import com.scentstation.entity.DispenseExecutionStatus;
import com.scentstation.entity.Machine;
import com.scentstation.entity.MachineSlot;
import com.scentstation.entity.MachineSlotStatus;
import com.scentstation.entity.Order;
import com.scentstation.entity.Product;
import com.scentstation.common.security.CurrentUserService;
import com.scentstation.exception.ResourceConflictException;
import com.scentstation.repository.DispenseCommandRepository;
import com.scentstation.repository.DispenseExecutionRepository;
import com.scentstation.repository.MachineRepository;
import com.scentstation.repository.MachineSlotRepository;
import com.scentstation.repository.OrderRepository;
import com.scentstation.contract.DispenseRequest;
import com.scentstation.contract.DispenseNotice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DispenseServiceTest {
    @Mock private DispenseCommandRepository commandRepository;
    @Mock private DispenseExecutionRepository executionRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private MachineRepository machineRepository;
    @Mock private MachineSlotRepository slotRepository;
    @Mock private CurrentUserService currentUserService;

    private DispenseService service;
    private Order order;
    private Machine machine;
    private MachineSlot slot;

    @BeforeEach
    void setUp() {
        service = new DispenseService(commandRepository, executionRepository,
                orderRepository, machineRepository, slotRepository, currentUserService);

        machine = new Machine();
        machine.setId(20L);
        machine.setTenantId(10L);
        machine.setConnectionStatus("ONLINE");
        machine.setOperationalStatus("ACTIVE");

        Product product = new Product();
        product.setTenantId(10L);
        product.setStatus("ACTIVE");

        slot = new MachineSlot();
        slot.setId(30L);
        slot.setMachine(machine);
        slot.setProduct(product);
        slot.setStatus(MachineSlotStatus.AVAILABLE);
        slot.setCurrentVolumeMl(new BigDecimal("10.00"));

        order = new Order();
        order.setId(40L);
        order.setTenantId(10L);
        order.setMachineId(20L);
        order.setSlotId(30L);
        order.setStatus("PAID");
    }

    @Test
    void createsCommandOnlyAfterAllSafetyChecksPass() {
        when(orderRepository.findById(40L)).thenReturn(Optional.of(order));
        when(machineRepository.findById(20L)).thenReturn(Optional.of(machine));
        when(slotRepository.findById(30L)).thenReturn(Optional.of(slot));
        when(commandRepository.findByOrder_Id(40L)).thenReturn(Optional.empty());
        when(commandRepository.saveAndFlush(any(DispenseCommand.class))).thenAnswer(invocation -> {
            DispenseCommand command = invocation.getArgument(0);
            command.setStatus(DispenseCommandStatus.PENDING);
            return command;
        });

        var result = service.createCommand(new DispenseRequest(
                40L, 20L, 30L, new BigDecimal("1.00")));

        assertEquals("PENDING", result.status());
        verify(commandRepository).saveAndFlush(any(DispenseCommand.class));
    }

    @Test
    void rejectsOrderThatIsNotPaid() {
        order.setStatus("PENDING_PAYMENT");
        when(orderRepository.findById(40L)).thenReturn(Optional.of(order));
        when(machineRepository.findById(20L)).thenReturn(Optional.of(machine));
        when(slotRepository.findById(30L)).thenReturn(Optional.of(slot));

        assertThrows(ResourceConflictException.class, () -> service.createCommand(
                new DispenseRequest(40L, 20L, 30L, new BigDecimal("1.00"))));
        verify(commandRepository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsDuplicateOrderCommand() {
        when(orderRepository.findById(40L)).thenReturn(Optional.of(order));
        when(machineRepository.findById(20L)).thenReturn(Optional.of(machine));
        when(slotRepository.findById(30L)).thenReturn(Optional.of(slot));
        DispenseCommand existing = new DispenseCommand();
        existing.setCommandType(DispenseCommandType.CUSTOMER_ORDER);
        when(commandRepository.findByOrder_Id(40L)).thenReturn(Optional.of(existing));

        assertThrows(ResourceConflictException.class, () -> service.createCommand(
                new DispenseRequest(40L, 20L, 30L, new BigDecimal("1.00"))));
    }

    @Test
    void recordsSuccessAndDoesNotCreateDuplicateExecution() {
        DispenseCommand command = new DispenseCommand();
        command.setId(50L);
        command.setCommandToken("command-50");
        command.setMachine(machine);
        command.setStatus(DispenseCommandStatus.ACKNOWLEDGED);
        when(commandRepository.findByCommandToken("command-50")).thenReturn(Optional.of(command));
        when(executionRepository.findByCommand_Id(50L)).thenReturn(Optional.empty());
        when(executionRepository.save(any(DispenseExecution.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DispenseNotice notice = new DispenseNotice("command-50", 20L,
                DispenseExecutionStatus.SUCCESS, null, "{\"volume\":1.0}");
        DispenseExecution first = service.recordNotice(notice);

        assertEquals(DispenseExecutionStatus.SUCCESS, first.getExecutionStatus());
        assertEquals(DispenseCommandStatus.SUCCESS, command.getStatus());

        when(executionRepository.findByCommand_Id(50L)).thenReturn(Optional.of(first));
        DispenseExecution duplicate = service.recordNotice(notice);
        assertEquals(first, duplicate);
        verify(executionRepository, times(1)).save(any(DispenseExecution.class));
    }

    @Test
    void rejectsResultFromAnotherMachine() {
        DispenseCommand command = new DispenseCommand();
        command.setId(50L);
        command.setCommandToken("command-50");
        command.setMachine(machine);
        when(commandRepository.findByCommandToken("command-50")).thenReturn(Optional.of(command));

        assertThrows(ResourceConflictException.class, () -> service.recordNotice(
                new DispenseNotice("command-50", 999L,
                        DispenseExecutionStatus.SUCCESS, null, null)));
        verifyNoInteractions(executionRepository);
    }
}
