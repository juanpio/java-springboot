package com.microservices.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standardized API response wrapper for all endpoints
 * Provides consistent response structure across all services
 * 
 * @param <T> The type of data being returned
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API response wrapper")
public class ApiResponse<T> {
    
    @Schema(description = "Indicates if the request was successful", example = "true")
    private boolean success;
    
    @Schema(description = "Human-readable message about the response")
    private String message;
    
    @Schema(description = "The actual data payload")
    private T data;
    
    @Schema(description = "Timestamp when the response was generated")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    @Schema(description = "Additional metadata about the response")
    private Map<String, Object> metadata;
    
    /**
     * Factory method for successful responses with data and message
     * @param data The response data
     * @param message Success message
     * @return ApiResponse with success=true
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * Factory method for successful responses with data and default message
     * @param data The response data
     * @return ApiResponse with success=true
     */
    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Operation successful");
    }
    
    /**
     * Factory method for successful responses without data
     * @param message Success message
     * @return ApiResponse with success=true
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * Factory method for error responses
     * @param message Error message
     * @return ApiResponse with success=false
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * Factory method for error responses with metadata
     * @param message Error message
     * @param metadata Additional error metadata
     * @return ApiResponse with success=false
     */
    public static <T> ApiResponse<T> error(String message, Map<String, Object> metadata) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .metadata(metadata)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
