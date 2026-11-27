package com.scentstation.service;

import java.time.LocalDateTime;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderExpiryJob {

    private final OrderService orderService;

    public OrderExpiryJob(OrderService orderService) {
        this.orderService = orderService;
    }

    @Scheduled(fixedDelayString = "${app.order.expiry-check-ms:60000}")
    public void expireOrders() {
        orderService.expirePendingOrders(LocalDateTime.now());
    }
}
