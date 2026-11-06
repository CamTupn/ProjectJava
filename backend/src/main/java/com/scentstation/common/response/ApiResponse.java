package com.scentstation.common.response;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        Map<String, String> errors,
        LocalDateTime timestamp
) {

    // Successful response
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(
                true,
                message,
                data,
                Map.of(),
                LocalDateTime.now()
        );
    }

    // Error response
    public static <T> ApiResponse<T> error(
            String message,
            Map<String, String> errors
    ) {
        return new ApiResponse<>(
                false,
                message,
                null,
                errors == null ? Map.of() : errors,
                LocalDateTime.now()
        );
    }
}
