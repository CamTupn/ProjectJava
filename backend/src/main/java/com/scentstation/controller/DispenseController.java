package com.scentstation.controller;

import com.scentstation.dto.response.DispenseCommandResponse;
import com.scentstation.dto.response.DispenseExecutionResponse;
import com.scentstation.entity.DispenseCommandStatus;
import com.scentstation.service.DispenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dispense/commands")
@PreAuthorize("hasAnyRole('PLATFORM_SUPER_ADMIN', 'BRAND_ADMIN', 'OPERATIONS_MANAGER', 'TECHNICIAN')")
public class DispenseController {
    private final DispenseService dispenseService;

    public DispenseController(DispenseService dispenseService) {
        this.dispenseService = dispenseService;
    }

    @GetMapping
    public ResponseEntity<List<DispenseCommandResponse>> list(
            @RequestParam(required = false) Long machineId,
            @RequestParam(required = false) DispenseCommandStatus status) {
        return ResponseEntity.ok(dispenseService.findCommands(machineId, status).stream()
                .map(DispenseCommandResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DispenseCommandResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(DispenseCommandResponse.from(dispenseService.getCommand(id)));
    }

    @GetMapping("/{id}/execution")
    public ResponseEntity<DispenseExecutionResponse> getExecution(@PathVariable Long id) {
        return ResponseEntity.ok(DispenseExecutionResponse.from(dispenseService.getExecution(id)));
    }
}
