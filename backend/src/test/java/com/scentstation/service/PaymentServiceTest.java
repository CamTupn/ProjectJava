package com.scentstation.service;

import com.scentstation.common.security.CurrentUserService;
import com.scentstation.contract.PaymentNotice;
import com.scentstation.entity.Order;
import com.scentstation.entity.Payment;
import com.scentstation.entity.PaymentStatus;
import com.scentstation.exception.ResourceConflictException;
import com.scentstation.mapper.PaymentMapper;
import com.scentstation.repository.OrderRepository;
import com.scentstation.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock private PaymentRepository paymentRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private CurrentUserService currentUserService;
    @Mock private ApplicationEventPublisher eventPublisher;

    private PaymentService service;
    private Order order;

    @BeforeEach
    void setUp() {
        service = new PaymentService(paymentRepository, orderRepository, new PaymentMapper(),
                currentUserService, eventPublisher);
        order = new Order();
        order.setId(10L);
        order.setTenantId(20L);
        order.setUnitPrice(new BigDecimal("15000.00"));
        order.setCurrency("VND");
        order.setStatus("PENDING_PAYMENT");
        lenient().when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
    }

    @Test
    void createsPendingPaymentUsingAmountFromOrder() {
        when(paymentRepository.findFirstByOrder_IdAndStatusOrderByCreatedAtDesc(10L, PaymentStatus.PENDING))
                .thenReturn(Optional.empty());
        when(paymentRepository.saveAndFlush(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.createPayment(10L);

        assertEquals(new BigDecimal("15000.00"), result.amount());
        assertEquals("VND", result.currency());
        assertEquals(PaymentStatus.PENDING, result.status());
        verify(paymentRepository).saveAndFlush(any(Payment.class));
    }

    @Test
    void returnsExistingPendingPayment() {
        Payment existing = payment(PaymentStatus.PENDING);
        when(paymentRepository.findFirstByOrder_IdAndStatusOrderByCreatedAtDesc(10L, PaymentStatus.PENDING))
                .thenReturn(Optional.of(existing));

        var result = service.createPayment(10L);

        assertEquals(existing.getPaymentRef(), result.paymentRef());
        verify(paymentRepository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsAlreadyPaidOrder() {
        order.setStatus("PAID");

        assertThrows(ResourceConflictException.class, () -> service.createPayment(10L));
        verify(paymentRepository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsExpiredOrder() {
        order.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        assertThrows(ResourceConflictException.class, () -> service.createPayment(10L));
        verify(paymentRepository, never()).saveAndFlush(any());
    }

    @Test
    void processesSuccessCallbackAndPublishesNotice() {
        Payment payment = payment(PaymentStatus.PENDING);
        when(paymentRepository.findByPaymentRef("PAY-10")).thenReturn(Optional.of(payment));
        when(paymentRepository.saveAndFlush(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.processCallback(new PaymentNotice("PAY-10", 10L,
                new BigDecimal("15000.00"), "VND", PaymentStatus.SUCCESS,
                "GW-10", LocalDateTime.now(), "mock", false));

        assertEquals(PaymentStatus.SUCCESS, result.status());
        verify(eventPublisher).publishEvent(any(PaymentNotice.class));
    }

    @Test
    void rejectsCallbackWithWrongAmountOrCurrency() {
        Payment payment = payment(PaymentStatus.PENDING);
        when(paymentRepository.findByPaymentRef("PAY-10")).thenReturn(Optional.of(payment));

        assertThrows(ResourceConflictException.class, () -> service.processCallback(new PaymentNotice(
                "PAY-10", 10L, new BigDecimal("1.00"), "VND", PaymentStatus.SUCCESS,
                "GW-10", null, "mock", false)));
        assertThrows(ResourceConflictException.class, () -> service.processCallback(new PaymentNotice(
                "PAY-10", 10L, new BigDecimal("15000.00"), "USD", PaymentStatus.SUCCESS,
                "GW-10", null, "mock", false)));
        verify(paymentRepository, never()).saveAndFlush(any());
    }

    @Test
    void duplicateCallbackDoesNotOverwriteSuccess() {
        Payment payment = payment(PaymentStatus.SUCCESS);
        payment.setGatewayTransactionId("GW-10");
        when(paymentRepository.findByPaymentRef("PAY-10")).thenReturn(Optional.of(payment));
        when(paymentRepository.findByGatewayTransactionId("GW-10")).thenReturn(Optional.of(payment));

        var result = service.processCallback(new PaymentNotice("PAY-10", 10L,
                new BigDecimal("15000.00"), "VND", PaymentStatus.FAILED,
                "GW-10", null, "mock", false));

        assertEquals(PaymentStatus.SUCCESS, result.status());
        verify(paymentRepository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsGatewayTransactionUsedByAnotherPayment() {
        Payment payment = payment(PaymentStatus.PENDING);
        Payment other = payment(PaymentStatus.SUCCESS);
        other.setId(99L);
        when(paymentRepository.findByPaymentRef("PAY-10")).thenReturn(Optional.of(payment));
        when(paymentRepository.findByGatewayTransactionId("GW-OTHER")).thenReturn(Optional.of(other));

        assertThrows(ResourceConflictException.class, () -> service.processCallback(new PaymentNotice(
                "PAY-10", 10L, new BigDecimal("15000.00"), "VND", PaymentStatus.SUCCESS,
                "GW-OTHER", null, "mock", false)));
    }

    private Payment payment(PaymentStatus status) {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setOrder(order);
        payment.setPaymentRef("PAY-10");
        payment.setProvider("MOCK");
        payment.setAmount(order.getUnitPrice());
        payment.setCurrency(order.getCurrency());
        payment.setStatus(status);
        return payment;
    }
}
