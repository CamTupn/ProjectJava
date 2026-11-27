package com.scentstation.controller;

import com.scentstation.common.response.ApiResponse;
import com.scentstation.dto.request.CreateOrderRequest;
import com.scentstation.dto.response.CreateOrderResponse;
import com.scentstation.dto.response.KioskProductResponse;
import com.scentstation.service.KioskCatalogService;
import com.scentstation.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/kiosks")
public class KioskController {

    private final KioskCatalogService kioskCatalogService;
    private final OrderService orderService;

    public KioskController(
            KioskCatalogService kioskCatalogService,
            OrderService orderService
    ) {
        this.kioskCatalogService = kioskCatalogService;
        this.orderService = orderService;
    }

    @GetMapping("/{machineId}/products")
    public ResponseEntity<ApiResponse<List<KioskProductResponse>>> getAvailableProducts(
            @PathVariable Long machineId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Kiosk catalog retrieved successfully",
                        kioskCatalogService.getAvailableProducts(machineId)
                )
        );
    }

    @PostMapping("/orders")
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Order created successfully",
                        orderService.createOrder(request)
                ));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<CreateOrderResponse>> getKioskOrder(
            @PathVariable Long orderId,
            @RequestHeader(value = "X-Kiosk-Token", required = false) String kioskToken
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Order retrieved successfully",
                        orderService.getKioskOrder(orderId, kioskToken)
                )
        );
    }
}
