package com.scentstation.service;

import com.scentstation.contract.DispenseResultEvent;
import com.scentstation.contract.PaymentResultEvent;
import com.scentstation.service.OrderService.DispenseDispatchPlan;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderEventHandler {

    private final OrderService orderService;

    public OrderEventHandler(OrderService orderService) {
        this.orderService = orderService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onPaymentResult(PaymentResultEvent event) {
        Optional<DispenseDispatchPlan> plan = orderService.processPaymentResult(event);
        plan.ifPresent(orderService::dispatchToIoT);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onDispenseResult(DispenseResultEvent event) {
        orderService.processDispenseResult(event);
    }
}
