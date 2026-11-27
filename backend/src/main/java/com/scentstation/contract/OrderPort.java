package com.scentstation.contract;

import java.util.Optional;

public interface OrderPort {

    Optional<OrderSnapshot> findOrderById(Long orderId);

    OrderSnapshot getOrderById(Long orderId);

    boolean existsById(Long orderId);
}
