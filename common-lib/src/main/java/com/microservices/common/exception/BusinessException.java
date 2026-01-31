package com.microservices.common.exception;

/**
 * Base exception class for all business/domain exceptions
 * Provides a consistent error code mechanism across all services
 */
public abstract class BusinessException extends RuntimeException {
    
    private final String errorCode;
    
    /**
     * Create a business exception with message and error code
     * @param message Human-readable error message
     * @param errorCode Machine-readable error code for client-side handling
     */
    protected BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * Create a business exception with message, error code, and root cause
     * @param message Human-readable error message
     * @param errorCode Machine-readable error code
     * @param cause Root cause exception
     */
    protected BusinessException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
