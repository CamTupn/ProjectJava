package com.scentstation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.scentstation.common.security.CurrentUserPrincipal;
import com.scentstation.common.security.CurrentUserService;
import com.scentstation.contract.DispensePort;
import com.scentstation.contract.DispenseResultEvent;
import com.scentstation.contract.InventoryPort;
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
import com.scentstation.mapper.OrderMapper;
import com.scentstation.repository.MachineRepository;
import com.scentstation.repository.MachineSlotRepository;
import com.scentstation.repository.OrderRepository;
import com.scentstation.security.KioskTokenService;
import com.scentstation.service.OrderService.DispenseDispatchPlan;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MachineRepository machineRepository;

    @Mock
    private MachineSlotRepository machineSlotRepository;

    @Mock
    private KioskTokenService kioskTokenService;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private DispensePort dispensePort;

    @Mock
    private InventoryPort inventoryPort;

    private OrderMapper orderMapper;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderMapper = new OrderMapper();
        orderService = new OrderService(
                orderRepository,
                machineRepository,
                machineSlotRepository,
                orderMapper,
                kioskTokenService,
                currentUserService,
                dispensePort,
                inventoryPort,
                10L
        );
    }

    @Test
    void createOrder_succeedsWithBackendPriceAndProductNameSnapshot() {
        Machine machine = createMachine(10L, 1L, "ONLINE", "ACTIVE");
        Product product = createProduct(30L, 1L, "Midnight Oud", "25000.00", "2.50", "ACTIVE");
        MachineSlot slot = createSlot(20L, machine, product, "50.00", MachineSlotStatus.AVAILABLE);

        when(machineRepository.findById(10L)).thenReturn(Optional.of(machine));
        when(machineSlotRepository.findById(20L)).thenReturn(Optional.of(slot));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(100L);
            return o;
        });
        when(kioskTokenService.generateToken(100L, 10L)).thenReturn("kiosk-jwt-token");

        CreateOrderResponse response = orderService.createOrder(new CreateOrderRequest(10L, 20L, 30L));

        assertEquals(100L, response.id());
        assertEquals(10L, response.machineId());
        assertEquals(20L, response.slotId());
        assertEquals(30L, response.productId());
        assertEquals("Midnight Oud", response.productName());
        assertEquals(new BigDecimal("25000.00"), response.unitPrice());
        assertEquals("VND", response.currency());
        assertEquals(Order.STATUS_PENDING_PAYMENT, response.status());
        assertNotNull(response.expiresAt());
        assertEquals("kiosk-jwt-token", response.accessToken());
    }

    @Test
    void createOrder_rejectsOfflineMachine() {
        Machine offlineMachine = createMachine(10L, 1L, "OFFLINE", "ACTIVE");
        when(machineRepository.findById(10L)).thenReturn(Optional.of(offlineMachine));

        assertThrows(ResourceConflictException.class,
                () -> orderService.createOrder(new CreateOrderRequest(10L, 20L, 30L)));
    }

    @Test
    void createOrder_rejectsSlotBelongingToDifferentMachine() {
        Machine machine = createMachine(10L, 1L, "ONLINE", "ACTIVE");
        Machine otherMachine = createMachine(99L, 1L, "ONLINE", "ACTIVE");
        Product product = createProduct(30L, 1L, "Ocean Mist", "15000.00", "2.00", "ACTIVE");
        MachineSlot slot = createSlot(20L, otherMachine, product, "50.00", MachineSlotStatus.AVAILABLE);

        when(machineRepository.findById(10L)).thenReturn(Optional.of(machine));
        when(machineSlotRepository.findById(20L)).thenReturn(Optional.of(slot));

        assertThrows(IllegalArgumentException.class,
                () -> orderService.createOrder(new CreateOrderRequest(10L, 20L, 30L)));
    }

    @Test
    void createOrder_rejectsInactiveProduct() {
        Machine machine = createMachine(10L, 1L, "ONLINE", "ACTIVE");
        Product inactive = createProduct(30L, 1L, "Discontinued", "15000.00", "2.00", "INACTIVE");
        MachineSlot slot = createSlot(20L, machine, inactive, "50.00", MachineSlotStatus.AVAILABLE);

        when(machineRepository.findById(10L)).thenReturn(Optional.of(machine));
        when(machineSlotRepository.findById(20L)).thenReturn(Optional.of(slot));

        assertThrows(ResourceConflictException.class,
                () -> orderService.createOrder(new CreateOrderRequest(10L, 20L, 30L)));
    }

    @Test
    void createOrder_rejectsInsufficientInventory() {
        Machine machine = createMachine(10L, 1L, "ONLINE", "ACTIVE");
        Product product = createProduct(30L, 1L, "Ocean Mist", "15000.00", "2.50", "ACTIVE");
        MachineSlot slot = createSlot(20L, machine, product, "1.00", MachineSlotStatus.LOW_STOCK);

        when(machineRepository.findById(10L)).thenReturn(Optional.of(machine));
        when(machineSlotRepository.findById(20L)).thenReturn(Optional.of(slot));

        assertThrows(ResourceConflictException.class,
                () -> orderService.createOrder(new CreateOrderRequest(10L, 20L, 30L)));
    }

    @Test
    void processPaymentResult_successTransitionsToDispensingAndDispatchesOnce() {
        Order order = createOrderEntity(100L, 1L, 10L, 20L, 30L, "15000.00", Order.STATUS_PENDING_PAYMENT,
                LocalDateTime.now().plusMinutes(5));
        Machine machine = createMachine(10L, 1L, "ONLINE", "ACTIVE");
        Product product = createProduct(30L, 1L, "Ocean Mist", "15000.00", "2.00", "ACTIVE");
        MachineSlot slot = createSlot(20L, machine, product, "50.00", MachineSlotStatus.AVAILABLE);

        when(orderRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(order));
        when(machineRepository.findById(10L)).thenReturn(Optional.of(machine));
        when(machineSlotRepository.findById(20L)).thenReturn(Optional.of(slot));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentResultEvent event = new PaymentResultEvent(
                100L, "PAY-001", PaymentResultEvent.STATUS_SUCCESS, new BigDecimal("15000.00"), "VND");

        Optional<DispenseDispatchPlan> planOpt = orderService.processPaymentResult(event);

        assertTrue(planOpt.isPresent());
        assertEquals(Order.STATUS_DISPENSING, order.getStatus());

        orderService.dispatchToIoT(planOpt.get());
        verify(dispensePort, times(1)).dispatchForOrder(any(OrderSnapshot.class), eq(new BigDecimal("2.00")));
    }

    @Test
    void processPaymentResult_duplicateEventDoesNotCreateSecondCommand() {
        Order alreadyDispensing = createOrderEntity(100L, 1L, 10L, 20L, 30L, "15000.00", Order.STATUS_DISPENSING,
                LocalDateTime.now().plusMinutes(5));
        when(orderRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(alreadyDispensing));

        PaymentResultEvent duplicateEvent = new PaymentResultEvent(
                100L, "PAY-001", PaymentResultEvent.STATUS_SUCCESS, new BigDecimal("15000.00"), "VND");

        Optional<DispenseDispatchPlan> planOpt = orderService.processPaymentResult(duplicateEvent);

        assertFalse(planOpt.isPresent());
        assertEquals(Order.STATUS_DISPENSING, alreadyDispensing.getStatus());
        verifyNoInteractions(dispensePort);
    }

    @Test
    void processPaymentResult_latePaymentAfterExpiryMovesToReviewWithoutDispensing() {
        Order expiredOrder = createOrderEntity(100L, 1L, 10L, 20L, 30L, "15000.00", Order.STATUS_EXPIRED,
                LocalDateTime.now().minusMinutes(2));
        when(orderRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(expiredOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentResultEvent lateEvent = new PaymentResultEvent(
                100L, "PAY-002", PaymentResultEvent.STATUS_SUCCESS, new BigDecimal("15000.00"), "VND");

        Optional<DispenseDispatchPlan> planOpt = orderService.processPaymentResult(lateEvent);

        assertFalse(planOpt.isPresent());
        assertEquals(Order.STATUS_REVIEW, expiredOrder.getStatus());
        verifyNoInteractions(dispensePort);
    }

    @Test
    void expirePendingOrders_marksExpiredOrders() {
        LocalDateTime now = LocalDateTime.now();
        Order staleOrder = createOrderEntity(101L, 1L, 10L, 20L, 30L, "15000.00", Order.STATUS_PENDING_PAYMENT,
                now.minusMinutes(1));
        when(orderRepository.findByStatusAndExpiresAtBefore(Order.STATUS_PENDING_PAYMENT, now))
                .thenReturn(List.of(staleOrder));
        when(orderRepository.findByIdForUpdate(101L)).thenReturn(Optional.of(staleOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        int expiredCount = orderService.expirePendingOrders(now);

        assertEquals(1, expiredCount);
        assertEquals(Order.STATUS_EXPIRED, staleOrder.getStatus());
    }

    @Test
    void processDispenseResult_successDeductsInventoryAndMarksDispensed() {
        Order order = createOrderEntity(100L, 1L, 10L, 20L, 30L, "15000.00", Order.STATUS_DISPENSING,
                LocalDateTime.now().plusMinutes(5));
        Machine machine = createMachine(10L, 1L, "ONLINE", "ACTIVE");
        Product product = createProduct(30L, 1L, "Ocean Mist", "15000.00", "2.00", "ACTIVE");
        MachineSlot slot = createSlot(20L, machine, product, "50.00", MachineSlotStatus.AVAILABLE);

        when(orderRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(order));
        when(machineSlotRepository.findById(20L)).thenReturn(Optional.of(slot));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.processDispenseResult(new DispenseResultEvent(100L, 500L, DispenseResultEvent.STATUS_SUCCESS, null));

        verify(inventoryPort).deductForDispense(100L, 20L, new BigDecimal("2.00"));
        assertEquals(Order.STATUS_DISPENSED, order.getStatus());
    }

    @Test
    void processDispenseResult_failedMarksOrderFailedWithoutInventoryDeduction() {
        Order order = createOrderEntity(100L, 1L, 10L, 20L, 30L, "15000.00", Order.STATUS_DISPENSING,
                LocalDateTime.now().plusMinutes(5));
        when(orderRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.processDispenseResult(new DispenseResultEvent(100L, 500L, DispenseResultEvent.STATUS_FAILED, "NOZZLE_ERR"));

        assertEquals(Order.STATUS_FAILED, order.getStatus());
        verifyNoInteractions(inventoryPort);
    }

    @Test
    void processDispenseResult_uncertainMarksOrderReviewWithoutInventoryDeduction() {
        Order order = createOrderEntity(100L, 1L, 10L, 20L, 30L, "15000.00", Order.STATUS_DISPENSING,
                LocalDateTime.now().plusMinutes(5));
        when(orderRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.processDispenseResult(new DispenseResultEvent(100L, 500L, DispenseResultEvent.STATUS_UNCERTAIN, "TIMEOUT"));

        assertEquals(Order.STATUS_REVIEW, order.getStatus());
        verifyNoInteractions(inventoryPort);
    }

    @Test
    void processDispenseResult_inventoryDeductionFailureMarksReview() {
        Order order = createOrderEntity(100L, 1L, 10L, 20L, 30L, "15000.00", Order.STATUS_DISPENSING,
                LocalDateTime.now().plusMinutes(5));
        Machine machine = createMachine(10L, 1L, "ONLINE", "ACTIVE");
        Product product = createProduct(30L, 1L, "Ocean Mist", "15000.00", "2.00", "ACTIVE");
        MachineSlot slot = createSlot(20L, machine, product, "50.00", MachineSlotStatus.AVAILABLE);

        when(orderRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(order));
        when(machineSlotRepository.findById(20L)).thenReturn(Optional.of(slot));
        doThrow(new IllegalStateException("Inventory lock failed"))
                .when(inventoryPort).deductForDispense(100L, 20L, new BigDecimal("2.00"));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.processDispenseResult(new DispenseResultEvent(100L, 500L, DispenseResultEvent.STATUS_SUCCESS, null));

        assertEquals(Order.STATUS_REVIEW, order.getStatus());
    }

    @Test
    void getOrder_enforcesTenantIsolation() {
        CurrentUserPrincipal brandAdminTenant2 = new CurrentUserPrincipal(
                5L, "brand2", 2L, "T2", Set.of("BRAND_ADMIN"));
        Order tenant1Order = createOrderEntity(100L, 1L, 10L, 20L, 30L, "15000.00", Order.STATUS_PAID,
                LocalDateTime.now().plusMinutes(5));

        when(currentUserService.getCurrentUser()).thenReturn(brandAdminTenant2);
        when(orderRepository.findById(100L)).thenReturn(Optional.of(tenant1Order));

        assertThrows(AccessDeniedException.class, () -> orderService.getOrder(100L));
    }

    @Test
    void getOrders_scopesBrandAdminToOwnTenantAndSuperAdminGlobal() {
        CurrentUserPrincipal brandAdminTenant2 = new CurrentUserPrincipal(
                5L, "brand2", 2L, "T2", Set.of("BRAND_ADMIN"));
        when(currentUserService.getCurrentUser()).thenReturn(brandAdminTenant2);
        when(orderRepository.search(2L, 10L, "PAID")).thenReturn(List.of());

        List<OrderResponse> scoped = orderService.getOrders(10L, "paid");
        assertEquals(0, scoped.size());
        verify(orderRepository).search(2L, 10L, "PAID");

        CurrentUserPrincipal superAdmin = new CurrentUserPrincipal(
                1L, "super", null, null, Set.of("PLATFORM_SUPER_ADMIN"));
        when(currentUserService.getCurrentUser()).thenReturn(superAdmin);
        when(orderRepository.search(null, null, null)).thenReturn(List.of());

        orderService.getOrders(null, null);
        verify(orderRepository).search(null, null, null);
    }

    @Test
    void orderPort_getOrderByIdReturnsSnapshot() {
        Order order = createOrderEntity(100L, 1L, 10L, 20L, 30L, "15000.00", Order.STATUS_PAID,
                LocalDateTime.now().plusMinutes(5));
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        OrderSnapshot snapshot = orderService.getOrderById(100L);

        assertEquals(100L, snapshot.id());
        assertEquals(1L, snapshot.tenantId());
        assertEquals("Ocean Mist", snapshot.productNameSnapshot());
        assertEquals(Order.STATUS_PAID, snapshot.status());
    }

    private Machine createMachine(Long id, Long tenantId, String conn, String op) {
        Machine m = new Machine();
        m.setId(id);
        m.setTenantId(tenantId);
        m.setConnectionStatus(conn);
        m.setOperationalStatus(op);
        return m;
    }

    private Product createProduct(Long id, Long tenantId, String name, String price, String sprayVol, String status) {
        Product p = new Product();
        try {
            var field = Product.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(p, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        p.setTenantId(tenantId);
        p.setName(name);
        p.setPrice(new BigDecimal(price));
        p.setVolumePerSprayMl(new BigDecimal(sprayVol));
        p.setStatus(status);
        return p;
    }

    private MachineSlot createSlot(Long id, Machine machine, Product product, String currentVol, MachineSlotStatus status) {
        MachineSlot s = new MachineSlot();
        s.setId(id);
        s.setMachine(machine);
        s.setSlotNumber(1);
        s.setProduct(product);
        s.setCurrentVolumeMl(new BigDecimal(currentVol));
        s.setStatus(status);
        return s;
    }

    private Order createOrderEntity(Long id, Long tenantId, Long machineId, Long slotId, Long productId,
                                    String price, String status, LocalDateTime expiresAt) {
        Order o = new Order();
        o.setId(id);
        o.setTenantId(tenantId);
        o.setMachineId(machineId);
        o.setSlotId(slotId);
        o.setProductId(productId);
        o.setProductNameSnapshot("Ocean Mist");
        o.setUnitPrice(new BigDecimal(price));
        o.setCurrency("VND");
        o.setStatus(status);
        o.setExpiresAt(expiresAt);
        return o;
    }
}
