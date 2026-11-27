package com.scentstation.contract;

import java.math.BigDecimal;

public record DispenseRequest(Long orderId, Long machineId, Long slotId, BigDecimal volumeMl) { }
