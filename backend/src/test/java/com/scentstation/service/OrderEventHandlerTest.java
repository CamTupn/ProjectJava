package com.scentstation.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.scentstation.contract.DispenseResultEvent;
import com.scentstation.contract.OrderSnapshot;
import com.scentstation.contract.PaymentResultEvent;
import com.scentstation.service.OrderService.DispenseDispatchPlan;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderEventHandlerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderEventHandler orderEventHandler;

    @Test
    void onPaymentResult_dispatchesWhenPlanReturned() {
        PaymentResultEvent event = new PaymentResultEvent(1L, "PAY-1", "SUCCESS", new BigDecimal("15000.00"), "VND");
        OrderSnapshot snapshot = new OrderSnapshot(
                1L, 10L, 20L, 30L, 40L, "Ocean Mist",
                new BigDecimal("15000.00"), "VND", "DISPENSING",
                LocalDateTime.now().plusMinutes(10), LocalDateTime.now(), LocalDateTime.now()
        );
        DispenseDispatchPlan plan = new DispenseDispatchPlan(snapshot, new BigDecimal("2.00"));
        when(orderService.processPaymentResult(event)).thenReturn(Optional.of(plan));

        orderEventHandler.onPaymentResult(event);

        verify(orderService).processPaymentResult(event);
        verify(orderService).dispatchToIoT(plan);
    }

    @Test
    void onPaymentResult_doesNotDispatchWhenNoPlanReturned() {
        PaymentResultEvent event = new PaymentResultEvent(1L, "PAY-1", "SUCCESS", new BigDecimal("15000.00"), "VND");
        when(orderService.processPaymentResult(event)).thenReturn(Optional.empty());

        orderEventHandler.onPaymentResult(event);

        verify(orderService).processPaymentResult(event);
        verifyNoMoreInteractions(orderService);
    }

    @Test
    void onDispenseResult_delegatesToOrderService() {
        DispenseResultEvent event = new DispenseResultEvent(1L, 50L, "SUCCESS", null);

        orderEventHandler.onDispenseResult(event);

        verify(orderService).processDispenseResult(event);
    }
}
