package com.scentstation.mapper;

import com.scentstation.dto.response.InventoryResponse;
import com.scentstation.dto.response.InventoryTransactionResponse;
import com.scentstation.dto.response.RefillResponse;
import com.scentstation.entity.InventoryTransaction;
import com.scentstation.entity.MachineSlot;
import com.scentstation.entity.Refill;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {

    public InventoryResponse toInventoryResponse(MachineSlot slot) {
        return new InventoryResponse(
                slot.getId(),
                slot.getMachine().getId(),
                slot.getMachine().getMachineCode(),
                slot.getSlotNumber(),
                slot.getProduct() != null ? slot.getProduct().getId() : null,
                slot.getProduct() != null ? slot.getProduct().getName() : null,
                slot.getCapacityMl(),
                slot.getCurrentVolumeMl(),
                slot.getCapacityMl().subtract(slot.getCurrentVolumeMl()),
                slot.getLowStockThresholdMl(),
                slot.getStatus()
        );
    }

    public InventoryTransactionResponse toTransactionResponse(InventoryTransaction tx) {
        return new InventoryTransactionResponse(
                tx.getId(),
                tx.getSlot().getId(),
                tx.getOrderId(),
                tx.getExecutionId(),
                tx.getRefillId(),
                tx.getPerformedByUserId(),
                tx.getTransactionType(),
                tx.getVolumeChangeMl(),
                tx.getNotes(),
                tx.getCreatedAt()
        );
    }

    /** Refill read from history: no slot snapshot. */
    public RefillResponse toRefillResponse(Refill refill) {
        return toRefillResponse(refill, false);
    }

    /** Refill just created: includes the slot's volume and status after the refill. */
    public RefillResponse toCreatedRefillResponse(Refill refill) {
        return toRefillResponse(refill, true);
    }

    private RefillResponse toRefillResponse(Refill refill, boolean withSlotState) {
        MachineSlot slot = refill.getSlot();
        return new RefillResponse(
                refill.getId(),
                slot.getId(),
                slot.getMachine().getId(),
                refill.getProduct().getId(),
                refill.getProduct().getName(),
                refill.getPerformedByUserId(),
                refill.getVolumeAddedMl(),
                refill.getNotes(),
                refill.getRefilledAt(),
                refill.getCreatedAt(),
                withSlotState ? slot.getCurrentVolumeMl() : null,
                withSlotState ? slot.getStatus() : null
        );
    }
}
