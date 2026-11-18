package com.scentstation.controller;

import com.scentstation.common.response.ApiResponse;
import com.scentstation.dto.request.CreateLocationRequest;
import com.scentstation.dto.request.UpdateLocationRequest;
import com.scentstation.dto.response.LocationResponse;
import com.scentstation.service.LocationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Pattern;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/locations")
@PreAuthorize("hasAnyRole('PLATFORM_SUPER_ADMIN', 'BRAND_ADMIN')")
public class LocationController {
    private final LocationService service;
    public LocationController(LocationService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<List<LocationResponse>> getAll(
            @RequestParam(required = false) @Pattern(regexp = "ACTIVE|INACTIVE", message = "status must be ACTIVE or INACTIVE") String status,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(service.getAll(status, name, tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<LocationResponse> create(@Valid @RequestBody CreateLocationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocationResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody UpdateLocationRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Location deactivated successfully", null));
    }
}
