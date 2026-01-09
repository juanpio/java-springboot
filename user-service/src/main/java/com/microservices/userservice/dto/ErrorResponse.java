package com.microservices.userservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Standardized error response for all error scenarios
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard error response")
public class ErrorResponse {
    
    @Schema(description = "Timestamp when the error occurred")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    @Schema(description = "HTTP status code", example = "404")
    private int status;
    
    @Schema(description = "Short error code for client-side handling", example = "RESOURCE_NOT_FOUND")
    private String errorCode;
    
    @Schema(description = "Human-readable error message")
    private String message;
    
    @Schema(description = "Additional details about the error")
    private String details;
    
    @Schema(description = "Request path that caused the error", example = "/api/v1/users/123")
    private String path;
    
    @Schema(description = "Unique request ID for tracing")
    private String requestId;
    
    @Schema(description = "Field-level validation errors")
    private List<FieldError> fieldErrors;
    
    @Schema(description = "Additional error metadata")
    private Map<String, Object> metadata;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Field validation error")
    public static class FieldError {
        @Schema(description = "Name of the field that failed validation", example = "email")
        private String field;
        
        @Schema(description = "Rejected value")
        private Object rejectedValue;
        
        @Schema(description = "Validation error message", example = "must be a valid email address")
        private String message;
    }
}
