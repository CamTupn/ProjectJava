package com.scentstation.service;

import com.scentstation.dto.response.KioskProductResponse;
import com.scentstation.entity.Machine;
import com.scentstation.entity.MachineSlot;
import com.scentstation.entity.MachineSlotStatus;
import com.scentstation.entity.Order;
import com.scentstation.entity.Product;
import com.scentstation.exception.ResourceConflictException;
import com.scentstation.exception.ResourceNotFoundException;
import com.scentstation.repository.MachineRepository;
import com.scentstation.repository.MachineSlotRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class KioskCatalogService {

    private final MachineRepository machineRepository;
    private final MachineSlotRepository machineSlotRepository;

    public KioskCatalogService(
            MachineRepository machineRepository,
            MachineSlotRepository machineSlotRepository
    ) {
        this.machineRepository = machineRepository;
        this.machineSlotRepository = machineSlotRepository;
    }

    public List<KioskProductResponse> getAvailableProducts(Long machineId) {
        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new ResourceNotFoundException("Machine not found with id: " + machineId));

        if (!"ONLINE".equalsIgnoreCase(machine.getConnectionStatus())) {
            throw new ResourceConflictException("Machine is offline");
        }
        if (!"ACTIVE".equalsIgnoreCase(machine.getOperationalStatus())) {
            throw new ResourceConflictException("Machine is not operational");
        }

        List<MachineSlot> slots = machineSlotRepository.findAllByMachine_IdOrderBySlotNumber(machineId);

        return slots.stream()
                .filter(slot -> isSlotOrderable(slot, machine.getTenantId()))
                .map(this::toKioskProductResponse)
                .toList();
    }

    private boolean isSlotOrderable(MachineSlot slot, Long machineTenantId) {
        if (slot.getStatus() != MachineSlotStatus.AVAILABLE
                && slot.getStatus() != MachineSlotStatus.LOW_STOCK) {
            return false;
        }
        Product product = slot.getProduct();
        if (product == null) {
            return false;
        }
        if (!Objects.equals(product.getTenantId(), machineTenantId)) {
            return false;
        }
        if (!"ACTIVE".equalsIgnoreCase(product.getStatus())) {
            return false;
        }
        BigDecimal volumePerSpray = product.getVolumePerSprayMl();
        BigDecimal currentVolume = slot.getCurrentVolumeMl();
        return volumePerSpray != null
                && currentVolume != null
                && volumePerSpray.compareTo(BigDecimal.ZERO) > 0
                && currentVolume.compareTo(volumePerSpray) >= 0;
    }

    private KioskProductResponse toKioskProductResponse(MachineSlot slot) {
        Product product = slot.getProduct();
        return new KioskProductResponse(
                slot.getId(),
                slot.getSlotNumber(),
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getFragranceNotes(),
                product.getImageUrl(),
                product.getPrice(),
                Order.DEFAULT_CURRENCY,
                product.getVolumePerSprayMl(),
                true
        );
    }
}
