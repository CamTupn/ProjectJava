package com.scentstation.mapper;

import com.scentstation.contract.OrderSnapshot;
import com.scentstation.dto.response.CreateOrderResponse;
import com.scentstation.dto.response.OrderResponse;
import com.scentstation.entity.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getTenantId(),
                order.getMachineId(),
                order.getSlotId(),
                order.getProductId(),
                order.getProductNameSnapshot(),
                order.getUnitPrice(),
                order.getCurrency(),
                order.getStatus(),
                order.getExpiresAt(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    public CreateOrderResponse toKioskResponse(Order order, String accessToken) {
        return new CreateOrderResponse(
                order.getId(),
                order.getMachineId(),
                order.getSlotId(),
                order.getProductId(),
                order.getProductNameSnapshot(),
                order.getUnitPrice(),
                order.getCurrency(),
                order.getStatus(),
                order.getExpiresAt(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                accessToken
        );
    }

    public OrderSnapshot toSnapshot(Order order) {
        return new OrderSnapshot(
                order.getId(),
                order.getTenantId(),
                order.getMachineId(),
                order.getSlotId(),
                order.getProductId(),
                order.getProductNameSnapshot(),
                order.getUnitPrice(),
                order.getCurrency(),
                order.getStatus(),
                order.getExpiresAt(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
