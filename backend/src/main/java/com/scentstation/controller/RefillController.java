package com.scentstation.controller;

import com.scentstation.dto.request.CreateRefillRequest;
import com.scentstation.dto.response.RefillResponse;
import com.scentstation.service.RefillService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory/refills")
public class RefillController {

    private static final String REFILL_ROLES =
            "hasAnyRole('PLATFORM_SUPER_ADMIN', 'BRAND_ADMIN', 'INVENTORY_STAFF')";

    private final RefillService refillService;

    public RefillController(RefillService refillService) {
        this.refillService = refillService;
    }

    @PostMapping
    @PreAuthorize(REFILL_ROLES)
    public ResponseEntity<RefillResponse> createRefill(
            @Valid @RequestBody CreateRefillRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(refillService.createRefill(request));
    }

    @GetMapping
    @PreAuthorize(InventoryController.INVENTORY_READ_ROLES)
    public ResponseEntity<List<RefillResponse>> getRefills(
            @RequestParam(required = false) Long slotId,
            @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(refillService.getRefillsBySlot(slotId, limit));
    }

    @GetMapping("/{id}")
    @PreAuthorize(InventoryController.INVENTORY_READ_ROLES)
    public ResponseEntity<RefillResponse> getRefill(@PathVariable Long id) {
        return ResponseEntity.ok(refillService.getRefill(id));
    }
}
