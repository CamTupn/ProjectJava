package com.scentstation.contract;

import com.scentstation.entity.DispenseExecutionStatus;

public record DispenseNotice(String commandToken, Long machineId,
                             DispenseExecutionStatus status, String errorCode,
                             String telemetry) { }
