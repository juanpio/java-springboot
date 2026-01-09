package com.microservices.userservice.exception;

/**
 * Exception thrown when authentication fails
 */
public class AuthenticationException extends BusinessException {
    
    public AuthenticationException(String message) {
        super(message, "AUTHENTICATION_FAILED");
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, "AUTHENTICATION_FAILED", cause);
    }
}
