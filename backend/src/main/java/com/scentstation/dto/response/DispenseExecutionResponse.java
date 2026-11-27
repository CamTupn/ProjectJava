package com.scentstation.dto.response;

import com.scentstation.entity.DispenseExecution;

import java.time.LocalDateTime;

public record DispenseExecutionResponse(Long id, Long commandId, String executionStatus,
                                        String errorCode, LocalDateTime recordedAt) {
    public static DispenseExecutionResponse from(DispenseExecution e) {
        return new DispenseExecutionResponse(e.getId(), e.getCommand().getId(),
                e.getExecutionStatus().name(), e.getErrorCode(), e.getRecordedAt());
    }
}
