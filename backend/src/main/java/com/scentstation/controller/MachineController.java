package com.scentstation.controller;

import com.scentstation.common.response.ApiResponse;
import com.scentstation.dto.request.CreateMachineRequest;
import com.scentstation.dto.request.UpdateMachineRequest;
import com.scentstation.dto.response.MachineResponse;
import com.scentstation.service.MachineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/machines")
public class MachineController {

    private final MachineService machineService;

    public MachineController(MachineService machineService) {
        this.machineService = machineService;
    }

    // 1. Lấy danh sách Machine (hỗ trợ lọc theo connectionStatus, operationalStatus, locationId)
    @GetMapping
    @PreAuthorize("hasAnyRole('PLATFORM_SUPER_ADMIN', 'BRAND_ADMIN', 'OPERATIONS_MANAGER', 'TECHNICIAN')")
    public ResponseEntity<List<MachineResponse>> getAllMachines(
            @RequestParam(required = false) String connectionStatus,
            @RequestParam(required = false) String operationalStatus,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) Long tenantId) {

        return ResponseEntity.ok(
                machineService.getAll(connectionStatus, operationalStatus, locationId, tenantId)
        );
    }

    // 2. Xem chi tiết Machine theo ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PLATFORM_SUPER_ADMIN', 'BRAND_ADMIN', 'OPERATIONS_MANAGER', 'TECHNICIAN')")
    public ResponseEntity<MachineResponse> getMachineById(@PathVariable Long id) {
        return ResponseEntity.ok(machineService.getById(id));
    }

    // 3. Đăng ký Machine mới
    @PostMapping
    @PreAuthorize("hasAnyRole('PLATFORM_SUPER_ADMIN', 'BRAND_ADMIN')")
    public ResponseEntity<MachineResponse> createMachine(
            @Valid @RequestBody CreateMachineRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(machineService.create(request));
    }

    // 4. Cập nhật thông tin Machine
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PLATFORM_SUPER_ADMIN', 'BRAND_ADMIN')")
    public ResponseEntity<MachineResponse> updateMachine(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMachineRequest request) {

        return ResponseEntity.ok(machineService.update(id, request));
    }

    // 5. Vô hiệu hóa Machine (Soft Delete -> operationalStatus = DISABLED)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PLATFORM_SUPER_ADMIN', 'BRAND_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteMachine(@PathVariable Long id) {
        machineService.delete(id);
        return ResponseEntity.ok(
                ApiResponse.success("Machine disabled successfully", null)
        );
    }

    // 6. Cập nhật Operational Status độc lập
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('PLATFORM_SUPER_ADMIN', 'BRAND_ADMIN', 'OPERATIONS_MANAGER', 'TECHNICIAN')")
    public ResponseEntity<MachineResponse> updateOperationalStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        return ResponseEntity.ok(
                machineService.updateOperationalStatus(id, status)
        );
    }

    // 7. Cập nhật Connection Status độc lập
    @PatchMapping("/{id}/connection-status")
    @PreAuthorize("hasAnyRole('PLATFORM_SUPER_ADMIN', 'TECHNICIAN')")
    public ResponseEntity<MachineResponse> updateConnectionStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        return ResponseEntity.ok(
                machineService.updateConnectionStatus(id, status)
        );
    }
}
