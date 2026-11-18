package com.scentstation.dto.response;

import java.util.List;

public record MachineSlotListResponse(
        Long machineId,
        int totalSlots,
        List<MachineSlotResponse> slots
) {}
