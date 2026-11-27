package com.scentstation.controller;

import com.scentstation.common.response.ApiResponse;
import com.scentstation.dto.response.OrderResponse;
import com.scentstation.service.OrderService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private static final String ORDER_READ_ROLES =
            "hasAnyRole('PLATFORM_SUPER_ADMIN', 'BRAND_ADMIN', 'OPERATIONS_MANAGER')";

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @PreAuthorize(ORDER_READ_ROLES)
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrders(
            @RequestParam(required = false) Long machineId,
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("Orders retrieved successfully", orderService.getOrders(machineId, status))
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize(ORDER_READ_ROLES)
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Order retrieved successfully", orderService.getOrder(id))
        );
    }
}
