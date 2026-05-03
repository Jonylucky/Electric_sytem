package com.index_electric_server.device_system_electric_server.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
            .success(true)
            .message("OK")
            .data(data)
            .timestamp(LocalDateTime.now())
            .build();
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return ApiResponse.<T>builder()
            .success(true)
            .message(message)
            .data(data)
            .timestamp(LocalDateTime.now())
            .build();
    }

    public static <T> ApiResponse<T> created(T data) {
        return ApiResponse.<T>builder()
            .success(true)
            .message("Created")
            .data(data)
            .timestamp(LocalDateTime.now())
            .build();
    }

    public static ApiResponse<Void> noContent() {
        return ApiResponse.<Void>builder()
            .success(true)
            .message("Deleted")
            .data(null)
            .timestamp(LocalDateTime.now())
            .build();
    }
}
