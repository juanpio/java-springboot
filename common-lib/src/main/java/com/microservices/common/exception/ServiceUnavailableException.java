package com.microservices.common.exception;

/**
 * Exception thrown when an external service is unavailable
 * HTTP Status: 503 SERVICE UNAVAILABLE
 */
public class ServiceUnavailableException extends BusinessException {
    
    /**
     * Create exception for service unavailability
     * @param serviceName Name of the unavailable service
     */
    public ServiceUnavailableException(String serviceName) {
        super(String.format("%s is currently unavailable. Please try again later.", serviceName), 
              "SERVICE_UNAVAILABLE");
    }
    
    /**
     * Create exception with custom message
     * @param serviceName Name of the service
     * @param cause Root cause exception
     */
    public ServiceUnavailableException(String serviceName, Throwable cause) {
        super(String.format("%s is currently unavailable. Please try again later.", serviceName), 
              "SERVICE_UNAVAILABLE", 
              cause);
    }
}
