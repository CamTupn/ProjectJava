package com.scentstation.controller;

import com.scentstation.dto.response.InventoryResponse;
import com.scentstation.dto.response.InventoryTransactionResponse;
import com.scentstation.service.InventoryService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    static final String INVENTORY_READ_ROLES =
            "hasAnyRole('PLATFORM_SUPER_ADMIN', 'BRAND_ADMIN', "
                    + "'OPERATIONS_MANAGER', 'TECHNICIAN', 'INVENTORY_STAFF')";

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    @PreAuthorize(INVENTORY_READ_ROLES)
    public ResponseEntity<List<InventoryResponse>> getInventory(
            @RequestParam(required = false) Long machineId
    ) {
        return ResponseEntity.ok(inventoryService.getInventory(machineId));
    }

    @GetMapping("/slots/{slotId}")
    @PreAuthorize(INVENTORY_READ_ROLES)
    public ResponseEntity<InventoryResponse> getSlotInventory(
            @PathVariable Long slotId
    ) {
        return ResponseEntity.ok(inventoryService.getSlotInventory(slotId));
    }

    @GetMapping("/slots/{slotId}/transactions")
    @PreAuthorize(INVENTORY_READ_ROLES)
    public ResponseEntity<List<InventoryTransactionResponse>> getTransactions(
            @PathVariable Long slotId,
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(inventoryService.getTransactions(slotId, limit));
    }
}
