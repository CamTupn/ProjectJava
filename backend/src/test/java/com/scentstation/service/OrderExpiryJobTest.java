package com.scentstation.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderExpiryJobTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderExpiryJob orderExpiryJob;

    @Test
    void expireOrders_delegatesToOrderService() {
        orderExpiryJob.expireOrders();

        verify(orderService).expirePendingOrders(any(LocalDateTime.class));
    }
}
