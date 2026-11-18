package com.scentstation.controller;

import com.scentstation.dto.request.CreateMachineSlotRequest;
import com.scentstation.dto.request.UpdateMachineSlotRequest;
import com.scentstation.dto.request.UpdateMachineSlotStatusRequest;
import com.scentstation.dto.response.MachineSlotListResponse;
import com.scentstation.dto.response.MachineSlotResponse;
import com.scentstation.service.MachineSlotService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class MachineSlotController {

    private static final String SLOT_READ_ROLES =
            "hasAnyRole('PLATFORM_SUPER_ADMIN', 'BRAND_ADMIN', "
                    + "'OPERATIONS_MANAGER', 'TECHNICIAN', 'INVENTORY_STAFF')";

    private static final String SLOT_CONFIG_ROLES =
            "hasAnyRole('PLATFORM_SUPER_ADMIN', 'BRAND_ADMIN', "
                    + "'OPERATIONS_MANAGER', 'INVENTORY_STAFF')";

    private final MachineSlotService machineSlotService;

    public MachineSlotController(MachineSlotService machineSlotService) {
        this.machineSlotService = machineSlotService;
    }

    @GetMapping("/machines/{machineId}/slots")
    @PreAuthorize(SLOT_READ_ROLES)
    public ResponseEntity<MachineSlotListResponse> getSlotsByMachine(
            @PathVariable Long machineId
    ) {
        return ResponseEntity.ok(
                machineSlotService.getSlotsByMachine(machineId)
        );
    }

    @GetMapping("/machine-slots/{id}")
    @PreAuthorize(SLOT_READ_ROLES)
    public ResponseEntity<MachineSlotResponse> getSlot(@PathVariable Long id) {
        return ResponseEntity.ok(machineSlotService.getSlot(id));
    }

    @PostMapping("/machines/{machineId}/slots")
    @PreAuthorize(SLOT_CONFIG_ROLES)
    public ResponseEntity<MachineSlotResponse> createSlot(
            @PathVariable Long machineId,
            @Valid @RequestBody CreateMachineSlotRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(machineSlotService.createSlot(machineId, request));
    }

    @PutMapping("/machine-slots/{id}")
    @PreAuthorize(SLOT_CONFIG_ROLES)
    public ResponseEntity<MachineSlotResponse> updateSlot(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMachineSlotRequest request
    ) {
        return ResponseEntity.ok(machineSlotService.updateSlot(id, request));
    }

    @PatchMapping("/machine-slots/{id}/status")
    @PreAuthorize("hasAnyRole('PLATFORM_SUPER_ADMIN', 'BRAND_ADMIN', "
            + "'OPERATIONS_MANAGER', 'TECHNICIAN')")
    public ResponseEntity<MachineSlotResponse> updateSlotStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMachineSlotStatusRequest request
    ) {
        return ResponseEntity.ok(
                machineSlotService.updateSlotStatus(id, request.getStatus())
        );
    }

    @DeleteMapping("/machine-slots/{id}")
    @PreAuthorize("hasAnyRole('PLATFORM_SUPER_ADMIN', 'BRAND_ADMIN', "
            + "'OPERATIONS_MANAGER')")
    public ResponseEntity<MachineSlotResponse> disableSlot(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(machineSlotService.disableSlot(id));
    }
}
