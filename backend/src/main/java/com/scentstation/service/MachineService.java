package com.scentstation.service;

import com.scentstation.common.security.CurrentUserPrincipal;
import com.scentstation.common.security.CurrentUserService;
import com.scentstation.dto.request.CreateMachineRequest;
import com.scentstation.dto.request.UpdateMachineRequest;
import com.scentstation.dto.response.MachineResponse;
import com.scentstation.entity.Location;
import com.scentstation.entity.Machine;
import com.scentstation.entity.Tenant;
import com.scentstation.exception.ResourceConflictException;
import com.scentstation.exception.ResourceNotFoundException;
import com.scentstation.mapper.MachineMapper;
import com.scentstation.repository.LocationRepository;
import com.scentstation.repository.MachineRepository;
import com.scentstation.repository.TenantRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class MachineService {

    private static final Set<String> VALID_CONNECTION_STATUSES = Set.of("ONLINE", "OFFLINE");
    private static final Set<String> VALID_OPERATIONAL_STATUSES = Set.of("ACTIVE", "MAINTENANCE", "DISABLED", "ERROR");

    private final MachineRepository machineRepository;
    private final LocationRepository locationRepository;
    private final TenantRepository tenantRepository;
    private final MachineMapper machineMapper;
    private final CurrentUserService currentUserService;

    public MachineService(
            MachineRepository machineRepository,
            LocationRepository locationRepository,
            TenantRepository tenantRepository,
            MachineMapper machineMapper,
            CurrentUserService currentUserService) {
        this.machineRepository = machineRepository;
        this.locationRepository = locationRepository;
        this.tenantRepository = tenantRepository;
        this.machineMapper = machineMapper;
        this.currentUserService = currentUserService;
    }

    // 1. Lấy danh sách Machine (có lọc theo connectionStatus, operationalStatus, locationId)
    @Transactional(readOnly = true)
    public List<MachineResponse> getAll(
            String connectionStatus,
            String operationalStatus,
            Long locationId,
            Long requestedTenantId) {

        CurrentUserPrincipal current = currentUserService.getCurrentUser();
        requireReadRole(current);

        Long tenantId = resolveReadTenant(current, requestedTenantId);
        String normalizedConnStatus = normalizeAndValidateConnectionStatus(connectionStatus, false);
        String normalizedOpStatus = normalizeAndValidateOperationalStatus(operationalStatus, false);

        return machineRepository.search(tenantId, normalizedConnStatus, normalizedOpStatus, locationId)
                .stream()
                .map(machineMapper::toResponse)
                .toList();
    }

    // 2. Xem chi tiết Machine theo ID
    @Transactional(readOnly = true)
    public MachineResponse getById(Long id) {
        CurrentUserPrincipal current = currentUserService.getCurrentUser();
        requireReadRole(current);

        Machine machine = findAccessibleMachine(id, current);
        return machineMapper.toResponse(machine);
    }

    // 3. Đăng ký Machine mới
    public MachineResponse create(CreateMachineRequest request) {
        CurrentUserPrincipal current = currentUserService.getCurrentUser();
        requireAdminManageRole(current);

        Long tenantId = resolveCreateTenant(current, request.tenantId());
        ensureActiveTenant(tenantId);

        String machineCode = request.machineCode().trim();
        String serialNumber = request.serialNumber().trim();
        String deviceIdentity = request.deviceIdentity().trim();

        if (machineRepository.existsByMachineCode(machineCode)) {
            throw new ResourceConflictException("Machine code already exists: " + machineCode);
        }
        if (machineRepository.existsBySerialNumber(serialNumber)) {
            throw new ResourceConflictException("Serial number already exists: " + serialNumber);
        }
        if (machineRepository.existsByDeviceIdentity(deviceIdentity)) {
            throw new ResourceConflictException("Device identity already exists: " + deviceIdentity);
        }

        Location location = resolveValidLocation(request.locationId(), tenantId, current);

        // Mặc định OFFLINE nếu chưa có xác nhận kết nối thực tế
        String connectionStatus = normalizeAndValidateConnectionStatus(request.connectionStatus(), false);
        if (connectionStatus == null) {
            connectionStatus = "OFFLINE";
        }
        if ("ONLINE".equals(connectionStatus) && !current.isPlatformSuperAdmin()) {
            throw new AccessDeniedException("Regular users cannot register a machine directly as ONLINE");
        }

        // Mặc định ACTIVE nếu không chỉ định
        String operationalStatus = normalizeAndValidateOperationalStatus(request.operationalStatus(), false);
        if (operationalStatus == null) {
            operationalStatus = "ACTIVE";
        }

        Machine machine = new Machine();
        machine.setTenantId(tenantId);
        machine.setLocation(location);
        machine.setMachineCode(machineCode);
        machine.setSerialNumber(serialNumber);
        machine.setDeviceIdentity(deviceIdentity);
        machine.setFirmwareVersion(blankToNull(request.firmwareVersion()));
        machine.setConnectionStatus(connectionStatus);
        machine.setOperationalStatus(operationalStatus);
        if ("ONLINE".equals(connectionStatus)) {
            machine.setLastPingAt(LocalDateTime.now());
        }

        try {
            Machine saved = machineRepository.save(machine);
            return machineMapper.toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new ResourceConflictException("Machine code, serial number, or device identity already exists");
        }
    }

    // 4. Cập nhật thông tin Machine (machineCode, locationId, firmwareVersion...)
    public MachineResponse update(Long id, UpdateMachineRequest request) {
        CurrentUserPrincipal current = currentUserService.getCurrentUser();
        requireAdminManageRole(current);

        Machine machine = findAccessibleMachine(id, current);

        if (request.machineCode() != null) {
            String newCode = request.machineCode().trim();
            if (newCode.isEmpty()) {
                throw new IllegalArgumentException("Machine code must not be blank");
            }
            if (machineRepository.existsByMachineCodeAndIdNot(newCode, id)) {
                throw new ResourceConflictException("Machine code already exists: " + newCode);
            }
            machine.setMachineCode(newCode);
        }

        if (request.serialNumber() != null) {
            String newSerial = request.serialNumber().trim();
            if (newSerial.isEmpty()) {
                throw new IllegalArgumentException("Serial number must not be blank");
            }
            if (machineRepository.existsBySerialNumberAndIdNot(newSerial, id)) {
                throw new ResourceConflictException("Serial number already exists: " + newSerial);
            }
            machine.setSerialNumber(newSerial);
        }

        if (request.deviceIdentity() != null) {
            String newIdentity = request.deviceIdentity().trim();
            if (newIdentity.isEmpty()) {
                throw new IllegalArgumentException("Device identity must not be blank");
            }
            if (machineRepository.existsByDeviceIdentityAndIdNot(newIdentity, id)) {
                throw new ResourceConflictException("Device identity already exists: " + newIdentity);
            }
            machine.setDeviceIdentity(newIdentity);
        }

        if (request.locationId() != null) {
            Location location = resolveValidLocation(request.locationId(), machine.getTenantId(), current);
            machine.setLocation(location);
        }

        if (request.firmwareVersion() != null) {
            machine.setFirmwareVersion(blankToNull(request.firmwareVersion()));
        }

        try {
            Machine saved = machineRepository.save(machine);
            return machineMapper.toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new ResourceConflictException("Machine code, serial number, or device identity already exists");
        }
    }

    // 5. Vô hiệu hóa Machine (Soft Delete -> operationalStatus = DISABLED)
    public void delete(Long id) {
        CurrentUserPrincipal current = currentUserService.getCurrentUser();
        requireAdminManageRole(current);

        Machine machine = findAccessibleMachine(id, current);
        machine.setOperationalStatus("DISABLED");
        machineRepository.save(machine);
    }

    // 6. Cập nhật Operational Status độc lập (ACTIVE, MAINTENANCE, DISABLED, ERROR)
    public MachineResponse updateOperationalStatus(Long id, String status) {
        CurrentUserPrincipal current = currentUserService.getCurrentUser();
        requireOperationalStatusRole(current);

        String normalizedStatus = normalizeAndValidateOperationalStatus(status, true);
        Machine machine = findAccessibleMachine(id, current);

        machine.setOperationalStatus(normalizedStatus);
        Machine saved = machineRepository.save(machine);
        return machineMapper.toResponse(saved);
    }

    // 7. Cập nhật Connection Status độc lập (ONLINE, OFFLINE - chỉ dành cho luồng kỹ thuật / quản trị hệ thống)
    public MachineResponse updateConnectionStatus(Long id, String status) {
        CurrentUserPrincipal current = currentUserService.getCurrentUser();
        requireConnectionStatusRole(current);

        String normalizedStatus = normalizeAndValidateConnectionStatus(status, true);
        Machine machine = findAccessibleMachine(id, current);

        machine.setConnectionStatus(normalizedStatus);
        if ("ONLINE".equals(normalizedStatus)) {
            machine.setLastPingAt(LocalDateTime.now());
        }
        Machine saved = machineRepository.save(machine);
        return machineMapper.toResponse(saved);
    }

    // --- Helper methods for RBAC & Tenant Isolation ---

    private Machine findAccessibleMachine(Long id, CurrentUserPrincipal current) {
        if (current.isPlatformSuperAdmin()) {
            return machineRepository.findWithLocationById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Machine not found with ID: " + id));
        }
        if (current.tenantId() == null) {
            throw new ResourceNotFoundException("Machine not found with ID: " + id);
        }
        return machineRepository.findWithLocationByIdAndTenantId(id, current.tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Machine not found with ID: " + id));
    }

    private Long resolveReadTenant(CurrentUserPrincipal current, Long requestedTenantId) {
        if (current.isPlatformSuperAdmin()) {
            if (requestedTenantId != null) {
                ensureActiveTenant(requestedTenantId);
            }
            return requestedTenantId;
        }
        if (current.tenantId() == null) {
            throw new AccessDeniedException("Current user does not belong to a tenant");
        }
        if (requestedTenantId != null && !requestedTenantId.equals(current.tenantId())) {
            throw new ResourceNotFoundException("Machine not found");
        }
        return current.tenantId();
    }

    private Long resolveCreateTenant(CurrentUserPrincipal current, Long requestedTenantId) {
        if (current.isPlatformSuperAdmin()) {
            if (requestedTenantId == null) {
                throw new IllegalArgumentException("tenantId is required for PLATFORM_SUPER_ADMIN");
            }
            return requestedTenantId;
        }
        if (current.tenantId() == null) {
            throw new AccessDeniedException("Authenticated user has no tenant");
        }
        if (requestedTenantId != null && !requestedTenantId.equals(current.tenantId())) {
            throw new AccessDeniedException("Cannot create machine for another tenant");
        }
        return current.tenantId();
    }

    private Location resolveValidLocation(Long locationId, Long tenantId, CurrentUserPrincipal current) {
        if (locationId == null) {
            return null;
        }
        Location location;
        if (current.isPlatformSuperAdmin()) {
            location = locationRepository.findById(locationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Location not found with ID: " + locationId));
            if (!location.getTenantId().equals(tenantId)) {
                throw new IllegalArgumentException("Location does not belong to the machine's tenant");
            }
        } else {
            location = locationRepository.findByIdAndTenantId(locationId, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Location not found with ID: " + locationId));
        }

        if (!"ACTIVE".equals(location.getStatus())) {
            throw new ResourceConflictException("Cannot assign machine to an inactive location");
        }
        return location;
    }

    private void ensureActiveTenant(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with ID: " + tenantId));
        if (!"ACTIVE".equals(tenant.getStatus())) {
            throw new ResourceConflictException("Cannot manage machines for an inactive tenant");
        }
    }

    private void requireReadRole(CurrentUserPrincipal current) {
        if (!current.isPlatformSuperAdmin()
                && !current.hasRole("BRAND_ADMIN")
                && !current.hasRole("OPERATIONS_MANAGER")
                && !current.hasRole("TECHNICIAN")) {
            throw new AccessDeniedException("Role is not permitted to view machines");
        }
    }

    private void requireAdminManageRole(CurrentUserPrincipal current) {
        if (!current.isPlatformSuperAdmin() && !current.hasRole("BRAND_ADMIN")) {
            throw new AccessDeniedException("Role is not permitted to create, update, or disable machines");
        }
    }

    private void requireOperationalStatusRole(CurrentUserPrincipal current) {
        if (!current.isPlatformSuperAdmin()
                && !current.hasRole("BRAND_ADMIN")
                && !current.hasRole("OPERATIONS_MANAGER")
                && !current.hasRole("TECHNICIAN")) {
            throw new AccessDeniedException("Role is not permitted to update machine operational status");
        }
    }

    private void requireConnectionStatusRole(CurrentUserPrincipal current) {
        if (!current.isPlatformSuperAdmin() && !current.hasRole("TECHNICIAN")) {
            throw new AccessDeniedException("Role is not permitted to manually update machine connection status");
        }
    }

    private String normalizeAndValidateConnectionStatus(String status, boolean required) {
        if (status == null || status.isBlank()) {
            if (required) {
                throw new IllegalArgumentException("Connection status is required. Allowed: ONLINE, OFFLINE");
            }
            return null;
        }
        String normalized = status.trim().toUpperCase();
        if (!VALID_CONNECTION_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("Invalid connection status. Allowed: ONLINE, OFFLINE");
        }
        return normalized;
    }

    private String normalizeAndValidateOperationalStatus(String status, boolean required) {
        if (status == null || status.isBlank()) {
            if (required) {
                throw new IllegalArgumentException("Operational status is required. Allowed: ACTIVE, MAINTENANCE, DISABLED, ERROR");
            }
            return null;
        }
        String normalized = status.trim().toUpperCase();
        if (!VALID_OPERATIONAL_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("Invalid operational status. Allowed: ACTIVE, MAINTENANCE, DISABLED, ERROR");
        }
        return normalized;
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
