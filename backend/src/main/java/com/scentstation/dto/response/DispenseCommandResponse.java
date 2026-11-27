package com.scentstation.dto.response;

import com.scentstation.entity.DispenseCommand;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DispenseCommandResponse(Long id, String commandToken, Long orderId,
                                      Long machineId, Long slotId, String commandType,
                                      BigDecimal volumeMl, String status,
                                      LocalDateTime createdAt, LocalDateTime sentAt,
                                      LocalDateTime completedAt) {
    public static DispenseCommandResponse from(DispenseCommand c) {
        return new DispenseCommandResponse(c.getId(), c.getCommandToken(),
                c.getOrder() == null ? null : c.getOrder().getId(), c.getMachine().getId(),
                c.getSlot().getId(), c.getCommandType().name(), c.getVolumeMl(),
                c.getStatus().name(), c.getCreatedAt(), c.getSentAt(), c.getCompletedAt());
    }
}
