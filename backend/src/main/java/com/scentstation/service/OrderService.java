package com.scentstation.service;

import com.scentstation.common.security.CurrentUserPrincipal;
import com.scentstation.common.security.CurrentUserService;
import com.scentstation.contract.DispensePort;
import com.scentstation.contract.DispenseResultEvent;
import com.scentstation.contract.InventoryPort;
import com.scentstation.contract.OrderPort;
import com.scentstation.contract.OrderSnapshot;
import com.scentstation.contract.PaymentResultEvent;
import com.scentstation.dto.request.CreateOrderRequest;
import com.scentstation.dto.response.CreateOrderResponse;
import com.scentstation.dto.response.OrderResponse;
import com.scentstation.entity.Machine;
import com.scentstation.entity.MachineSlot;
import com.scentstation.entity.MachineSlotStatus;
import com.scentstation.entity.Order;
import com.scentstation.entity.Product;
import com.scentstation.exception.ResourceConflictException;
import com.scentstation.exception.ResourceNotFoundException;
import com.scentstation.mapper.OrderMapper;
import com.scentstation.repository.MachineRepository;
import com.scentstation.repository.MachineSlotRepository;
import com.scentstation.repository.OrderRepository;
import com.scentstation.security.KioskTokenService;
import jakarta.annotation.Nullable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService implements OrderPort {

    private static final Set<String> MANAGEMENT_ROLES = Set.of(
            "PLATFORM_SUPER_ADMIN",
            "BRAND_ADMIN",
            "OPERATIONS_MANAGER"
    );

    private final OrderRepository orderRepository;
    private final MachineRepository machineRepository;
    private final MachineSlotRepository machineSlotRepository;
    private final OrderMapper orderMapper;
    private final KioskTokenService kioskTokenService;
    private final CurrentUserService currentUserService;
    private final DispensePort dispensePort;
    private final InventoryPort inventoryPort;
    private final long orderExpiryMinutes;

    public OrderService(
            OrderRepository orderRepository,
            MachineRepository machineRepository,
            MachineSlotRepository machineSlotRepository,
            OrderMapper orderMapper,
            KioskTokenService kioskTokenService,
            CurrentUserService currentUserService,
            @Nullable DispensePort dispensePort,
            @Nullable InventoryPort inventoryPort,
            @Value("${app.order.expiry-minutes:10}") long orderExpiryMinutes
    ) {
        this.orderRepository = orderRepository;
        this.machineRepository = machineRepository;
        this.machineSlotRepository = machineSlotRepository;
        this.orderMapper = orderMapper;
        this.kioskTokenService = kioskTokenService;
        this.currentUserService = currentUserService;
        this.dispensePort = dispensePort;
        this.inventoryPort = inventoryPort;
        this.orderExpiryMinutes = orderExpiryMinutes;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrderSnapshot> findOrderById(Long orderId) {
        return orderRepository.findById(orderId).map(orderMapper::toSnapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderSnapshot getOrderById(Long orderId) {
        return findOrderById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long orderId) {
        return orderRepository.existsById(orderId);
    }

    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        Machine machine = machineRepository.findById(request.getMachineId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Machine not found with id: " + request.getMachineId()));

        validateMachineReady(machine);

        MachineSlot slot = machineSlotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Machine slot not found with id: " + request.getSlotId()));

        if (slot.getMachine() == null || !Objects.equals(slot.getMachine().getId(), machine.getId())) {
            throw new IllegalArgumentException("Slot does not belong to the specified machine");
        }

        validateSlotReady(slot);

        Product product = slot.getProduct();
        if (product == null) {
            throw new ResourceConflictException("Slot has no assigned product");
        }

        if (request.getProductId() != null && !Objects.equals(product.getId(), request.getProductId())) {
            throw new IllegalArgumentException("Product does not match the specified slot");
        }

        if (!Objects.equals(product.getTenantId(), machine.getTenantId())) {
            throw new ResourceConflictException("Product does not belong to the machine tenant");
        }

        if (!"ACTIVE".equalsIgnoreCase(product.getStatus())) {
            throw new ResourceConflictException("Product is not active");
        }

        validateSufficientInventory(slot, product);

        Order order = new Order();
        order.setTenantId(machine.getTenantId());
        order.setMachineId(machine.getId());
        order.setSlotId(slot.getId());
        order.setProductId(product.getId());
        order.setProductNameSnapshot(product.getName());
        order.setUnitPrice(product.getPrice());
        order.setCurrency(Order.DEFAULT_CURRENCY);
        order.setStatus(Order.STATUS_PENDING_PAYMENT);
        order.setExpiresAt(LocalDateTime.now().plusMinutes(orderExpiryMinutes));

        Order saved = orderRepository.save(order);
        String accessToken = kioskTokenService.generateToken(saved.getId(), saved.getMachineId());
        return orderMapper.toKioskResponse(saved, accessToken);
    }

    @Transactional(readOnly = true)
    public CreateOrderResponse getKioskOrder(Long orderId, String kioskToken) {
        kioskTokenService.validateTokenOrThrow(kioskToken, orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        Long tokenMachineId = kioskTokenService.extractMachineId(kioskToken);
        if (!Objects.equals(tokenMachineId, order.getMachineId())) {
            throw new AccessDeniedException("Kiosk token does not match order machine");
        }

        return orderMapper.toKioskResponse(order, null);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders(Long machineId, String status) {
        CurrentUserPrincipal currentUser = requireManagementRole();
        Long scopedTenantId = currentUser.isPlatformSuperAdmin() ? null : currentUser.tenantId();
        String normalizedStatus = (status == null || status.isBlank()) ? null : status.trim().toUpperCase();

        return orderRepository.search(scopedTenantId, machineId, normalizedStatus).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        CurrentUserPrincipal currentUser = requireManagementRole();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!currentUser.isPlatformSuperAdmin() && !Objects.equals(order.getTenantId(), currentUser.tenantId())) {
            throw new AccessDeniedException("You do not have permission to access this order");
        }

        return orderMapper.toResponse(order);
    }

    /**
     * Handles a payment result with pessimistic locking on the order.
     * Returns a dispatch plan only when the order transitions PENDING_PAYMENT -> PAID -> DISPENSING
     * and passes fresh machine/slot/product safety checks.
     * DispensePort is invoked outside the transaction to avoid holding DB locks while waiting on MQTT.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<DispenseDispatchPlan> processPaymentResult(PaymentResultEvent event) {
        if (event == null || event.orderId() == null) {
            return Optional.empty();
        }

        Order order = orderRepository.findByIdForUpdate(event.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + event.orderId()));

        if (PaymentResultEvent.STATUS_SUCCESS.equalsIgnoreCase(event.status())) {
            if (!Order.STATUS_PENDING_PAYMENT.equals(order.getStatus())) {
                if (Order.STATUS_EXPIRED.equals(order.getStatus())) {
                    // Late payment on an already-expired order: flag for reconciliation, never dispense
                    order.setStatus(Order.STATUS_REVIEW);
                    orderRepository.save(order);
                }
                // Duplicate or already-processed payment event: idempotent no-op
                return Optional.empty();
            }

            if (order.getExpiresAt() != null && order.getExpiresAt().isBefore(LocalDateTime.now())) {
                // Payment arrived after expiry deadline: flag for reconciliation, never dispense
                order.setStatus(Order.STATUS_REVIEW);
                orderRepository.save(order);
                return Optional.empty();
            }

            if (event.amount() == null
                    || event.amount().compareTo(order.getUnitPrice()) != 0
                    || event.currency() == null
                    || !order.getCurrency().equalsIgnoreCase(event.currency())) {
                // Amount or currency mismatch: flag for reconciliation, never dispense
                order.setStatus(Order.STATUS_REVIEW);
                orderRepository.save(order);
                return Optional.empty();
            }

            order.setStatus(Order.STATUS_PAID);
            orderRepository.save(order);

            return prepareDispenseInternal(order);
        }

        if (PaymentResultEvent.STATUS_EXPIRED.equalsIgnoreCase(event.status())
                && Order.STATUS_PENDING_PAYMENT.equals(order.getStatus())) {
            order.setStatus(Order.STATUS_EXPIRED);
            orderRepository.save(order);
        }

        return Optional.empty();
    }

    public void dispatchToIoT(DispenseDispatchPlan plan) {
        if (plan == null || dispensePort == null) {
            return;
        }
        try {
            dispensePort.dispatchForOrder(plan.orderSnapshot(), plan.volumeMl());
        } catch (RuntimeException ex) {
            markDispenseDispatchFailed(plan.orderSnapshot().id());
            throw ex;
        }
    }

    @Transactional
    public void markDispenseDispatchFailed(Long orderId) {
        orderRepository.findByIdForUpdate(orderId).ifPresent(order -> {
            if (Order.STATUS_DISPENSING.equals(order.getStatus())
                    || Order.STATUS_PAID.equals(order.getStatus())) {
                order.setStatus(Order.STATUS_REVIEW);
                orderRepository.save(order);
            }
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processDispenseResult(DispenseResultEvent event) {
        if (event == null || event.orderId() == null) {
            return;
        }

        Order order = orderRepository.findByIdForUpdate(event.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + event.orderId()));

        if (!Order.STATUS_DISPENSING.equals(order.getStatus())
                && !Order.STATUS_PAID.equals(order.getStatus())) {
            // Idempotent guard: ignore duplicate or out-of-state dispense events
            return;
        }

        if (DispenseResultEvent.STATUS_SUCCESS.equalsIgnoreCase(event.executionStatus())) {
            BigDecimal volumeMl = resolveSprayVolumeMl(order);
            if (inventoryPort != null) {
                try {
                    inventoryPort.deductForDispense(order.getId(), order.getSlotId(), volumeMl);
                } catch (RuntimeException ex) {
                    // Inventory deduction failed after physical dispense: persist REVIEW for reconciliation
                    order.setStatus(Order.STATUS_REVIEW);
                    orderRepository.save(order);
                    return;
                }
            }
            order.setStatus(Order.STATUS_DISPENSED);
            orderRepository.save(order);
        } else if (DispenseResultEvent.STATUS_FAILED.equalsIgnoreCase(event.executionStatus())) {
            order.setStatus(Order.STATUS_FAILED);
            orderRepository.save(order);
        } else if (DispenseResultEvent.STATUS_UNCERTAIN.equalsIgnoreCase(event.executionStatus())) {
            order.setStatus(Order.STATUS_REVIEW);
            orderRepository.save(order);
        }
    }

    @Transactional
    public int expirePendingOrders(LocalDateTime now) {
        List<Order> expiredCandidates = orderRepository.findByStatusAndExpiresAtBefore(
                Order.STATUS_PENDING_PAYMENT, now);
        int count = 0;
        for (Order candidate : expiredCandidates) {
            Optional<Order> lockedOpt = orderRepository.findByIdForUpdate(candidate.getId());
            if (lockedOpt.isPresent()) {
                Order locked = lockedOpt.get();
                if (Order.STATUS_PENDING_PAYMENT.equals(locked.getStatus())
                        && locked.getExpiresAt() != null
                        && locked.getExpiresAt().isBefore(now)) {
                    locked.setStatus(Order.STATUS_EXPIRED);
                    orderRepository.save(locked);
                    count++;
                }
            }
        }
        return count;
    }

    private Optional<DispenseDispatchPlan> prepareDispenseInternal(Order order) {
        if (!Order.STATUS_PAID.equals(order.getStatus())) {
            return Optional.empty();
        }
        Machine machine = machineRepository.findById(order.getMachineId()).orElse(null);
        MachineSlot slot = machineSlotRepository.findById(order.getSlotId()).orElse(null);
        if (machine == null || slot == null || slot.getProduct() == null) {
            order.setStatus(Order.STATUS_FAILED);
            orderRepository.save(order);
            return Optional.empty();
        }
        Product product = slot.getProduct();
        if (!"ONLINE".equalsIgnoreCase(machine.getConnectionStatus())
                || !"ACTIVE".equalsIgnoreCase(machine.getOperationalStatus())
                || (slot.getStatus() != MachineSlotStatus.AVAILABLE
                        && slot.getStatus() != MachineSlotStatus.LOW_STOCK)
                || !"ACTIVE".equalsIgnoreCase(product.getStatus())
                || slot.getCurrentVolumeMl() == null
                || product.getVolumePerSprayMl() == null
                || slot.getCurrentVolumeMl().compareTo(product.getVolumePerSprayMl()) < 0) {
            order.setStatus(Order.STATUS_FAILED);
            orderRepository.save(order);
            return Optional.empty();
        }

        order.setStatus(Order.STATUS_DISPENSING);
        Order saved = orderRepository.save(order);
        return Optional.of(new DispenseDispatchPlan(
                orderMapper.toSnapshot(saved),
                product.getVolumePerSprayMl()
        ));
    }

    private BigDecimal resolveSprayVolumeMl(Order order) {
        return machineSlotRepository.findById(order.getSlotId())
                .map(MachineSlot::getProduct)
                .map(Product::getVolumePerSprayMl)
                .orElse(BigDecimal.ONE);
    }

    private CurrentUserPrincipal requireManagementRole() {
        CurrentUserPrincipal currentUser = currentUserService.getCurrentUser();
        boolean authorized = MANAGEMENT_ROLES.stream().anyMatch(currentUser::hasRole);
        if (!authorized) {
            throw new AccessDeniedException("You do not have permission to access orders");
        }
        return currentUser;
    }

    private void validateMachineReady(Machine machine) {
        if (!"ONLINE".equalsIgnoreCase(machine.getConnectionStatus())) {
            throw new ResourceConflictException("Machine is offline");
        }
        if (!"ACTIVE".equalsIgnoreCase(machine.getOperationalStatus())) {
            throw new ResourceConflictException("Machine is not operational");
        }
    }

    private void validateSlotReady(MachineSlot slot) {
        if (slot.getStatus() != MachineSlotStatus.AVAILABLE
                && slot.getStatus() != MachineSlotStatus.LOW_STOCK) {
            throw new ResourceConflictException("Machine slot is not available: " + slot.getStatus());
        }
    }

    private void validateSufficientInventory(MachineSlot slot, Product product) {
        BigDecimal volumePerSpray = product.getVolumePerSprayMl();
        BigDecimal currentVolume = slot.getCurrentVolumeMl();
        if (volumePerSpray == null
                || currentVolume == null
                || volumePerSpray.compareTo(BigDecimal.ZERO) <= 0
                || currentVolume.compareTo(volumePerSpray) < 0) {
            throw new ResourceConflictException("Insufficient slot inventory");
        }
    }

    public record DispenseDispatchPlan(OrderSnapshot orderSnapshot, BigDecimal volumeMl) {
    }
}
