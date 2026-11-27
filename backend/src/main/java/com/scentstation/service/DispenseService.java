package com.scentstation.service;

import com.scentstation.contract.CommandView;
import com.scentstation.contract.DispenseNotice;
import com.scentstation.contract.DispensePort;
import com.scentstation.contract.DispenseRequest;
import com.scentstation.common.security.CurrentUserPrincipal;
import com.scentstation.common.security.CurrentUserService;
import com.scentstation.entity.DispenseCommand;
import com.scentstation.entity.DispenseCommandStatus;
import com.scentstation.entity.DispenseCommandType;
import com.scentstation.entity.DispenseExecution;
import com.scentstation.entity.DispenseExecutionStatus;
import com.scentstation.entity.Machine;
import com.scentstation.entity.MachineSlot;
import com.scentstation.entity.Order;
import com.scentstation.exception.ResourceConflictException;
import com.scentstation.exception.ResourceNotFoundException;
import com.scentstation.repository.DispenseCommandRepository;
import com.scentstation.repository.DispenseExecutionRepository;
import com.scentstation.repository.MachineRepository;
import com.scentstation.repository.MachineSlotRepository;
import com.scentstation.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class DispenseService implements DispensePort {
    private final DispenseCommandRepository commandRepository;
    private final DispenseExecutionRepository executionRepository;
    private final OrderRepository orderRepository;
    private final MachineRepository machineRepository;
    private final MachineSlotRepository slotRepository;
    private final CurrentUserService currentUserService;

    public DispenseService(DispenseCommandRepository commandRepository,
                           DispenseExecutionRepository executionRepository,
                           OrderRepository orderRepository,
                           MachineRepository machineRepository,
                           MachineSlotRepository slotRepository,
                           CurrentUserService currentUserService) {
        this.commandRepository = commandRepository;
        this.executionRepository = executionRepository;
        this.orderRepository = orderRepository;
        this.machineRepository = machineRepository;
        this.slotRepository = slotRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    public CommandView createCommand(DispenseRequest request) {
        if (request == null || request.orderId() == null || request.machineId() == null
                || request.slotId() == null || request.volumeMl() == null
                || request.volumeMl().signum() <= 0) {
            throw new IllegalArgumentException("Dispense request is invalid");
        }

        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + request.orderId()));
        Machine machine = machineRepository.findById(request.machineId())
                .orElseThrow(() -> new ResourceNotFoundException("Machine not found with ID: " + request.machineId()));
        MachineSlot slot = slotRepository.findById(request.slotId())
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found with ID: " + request.slotId()));

        validateRequest(order, machine, slot, request);
        if (commandRepository.findByOrder_Id(order.getId()).isPresent()) {
            throw new ResourceConflictException("A dispense command already exists for order: " + order.getId());
        }

        DispenseCommand command = new DispenseCommand();
        command.setCommandToken(UUID.randomUUID().toString());
        command.setOrder(order);
        command.setMachine(machine);
        command.setSlot(slot);
        command.setCommandType(DispenseCommandType.CUSTOMER_ORDER);
        command.setVolumeMl(request.volumeMl());
        command.setStatus(DispenseCommandStatus.PENDING);
        command.setCreatedAt(LocalDateTime.now());
        try {
            command = commandRepository.saveAndFlush(command);
        } catch (DataIntegrityViolationException ex) {
            throw new ResourceConflictException("A dispense command already exists for this order", ex);
        }
        return new CommandView(command.getId(), command.getCommandToken(), command.getStatus().name());
    }

    private void validateRequest(Order order, Machine machine, MachineSlot slot, DispenseRequest request) {
        if (!"PAID".equals(order.getStatus()) && !"DISPENSING".equals(order.getStatus())) {
            throw new ResourceConflictException("Order must be PAID before dispensing");
        }
        if (!machine.getId().equals(order.getMachineId()) || !slot.getId().equals(order.getSlotId())
                || !machine.getId().equals(slot.getMachine().getId())) {
            throw new ResourceConflictException("Order, machine and slot do not match");
        }
        if (!"ONLINE".equals(machine.getConnectionStatus()) || !"ACTIVE".equals(machine.getOperationalStatus())) {
            throw new ResourceConflictException("Machine is not ready for dispensing");
        }
        if (slot.getStatus() == null ||
                !(slot.getStatus().name().equals("AVAILABLE") || slot.getStatus().name().equals("LOW_STOCK"))) {
            throw new ResourceConflictException("Slot is not available for dispensing");
        }
        if (slot.getProduct() == null || !"ACTIVE".equals(slot.getProduct().getStatus())
                || !slot.getMachine().getTenantId().equals(slot.getProduct().getTenantId())
                || !slot.getMachine().getTenantId().equals(order.getTenantId())) {
            throw new ResourceConflictException("Slot product is not active or is outside the tenant scope");
        }
        if (slot.getCurrentVolumeMl().compareTo(request.volumeMl()) < 0) {
            throw new ResourceConflictException("Slot does not have enough volume");
        }
    }

    public List<DispenseCommand> findCommands(Long machineId, DispenseCommandStatus status) {
        List<DispenseCommand> commands;
        if (machineId != null && status != null) {
            commands = commandRepository.findByMachine_IdAndStatus(machineId, status);
        } else if (machineId != null) {
            commands = commandRepository.findByMachine_Id(machineId);
        } else if (status != null) {
            commands = commandRepository.findByStatus(status);
        } else {
            commands = commandRepository.findAllByOrderByCreatedAtDesc();
        }
        CurrentUserPrincipal current = currentUserService.getCurrentUser();
        if (current.isPlatformSuperAdmin()) return commands;
        return commands.stream()
                .filter(command -> command.getMachine().getTenantId().equals(current.tenantId()))
                .toList();
    }

    public DispenseCommand getCommand(Long id) {
        return commandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dispense command not found with ID: " + id));
    }

    public DispenseExecution getExecution(Long commandId) {
        return executionRepository.findByCommand_Id(commandId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispense execution not found"));
    }

    public void markSent(DispenseCommand command) {
        if (command.getStatus() != DispenseCommandStatus.PENDING) return;
        command.setStatus(DispenseCommandStatus.SENT);
        command.setSentAt(LocalDateTime.now());
        commandRepository.save(command);
    }

    public void acknowledge(String token) {
        DispenseCommand command = findByToken(token);
        if (command.getStatus() == DispenseCommandStatus.SENT) {
            command.setStatus(DispenseCommandStatus.ACKNOWLEDGED);
            commandRepository.save(command);
        }
    }

    public DispenseExecution recordNotice(DispenseNotice notice) {
        DispenseCommand command = findByToken(notice.commandToken());
        if (!command.getMachine().getId().equals(notice.machineId())) {
            throw new ResourceConflictException("Dispense result belongs to another machine");
        }
        DispenseExecution existing = executionRepository.findByCommand_Id(command.getId()).orElse(null);
        if (existing != null && existing.getExecutionStatus() != DispenseExecutionStatus.UNCERTAIN) {
            return existing;
        }
        if (existing == null) {
            existing = new DispenseExecution();
            existing.setCommand(command);
            existing.setRecordedAt(LocalDateTime.now());
        }
        existing.setExecutionStatus(notice.status());
        existing.setErrorCode(notice.errorCode());
        existing.setHardwareTelemetry(notice.telemetry());
        command.setStatus(toCommandStatus(notice.status()));
        command.setCompletedAt(LocalDateTime.now());
        commandRepository.save(command);
        return executionRepository.save(existing);
    }

    public void markUncertain(DispenseCommand command) {
        if (command.getStatus() == DispenseCommandStatus.PENDING
                || command.getStatus() == DispenseCommandStatus.SENT
                || command.getStatus() == DispenseCommandStatus.ACKNOWLEDGED) {
            command.setStatus(DispenseCommandStatus.UNCERTAIN);
            commandRepository.save(command);
            if (executionRepository.findByCommand_Id(command.getId()).isEmpty()) {
                DispenseExecution execution = new DispenseExecution();
                execution.setCommand(command);
                execution.setExecutionStatus(DispenseExecutionStatus.UNCERTAIN);
                execution.setRecordedAt(LocalDateTime.now());
                executionRepository.save(execution);
            }
        }
    }

    private DispenseCommand findByToken(String token) {
        return commandRepository.findByCommandToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Dispense command not found"));
    }

    private DispenseCommandStatus toCommandStatus(DispenseExecutionStatus status) {
        return switch (status) {
            case SUCCESS -> DispenseCommandStatus.SUCCESS;
            case FAILED -> DispenseCommandStatus.FAILED;
            case UNCERTAIN -> DispenseCommandStatus.UNCERTAIN;
        };
    }
}
