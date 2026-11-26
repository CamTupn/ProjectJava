package com.scentstation.mapper;

import com.scentstation.dto.response.MachineSlotResponse;
import com.scentstation.entity.MachineSlot;
import org.springframework.stereotype.Component;

@Component
public class MachineSlotMapper {

    public MachineSlotResponse toResponse(MachineSlot slot) {
        return new MachineSlotResponse(
                slot.getId(),
                slot.getMachine().getId(),
                slot.getSlotNumber(),
                slot.getProduct() != null ? slot.getProduct().getId() : null,
                slot.getProduct() != null ? slot.getProduct().getName() : null,
                slot.getCapacityMl(),
                slot.getCurrentVolumeMl(),
                slot.getLowStockThresholdMl(),
                slot.getStatus(),
                slot.getCreatedAt(),
                slot.getUpdatedAt()
        );
    }
}
