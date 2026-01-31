package com.microservices.common.handler;

import com.microservices.common.dto.ErrorResponse;
import com.microservices.common.exception.BusinessException;
import com.microservices.common.exception.ResourceNotFoundException;
import com.microservices.common.exception.InsufficientStockException;
import com.microservices.common.exception.ServiceUnavailableException;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Base global exception handler for all microservices
 * Can be extended by individual services to add service-specific exception handling
 * 
 * Usage:
 * @RestControllerAdvice
 * public class ServiceExceptionHandler extends BaseExceptionHandler {
 *     // Add service-specific exception handlers
 * }
 */
@RestControllerAdvice
public class BaseExceptionHandler {

    /**
     * Handle validation errors from @Valid annotations
     * Returns field-level error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    String fieldName = ((FieldError) error).getField();
                    Object rejectedValue = ((FieldError) error).getRejectedValue();
                    String message = error.getDefaultMessage();
                    return ErrorResponse.FieldError.builder()
                            .field(fieldName)
                            .rejectedValue(rejectedValue)
                            .message(message)
                            .build();
                })
                .collect(Collectors.toList());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode("VALIDATION_ERROR")
                .message("Validation failed for one or more fields")
                .path(request.getRequestURI())
                .requestId(generateRequestId())
                .fieldErrors(fieldErrors)
                .build();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle resource not found errors
     * Returns 404 NOT FOUND
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getErrorCode(),
                ex.getMessage(),
                null,
                request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle insufficient stock errors
     * Returns 409 CONFLICT
     */
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStockException(
            InsufficientStockException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.CONFLICT,
                ex.getErrorCode(),
                ex.getMessage(),
                null,
                request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handle service unavailable errors
     * Returns 503 SERVICE UNAVAILABLE
     */
    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleServiceUnavailableException(
            ServiceUnavailableException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                ex.getErrorCode(),
                ex.getMessage(),
                null,
                request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Handle Feign client errors (inter-service communication failures)
     * Returns 503 SERVICE UNAVAILABLE
     */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(
            FeignException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "EXTERNAL_SERVICE_ERROR",
                "External service communication failed",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Handle all business exceptions
     * Returns 400 BAD REQUEST by default
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getErrorCode(),
                ex.getMessage(),
                null,
                request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle all uncaught exceptions
     * Returns 500 INTERNAL SERVER ERROR
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        
        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Helper method to build error response
     */
    protected ErrorResponse buildErrorResponse(
            HttpStatus status,
            String errorCode,
            String message,
            String details,
            String path) {
        
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .errorCode(errorCode)
                .message(message)
                .details(details)
                .path(path)
                .requestId(generateRequestId())
                .build();
    }

    /**
     * Generate unique request ID for tracing
     */
    protected String generateRequestId() {
        return UUID.randomUUID().toString();
    }
}
